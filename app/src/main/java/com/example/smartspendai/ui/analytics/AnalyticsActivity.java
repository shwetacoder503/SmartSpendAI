package com.example.smartspendai.ui.analytics;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.smartspendai.R;
import com.example.smartspendai.data.local.entity.ExpenseEntity;
import com.example.smartspendai.data.local.pojo.CategoryTotal;
import com.example.smartspendai.data.local.pojo.DayTotal;
import com.example.smartspendai.ui.dashboard.DashboardActivity;
import com.example.smartspendai.ui.expense.ExpenseListActivity;
import com.example.smartspendai.ui.expense.ExpenseViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AnalyticsActivity extends AppCompatActivity {

    private static final String[] DAY_NAMES = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

    private ExpenseViewModel viewModel;

    private PieChart pieChartCategory;
    private BarChart barChartWeekly;
    private TextView tvNoCategoryData, tvHighestCategory, tvHighestDay, tvDailyAverage, tvWeekendVsWeekday;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        viewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);

        pieChartCategory = findViewById(R.id.pieChartCategory);
        barChartWeekly = findViewById(R.id.barChartWeekly);
        tvNoCategoryData = findViewById(R.id.tvNoCategoryData);
        tvHighestCategory = findViewById(R.id.tvHighestCategory);
        tvHighestDay = findViewById(R.id.tvHighestDay);
        tvDailyAverage = findViewById(R.id.tvDailyAverage);
        tvWeekendVsWeekday = findViewById(R.id.tvWeekendVsWeekday);

        setupChartStyles();
        setupBottomNav();

        viewModel.getCategoryTotalsThisMonth().observe(this, this::renderCategoryPie);
        viewModel.getExpensesLast7Days().observe(this, this::renderWeeklyBar);
        viewModel.getDayOfWeekTotalsThisMonth().observe(this, this::renderDayStats);
        viewModel.getMonthTotal().observe(this, this::renderDailyAverage);
    }

    private void setupChartStyles() {
        pieChartCategory.getDescription().setEnabled(false);
        pieChartCategory.setHoleColor(android.graphics.Color.TRANSPARENT);
        pieChartCategory.setHoleRadius(58f);
        pieChartCategory.setTransparentCircleRadius(0f);
        pieChartCategory.setEntryLabelColor(0xFFF2F0EA);
        pieChartCategory.getLegend().setTextColor(0xFF8B96A8);
        pieChartCategory.setNoDataText("No expenses this month yet");
        pieChartCategory.setNoDataTextColor(0xFF8B96A8);

        barChartWeekly.getDescription().setEnabled(false);
        barChartWeekly.getLegend().setEnabled(false);
        barChartWeekly.getAxisRight().setEnabled(false);
        barChartWeekly.getAxisLeft().setTextColor(0xFF8B96A8);
        barChartWeekly.getAxisLeft().setGridColor(0x14FFFFFF);
        barChartWeekly.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChartWeekly.getXAxis().setTextColor(0xFF8B96A8);
        barChartWeekly.getXAxis().setGridColor(0x00000000);
        barChartWeekly.setNoDataText("Add expenses to see this");
        barChartWeekly.setNoDataTextColor(0xFF8B96A8);
    }

    private void renderCategoryPie(List<CategoryTotal> totals) {
        if (totals == null || totals.isEmpty()) {
            pieChartCategory.clear();
            tvNoCategoryData.setVisibility(View.VISIBLE);
            tvHighestCategory.setText("—");
            return;
        }
        tvNoCategoryData.setVisibility(View.GONE);

        List<PieEntry> entries = new ArrayList<>();
        for (CategoryTotal t : totals) {
            entries.add(new PieEntry((float) t.total, t.category));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextColor(0xFF0F1B2D);
        dataSet.setValueTextSize(11f);
        dataSet.setSliceSpace(2f);

        pieChartCategory.setData(new PieData(dataSet));
        pieChartCategory.invalidate();

        // totals is ordered by total DESC (from the SQL query), so the
        // first row is always the highest-spending category.
        tvHighestCategory.setText(totals.get(0).category);
    }

    private void renderWeeklyBar(List<ExpenseEntity> last7DaysExpenses) {
        // Bucket the last 7 days' expenses into 7 fixed day-slots (oldest -> today),
        // regardless of whether every day actually has an expense.
        double[] dayTotals = new double[7];
        String[] dayLabels = new String[7];

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -6);
        for (int i = 0; i < 7; i++) {
            dayLabels[i] = DAY_NAMES[cal.get(Calendar.DAY_OF_WEEK) - 1];
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        if (last7DaysExpenses != null) {
            long startOfWindow = ExpenseViewModel.startOfLast7Days();
            for (ExpenseEntity expense : last7DaysExpenses) {
                int dayIndex = (int) ((expense.dateMillis - startOfWindow) / (24L * 60 * 60 * 1000));
                if (dayIndex >= 0 && dayIndex < 7) {
                    dayTotals[dayIndex] += expense.amount;
                }
            }
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            entries.add(new BarEntry(i, (float) dayTotals[i]));
            labels.add(dayLabels[i]);
        }

        BarDataSet dataSet = new BarDataSet(entries, "Daily spend");
        dataSet.setColor(0xFFE8A33D);
        dataSet.setDrawValues(false);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);
        barChartWeekly.setData(barData);
        barChartWeekly.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChartWeekly.getXAxis().setGranularity(1f);
        barChartWeekly.invalidate();
    }

    private void renderDayStats(List<DayTotal> dayTotals) {
        if (dayTotals == null || dayTotals.isEmpty()) {
            tvHighestDay.setText("—");
            tvWeekendVsWeekday.setText("—");
            return;
        }

        DayTotal highest = dayTotals.get(0);
        double weekdayTotal = 0, weekendTotal = 0;

        for (DayTotal dt : dayTotals) {
            if (dt.total > highest.total) highest = dt;
            // SQLite's strftime('%w'): 0 = Sunday, 6 = Saturday
            if (dt.dow == 0 || dt.dow == 6) {
                weekendTotal += dt.total;
            } else {
                weekdayTotal += dt.total;
            }
        }

        tvHighestDay.setText(DAY_NAMES[highest.dow]);

        double total = weekdayTotal + weekendTotal;
        if (total > 0) {
            int weekendPercent = (int) Math.round((weekendTotal / total) * 100);
            tvWeekendVsWeekday.setText(weekendPercent >= 50
                    ? "+" + weekendPercent + "% weekend"
                    : "+" + (100 - weekendPercent) + "% weekday");
        } else {
            tvWeekendVsWeekday.setText("—");
        }
    }

    private void renderDailyAverage(Double monthTotal) {
        if (monthTotal == null || monthTotal <= 0) {
            tvDailyAverage.setText("₹0");
            return;
        }
        int daysElapsed = Calendar.getInstance().get(Calendar.DAY_OF_MONTH); // e.g. "15th" -> 15 days counted so far
        double average = monthTotal / Math.max(1, daysElapsed);
        tvDailyAverage.setText(String.format(Locale.getDefault(), "₹%.0f", average));
    }

    private void setupBottomNav() {
        findViewById(R.id.navHome).setOnClickListener(v -> {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        });
        findViewById(R.id.navExpenses).setOnClickListener(v ->
                startActivity(new Intent(this, ExpenseListActivity.class)));
        // navAnalytics: already here, intentionally does nothing.
    }
}
