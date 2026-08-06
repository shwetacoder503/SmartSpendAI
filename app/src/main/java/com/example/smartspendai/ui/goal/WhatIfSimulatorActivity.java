package com.example.smartspendai.ui.goal;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.smartspendai.R;
import com.example.smartspendai.data.local.pojo.CategoryTotal;
import com.example.smartspendai.ui.expense.ExpenseViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WhatIfSimulatorActivity extends AppCompatActivity {

    private ExpenseViewModel viewModel;

    private Spinner spinnerCategory;
    private TextView chip10, chip20, chip30;
    private TextView tvCurrentSpend, tvMonthlySaving, tvYearlySaving;
    private View llSimulatorBody;
    private TextView tvNoCategoryData;

    private List<CategoryTotal> categoryTotals = new ArrayList<>();
    private int selectedPercent = 20; // matches the default-highlighted chip in the layout

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_what_if);

        viewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);

        findViewById(R.id.tvClose).setOnClickListener(v -> finish());

        spinnerCategory = findViewById(R.id.spinnerWhatIfCategory);
        chip10 = findViewById(R.id.chip10);
        chip20 = findViewById(R.id.chip20);
        chip30 = findViewById(R.id.chip30);
        tvCurrentSpend = findViewById(R.id.tvCurrentSpend);
        tvMonthlySaving = findViewById(R.id.tvMonthlySaving);
        tvYearlySaving = findViewById(R.id.tvYearlySaving);
        llSimulatorBody = findViewById(R.id.llSimulatorBody);
        tvNoCategoryData = findViewById(R.id.tvNoCategoryData);

        chip10.setOnClickListener(v -> selectPercent(10));
        chip20.setOnClickListener(v -> selectPercent(20));
        chip30.setOnClickListener(v -> selectPercent(30));

        spinnerCategory.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                recalculate();
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        viewModel.getCategoryTotalsThisMonth().observe(this, this::onCategoryTotalsLoaded);
    }

    private void onCategoryTotalsLoaded(List<CategoryTotal> totals) {
        categoryTotals = totals != null ? totals : new ArrayList<>();

        if (categoryTotals.isEmpty()) {
            llSimulatorBody.setVisibility(View.GONE);
            spinnerCategory.setVisibility(View.GONE);
            tvNoCategoryData.setVisibility(View.VISIBLE);
            return;
        }
        tvNoCategoryData.setVisibility(View.GONE);
        spinnerCategory.setVisibility(View.VISIBLE);
        llSimulatorBody.setVisibility(View.VISIBLE);

        List<String> names = new ArrayList<>();
        for (CategoryTotal t : categoryTotals) {
            names.add(t.category);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item_selected, names);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        recalculate();
    }

    private void selectPercent(int percent) {
        selectedPercent = percent;

        chip10.setBackgroundResource(percent == 10 ? R.drawable.bg_button_primary : R.drawable.bg_button_ghost);
        chip10.setTextColor(percent == 10 ? 0xFF0F1B2D : 0xFF8B96A8);
        chip20.setBackgroundResource(percent == 20 ? R.drawable.bg_button_primary : R.drawable.bg_button_ghost);
        chip20.setTextColor(percent == 20 ? 0xFF0F1B2D : 0xFF8B96A8);
        chip30.setBackgroundResource(percent == 30 ? R.drawable.bg_button_primary : R.drawable.bg_button_ghost);
        chip30.setTextColor(percent == 30 ? 0xFF0F1B2D : 0xFF8B96A8);

        recalculate();
    }

    private void recalculate() {
        if (categoryTotals.isEmpty()) return;
        int position = spinnerCategory.getSelectedItemPosition();
        if (position < 0 || position >= categoryTotals.size()) return;

        double currentSpend = categoryTotals.get(position).total;
        double monthlySaving = currentSpend * (selectedPercent / 100.0);
        double yearlySaving = monthlySaving * 12;

        tvCurrentSpend.setText(String.format(Locale.getDefault(), "₹%.0f", currentSpend));
        tvMonthlySaving.setText(String.format(Locale.getDefault(), "₹%.0f", monthlySaving));
        tvYearlySaving.setText(String.format(Locale.getDefault(), "₹%.0f", yearlySaving));
    }
}
