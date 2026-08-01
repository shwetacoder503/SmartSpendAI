package com.example.smartspendai.ui.expense;

import android.app.Application;

import androidx.annotation.NonNull;
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
