package com.example.smartspendai.ui.goal;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.smartspendai.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddGoalActivity extends AppCompatActivity {

    private GoalViewModel viewModel;
    private EditText etTitle, etTargetAmount;
    private Button btnPickDate;

    @Nullable
    private Long selectedTargetDateMillis = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_goal);

        viewModel = new ViewModelProvider(this).get(GoalViewModel.class);

        TextView tvClose = findViewById(R.id.tvClose);
        etTitle = findViewById(R.id.etTitle);
        etTargetAmount = findViewById(R.id.etTargetAmount);
        btnPickDate = findViewById(R.id.btnPickDate);
        Button btnSaveGoal = findViewById(R.id.btnSaveGoal);

        tvClose.setOnClickListener(v -> finish());
        btnPickDate.setOnClickListener(v -> showDatePicker());
        btnSaveGoal.setOnClickListener(v -> saveGoal());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, dayOfMonth, 23, 59, 59);
            selectedTargetDateMillis = selected.getTimeInMillis();
            btnPickDate.setText(new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(selectedTargetDateMillis));
            btnPickDate.setTextColor(0xFFF2F0EA);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveGoal() {
        String title = etTitle.getText().toString().trim();
        String amountStr = etTargetAmount.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            etTitle.setError("Enter a goal title");
            etTitle.requestFocus();
            return;
        }

        double targetAmount;
        try {
            targetAmount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            etTargetAmount.setError("Enter a valid amount");
            etTargetAmount.requestFocus();
            return;
        }
        if (targetAmount <= 0) {
            etTargetAmount.setError("Target must be greater than 0");
            etTargetAmount.requestFocus();
            return;
        }

        viewModel.createGoal(title, targetAmount, selectedTargetDateMillis, goalId -> runOnUiThread(() -> {
            Toast.makeText(this, "Goal created!", Toast.LENGTH_SHORT).show();
            finish();
        }));
    }
}
