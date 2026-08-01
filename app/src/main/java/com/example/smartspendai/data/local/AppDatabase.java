package com.example.smartspendai.data.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.smartspendai.data.local.dao.ExpenseDao;
import com.example.smartspendai.data.local.entity.ExpenseEntity;

@Database(entities = {ExpenseEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ExpenseDao expenseDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "smartspend.db")
                            // fallbackToDestructiveMigration is fine while we're still
                            // actively changing the schema during development.
                            // Before shipping a real update, replace this with a
                            // proper Migration so users don't lose their data.
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
