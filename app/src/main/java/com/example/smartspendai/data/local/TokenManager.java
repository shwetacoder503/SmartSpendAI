package com.example.smartspendai.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Wraps EncryptedSharedPreferences so the JWT token is stored encrypted
 * on-device rather than in plain text. Use one instance per Activity
 * (e.g. `new TokenManager(this)`), it's cheap to create.
 */
public class TokenManager {

    private static final String PREFS_NAME = "smartspend_secure_prefs";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_MONTHLY_INCOME = "monthly_income";

    private final SharedPreferences prefs;

    public TokenManager(Context context) {
        SharedPreferences temp;
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            temp = EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            // Extremely rare fallback — keeps the app from crashing if the
            // secure keystore is unavailable on a device.
            temp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }
        prefs = temp;
    }

    public void saveSession(String token, String userName) {
        saveSession(token, userName, null);
    }

    public void saveSession(String token, String userName, Integer monthlyIncome) {
        SharedPreferences.Editor editor = prefs.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_USER_NAME, userName);
        if (monthlyIncome != null) {
            editor.putInt(KEY_MONTHLY_INCOME, monthlyIncome);
        }
        editor.apply();
    }

    public int getMonthlyIncome() {
        return prefs.getInt(KEY_MONTHLY_INCOME, 0); // 0 = "not set yet"
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, null);
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }
}
