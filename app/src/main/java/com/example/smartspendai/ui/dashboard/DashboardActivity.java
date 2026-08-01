package com.example.smartspendai.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.smartspendai.R;
import com.example.smartspendai.data.local.TokenManager;
import com.example.smartspendai.data.local.entity.ExpenseEntity;
import com.example.smartspendai.data.local.pojo.MonthTotal;
import com.example.smartspendai.ui.analytics.AnalyticsActivity;
import com.example.smartspendai.ui.expense.ExpenseListActivity;
import com.example.smartspendai.ui.expense.ExpenseViewModel;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private ExpenseViewModel viewModel;
    private TokenManager tokenManager;

    private TextView tvGreeting, tvMonthTotal, tvTodayTotal, tvRemainingBudget, tvBudgetCaption;
    private View budgetFill;
    private LinearLayout llRecentExpenses;
    private TextView tvNoExpenses;
    private LineChart lineChartTrend;

    private double currentMonthTotal = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        viewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);
        tokenManager = new TokenManager(this);

        tvGreeting = findViewById(R.id.tvGreeting);
        tvMonthTotal = findViewById(R.id.tvMonthTotal);
        tvTodayTotal = findViewById(R.id.tvTodayTotal);
        tvRemainingBudget = findViewById(R.id.tvRemainingBudget);
        tvBudgetCaption = findViewById(R.id.tvBudgetCaption);
        budgetFill = findViewById(R.id.budgetFill);
        llRecentExpenses = findViewById(R.id.llRecentExpenses);
        tvNoExpenses = findViewById(R.id.tvNoExpenses);
        lineChartTrend = findViewById(R.id.lineChartTrend);

        String userName = tokenManager.getUserName();
        tvGreeting.setText(userName != null ? "Good to see you, " + userName : "Good to see you");

        setupChartStyle();
        setupBottomNav();
        observeData();

        findViewById(R.id.tvSeeAll).setOnClickListener(v ->
                startActivity(new Intent(this, ExpenseListActivity.class)));
    }

    private void observeData() {
        viewModel.getTodayTotal().observe(this, total -> {
            double t = total != null ? total : 0;
            tvTodayTotal.setText(String.format(Locale.getDefault(), "₹%.0f", t));
        });

        viewModel.getMonthTotal().observe(this, total -> {
            currentMonthTotal = total != null ? total : 0;
            tvMonthTotal.setText(String.format(Locale.getDefault(), "₹%.0f", currentMonthTotal));
            updateBudgetUi();
        });

        viewModel.getAllExpenses().observe(this, this::renderRecentExpenses);

        viewModel.getLast5MonthsTotals().observe(this, this::renderTrendChart);
    }

    private void updateBudgetUi() {
        int income = tokenManager.getMonthlyIncome();
        if (income <= 0) {
            tvRemainingBudget.setText("Set income to see");
            tvBudgetCaption.setText("Add your income at registration to track budget %");
            budgetFill.getLayoutParams().width = 0;
            budgetFill.requestLayout();
            return;
        }

        double remaining = income - currentMonthTotal;
        tvRemainingBudget.setText(String.format(Locale.getDefault(), "₹%.0f", remaining));

        int percentUsed = (int) Math.min(100, Math.round((currentMonthTotal / income) * 100));
        tvBudgetCaption.setText(percentUsed + "% of monthly budget used");

        // Grow the gold fill bar proportionally inside its 8dp-tall track.
        View track = (View) budgetFill.getParent();
        track.post(() -> {
            int trackWidth = track.getWidth();
            budgetFill.getLayoutParams().width = (int) (trackWidth * (percentUsed / 100f));
            budgetFill.requestLayout();
        });
    }

    private void renderRecentExpenses(List<ExpenseEntity> allExpenses) {
        llRecentExpenses.removeAllViews();

        if (allExpenses == null || allExpenses.isEmpty()) {
            tvNoExpenses.setVisibility(View.VISIBLE);
            return;
        }
        tvNoExpenses.setVisibility(View.GONE);

        int count = Math.min(4, allExpenses.size());
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < count; i++) {
            ExpenseEntity expense = allExpenses.get(i);
            View row = inflater.inflate(R.layout.item_expense, llRecentExpenses, false);

            TextView tvTitle = row.findViewById(R.id.tvTitle);
            TextView tvCategoryDate = row.findViewById(R.id.tvCategoryDate);
            TextView tvAmount = row.findViewById(R.id.tvAmount);
            TextView tvEmoji = row.findViewById(R.id.tvCategoryEmoji);

            tvTitle.setText(expense.title);
            tvAmount.setText(String.format(Locale.getDefault(), "− ₹%.0f", expense.amount));
            CharSequence relativeDate = DateUtils.getRelativeTimeSpanString(
                    expense.dateMillis, System.currentTimeMillis(), DateUtils.DAY_IN_MILLIS);
            tvCategoryDate.setText(expense.category + " · " + relativeDate);
            tvEmoji.setText(emojiFor(expense.category));

            llRecentExpenses.addView(row);
        }
    }

    private String emojiFor(String category) {
        if (category == null) return "📦";
        switch (category) {
            case "Food": return "🍔";
            case "Shopping": return "🛍️";
            case "Travel": return "🚕";
            case "Medical": return "💊";
            case "Bills": return "💡";
            case "Education": return "📚";
            case "Entertainment": return "🎬";
            case "Investment": return "📈";
            default: return "📦";
        }
    }

    private void setupChartStyle() {
        lineChartTrend.getDescription().setEnabled(false);
        lineChartTrend.getLegend().setEnabled(false);
        lineChartTrend.getAxisRight().setEnabled(false);
        lineChartTrend.getAxisLeft().setTextColor(0xFF8B96A8);
        lineChartTrend.getAxisLeft().setGridColor(0x14FFFFFF);
        lineChartTrend.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChartTrend.getXAxis().setTextColor(0xFF8B96A8);
        lineChartTrend.getXAxis().setGridColor(0x00000000);
        lineChartTrend.setNoDataText("Add expenses to see your trend");
        lineChartTrend.setNoDataTextColor(0xFF8B96A8);
        lineChartTrend.setTouchEnabled(true);
        lineChartTrend.setPinchZoom(false);
        lineChartTrend.setDragEnabled(false);
    }

    private void renderTrendChart(List<MonthTotal> monthTotals) {
        if (monthTotals == null || monthTotals.isEmpty()) {
            lineChartTrend.clear();
            return;
        }

        // Query returns most-recent-first (DESC LIMIT 5) — reverse so the
        // chart reads left-to-right, oldest to newest.
        List<MonthTotal> chronological = new ArrayList<>(monthTotals);
        Collections.reverse(chronological);

        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < chronological.size(); i++) {
            entries.add(new Entry(i, (float) chronological.get(i).total));
            labels.add(chronological.get(i).yearMonth.substring(5)); // "2026-07" -> "07"
        }

        LineDataSet dataSet = new LineDataSet(entries, "Monthly spend");
        dataSet.setColor(0xFFE8A33D);
        dataSet.setCircleColor(0xFFE8A33D);
        dataSet.setLineWidth(2.5f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(0xFFE8A33D);
        dataSet.setFillAlpha(30);

        lineChartTrend.setData(new LineData(dataSet));
        lineChartTrend.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        lineChartTrend.getXAxis().setGranularity(1f);
        lineChartTrend.invalidate();
    }

    private void setupBottomNav() {
        findViewById(R.id.navAnalytics).setOnClickListener(v ->
                startActivity(new Intent(this, AnalyticsActivity.class)));
        findViewById(R.id.navExpenses).setOnClickListener(v ->
                startActivity(new Intent(this, ExpenseListActivity.class)));
        // navHome: already here, intentionally does nothing.
    }
}
