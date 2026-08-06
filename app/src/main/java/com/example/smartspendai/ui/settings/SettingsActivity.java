package com.example.smartspendai.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import com.example.smartspendai.BuildConfig;
import com.example.smartspendai.R;
import com.example.smartspendai.autodetect.CategoryGuesser;
import com.example.smartspendai.autodetect.ParsedTransaction;
import com.example.smartspendai.autodetect.UpiNotificationParser;
import com.example.smartspendai.data.local.AppDatabase;
import com.example.smartspendai.data.local.entity.ExpenseEntity;

import java.util.Random;
import java.util.concurrent.Executors;

public class SettingsActivity extends AppCompatActivity {

    // Realistic sample notification texts — same wording style real GPay/PhonePe/Paytm
    // notifications use, so this exercises the EXACT SAME parser code path.
    private static final String[] SAMPLE_NOTIFICATIONS = {
            "You paid ₹150 to Swiggy",
            "₹420 paid to Zomato",
            "Payment of ₹85 to Uber was successful",
            "₹1,200 paid to Amazon",
            "Rs.99 paid successfully to Netflix",
    };

    private Switch switchAutoDetect;
    private TextView tvPermissionStatus;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        findViewById(R.id.tvClose).setOnClickListener(v -> finish());

        switchAutoDetect = findViewById(R.id.switchAutoDetect);
        tvPermissionStatus = findViewById(R.id.tvPermissionStatus);
        Button btnSimulate = findViewById(R.id.btnSimulateNotification);
        View testSection = findViewById(R.id.llTestSection);

        // This debug tool should NEVER be visible to a real end user — it's
        // purely a development aid. BuildConfig.DEBUG is automatically
        // true for every debug/run-from-Android-Studio build and
        // automatically false in a signed release build, so this hides
        // itself for the final APK with zero manual cleanup needed.
        testSection.setVisibility(BuildConfig.DEBUG ? View.VISIBLE : View.GONE);

        switchAutoDetect.setOnClickListener(v -> {
            if (switchAutoDetect.isChecked() && !isNotificationAccessGranted()) {
                // This specific permission has NO normal runtime dialog — Android
                // only allows granting it from this exact system settings screen.
                Toast.makeText(this, "Turn on notification access for SmartSpend AI on the next screen", Toast.LENGTH_LONG).show();
                startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
            }
        });

        btnSimulate.setOnClickListener(v -> simulateTestNotification());
    }

    /**
     * Bypasses the real OS notification pipeline entirely — calls the SAME
     * UpiNotificationParser + save logic the real service uses, just with a
     * hardcoded sample string instead of a real StatusBarNotification. This
     * proves the parsing + categorization + save-to-Room pipeline all work
     * correctly, independent of whether GPay/PhonePe/Paytm happens to post
     * an actual system notification for any given transaction (which, as
     * you've seen, they don't always do).
     */
    private void simulateTestNotification() {
        String sampleText = SAMPLE_NOTIFICATIONS[new Random().nextInt(SAMPLE_NOTIFICATIONS.length)];

        ParsedTransaction parsed = UpiNotificationParser.parse(
                "com.google.android.apps.nbu.paisa.user", "Google Pay", sampleText);

        if (parsed == null) {
            Toast.makeText(this, "Simulation failed to parse — this would be a real bug, please report it.", Toast.LENGTH_LONG).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            ExpenseEntity expense = new ExpenseEntity();
            expense.title = parsed.merchant;
            expense.amount = parsed.amount;
            expense.category = CategoryGuesser.guess(parsed.merchant);
            expense.paymentMethod = "UPI";
            expense.note = "Simulated test notification";
            expense.dateMillis = System.currentTimeMillis();
            expense.isAutoDetected = true;

            AppDatabase.getInstance(getApplicationContext()).expenseDao().insert(expense);

            runOnUiThread(() -> Toast.makeText(this,
                    "✅ Simulated: ₹" + (long) parsed.amount + " to " + parsed.merchant +
                            " (" + expense.category + ") — check Expense List",
                    Toast.LENGTH_LONG).show());
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // The user might be coming BACK from the system settings screen right now —
        // re-check every time this screen becomes visible.
        refreshPermissionStatus();
    }

    private void refreshPermissionStatus() {
        boolean granted = isNotificationAccessGranted();
        switchAutoDetect.setChecked(granted);
        tvPermissionStatus.setText(granted
                ? "✅ Notification access granted — auto-detect is active."
                : "⚠️ Notification access not granted yet — tap the switch above to enable it.");
        tvPermissionStatus.setTextColor(granted ? 0xFF4FBA82 : 0xFFE8A33D);
    }

    private boolean isNotificationAccessGranted() {
        return NotificationManagerCompat.getEnabledListenerPackages(this).contains(getPackageName());
    }
}
