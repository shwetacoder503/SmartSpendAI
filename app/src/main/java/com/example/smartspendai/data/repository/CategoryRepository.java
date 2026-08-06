package com.example.smartspendai.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.smartspendai.data.local.AppDatabase;
import com.example.smartspendai.data.local.dao.CategoryDao;
import com.example.smartspendai.data.local.entity.CategoryEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CategoryRepository {

    private final CategoryDao categoryDao;
    private final ExecutorService writeExecutor = Executors.newSingleThreadExecutor();

    public CategoryRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        categoryDao = db.categoryDao();
    }

    public LiveData<List<CategoryEntity>> getAllCategories() {
        return categoryDao.getAllCategories();
    }

    public interface AddCategoryCallback {
        void onDuplicate();
        void onAdded();
    }

    public void addCategory(String name, AddCategoryCallback callback) {

        final String formattedName;

        if (name == null) {
            formattedName = "";
        } else {
            String temp = name.trim();

            if (!temp.isEmpty()) {
                temp = temp.substring(0, 1).toUpperCase()
                        + temp.substring(1).toLowerCase();
            }

            formattedName = temp;
        }

        writeExecutor.execute(() -> {

            List<CategoryEntity> categories = categoryDao.getAllCategoriesSync();

            for (CategoryEntity c : categories) {
                if (c.name.equalsIgnoreCase(formattedName)) {
                    callback.onDuplicate();
                    return;
                }
            }

            CategoryEntity category = new CategoryEntity();
            category.name = formattedName;
            category.isDefault = false;

            categoryDao.insert(category);

            callback.onAdded();
        });
    }

    public void deleteCategory(CategoryEntity category) {
        writeExecutor.execute(() -> categoryDao.delete(category));
    }
}
