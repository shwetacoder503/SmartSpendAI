package com.example.smartspendai.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories")
public class CategoryEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    @ColumnInfo(name = "name")
    public String name = "";

    /** Default categories (Food, Shopping, ...) can't be deleted — only custom ones the user adds. */
    @ColumnInfo(name = "is_default", defaultValue = "0")
    public boolean isDefault = false;
}
