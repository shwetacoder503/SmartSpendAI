package com.example.smartspendai.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.smartspendai.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Button btnGetStarted = findViewById(R.id.btnGetStarted);
        btnGetStarted.setOnClickListener(v -> {
            // TODO (Milestone 1): check if a valid JWT/session already exists
            // in EncryptedSharedPreferences — if so, skip straight to Dashboard.
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        });
    }
}
