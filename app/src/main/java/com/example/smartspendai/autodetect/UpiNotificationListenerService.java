package com.example.smartspendai.autodetect;

import android.app.Notification;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.example.smartspendai.data.local.AppDatabase;
import com.example.smartspendai.data.local.dao.ExpenseDao;
import com.example.smartspendai.data.local.entity.ExpenseEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * PRIVACY NOTE (important — this is a special, sensitive Android permission):
 * Once the user grants "Notification Access" in Settings, this service can
 * technically see EVERY notification on the phone — WhatsApp, Instagram,
 * OTPs, everything. To respect that:
 *   1. We check the package name FIRST and immediately return/ignore
 *      anything not from a supported UPI app (see UpiNotificationParser).
 *   2. We never log, store, or transmit the raw notification text anywhere
 *      — only the 2-3 extracted numbers/words (amount, merchant) that
 *      become the actual expense row.
 *   3. Everything here runs fully on-device; nothing is sent to any server
 *      just from reading a notification.
 */
public class UpiNotificationListenerService extends NotificationListenerService {

    private static final String TAG = "UpiAutoDetect";
    private static final long DUPLICATE_WINDOW_MILLIS = 3 * 60 * 1000; // 3 minutes

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        // If this line NEVER appears in Logcat, the OS never bound the service —
        // meaning the permission isn't actually active, regardless of what the
        // in-app toggle shows. This is the #1 thing to check first.
        Log.d(TAG, "Listener CONNECTED — service is bound and active.");
    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        Log.d(TAG, "Listener DISCONNECTED — OS unbound the service (common on MIUI/Oppo/Vivo battery optimization).");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn == null || sbn.getNotification() == null) return;

        String packageName = sbn.getPackageName();
        // Package name alone is not sensitive content — safe to log, and this
        // is the fastest way to confirm whether OUR whitelist actually matches
        // what your GPay/PhonePe/Paytm app is really called on this device.
        Log.d(TAG, "Notification from: " + packageName);

        Notification notification = sbn.getNotification();
        Bundle extras = notification.extras;
        if (extras == null) return;

        CharSequence titleChars = extras.getCharSequence(Notification.EXTRA_TITLE);
        CharSequence textChars = extras.getCharSequence(Notification.EXTRA_TEXT);
        String title = titleChars != null ? titleChars.toString() : null;
        String text = textChars != null ? textChars.toString() : null;

        // Deliberately not logging the FULL `title`/`text` content in production
        // builds — see the privacy note above. Temporarily uncommenting the next
        // line while debugging is fine on your own dev device:
        // Log.d(TAG, "title=" + title + " text=" + text);

        ParsedTransaction parsed = UpiNotificationParser.parse(packageName, title, text);
        if (parsed == null) {
            if (UpiNotificationParser.isFromSupportedAppPublic(packageName)) {
                Log.d(TAG, "From a supported app, but parsing FAILED — regex didn't match this notification's wording.");
            }
            return;
        }
        if (!parsed.isDebit) return;

        Log.d(TAG, "PARSED OK: amount=" + parsed.amount + " merchant=" + parsed.merchant);
        saveAsExpense(parsed);
    }

    private void saveAsExpense(ParsedTransaction parsed) {
        executor.execute(() -> {
            ExpenseDao dao = AppDatabase.getInstance(getApplicationContext()).expenseDao();

            long now = System.currentTimeMillis();
            int existingCount = dao.countSimilarRecent(parsed.amount, now - DUPLICATE_WINDOW_MILLIS, now + DUPLICATE_WINDOW_MILLIS);
            if (existingCount > 0) {
                Log.d(TAG, "Skipped likely-duplicate auto-detected expense (same amount already logged nearby in time).");
                return;
            }

            ExpenseEntity expense = new ExpenseEntity();
            expense.title = parsed.merchant;
            expense.amount = parsed.amount;
            expense.category = CategoryGuesser.guess(parsed.merchant);
            expense.paymentMethod = "UPI";
            expense.note = "Auto-detected from notification";
            expense.dateMillis = now;
            expense.isAutoDetected = true;

            dao.insert(expense);
            Log.d(TAG, "Auto-added expense: ₹" + parsed.amount + " (" + expense.category + ")");
        });
    }
}
