package com.example.smartspendai.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.smartspendai.data.local.entity.GoalContributionEntity;
import com.example.smartspendai.data.local.entity.GoalEntity;

import java.util.List;

@Dao
public interface GoalDao {

    @Insert
    long insertGoal(GoalEntity goal);

    @Update
    void updateGoal(GoalEntity goal);

    @Delete
    void deleteGoal(GoalEntity goal);

    @Query("SELECT * FROM goals ORDER BY created_date_millis DESC")
    LiveData<List<GoalEntity>> getAllGoals();

    @Query("SELECT g.*, COALESCE(SUM(c.amount), 0) as total_contributed " +
            "FROM goals g LEFT JOIN goal_contributions c ON c.goal_id = g.id " +
            "GROUP BY g.id ORDER BY g.created_date_millis DESC")
    LiveData<List<com.example.smartspendai.data.local.pojo.GoalWithProgress>> getAllGoalsWithProgress();

    @Query("SELECT * FROM goals WHERE id = :goalId LIMIT 1")
    LiveData<GoalEntity> getGoalById(long goalId);

    @Insert
    void insertContribution(GoalContributionEntity contribution);

    @Query("SELECT * FROM goal_contributions WHERE goal_id = :goalId ORDER BY date_millis DESC")
    LiveData<List<GoalContributionEntity>> getContributionsForGoal(long goalId);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM goal_contributions WHERE goal_id = :goalId")
    LiveData<Double> getTotalContributed(long goalId);
}
