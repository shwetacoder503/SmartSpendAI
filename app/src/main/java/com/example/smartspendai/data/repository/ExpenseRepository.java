package com.example.smartspendai.data.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.smartspendai.data.local.AppDatabase;
import com.example.smartspendai.data.local.dao.ExpenseDao;
import com.example.smartspendai.data.local.entity.ExpenseEntity;
import com.example.smartspendai.data.model.ExpenseRequest;
import com.example.smartspendai.data.model.ExpenseResponse;
import com.example.smartspendai.data.remote.ExpenseApiService;
import com.example.smartspendai.data.remote.RetrofitClient;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;

/**
 * Room does NOT allow database writes (insert/update/delete) on the main
 * thread — it'll crash the app if you try. This repository pushes every
 * write onto a small background thread pool so the UI never freezes and
 * never crashes. Reads use LiveData, which Room already runs off the main
 * thread automatically and delivers back on the main thread for you.
 */
public class ExpenseRepository {

    private static final String TAG = "ExpenseRepository";

    private final ExpenseDao expenseDao;
    private final ExpenseApiService api;
    private final ExecutorService writeExecutor = Executors.newSingleThreadExecutor();

    public ExpenseRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        expenseDao = db.expenseDao();
        api = RetrofitClient.getInstance(application).create(ExpenseApiService.class);
    }

    public LiveData<List<ExpenseEntity>> getAllExpenses() {
        return expenseDao.getAllExpenses();
    }

    public LiveData<ExpenseEntity> getExpenseById(long id) {
        return expenseDao.getExpenseById(id);
    }

    public LiveData<Double> getTodayTotal(long startOfDayMillis) {
        return expenseDao.getTodayTotal(startOfDayMillis);
    }

    public LiveData<List<ExpenseEntity>> getExpensesBetween(long startMillis, long endMillis) {
        return expenseDao.getExpensesBetween(startMillis, endMillis);
    }

    public LiveData<Double> getTotalBetween(long startMillis, long endMillis) {
        return expenseDao.getTotalBetween(startMillis, endMillis);
    }

    public LiveData<List<com.example.smartspendai.data.local.pojo.CategoryTotal>> getCategoryTotals(long startMillis, long endMillis) {
        return expenseDao.getCategoryTotals(startMillis, endMillis);
    }

    public LiveData<List<com.example.smartspendai.data.local.pojo.DayTotal>> getDayOfWeekTotals(long startMillis, long endMillis) {
        return expenseDao.getDayOfWeekTotals(startMillis, endMillis);
    }

    public LiveData<List<com.example.smartspendai.data.local.pojo.MonthTotal>> getLast5MonthsTotals() {
        return expenseDao.getLast5MonthsTotals();
    }

    public void insert(ExpenseEntity expense) {
        writeExecutor.execute(() -> expenseDao.insert(expense));
    }

    public void update(ExpenseEntity expense) {
        writeExecutor.execute(() -> expenseDao.update(expense));
    }

    public void delete(ExpenseEntity expense) {
        writeExecutor.execute(() -> {
            expenseDao.delete(expense);
            // Best-effort: if this row already existed on the server, try to
            // remove it there too. Wrapped in try/catch since we're offline
            // just as often as online — a failed delete-on-server here isn't
            // fatal, it just means that row will reappear on the next PULL.
            // (A more complete version would use a "pending_delete" tombstone
            // flag instead — worth doing before a real production release.)
            if (expense.remoteId != null) {
                try {
                    api.deleteExpense(expense.remoteId).execute();
                } catch (Exception e) {
                    Log.w(TAG, "Could not delete expense " + expense.remoteId + " from server: " + e.getMessage());
                }
            }
        });
    }

    // ---- Sync (Milestone 3) ----

    public interface SyncCallback {
        void onSyncSuccess(int pushedCount, int pulledCount);
        void onSyncError(String message);
    }

    /**
     * Simple two-phase, last-write-wins sync — deliberately NOT automatic
     * or scheduled yet (that's a Milestone 10 stretch goal with WorkManager).
     * For now this runs once, when the user taps a "Sync" button:
     *
     *  Phase 1 (PUSH): every local expense with is_synced = 0 gets POSTed
     *  to the server. The server's response gives us back a `remote_id`,
     *  which we save locally so we know this row is no longer "new".
     *
     *  Phase 2 (PULL): we GET every expense the server has for this user,
     *  and insert any we don't already have locally (matched by remote_id)
     *  — this is what brings in expenses added from a different device.
     */
    public void syncNow(SyncCallback callback) {
        writeExecutor.execute(() -> {
            int pushedCount = 0;
            int pulledCount = 0;
            try {
                // ---- PUSH ----
                List<ExpenseEntity> unsynced = expenseDao.getUnsyncedExpenses();
                for (ExpenseEntity expense : unsynced) {
                    ExpenseRequest request = new ExpenseRequest(
                            expense.title, expense.amount, expense.category,
                            expense.paymentMethod, expense.note, expense.dateMillis);

                    if (expense.remoteId != null) {
                        // Already existed on the server before — this is an
                        // EDIT, so PUT to the same remote_id instead of
                        // creating a duplicate row.
                        Response<ExpenseResponse> response =
                                api.updateExpense(expense.remoteId, request).execute();
                        if (response.isSuccessful() && response.body() != null) {
                            expenseDao.markSynced(expense.id, expense.remoteId);
                            pushedCount++;
                        } else {
                            Log.w(TAG, "Update-push failed for expense " + expense.id + ": " + response.code());
                        }
                    } else {
                        // Brand new, never synced before.
                        Response<ExpenseResponse> response = api.createExpense(request).execute();
                        if (response.isSuccessful() && response.body() != null) {
                            expenseDao.markSynced(expense.id, response.body().id);
                            pushedCount++;
                        } else {
                            Log.w(TAG, "Create-push failed for expense " + expense.id + ": " + response.code());
                        }
                    }
                }

                // ---- PULL ----
                Response<List<ExpenseResponse>> listResponse = api.listExpenses().execute();
                if (listResponse.isSuccessful() && listResponse.body() != null) {
                    for (ExpenseResponse remote : listResponse.body()) {
                        boolean alreadyExists = expenseDao.countByRemoteId(remote.id) > 0;
                        if (!alreadyExists) {
                            ExpenseEntity entity = new ExpenseEntity();
                            entity.title = remote.title;
                            entity.amount = remote.amount;
                            entity.category = remote.category;
                            entity.paymentMethod = remote.payment_method;
                            entity.note = remote.note;
                            entity.dateMillis = remote.date_millis;
                            entity.isSynced = true;
                            entity.remoteId = remote.id;
                            expenseDao.insert(entity);
                            pulledCount++;
                        }
                    }
                } else {
                    callback.onSyncError("Couldn't fetch expenses from server (code " + listResponse.code() + ")");
                    return;
                }

                callback.onSyncSuccess(pushedCount, pulledCount);
            } catch (Exception e) {
                callback.onSyncError("Sync failed: " + e.getMessage());
            }
        });
    }
}
