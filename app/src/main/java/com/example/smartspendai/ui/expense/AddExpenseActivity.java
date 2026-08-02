package com.example.smartspendai.ui.expense;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.smartspendai.R;
import com.example.smartspendai.data.local.entity.CategoryEntity;
import com.example.smartspendai.data.local.entity.ExpenseEntity;
import com.example.smartspendai.ui.category.CategoryViewModel;
import com.example.smartspendai.ui.category.ManageCategoriesActivity;

import java.util.ArrayList;
import java.util.List;

public class AddExpenseActivity extends AppCompatActivity {

    public static final String EXTRA_EXPENSE_ID = "extra_expense_id";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_AMOUNT = "extra_amount";
    public static final String EXTRA_CATEGORY = "extra_category";
    public static final String EXTRA_PAYMENT_METHOD = "extra_payment_method";
    public static final String EXTRA_NOTE = "extra_note";
    public static final String EXTRA_DATE_MILLIS = "extra_date_millis";
    public static final String EXTRA_REMOTE_ID = "extra_remote_id";

    private ExpenseViewModel viewModel;
    private CategoryViewModel categoryViewModel;

    private EditText etTitle, etAmount, etNote;
    private Spinner spinnerCategory, spinnerPaymentMethod;

    private long editingExpenseId = -1L;
    private long editingDateMillis = -1L;
    private long editingRemoteId = -1L; // -1 sentinel means "never synced yet"
    private boolean isEditMode = false;

    /** Category the Intent asked us to preselect — applied once the DB list finishes loading. */
    private String pendingCategorySelection;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        viewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);

        TextView tvClose = findViewById(R.id.tvClose);
        TextView tvScreenTitle = findViewById(R.id.tvScreenTitle);
        TextView tvManageCategories = findViewById(R.id.tvManageCategories);
        etTitle = findViewById(R.id.etTitle);
        etAmount = findViewById(R.id.etAmount);
        etNote = findViewById(R.id.etNote);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerPaymentMethod = findViewById(R.id.spinnerPaymentMethod);
        Button btnSave = findViewById(R.id.btnSaveExpense);
        Button btnDelete = findViewById(R.id.btnDeleteExpense);

        applyDarkSpinnerStyle(spinnerPaymentMethod, R.array.payment_methods);
        setupCategorySpinner();

        tvClose.setOnClickListener(v -> finish());
        tvManageCategories.setOnClickListener(v ->
                startActivity(new Intent(this, ManageCategoriesActivity.class)));

        // If this Activity was opened to EDIT an existing expense, the
        // caller passes the expense's fields as intent extras. We prefill
        // the form and switch the button label + show a Delete option.
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_EXPENSE_ID)) {
            isEditMode = true;
            editingExpenseId = intent.getLongExtra(EXTRA_EXPENSE_ID, -1L);
            editingDateMillis = intent.getLongExtra(EXTRA_DATE_MILLIS, System.currentTimeMillis());
            editingRemoteId = intent.getLongExtra(EXTRA_REMOTE_ID, -1L);

            tvScreenTitle.setText("Edit Expense");
            btnSave.setText("Update Expense");
            btnDelete.setVisibility(android.view.View.VISIBLE);

            etTitle.setText(intent.getStringExtra(EXTRA_TITLE));
            etAmount.setText(String.valueOf(intent.getDoubleExtra(EXTRA_AMOUNT, 0)));
            etNote.setText(intent.getStringExtra(EXTRA_NOTE));
            pendingCategorySelection = intent.getStringExtra(EXTRA_CATEGORY);
            setSpinnerSelection(spinnerPaymentMethod, intent.getStringExtra(EXTRA_PAYMENT_METHOD));
        }

        btnSave.setOnClickListener(v -> saveExpense());
        btnDelete.setOnClickListener(v -> deleteExpense());
    }

    /**
     * Categories now come from the `categories` table (Milestone 5), not a
     * static array — this lets the user add their own custom categories in
     * Manage Categories and see them here immediately.
     */
    private void setupCategorySpinner() {
        categoryViewModel.getAllCategories().observe(this, (List<CategoryEntity> categories) -> {
            List<String> names = new ArrayList<>();
            for (CategoryEntity category : categories) {
                names.add(category.name);
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item_selected, names);
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
            spinnerCategory.setAdapter(adapter);

            if (pendingCategorySelection != null) {
                setSpinnerSelection(spinnerCategory, pendingCategorySelection);
            }
        });
    }

    /**
     * Spinner's default popup uses the system's light theme colors, which
     * look broken on our dark background. Pointing it at our own layouts
     * (spinner_item_selected / spinner_dropdown_item, both already styled
     * dark) fixes that without needing a full Material dropdown component.
     */
    private void applyDarkSpinnerStyle(Spinner spinner, int arrayResId) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, arrayResId, R.layout.spinner_item_selected);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        if (value == null || spinner.getAdapter() == null) return;
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        int position = adapter.getPosition(value);
        if (position >= 0) spinner.setSelection(position);
    }

    private void saveExpense() {
        String title = etTitle.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        String note = etNote.getText().toString().trim();
        String category = String.valueOf(spinnerCategory.getSelectedItem());
        String paymentMethod = String.valueOf(spinnerPaymentMethod.getSelectedItem());

        if (TextUtils.isEmpty(title)) {
            etTitle.setError("Enter a title");
            etTitle.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(amountStr)) {
            etAmount.setError("Enter an amount");
            etAmount.requestFocus();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            etAmount.setError("Enter a valid number");
            etAmount.requestFocus();
            return;
        }
        if (amount <= 0) {
            etAmount.setError("Amount must be greater than 0");
            etAmount.requestFocus();
            return;
        }

        if (isEditMode) {
            ExpenseEntity expense = new ExpenseEntity();
            expense.id = editingExpenseId;
            expense.title = title;
            expense.amount = amount;
            expense.category = category;
            expense.paymentMethod = paymentMethod;
            expense.note = note;
            expense.dateMillis = editingDateMillis; // keep the original date on edit
            expense.remoteId = (editingRemoteId == -1L) ? null : editingRemoteId;
            expense.isSynced = false; // content changed — needs re-syncing (as an UPDATE if remoteId is set)
            viewModel.updateExpense(expense);
            Toast.makeText(this, "Expense updated", Toast.LENGTH_SHORT).show();
        } else {
            viewModel.addExpense(title, amount, category, paymentMethod, note);
            Toast.makeText(this, "Expense added", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void deleteExpense() {
        if (!isEditMode) return;
        ExpenseEntity expense = new ExpenseEntity();
        expense.id = editingExpenseId;
        viewModel.deleteExpense(expense);
        Toast.makeText(this, "Expense deleted", Toast.LENGTH_SHORT).show();
        finish();
    }
}
