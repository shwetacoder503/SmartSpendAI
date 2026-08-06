package com.example.smartspendai.ui.goal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartspendai.R;
import com.example.smartspendai.data.local.pojo.GoalWithProgress;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.GoalViewHolder> {

    public interface OnGoalClickListener {
        void onGoalClick(GoalWithProgress goal);
    }

    private List<GoalWithProgress> goals = new ArrayList<>();
    private final OnGoalClickListener listener;

    public GoalAdapter(OnGoalClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<GoalWithProgress> newGoals) {
        this.goals = newGoals != null ? newGoals : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_goal, parent, false);
        return new GoalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        GoalWithProgress goal = goals.get(position);

        holder.tvTitle.setText(goal.title);
        holder.tvProgress.setText(String.format(Locale.getDefault(), "₹%,.0f of ₹%,.0f saved",
                goal.total_contributed, goal.target_amount));

        int percent = goal.target_amount > 0
                ? (int) Math.min(100, Math.round((goal.total_contributed / goal.target_amount) * 100))
                : 0;

        // Same "measure after layout" trick used for the dashboard budget bar —
        // we need the track's actual pixel width before we can size the fill.
        FrameLayout track = (FrameLayout) holder.progressFill.getParent();
        int finalPercent = percent;
        track.post(() -> {
            int trackWidth = track.getWidth();
            holder.progressFill.getLayoutParams().width = (int) (trackWidth * (finalPercent / 100f));
            holder.progressFill.requestLayout();
        });

        Long estimateMillis = GoalEstimator.estimateCompletionMillis(
                goal.target_amount, goal.total_contributed, goal.created_date_millis);

        if (goal.total_contributed >= goal.target_amount) {
            holder.tvEstimate.setText("🎉 Goal reached!");
        } else if (estimateMillis != null) {
            String dateStr = new SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(estimateMillis);
            holder.tvEstimate.setText("Estimated completion: " + dateStr);
        } else {
            holder.tvEstimate.setText("Add a contribution to estimate completion date");
        }

        holder.itemView.setOnClickListener(v -> listener.onGoalClick(goal));
    }

    @Override
    public int getItemCount() {
        return goals.size();
    }

    static class GoalViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvProgress, tvEstimate;
        View progressFill;

        GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvGoalTitle);
            tvProgress = itemView.findViewById(R.id.tvGoalProgress);
            tvEstimate = itemView.findViewById(R.id.tvGoalEstimate);
            progressFill = itemView.findViewById(R.id.goalProgressFill);
        }
    }
}
