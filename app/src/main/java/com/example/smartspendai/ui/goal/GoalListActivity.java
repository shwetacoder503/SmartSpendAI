package com.example.smartspendai.ui.goal;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartspendai.R;
import com.example.smartspendai.data.local.pojo.GoalWithProgress;

import java.util.List;

public class GoalListActivity extends AppCompatActivity {

    private GoalViewModel viewModel;
    private GoalAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_list);

        viewModel = new ViewModelProvider(this).get(GoalViewModel.class);

        RecyclerView recyclerView = findViewById(R.id.recyclerGoals);
        TextView tvEmptyState = findViewById(R.id.tvEmptyState);
        Button fabAddGoal = findViewById(R.id.fabAddGoal);
        TextView tvWhatIfIcon = findViewById(R.id.tvWhatIfIcon);

        adapter = new GoalAdapter(goal ->
                startActivity(new Intent(this, GoalDetailActivity.class)
                        .putExtra(GoalDetailActivity.EXTRA_GOAL_ID, goal.id)));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        viewModel.getAllGoalsWithProgress().observe(this, (List<GoalWithProgress> goals) -> {
            adapter.submitList(goals);
            boolean isEmpty = goals == null || goals.isEmpty();
            tvEmptyState.setVisibility(isEmpty ? android.view.View.VISIBLE : android.view.View.GONE);
            recyclerView.setVisibility(isEmpty ? android.view.View.GONE : android.view.View.VISIBLE);
        });

        fabAddGoal.setOnClickListener(v ->
                startActivity(new Intent(this, AddGoalActivity.class)));

        tvWhatIfIcon.setOnClickListener(v ->
                startActivity(new Intent(this, WhatIfSimulatorActivity.class)));
    }
}
