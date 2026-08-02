package com.example.smartspendai.ui.expense;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartspendai.R;
import com.example.smartspendai.data.local.entity.CategoryEntity;
import com.example.smartspendai.data.local.entity.ExpenseEntity;
import com.example.smartspendai.ui.category.CategoryViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity {

    private static final String ALL_CATEGORIES = "All Categories";
    private static final long NO_FROM_FILTER = 0L;
    private static final long NO_TO_FILTER = Long.MAX_VALUE;

    private ExpenseViewModel viewModel;
    private CategoryViewModel categoryViewModel;
    private ExpenseAdapter adapter;

    private EditText etSearchQuery;
    private Spinner spinnerFilterCategory;
    private Button btnDateFrom, btnDateTo;
    private TextView tvResultsCount;

    private long fromMillis = NO_FROM_FILTER;
    private long toMillis = NO_TO_FILTER;

    /** Holds whichever search LiveData is currently active so we can stop observing it before switching filters. */
    @Nullable
    private LiveData<List<ExpenseEntity>> currentSearchLiveData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        viewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);

        TextView tvClose = findViewById(R.id.tvClose);
        etSearchQuery = findViewById(R.id.etSearchQuery);
        spinnerFilterCategory = findViewById(R.id.spinnerFilterCategory);
        btnDateFrom = findViewById(R.id.btnDateFrom);
        btnDateTo = findViewById(R.id.btnDateTo);
        tvResultsCount = findViewById(R.id.tvResultsCount);
        RecyclerView recyclerView = findViewById(R.id.recyclerSearchResults);

        tvClose.setOnClickListener(v -> finish());

        adapter = new ExpenseAdapter(new ExpenseAdapter.OnExpenseClickListener() {
            @Override
            public void onExpenseClick(ExpenseEntity expense) { /* read-only results list */ }

            @Override
            public void onExpenseLongClick(ExpenseEntity expense) { /* no-op here */ }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        setupCategoryFilter();
        setupDatePickers();

        etSearchQuery.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { runSearch(); }
        });

        runSearch();
    }

    private void setupCategoryFilter() {
        categoryViewModel.getAllCategories().observe(this, (List<CategoryEntity> categories) -> {
            List<String> names = new ArrayList<>();
            names.add(ALL_CATEGORIES);
            for (CategoryEntity category : categories) {
                names.add(category.name);
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item_selected, names);
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
            spinnerFilterCategory.setAdapter(adapter);

            spinnerFilterCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                    runSearch();
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        });
    }

    private void setupDatePickers() {
        btnDateFrom.setOnClickListener(v -> showDatePicker(true));
        btnDateTo.setOnClickListener(v -> showDatePicker(false));
    }

    private void showDatePicker(boolean isFromDate) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, dayOfMonth, isFromDate ? 0 : 23, isFromDate ? 0 : 59, isFromDate ? 0 : 59);

            String label = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year);

            if (isFromDate) {
                fromMillis = selected.getTimeInMillis();
                btnDateFrom.setText("From: " + label);
            } else {
                toMillis = selected.getTimeInMillis();
                btnDateTo.setText("To: " + label);
            }
            runSearch();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void runSearch() {
        String query = etSearchQuery.getText().toString().trim();

        String selectedCategory = null;
        if (spinnerFilterCategory.getSelectedItem() != null) {
            String selected = spinnerFilterCategory.getSelectedItem().toString();
            selectedCategory = ALL_CATEGORIES.equals(selected) ? null : selected;
        }

        if (currentSearchLiveData != null) {
            currentSearchLiveData.removeObservers(this);
        }

        currentSearchLiveData = viewModel.searchExpenses(selectedCategory, query, fromMillis, toMillis);
        currentSearchLiveData.observe(this, (List<ExpenseEntity> results) -> {
            adapter.submitList(results);
            int count = results != null ? results.size() : 0;
            tvResultsCount.setText(count + (count == 1 ? " result" : " results"));
        });
    }
}
