package com.example.smartspendai.data.repository;

import android.content.Context;

import com.example.smartspendai.data.model.BudgetResponse;
import com.example.smartspendai.data.model.ForecastResponse;
import com.example.smartspendai.data.model.HealthScoreResponse;
import com.example.smartspendai.data.model.SavingSuggestion;
import com.example.smartspendai.data.remote.MlApiService;
import com.example.smartspendai.data.remote.RetrofitClient;

import java.util.List;

import androidx.annotation.NonNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InsightsRepository {

    public interface ResultCallback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    private final MlApiService api;

    public InsightsRepository(Context context) {
        api = RetrofitClient.getInstance(context).create(MlApiService.class);
    }

    public void getForecast(ResultCallback<ForecastResponse> callback) {
        api.getForecast().enqueue(new Callback<ForecastResponse>() {
            @Override
            public void onResponse(@NonNull Call<ForecastResponse> call, @NonNull Response<ForecastResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Couldn't load forecast (code " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ForecastResponse> call, @NonNull Throwable t) {
                callback.onError("Couldn't reach the server for forecast: " + t.getMessage());
            }
        });
    }

    public void getBudget(ResultCallback<BudgetResponse> callback) {
        api.getBudget().enqueue(new Callback<BudgetResponse>() {
            @Override
            public void onResponse(@NonNull Call<BudgetResponse> call, @NonNull Response<BudgetResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Couldn't load budget (code " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(@NonNull Call<BudgetResponse> call, @NonNull Throwable t) {
                callback.onError("Couldn't reach the server for budget: " + t.getMessage());
            }
        });
    }

    public void getSuggestions(ResultCallback<List<SavingSuggestion>> callback) {
        api.getSuggestions().enqueue(new Callback<List<SavingSuggestion>>() {
            @Override
            public void onResponse(@NonNull Call<List<SavingSuggestion>> call, @NonNull Response<List<SavingSuggestion>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Couldn't load suggestions (code " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<SavingSuggestion>> call, @NonNull Throwable t) {
                callback.onError("Couldn't reach the server for suggestions: " + t.getMessage());
            }
        });
    }

    public void getHealthScore(ResultCallback<HealthScoreResponse> callback) {
        api.getHealthScore().enqueue(new Callback<HealthScoreResponse>() {
            @Override
            public void onResponse(@NonNull Call<HealthScoreResponse> call, @NonNull Response<HealthScoreResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Couldn't load health score (code " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(@NonNull Call<HealthScoreResponse> call, @NonNull Throwable t) {
                callback.onError("Couldn't reach the server for health score: " + t.getMessage());
            }
        });
    }
}
