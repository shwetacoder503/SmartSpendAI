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
import com.example.smartspendai.data.local.TokenManager;
import com.example.smartspendai.data.model.TokenResponse;
import com.example.smartspendai.data.repository.AuthRepository;
import com.example.smartspendai.ui.dashboard.DashboardActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;

    private AuthRepository authRepository;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tokenManager = new TokenManager(this);
        authRepository = new AuthRepository(this);
        // Already logged in from a previous session? Skip straight to the dashboard.
        if (tokenManager.isLoggedIn()) {
            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
            finish();
            return;
        }

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        TextView tvCreateAccount = findViewById(R.id.tvCreateAccount);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);

        btnLogin.setOnClickListener(v -> attemptLogin());

        tvCreateAccount.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        tvForgotPassword.setOnClickListener(v ->
                Toast.makeText(this, "Forgot-password flow: Milestone 1 stretch goal", Toast.LENGTH_SHORT).show());
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

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

        setLoading(true);
        authRepository.login(email, password, new AuthRepository.LoginCallback() {
            @Override
            public void onSuccess(TokenResponse tokenResponse) {
                runOnUiThread(() -> {
                    setLoading(false);
                    tokenManager.saveSession(
                            tokenResponse.getAccessToken(),
                            tokenResponse.getUser().getName(),
                            tokenResponse.getUser().getMonthlyIncome()
                    );
                    startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                    finish();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void setLoading(boolean loading) {
        btnLogin.setEnabled(!loading);
        btnLogin.setText(loading ? "Logging in…" : getString(R.string.btn_login));
    }
}
