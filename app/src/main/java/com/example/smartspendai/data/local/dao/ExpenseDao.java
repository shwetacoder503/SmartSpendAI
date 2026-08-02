package com.example.smartspendai.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.smartspendai.data.local.entity.ExpenseEntity;
import com.example.smartspendai.data.local.pojo.CategoryTotal;
import com.example.smartspendai.data.local.pojo.DayTotal;
import com.example.smartspendai.data.local.pojo.MonthTotal;

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

    // ---- Sync-related queries (Milestone 3) ----

    /** Expenses created/edited offline that the server doesn't know about yet. */
    @Query("SELECT * FROM expenses WHERE is_synced = 0")
    List<ExpenseEntity> getUnsyncedExpenses();

    @Query("UPDATE expenses SET is_synced = 1, remote_id = :remoteId WHERE id = :localId")
    void markSynced(long localId, long remoteId);

    @Query("SELECT COUNT(*) FROM expenses WHERE remote_id = :remoteId")
    int countByRemoteId(long remoteId);

    // ---- Analytics queries (Milestone 4) ----

    @Query("SELECT category, SUM(amount) as total FROM expenses " +
            "WHERE date_millis BETWEEN :startMillis AND :endMillis " +
            "GROUP BY category ORDER BY total DESC")
    LiveData<List<CategoryTotal>> getCategoryTotals(long startMillis, long endMillis);

    /**
     * strftime('%w', ...) returns the day of week as text ('0'..'6'), so we
     * CAST it to INTEGER to get a real number back into DayTotal.dow.
     * date_millis is stored in milliseconds, but strftime expects seconds
     * since epoch — hence the "/1000".
     */
    @Query("SELECT CAST(strftime('%w', date_millis/1000, 'unixepoch') AS INTEGER) as dow, " +
            "SUM(amount) as total FROM expenses " +
            "WHERE date_millis BETWEEN :startMillis AND :endMillis " +
            "GROUP BY dow")
    LiveData<List<DayTotal>> getDayOfWeekTotals(long startMillis, long endMillis);

    @Query("SELECT strftime('%Y-%m', date_millis/1000, 'unixepoch') as yearMonth, " +
            "SUM(amount) as total FROM expenses " +
            "GROUP BY yearMonth ORDER BY yearMonth DESC LIMIT 5")
    LiveData<List<MonthTotal>> getLast5MonthsTotals();

    // ---- Search (Milestone 5) ----

    /**
     * `:category` can be null (meaning "all categories" — the "category IS NULL"
     * branch makes that column check pass regardless of the row's actual category).
     * `:query` matches title using SQL LIKE, so partial/merchant-name search works.
     */
    @Query("SELECT * FROM expenses WHERE " +
            "(:category IS NULL OR category = :category) AND " +
            "title LIKE '%' || :query || '%' AND " +
            "date_millis BETWEEN :startMillis AND :endMillis " +
            "ORDER BY date_millis DESC")
    LiveData<List<ExpenseEntity>> searchExpenses(String category, String query, long startMillis, long endMillis);
}
