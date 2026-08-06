package com.example.smartspendai.ui.category;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartspendai.R;
import com.example.smartspendai.data.local.entity.CategoryEntity;
import com.example.smartspendai.data.repository.CategoryRepository;

public class ManageCategoriesActivity extends AppCompatActivity {

    private CategoryViewModel viewModel;
    private CategoryAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_categories);

        viewModel = new ViewModelProvider(this).get(CategoryViewModel.class);

        RecyclerView recyclerView = findViewById(R.id.recyclerCategories);
        TextView tvClose = findViewById(R.id.tvClose);
        Button fabAdd = findViewById(R.id.fabAddCategory);

        adapter = new CategoryAdapter(this::confirmDelete);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        viewModel.getAllCategories().observe(this, adapter::submitList);

        tvClose.setOnClickListener(v -> finish());
        fabAdd.setOnClickListener(v -> showAddDialog());
    }

    private void showAddDialog() {
        EditText input = new EditText(this);
        input.setHint("Category name");

        new AlertDialog.Builder(this)
                .setTitle("Add category")
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = input.getText().toString().trim();

                    if (!name.isEmpty()) {
                        name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
                    }
                    if (TextUtils.isEmpty(name)) {
                        Toast.makeText(this, "Enter a category name", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    viewModel.addCategory(name, new CategoryRepository.AddCategoryCallback() {
                        @Override
                        public void onDuplicate() {
                            runOnUiThread(() ->
                                    Toast.makeText(ManageCategoriesActivity.this,
                                            "That category already exists", Toast.LENGTH_SHORT).show());
                        }

                        @Override
                        public void onAdded() {
                            runOnUiThread(() ->
                                    Toast.makeText(ManageCategoriesActivity.this,
                                            "Category added", Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDelete(CategoryEntity category) {
        new AlertDialog.Builder(this)
                .setTitle("Delete \"" + category.name + "\"?")
                .setMessage("Existing expenses already using this category will keep it as text — they won't be deleted.")
                .setPositiveButton("Delete", (dialog, which) -> viewModel.deleteCategory(category))
                .setNegativeButton("Cancel", null)
                .show();
    }
}
