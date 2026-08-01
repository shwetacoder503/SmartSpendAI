package com.example.smartspendai.ui.expense;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartspendai.R;
import com.example.smartspendai.data.local.entity.ExpenseEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    public interface OnExpenseClickListener {
        void onExpenseClick(ExpenseEntity expense);
        void onExpenseLongClick(ExpenseEntity expense);
    }

    private List<ExpenseEntity> expenses = new ArrayList<>();
    private final OnExpenseClickListener listener;

    private static final Map<String, String> CATEGORY_EMOJI = new HashMap<>();
    static {
        CATEGORY_EMOJI.put("Food", "🍔");
        CATEGORY_EMOJI.put("Shopping", "🛍️");
        CATEGORY_EMOJI.put("Travel", "🚕");
        CATEGORY_EMOJI.put("Medical", "💊");
        CATEGORY_EMOJI.put("Bills", "💡");
        CATEGORY_EMOJI.put("Education", "📚");
        CATEGORY_EMOJI.put("Entertainment", "🎬");
        CATEGORY_EMOJI.put("Investment", "📈");
        CATEGORY_EMOJI.put("Others", "📦");
    }

    public ExpenseAdapter(OnExpenseClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<ExpenseEntity> newExpenses) {
        this.expenses = newExpenses != null ? newExpenses : new ArrayList<>();
        notifyDataSetChanged();
        // Note: notifyDataSetChanged() re-binds every visible row, which is
        // fine for a list this size. If the list grows into the thousands,
        // switch this class to ListAdapter + DiffUtil instead.
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        ExpenseEntity expense = expenses.get(position);

        holder.tvTitle.setText(expense.title);
        holder.tvAmount.setText(String.format(Locale.getDefault(), "− ₹%.0f", expense.amount));

        String emoji = CATEGORY_EMOJI.getOrDefault(expense.category, "📦");
        holder.tvCategoryEmoji.setText(emoji);

        CharSequence relativeDate = DateUtils.getRelativeTimeSpanString(
                expense.dateMillis, System.currentTimeMillis(), DateUtils.DAY_IN_MILLIS);
        holder.tvCategoryDate.setText(expense.category + " · " + relativeDate);

        holder.itemView.setOnClickListener(v -> listener.onExpenseClick(expense));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onExpenseLongClick(expense);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        FrameLayout iconWrap;
        TextView tvCategoryEmoji, tvTitle, tvCategoryDate, tvAmount;

        ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            iconWrap = itemView.findViewById(R.id.iconWrap);
            tvCategoryEmoji = itemView.findViewById(R.id.tvCategoryEmoji);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvCategoryDate = itemView.findViewById(R.id.tvCategoryDate);
            tvAmount = itemView.findViewById(R.id.tvAmount);
        }
    }
}
