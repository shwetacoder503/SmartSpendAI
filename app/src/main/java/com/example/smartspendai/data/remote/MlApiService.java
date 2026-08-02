package com.example.smartspendai.data.remote;

import com.example.smartspendai.data.model.BudgetResponse;
import com.example.smartspendai.data.model.ForecastResponse;
import com.example.smartspendai.data.model.HealthScoreResponse;
import com.example.smartspendai.data.model.SavingSuggestion;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface MlApiService {

    @GET("ml/forecast")
    Call<ForecastResponse> getForecast();

    @GET("ml/budget")
    Call<BudgetResponse> getBudget();

    @GET("ml/suggestions")
    Call<List<SavingSuggestion>> getSuggestions();

    @GET("ml/health-score")
    Call<HealthScoreResponse> getHealthScore();
}
