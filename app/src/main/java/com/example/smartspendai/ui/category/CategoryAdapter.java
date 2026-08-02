package com.example.smartspendai.ui.category;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartspendai.R;
import com.example.smartspendai.data.local.entity.CategoryEntity;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    public interface OnDeleteClickListener {
        void onDeleteClick(CategoryEntity category);
    }

    private List<CategoryEntity> categories = new ArrayList<>();
    private final OnDeleteClickListener listener;

    public CategoryAdapter(OnDeleteClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<CategoryEntity> newCategories) {
        this.categories = newCategories != null ? newCategories : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryEntity category = categories.get(position);
        holder.tvName.setText(category.name);
        holder.tvDefaultBadge.setVisibility(category.isDefault ? View.VISIBLE : View.GONE);
        holder.tvDelete.setVisibility(category.isDefault ? View.GONE : View.VISIBLE);
        holder.tvDelete.setOnClickListener(v -> listener.onDeleteClick(category));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDefaultBadge, tvDelete;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCategoryName);
            tvDefaultBadge = itemView.findViewById(R.id.tvDefaultBadge);
            tvDelete = itemView.findViewById(R.id.tvDelete);
        }
    }
}
