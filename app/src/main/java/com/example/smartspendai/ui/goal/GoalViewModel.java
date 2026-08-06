package com.example.smartspendai.ui.goal;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.smartspendai.data.local.entity.GoalContributionEntity;
import com.example.smartspendai.data.local.entity.GoalEntity;
import com.example.smartspendai.data.repository.GoalRepository;

import java.util.List;

public class GoalViewModel extends AndroidViewModel {

    private final GoalRepository repository;
    private final LiveData<List<GoalEntity>> allGoals;

    public GoalViewModel(@NonNull Application application) {
        super(application);
        repository = new GoalRepository(application);
        allGoals = repository.getAllGoals();
    }

    public LiveData<List<GoalEntity>> getAllGoals() {
        return allGoals;
    }

    public LiveData<List<com.example.smartspendai.data.local.pojo.GoalWithProgress>> getAllGoalsWithProgress() {
        return repository.getAllGoalsWithProgress();
    }

    public LiveData<GoalEntity> getGoalById(long goalId) {
        return repository.getGoalById(goalId);
    }

    public LiveData<List<GoalContributionEntity>> getContributionsForGoal(long goalId) {
        return repository.getContributionsForGoal(goalId);
    }

    public LiveData<Double> getTotalContributed(long goalId) {
        return repository.getTotalContributed(goalId);
    }

    public void createGoal(String title, double targetAmount, Long targetDateMillis, GoalRepository.NewGoalCallback callback) {
        repository.createGoal(title, targetAmount, targetDateMillis, callback);
    }

    public void deleteGoal(GoalEntity goal) {
        repository.deleteGoal(goal);
    }

    public void addContribution(long goalId, double amount, String note) {
        repository.addContribution(goalId, amount, note);
    }
}
