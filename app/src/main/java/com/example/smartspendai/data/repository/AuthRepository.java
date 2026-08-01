package com.example.smartspendai.data.repository;

import android.content.Context;

import com.example.smartspendai.data.model.LoginRequest;
import com.example.smartspendai.data.model.RegisterRequest;
import com.example.smartspendai.data.model.TokenResponse;
import com.example.smartspendai.data.model.UserResponse;
import com.example.smartspendai.data.remote.AuthApiService;
import com.example.smartspendai.data.remote.RetrofitClient;

import androidx.annotation.NonNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Sits between the Activities (UI) and Retrofit (network).
 * This is the "Repository" layer in MVVM — Activities never call
 * AuthApiService directly, they go through here.
 */
public class AuthRepository {

    private final AuthApiService api;

    public AuthRepository(Context context) {
        api = RetrofitClient.getInstance(context).create(AuthApiService.class);
    }

    public interface RegisterCallback {
        void onSuccess(UserResponse user);
        void onError(String message);
    }

    public interface LoginCallback {
        void onSuccess(TokenResponse tokenResponse);
        void onError(String message);
    }

    public void register(String name, String email, String password, Integer monthlyIncome, RegisterCallback callback) {
        api.register(new RegisterRequest(name, email, password, monthlyIncome)).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(parseError(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                callback.onError("Couldn't reach the server: " + t.getMessage());
            }
        });
    }

    public void login(String email, String password, LoginCallback callback) {
        api.login(new LoginRequest(email, password)).enqueue(new Callback<TokenResponse>() {
            @Override
            public void onResponse(@NonNull Call<TokenResponse> call, @NonNull Response<TokenResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(parseError(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<TokenResponse> call, @NonNull Throwable t) {
                callback.onError("Couldn't reach the server: " + t.getMessage());
            }
        });
    }

    /** Turns FastAPI's error JSON (e.g. {"detail": "Invalid email or password."}) into plain text. */
    private String parseError(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String raw = response.errorBody().string();
                // Very small manual parse to avoid pulling in a full JSON parser here.
                if (raw.contains("\"detail\"")) {
                    int start = raw.indexOf(":\"") + 2;
                    int end = raw.lastIndexOf("\"");
                    if (start > 1 && end > start) {
                        return raw.substring(start, end);
                    }
                }
                return raw;
            }
        } catch (Exception ignored) {
        }
        return "Something went wrong (code " + response.code() + ")";
    }
}
