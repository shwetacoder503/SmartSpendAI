package com.example.smartspendai.ui.goal;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.smartspendai.R;
import com.example.smartspendai.data.local.entity.GoalContributionEntity;
import com.example.smartspendai.data.local.entity.GoalEntity;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class GoalDetailActivity extends AppCompatActivity {

    public static final String EXTRA_GOAL_ID = "extra_goal_id";

    private GoalViewModel viewModel;
    private long goalId;

    private TextView tvScreenTitle, tvProgressAmount, tvEstimate, tvNoContributions;
    private View progressFill;
    private LinearLayout llContributions;
    private EditText etContributionAmount;

    private double targetAmount = 0;
    private double totalContributed = 0;
    private long createdDateMillis = System.currentTimeMillis();
    @Nullable private GoalEntity currentGoal;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_detail);

        goalId = getIntent().getLongExtra(EXTRA_GOAL_ID, -1L);
        if (goalId == -1L) {
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(GoalViewModel.class);

        findViewById(R.id.tvClose).setOnClickListener(v -> finish());
        tvScreenTitle = findViewById(R.id.tvScreenTitle);
        tvProgressAmount = findViewById(R.id.tvProgressAmount);
        tvEstimate = findViewById(R.id.tvEstimate);
        progressFill = findViewById(R.id.progressFill);
        llContributions = findViewById(R.id.llContributions);
        tvNoContributions = findViewById(R.id.tvNoContributions);
        etContributionAmount = findViewById(R.id.etContributionAmount);
        Button btnAddContribution = findViewById(R.id.btnAddContribution);
        TextView tvDelete = findViewById(R.id.tvDelete);

        viewModel.getGoalById(goalId).observe(this, this::onGoalLoaded);
        viewModel.getTotalContributed(goalId).observe(this, this::onTotalContributedLoaded);
        viewModel.getContributionsForGoal(goalId).observe(this, this::renderContributions);

        btnAddContribution.setOnClickListener(v -> addContribution());
        tvDelete.setOnClickListener(v -> confirmDelete());
    }

    private void onGoalLoaded(@Nullable GoalEntity goal) {
        if (goal == null) return;
        currentGoal = goal;
        targetAmount = goal.targetAmount;
        createdDateMillis = goal.createdDateMillis;
        tvScreenTitle.setText(goal.title);
        updateProgressUi();
    }

    private void onTotalContributedLoaded(@Nullable Double total) {
        totalContributed = total != null ? total : 0;
        updateProgressUi();
    }

    private void updateProgressUi() {
        tvProgressAmount.setText(String.format(Locale.getDefault(), "₹%,.0f of ₹%,.0f",
                totalContributed, targetAmount));

        int percent = targetAmount > 0
                ? (int) Math.min(100, Math.round((totalContributed / targetAmount) * 100))
                : 0;

        View track = (View) progressFill.getParent();
        track.post(() -> {
            int trackWidth = track.getWidth();
            progressFill.getLayoutParams().width = (int) (trackWidth * (percent / 100f));
            progressFill.requestLayout();
        });

        Long estimateMillis = GoalEstimator.estimateCompletionMillis(targetAmount, totalContributed, createdDateMillis);
        if (totalContributed >= targetAmount && targetAmount > 0) {
            tvEstimate.setText("🎉 Goal reached!");
        } else if (estimateMillis != null) {
            String dateStr = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(estimateMillis);
            tvEstimate.setText("Estimated completion: " + dateStr + " (based on your average pace so far)");
        } else {
            tvEstimate.setText("Add a contribution to estimate a completion date");
        }
    }

    private void addContribution() {
        String amountStr = etContributionAmount.getText().toString().trim();
        if (TextUtils.isEmpty(amountStr)) {
            etContributionAmount.setError("Enter an amount");
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            etContributionAmount.setError("Enter a valid number");
            return;
        }
        if (amount <= 0) {
            etContributionAmount.setError("Amount must be greater than 0");
            return;
        }

        viewModel.addContribution(goalId, amount, "");
        etContributionAmount.setText("");
        Toast.makeText(this, "Contribution added!", Toast.LENGTH_SHORT).show();
    }

    private void renderContributions(List<GoalContributionEntity> contributions) {
        llContributions.removeAllViews();

        if (contributions == null || contributions.isEmpty()) {
            tvNoContributions.setVisibility(View.VISIBLE);
            return;
        }
        tvNoContributions.setVisibility(View.GONE);

        for (GoalContributionEntity c : contributions) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            int pad = (int) (14 * getResources().getDisplayMetrics().density);
            row.setPadding(pad, pad - 4, pad, pad - 4);

            TextView tvDate = new TextView(this);
            CharSequence relativeDate = DateUtils.getRelativeTimeSpanString(
                    c.dateMillis, System.currentTimeMillis(), DateUtils.DAY_IN_MILLIS);
            tvDate.setText(relativeDate);
            tvDate.setTextColor(0xFF8B96A8);
            tvDate.setTextSize(12.5f);
            row.addView(tvDate, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            TextView tvAmount = new TextView(this);
            tvAmount.setText(String.format(Locale.getDefault(), "+ ₹%.0f", c.amount));
            tvAmount.setTextColor(0xFF4FBA82);
            tvAmount.setTypeface(null, android.graphics.Typeface.BOLD);
            tvAmount.setTextSize(13f);
            row.addView(tvAmount);

            llContributions.addView(row);
        }
    }

    private void confirmDelete() {
        if (currentGoal == null) return;
        new AlertDialog.Builder(this)
                .setTitle("Delete this goal?")
                .setMessage("This will also delete all its contribution history. This can't be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteGoal(currentGoal);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
