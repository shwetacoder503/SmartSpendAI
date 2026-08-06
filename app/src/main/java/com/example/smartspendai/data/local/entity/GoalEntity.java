package com.example.smartspendai.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "goals")
public class GoalEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    @ColumnInfo(name = "title")
    public String title = "";

    @ColumnInfo(name = "target_amount")
    public double targetAmount;

    /** Optional — user's desired deadline. Null means "no specific deadline". */
    @ColumnInfo(name = "target_date_millis")
    public Long targetDateMillis;

    @ColumnInfo(name = "created_date_millis")
    public long createdDateMillis;
}
