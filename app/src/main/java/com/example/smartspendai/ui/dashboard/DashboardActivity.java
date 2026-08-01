package com.example.smartspendai.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.smartspendai.R;
import com.example.smartspendai.ui.expense.ExpenseListActivity;

/**
 * Placeholder for now. Real dashboard (balance hero, charts, recent
 * expenses) gets built in Milestone 4 once Room + Retrofit layers exist.
 */
public class DashboardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Button btnViewExpenses = findViewById(R.id.btnViewExpenses);
        btnViewExpenses.setOnClickListener(v ->
                startActivity(new Intent(this, ExpenseListActivity.class)));
    }
}
