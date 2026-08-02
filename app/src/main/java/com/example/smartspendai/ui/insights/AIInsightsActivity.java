package com.example.smartspendai.ui.insights;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.smartspendai.R;
import com.example.smartspendai.data.model.BudgetResponse;
import com.example.smartspendai.data.model.ForecastResponse;
import com.example.smartspendai.data.model.HealthScoreResponse;
import com.example.smartspendai.data.model.SavingSuggestion;
import com.example.smartspendai.ui.analytics.AnalyticsActivity;
import com.example.smartspendai.ui.dashboard.DashboardActivity;
import com.example.smartspendai.ui.expense.ExpenseListActivity;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AIInsightsActivity extends AppCompatActivity {

    private InsightsViewModel viewModel;

    private PieChart gaugeChart;
    private TextView tvHealthScoreValue;
    private LinearLayout llHealthExplanation;
    private TextView tvForecastWeek, tvForecastMonth, tvForecastMessage;
    private TextView tvBudgetMessage;
    private LinearLayout llBudgetBreakdown;
    private LinearLayout llSuggestions;
    private TextView tvNoSuggestions;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_insights);

        viewModel = new ViewModelProvider(this).get(InsightsViewModel.class);

        gaugeChart = findViewById(R.id.gaugeChart);
        tvHealthScoreValue = findViewById(R.id.tvHealthScoreValue);
        llHealthExplanation = findViewById(R.id.llHealthExplanation);
        tvForecastWeek = findViewById(R.id.tvForecastWeek);
        tvForecastMonth = findViewById(R.id.tvForecastMonth);
        tvForecastMessage = findViewById(R.id.tvForecastMessage);
        tvBudgetMessage = findViewById(R.id.tvBudgetMessage);
        llBudgetBreakdown = findViewById(R.id.llBudgetBreakdown);
        llSuggestions = findViewById(R.id.llSuggestions);
        tvNoSuggestions = findViewById(R.id.tvNoSuggestions);

        setupGaugeStyle();
        setupBottomNav();
        observeViewModel();

        viewModel.loadAll();
    }

    private void observeViewModel() {
        viewModel.healthScore.observe(this, this::renderHealthScore);
        viewModel.forecast.observe(this, this::renderForecast);
        viewModel.budget.observe(this, this::renderBudget);
        viewModel.suggestions.observe(this, this::renderSuggestions);
        viewModel.errorMessage.observe(this, msg ->
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }

    private void setupGaugeStyle() {
        gaugeChart.getDescription().setEnabled(false);
        gaugeChart.getLegend().setEnabled(false);
        gaugeChart.setTouchEnabled(false);
        gaugeChart.setHoleColor(Color.TRANSPARENT);
        gaugeChart.setHoleRadius(78f);
        gaugeChart.setTransparentCircleRadius(0f);
        gaugeChart.setRotationEnabled(false);
        gaugeChart.setDrawEntryLabels(false);
    }

    private void renderHealthScore(HealthScoreResponse response) {
        if (response == null) return;

        tvHealthScoreValue.setText(String.valueOf(response.score));

        int color = response.score >= 70 ? 0xFF4FBA82 : (response.score >= 40 ? 0xFFE8A33D : 0xFFE8615A);

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(response.score));
        entries.add(new PieEntry(100 - response.score));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(color, 0x14FFFFFF);
        dataSet.setDrawValues(false);
        dataSet.setSliceSpace(0f);

        PieData data = new PieData(dataSet);
        gaugeChart.setData(data);
        gaugeChart.setRotationAngle(270); // starts the arc at the top, opens like a gauge
        gaugeChart.setMaxAngle(180);      // half-circle "gauge" look instead of a full donut
        gaugeChart.setCenterTextRadiusPercent(100f);
        gaugeChart.invalidate();

        llHealthExplanation.removeAllViews();
        for (String line : response.explanation) {
            TextView tv = new TextView(this);
            tv.setText("• " + line);
            tv.setTextColor(0xFF8B96A8);
            tv.setTextSize(11.5f);
            tv.setPadding(0, 4, 0, 4);
            llHealthExplanation.addView(tv);
        }
    }

    private void renderForecast(ForecastResponse response) {
        if (response == null) return;
        tvForecastWeek.setText(String.format(Locale.getDefault(), "₹%.0f", response.next_week_total));
        tvForecastMonth.setText(String.format(Locale.getDefault(), "₹%.0f", response.next_month_total));
        tvForecastMessage.setText(response.message);
    }

    private void renderBudget(BudgetResponse response) {
        if (response == null) return;
        tvBudgetMessage.setText(response.message);
        llBudgetBreakdown.removeAllViews();

        for (Map.Entry<String, Double> entry : response.recommended_by_category.entrySet()) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            int pad = (int) (14 * getResources().getDisplayMetrics().density);
            row.setPadding(pad, pad - 4, pad, pad - 4);

            TextView tvCategory = new TextView(this);
            tvCategory.setText(entry.getKey());
            tvCategory.setTextColor(0xFFF2F0EA);
            tvCategory.setTextSize(13f);
            LinearLayout.LayoutParams categoryParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            row.addView(tvCategory, categoryParams);

            TextView tvAmount = new TextView(this);
            tvAmount.setText(String.format(Locale.getDefault(), "₹%.0f", entry.getValue()));
            tvAmount.setTextColor(0xFFE8A33D);
            tvAmount.setTextSize(13f);
            tvAmount.setTypeface(null, android.graphics.Typeface.BOLD);
            row.addView(tvAmount);

            llBudgetBreakdown.addView(row);
        }
    }

    private void renderSuggestions(List<SavingSuggestion> suggestions) {
        llSuggestions.removeAllViews();

        if (suggestions == null || suggestions.isEmpty()) {
            tvNoSuggestions.setVisibility(View.VISIBLE);
            return;
        }
        tvNoSuggestions.setVisibility(View.GONE);

        for (SavingSuggestion s : suggestions) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundColor(0xFF16243A);
            int pad = (int) (14 * getResources().getDisplayMetrics().density);
            card.setPadding(pad, pad, pad, pad);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            cardParams.bottomMargin = (int) (10 * getResources().getDisplayMetrics().density);
            card.setLayoutParams(cardParams);

            TextView tvTitle = new TextView(this);
            tvTitle.setText(s.category + " — " + (int) s.overspend_percent + "% above your average");
            tvTitle.setTextColor(0xFFE8615A);
            tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            tvTitle.setTextSize(13f);
            card.addView(tvTitle);

            TextView tvDetail = new TextView(this);
            tvDetail.setText(String.format(Locale.getDefault(),
                    "This month: ₹%.0f vs your usual ₹%.0f. Cutting %d%% could save ₹%.0f/month (₹%.0f/year).",
                    s.current_month_spend, s.historical_average, s.suggested_reduction_percent,
                    s.estimated_monthly_saving, s.estimated_yearly_saving));
            tvDetail.setTextColor(0xFF8B96A8);
            tvDetail.setTextSize(11.5f);
            tvDetail.setPadding(0, 6, 0, 0);
            card.addView(tvDetail);

            llSuggestions.addView(card);
        }
    }

    private void setupBottomNav() {
        findViewById(R.id.navHome).setOnClickListener(v -> {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        });
        findViewById(R.id.navAnalytics).setOnClickListener(v ->
                startActivity(new Intent(this, AnalyticsActivity.class)));
        findViewById(R.id.navExpenses).setOnClickListener(v ->
                startActivity(new Intent(this, ExpenseListActivity.class)));
        // navInsights: already here, intentionally does nothing.
    }
}
