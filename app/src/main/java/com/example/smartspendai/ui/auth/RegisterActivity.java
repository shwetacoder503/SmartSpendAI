package com.example.smartspendai.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.smartspendai.R;
import com.example.smartspendai.data.model.UserResponse;
import com.example.smartspendai.data.repository.AuthRepository;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etIncome;
    private Button btnCreateAccount;

    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etIncome = findViewById(R.id.etIncome);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        TextView tvLoginLink = findViewById(R.id.tvLoginLink);
        TextView tvBack = findViewById(R.id.tvBack);

        btnCreateAccount.setOnClickListener(v -> attemptRegister());

        tvLoginLink.setOnClickListener(v -> goToLogin());
        tvBack.setOnClickListener(v -> goToLogin());
    }

    private void attemptRegister() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String incomeStr = etIncome.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError(getString(R.string.error_required_field));
            etName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.error_invalid_email));
            etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            etPassword.setError(getString(R.string.error_password_length));
            etPassword.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(incomeStr)) {
            etIncome.setError(getString(R.string.error_required_field));
            etIncome.requestFocus();
            return;
        }

        Integer monthlyIncome;
        try {
            monthlyIncome = Integer.parseInt(incomeStr.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            etIncome.setError("Enter a valid number");
            etIncome.requestFocus();
            return;
        }

        setLoading(true);
        authRepository.register(name, email, password, monthlyIncome, new AuthRepository.RegisterCallback() {
            @Override
            public void onSuccess(UserResponse user) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(RegisterActivity.this, "Account created! Please log in.", Toast.LENGTH_SHORT).show();
                    goToLogin();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void setLoading(boolean loading) {
        btnCreateAccount.setEnabled(!loading);
        btnCreateAccount.setText(loading ? "Creating account…" : getString(R.string.btn_create_account));
    }

    private void goToLogin() {
        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        finish();
    }
}
