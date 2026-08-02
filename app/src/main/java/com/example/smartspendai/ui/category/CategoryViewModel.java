package com.example.smartspendai.ui.category;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.smartspendai.data.local.entity.CategoryEntity;
import com.example.smartspendai.data.repository.CategoryRepository;

import java.util.List;

public class CategoryViewModel extends AndroidViewModel {

    private final CategoryRepository repository;
    private final LiveData<List<CategoryEntity>> allCategories;

    public CategoryViewModel(@NonNull Application application) {
        super(application);
        repository = new CategoryRepository(application);
        allCategories = repository.getAllCategories();
    }

    public LiveData<List<CategoryEntity>> getAllCategories() {
        return allCategories;
    }

    public void addCategory(String name, CategoryRepository.AddCategoryCallback callback) {
        repository.addCategory(name, callback);
    }

    public void deleteCategory(CategoryEntity category) {
        repository.deleteCategory(category);
    }
}
