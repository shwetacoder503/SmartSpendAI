package com.example.smartspendai.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * One row = one expense.
 *
 * Design choice: `category` and `paymentMethod` are stored as plain Strings
 * here (not as a foreign key to a separate Categories table). For a solo
 * project at this scale that's simpler to reason about and query, and it's
 * an easy, defensible answer in an interview: "I denormalized category as a
 * string since categories are a fixed, small set (9 values) that rarely
 * change — a full relational table would add a join for no real benefit at
 * this scale."
 */
@Entity(tableName = "expenses")
public class ExpenseEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    @ColumnInfo(name = "title")
    public String title = "";

    @ColumnInfo(name = "amount")
    public double amount;

    @NonNull
    @ColumnInfo(name = "category")
    public String category = "Others";

    @ColumnInfo(name = "payment_method")
    public String paymentMethod;

    @ColumnInfo(name = "note")
    public String note;

    /** Stored as epoch millis (System.currentTimeMillis()) so we can sort/filter easily. */
    @ColumnInfo(name = "date_millis")
    public long dateMillis;

    /**
     * true  = already synced with the backend (Milestone 3)
     * false = created/edited offline, still needs to be pushed
     * Not used yet in Milestone 2, but keeping the column here now saves us
     * a painful Room migration later.
     */
    @ColumnInfo(name = "is_synced", defaultValue = "0")
    public boolean isSynced = false;
}
