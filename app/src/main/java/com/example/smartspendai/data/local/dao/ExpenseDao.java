package com.example.smartspendai.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.smartspendai.data.local.entity.ExpenseEntity;

import java.util.List;

@Dao
public interface ExpenseDao {

    @Insert
    long insert(ExpenseEntity expense);

    @Update
    void update(ExpenseEntity expense);

    @Delete
    void delete(ExpenseEntity expense);

    @Query("DELETE FROM expenses WHERE id = :expenseId")
    void deleteById(long expenseId);

    @Query("SELECT * FROM expenses ORDER BY date_millis DESC")
    LiveData<List<ExpenseEntity>> getAllExpenses();

    @Query("SELECT * FROM expenses WHERE id = :expenseId LIMIT 1")
    LiveData<ExpenseEntity> getExpenseById(long expenseId);

    @Query("SELECT * FROM expenses WHERE date_millis BETWEEN :startMillis AND :endMillis ORDER BY date_millis DESC")
    LiveData<List<ExpenseEntity>> getExpensesBetween(long startMillis, long endMillis);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE date_millis BETWEEN :startMillis AND :endMillis")
    LiveData<Double> getTotalBetween(long startMillis, long endMillis);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE date_millis >= :startOfDayMillis")
    LiveData<Double> getTodayTotal(long startOfDayMillis);
}
