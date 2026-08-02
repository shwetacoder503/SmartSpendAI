package com.example.smartspendai.ui.insights;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.smartspendai.data.model.BudgetResponse;
import com.example.smartspendai.data.model.ForecastResponse;
import com.example.smartspendai.data.model.HealthScoreResponse;
import com.example.smartspendai.data.model.SavingSuggestion;
import com.example.smartspendai.data.repository.InsightsRepository;

import java.util.List;

public class InsightsViewModel extends AndroidViewModel {

    private final InsightsRepository repository;

    public final MutableLiveData<ForecastResponse> forecast = new MutableLiveData<>();
    public final MutableLiveData<BudgetResponse> budget = new MutableLiveData<>();
    public final MutableLiveData<List<SavingSuggestion>> suggestions = new MutableLiveData<>();
    public final MutableLiveData<HealthScoreResponse> healthScore = new MutableLiveData<>();

    /** Simple flags so the UI can show/hide a loading spinner and an error banner per-section. */
    public final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public InsightsViewModel(@NonNull Application application) {
        super(application);
        repository = new InsightsRepository(application);
    }

    /** Kicks off all 4 network calls at once — they're independent, so no need to wait on each other. */
    public void loadAll() {
        repository.getForecast(new InsightsRepository.ResultCallback<ForecastResponse>() {
            @Override public void onSuccess(ForecastResponse result) { forecast.postValue(result); }
            @Override public void onError(String message) { errorMessage.postValue(message); }
        });

        repository.getBudget(new InsightsRepository.ResultCallback<BudgetResponse>() {
            @Override public void onSuccess(BudgetResponse result) { budget.postValue(result); }
            @Override public void onError(String message) { errorMessage.postValue(message); }
        });

        repository.getSuggestions(new InsightsRepository.ResultCallback<List<SavingSuggestion>>() {
            @Override public void onSuccess(List<SavingSuggestion> result) { suggestions.postValue(result); }
            @Override public void onError(String message) { errorMessage.postValue(message); }
        });

        repository.getHealthScore(new InsightsRepository.ResultCallback<HealthScoreResponse>() {
            @Override public void onSuccess(HealthScoreResponse result) { healthScore.postValue(result); }
            @Override public void onError(String message) { errorMessage.postValue(message); }
        });
    }
}
