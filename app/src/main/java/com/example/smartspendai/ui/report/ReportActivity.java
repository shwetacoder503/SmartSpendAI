package com.example.smartspendai.ui.report;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.example.smartspendai.R;
import com.example.smartspendai.data.local.pojo.CategoryTotal;
import com.example.smartspendai.ui.expense.ExpenseViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.AbstractMap;

public class ReportActivity extends AppCompatActivity {

    private enum Period { WEEKLY, MONTHLY, YEARLY }

    private ExpenseViewModel viewModel;

    private TextView chipWeekly, chipMonthly, chipYearly;
    private TextView tvReportPeriodLabel, tvReportTotal, tvNoData;
    private LinearLayout llCategoryBreakdown;
    private Button btnExportPdf;

    private Period selectedPeriod = Period.MONTHLY;
    private double currentTotal = 0;
    private List<Map.Entry<String, Double>> currentCategoryTotals = new ArrayList<>();
    private String currentPeriodLabel = "This Month";

    @Nullable private LiveData<Double> currentTotalLiveData;
    @Nullable private LiveData<List<CategoryTotal>> currentCategoryLiveData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        viewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);

        findViewById(R.id.tvClose).setOnClickListener(v -> finish());

        chipWeekly = findViewById(R.id.chipWeekly);
        chipMonthly = findViewById(R.id.chipMonthly);
        chipYearly = findViewById(R.id.chipYearly);
        tvReportPeriodLabel = findViewById(R.id.tvReportPeriodLabel);
        tvReportTotal = findViewById(R.id.tvReportTotal);
        tvNoData = findViewById(R.id.tvNoData);
        llCategoryBreakdown = findViewById(R.id.llCategoryBreakdown);
        btnExportPdf = findViewById(R.id.btnExportPdf);

        chipWeekly.setOnClickListener(v -> selectPeriod(Period.WEEKLY));
        chipMonthly.setOnClickListener(v -> selectPeriod(Period.MONTHLY));
        chipYearly.setOnClickListener(v -> selectPeriod(Period.YEARLY));
        btnExportPdf.setOnClickListener(v -> exportPdf());

        selectPeriod(Period.MONTHLY);
    }

    private void selectPeriod(Period period) {
        selectedPeriod = period;
        updateChipStyles();

        long[] range = rangeFor(period);
        currentPeriodLabel = labelFor(period);
        tvReportPeriodLabel.setText(currentPeriodLabel.toUpperCase(Locale.getDefault()));

        if (currentTotalLiveData != null) currentTotalLiveData.removeObservers(this);
        if (currentCategoryLiveData != null) currentCategoryLiveData.removeObservers(this);

        currentTotalLiveData = viewModel.getTotalForRange(range[0], range[1]);
        currentTotalLiveData.observe(this, total -> {
            currentTotal = total != null ? total : 0;
            tvReportTotal.setText(String.format(Locale.getDefault(), "₹%.0f", currentTotal));
        });

        currentCategoryLiveData = viewModel.getCategoryTotalsForRange(range[0], range[1]);
        currentCategoryLiveData.observe(this, this::renderCategoryBreakdown);
    }

    private void renderCategoryBreakdown(List<CategoryTotal> totals) {
        llCategoryBreakdown.removeAllViews();
        currentCategoryTotals = new ArrayList<>();

        if (totals == null || totals.isEmpty()) {
            tvNoData.setVisibility(View.VISIBLE);
            return;
        }
        tvNoData.setVisibility(View.GONE);

        for (CategoryTotal t : totals) {
            currentCategoryTotals.add(new AbstractMap.SimpleEntry<>(t.category, t.total));

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            int pad = (int) (14 * getResources().getDisplayMetrics().density);
            row.setPadding(pad, pad - 4, pad, pad - 4);

            TextView tvCategory = new TextView(this);
            tvCategory.setText(t.category);
            tvCategory.setTextColor(0xFFF2F0EA);
            tvCategory.setTextSize(13f);
            row.addView(tvCategory, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            TextView tvAmount = new TextView(this);
            tvAmount.setText(String.format(Locale.getDefault(), "₹%.0f", t.total));
            tvAmount.setTextColor(0xFFE8A33D);
            tvAmount.setTypeface(null, android.graphics.Typeface.BOLD);
            row.addView(tvAmount);

            llCategoryBreakdown.addView(row);
        }
    }

    private void updateChipStyles() {
        chipWeekly.setBackgroundResource(selectedPeriod == Period.WEEKLY ? R.drawable.bg_button_primary : R.drawable.bg_button_ghost);
        chipWeekly.setTextColor(selectedPeriod == Period.WEEKLY ? 0xFF0F1B2D : 0xFF8B96A8);

        chipMonthly.setBackgroundResource(selectedPeriod == Period.MONTHLY ? R.drawable.bg_button_primary : R.drawable.bg_button_ghost);
        chipMonthly.setTextColor(selectedPeriod == Period.MONTHLY ? 0xFF0F1B2D : 0xFF8B96A8);

        chipYearly.setBackgroundResource(selectedPeriod == Period.YEARLY ? R.drawable.bg_button_primary : R.drawable.bg_button_ghost);
        chipYearly.setTextColor(selectedPeriod == Period.YEARLY ? 0xFF0F1B2D : 0xFF8B96A8);
    }

    private long[] rangeFor(Period period) {
        Calendar cal = Calendar.getInstance();
        long end = System.currentTimeMillis();
        long start;

        switch (period) {
            case WEEKLY:
                cal.add(Calendar.DAY_OF_YEAR, -6);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                start = cal.getTimeInMillis();
                break;
            case YEARLY:
                cal.set(Calendar.DAY_OF_YEAR, 1);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                start = cal.getTimeInMillis();
                break;
            case MONTHLY:
            default:
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                start = cal.getTimeInMillis();
                break;
        }
        return new long[]{start, end};
    }

    private String labelFor(Period period) {
        switch (period) {
            case WEEKLY: return "Last 7 Days";
            case YEARLY: return "This Year";
            case MONTHLY:
            default: return "This Month";
        }
    }

    private void exportPdf() {
        try {
            File pdfFile = PdfReportGenerator.generate(this, currentPeriodLabel, currentTotal, currentCategoryTotals);

            Uri contentUri = FileProvider.getUriForFile(
                    this, getPackageName() + ".fileprovider", pdfFile);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, "Save or share your report"));
        } catch (Exception e) {
            Toast.makeText(this, "Couldn't generate PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
