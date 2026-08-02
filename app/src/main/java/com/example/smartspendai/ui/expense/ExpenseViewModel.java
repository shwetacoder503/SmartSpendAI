package com.example.smartspendai.ui.expense;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.smartspendai.data.local.entity.ExpenseEntity;
import com.example.smartspendai.data.repository.ExpenseRepository;

import java.util.Calendar;
import java.util.List;

public class ExpenseViewModel extends AndroidViewModel {

    private final ExpenseRepository repository;
    private final LiveData<List<ExpenseEntity>> allExpenses;

    public ExpenseViewModel(@NonNull Application application) {
        super(application);
        repository = new ExpenseRepository(application);
        allExpenses = repository.getAllExpenses();
    }

    public LiveData<List<ExpenseEntity>> getAllExpenses() {
        return allExpenses;
    }

    public LiveData<Double> getTodayTotal() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return repository.getTodayTotal(cal.getTimeInMillis());
    }

    public static long startOfThisMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public static long startOfLast7Days() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -6); // today + previous 6 days = 7 days total
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public LiveData<Double> getMonthTotal() {
        return repository.getTotalBetween(startOfThisMonth(), System.currentTimeMillis());
    }

    // ---- Generic range queries (Milestone 5 — Reports) ----

    public LiveData<Double> getTotalForRange(long startMillis, long endMillis) {
        return repository.getTotalBetween(startMillis, endMillis);
    }

    public LiveData<List<com.example.smartspendai.data.local.pojo.CategoryTotal>> getCategoryTotalsForRange(long startMillis, long endMillis) {
        return repository.getCategoryTotals(startMillis, endMillis);
    }

    public LiveData<List<com.example.smartspendai.data.local.pojo.CategoryTotal>> getCategoryTotalsThisMonth() {
        return repository.getCategoryTotals(startOfThisMonth(), System.currentTimeMillis());
    }

    public LiveData<List<com.example.smartspendai.data.local.pojo.DayTotal>> getDayOfWeekTotalsThisMonth() {
        return repository.getDayOfWeekTotals(startOfThisMonth(), System.currentTimeMillis());
    }

    public LiveData<List<com.example.smartspendai.data.local.pojo.MonthTotal>> getLast5MonthsTotals() {
        return repository.getLast5MonthsTotals();
    }

    public LiveData<List<ExpenseEntity>> getExpensesLast7Days() {
        return repository.getExpensesBetween(startOfLast7Days(), System.currentTimeMillis());
    }

    /**
     * @param category null means "all categories"
     * @param query    empty string means "match any title" (SQL LIKE '%%' matches everything)
     */
    public LiveData<List<ExpenseEntity>> searchExpenses(@Nullable String category, String query,
                                                         long startMillis, long endMillis) {
        return repository.searchExpenses(category, query, startMillis, endMillis);
    }

    public void addExpense(String title, double amount, String category,
                            String paymentMethod, String note) {
        ExpenseEntity expense = new ExpenseEntity();
        expense.title = title;
        expense.amount = amount;
        expense.category = category;
        expense.paymentMethod = paymentMethod;
        expense.note = note;
        expense.dateMillis = System.currentTimeMillis();
        repository.insert(expense);
    }

    public void updateExpense(ExpenseEntity expense) {
        repository.update(expense);
    }

    public void deleteExpense(ExpenseEntity expense) {
        repository.delete(expense);
    }

    public void syncNow(ExpenseRepository.SyncCallback callback) {
        repository.syncNow(callback);
    }
}
