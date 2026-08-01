package com.example.smartspendai.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.smartspendai.data.local.AppDatabase;
import com.example.smartspendai.data.local.dao.ExpenseDao;
import com.example.smartspendai.data.local.entity.ExpenseEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Room does NOT allow database writes (insert/update/delete) on the main
 * thread — it'll crash the app if you try. This repository pushes every
 * write onto a small background thread pool so the UI never freezes and
 * never crashes. Reads use LiveData, which Room already runs off the main
 * thread automatically and delivers back on the main thread for you.
 */
public class ExpenseRepository {

    private final ExpenseDao expenseDao;
    private final ExecutorService writeExecutor = Executors.newSingleThreadExecutor();

    public ExpenseRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        expenseDao = db.expenseDao();
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

    public LiveData<Double> getTotalBetween(long startMillis, long endMillis) {
        return expenseDao.getTotalBetween(startMillis, endMillis);
    }

    public void insert(ExpenseEntity expense) {
        writeExecutor.execute(() -> expenseDao.insert(expense));
    }

    public void update(ExpenseEntity expense) {
        writeExecutor.execute(() -> expenseDao.update(expense));
    }

    public void delete(ExpenseEntity expense) {
        writeExecutor.execute(() -> expenseDao.delete(expense));
    }
}
