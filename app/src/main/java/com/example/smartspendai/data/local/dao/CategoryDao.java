package com.example.smartspendai.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.smartspendai.data.local.entity.CategoryEntity;

import java.util.List;

@Dao
public interface CategoryDao {

    @Insert
    long insert(CategoryEntity category);

    @Delete
    void delete(CategoryEntity category);

    @Query("SELECT * FROM categories ORDER BY is_default DESC, name ASC")
    LiveData<List<CategoryEntity>> getAllCategories();

    @Query("SELECT COUNT(*) FROM categories WHERE name = :name")
    int countByName(String name);

    @Query("SELECT * FROM categories")
    List<CategoryEntity> getAllCategoriesSync();
}
