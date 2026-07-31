package com.example.smartspendai.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.smartspendai.R;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etIncome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etIncome = findViewById(R.id.etIncome);
        Button btnCreateAccount = findViewById(R.id.btnCreateAccount);
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
        String income = etIncome.getText().toString().trim();

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
        if (TextUtils.isEmpty(income)) {
            etIncome.setError(getString(R.string.error_required_field));
            etIncome.requestFocus();
            return;
        }

        // TODO (Milestone 1): POST /auth/register with { name, email, password }.
        // Monthly income gets saved separately once the `users` table / budget
        // recommendation API exists (Milestone 7).

        goToLogin();
    }

    private void goToLogin() {
        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        finish();
    }
}
