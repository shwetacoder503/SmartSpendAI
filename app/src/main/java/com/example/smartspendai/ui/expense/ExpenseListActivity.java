package com.example.smartspendai.ui.expense;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartspendai.R;
import com.example.smartspendai.data.local.entity.ExpenseEntity;

import java.util.List;

public class ExpenseListActivity extends AppCompatActivity {

    private ExpenseViewModel viewModel;
    private ExpenseAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_list);

        viewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);

        RecyclerView recyclerView = findViewById(R.id.recyclerExpenses);
        TextView tvEmptyState = findViewById(R.id.tvEmptyState);
        android.widget.Button fabAdd = findViewById(R.id.fabAddExpense);

        adapter = new ExpenseAdapter(new ExpenseAdapter.OnExpenseClickListener() {
            @Override
            public void onExpenseClick(ExpenseEntity expense) {
                openEditScreen(expense);
            }

            @Override
            public void onExpenseLongClick(ExpenseEntity expense) {
                confirmDelete(expense);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // This is the payoff of using LiveData: whenever the underlying
        // Room table changes (insert/update/delete, from ANY screen), this
        // callback fires automatically and the list redraws itself. We
        // never manually "refresh" the list after adding an expense.
        viewModel.getAllExpenses().observe(this, (List<ExpenseEntity> expenses) -> {
            adapter.submitList(expenses);
            boolean isEmpty = expenses == null || expenses.isEmpty();
            tvEmptyState.setVisibility(isEmpty ? android.view.View.VISIBLE : android.view.View.GONE);
            recyclerView.setVisibility(isEmpty ? android.view.View.GONE : android.view.View.VISIBLE);
        });

        fabAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddExpenseActivity.class)));
    }

    private void openEditScreen(ExpenseEntity expense) {
        Intent intent = new Intent(this, AddExpenseActivity.class);
        intent.putExtra(AddExpenseActivity.EXTRA_EXPENSE_ID, expense.id);
        intent.putExtra(AddExpenseActivity.EXTRA_TITLE, expense.title);
        intent.putExtra(AddExpenseActivity.EXTRA_AMOUNT, expense.amount);
        intent.putExtra(AddExpenseActivity.EXTRA_CATEGORY, expense.category);
        intent.putExtra(AddExpenseActivity.EXTRA_PAYMENT_METHOD, expense.paymentMethod);
        intent.putExtra(AddExpenseActivity.EXTRA_NOTE, expense.note);
        intent.putExtra(AddExpenseActivity.EXTRA_DATE_MILLIS, expense.dateMillis);
        startActivity(intent);
    }

    private void confirmDelete(ExpenseEntity expense) {
        new AlertDialog.Builder(this)
                .setTitle("Delete this expense?")
                .setMessage(expense.title + " — ₹" + (long) expense.amount)
                .setPositiveButton("Delete", (dialog, which) -> viewModel.deleteExpense(expense))
                .setNegativeButton("Cancel", null)
                .show();
    }
}
