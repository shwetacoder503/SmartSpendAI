package com.example.smartspendai.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.smartspendai.data.local.dao.CategoryDao;
import com.example.smartspendai.data.local.dao.ExpenseDao;
import com.example.smartspendai.data.local.dao.GoalDao;
import com.example.smartspendai.data.local.entity.CategoryEntity;
import com.example.smartspendai.data.local.entity.ExpenseEntity;
import com.example.smartspendai.data.local.entity.GoalContributionEntity;
import com.example.smartspendai.data.local.entity.GoalEntity;

@Database(entities = {ExpenseEntity.class, CategoryEntity.class, GoalEntity.class, GoalContributionEntity.class}, version = 5, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ExpenseDao expenseDao();
    public abstract CategoryDao categoryDao();
    public abstract GoalDao goalDao();

    private static volatile AppDatabase INSTANCE;

    private static final String[] DEFAULT_CATEGORIES = {
            "Food", "Shopping", "Travel", "Medical", "Bills",
            "Education", "Entertainment", "Investment", "Others"
    };

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
                            .addCallback(new Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    // Runs exactly once, the very first time the DB file
                                    // is created on a device — seeds the 9 default
                                    // categories so the app isn't empty on first launch.
                                    for (String name : DEFAULT_CATEGORIES) {
                                        ContentValues values = new ContentValues();
                                        values.put("name", name);
                                        values.put("is_default", 1);
                                        db.insert("categories", SQLiteDatabase.CONFLICT_IGNORE, values);
                                    }
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
