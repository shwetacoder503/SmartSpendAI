package com.example.smartspendai.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.smartspendai.data.local.AppDatabase;
import com.example.smartspendai.data.local.dao.GoalDao;
import com.example.smartspendai.data.local.entity.GoalContributionEntity;
import com.example.smartspendai.data.local.entity.GoalEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GoalRepository {

    private final GoalDao goalDao;
    private final ExecutorService writeExecutor = Executors.newSingleThreadExecutor();

    public GoalRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        goalDao = db.goalDao();
    }

    public interface NewGoalCallback {
        void onCreated(long goalId);
    }

    public LiveData<List<GoalEntity>> getAllGoals() {
        return goalDao.getAllGoals();
    }

    public LiveData<List<com.example.smartspendai.data.local.pojo.GoalWithProgress>> getAllGoalsWithProgress() {
        return goalDao.getAllGoalsWithProgress();
    }

    public LiveData<GoalEntity> getGoalById(long goalId) {
        return goalDao.getGoalById(goalId);
    }

    public LiveData<List<GoalContributionEntity>> getContributionsForGoal(long goalId) {
        return goalDao.getContributionsForGoal(goalId);
    }

    public LiveData<Double> getTotalContributed(long goalId) {
        return goalDao.getTotalContributed(goalId);
    }

    public void createGoal(String title, double targetAmount, Long targetDateMillis, NewGoalCallback callback) {
        writeExecutor.execute(() -> {
            GoalEntity goal = new GoalEntity();
            goal.title = title;
            goal.targetAmount = targetAmount;
            goal.targetDateMillis = targetDateMillis;
            goal.createdDateMillis = System.currentTimeMillis();
            long id = goalDao.insertGoal(goal);
            callback.onCreated(id);
        });
    }

    public void deleteGoal(GoalEntity goal) {
        writeExecutor.execute(() -> goalDao.deleteGoal(goal));
    }

    public void addContribution(long goalId, double amount, String note) {
        writeExecutor.execute(() -> {
            GoalContributionEntity contribution = new GoalContributionEntity();
            contribution.goalId = goalId;
            contribution.amount = amount;
            contribution.dateMillis = System.currentTimeMillis();
            contribution.note = note;
            goalDao.insertContribution(contribution);
        });
    }
}
