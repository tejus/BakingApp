package com.tejus.bakingapp.ui.detail;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tejus.bakingapp.R;
import com.tejus.bakingapp.model.Ingredient;

import java.util.List;
import java.util.Locale;

public class IngredientsAdapter extends RecyclerView.Adapter<IngredientsAdapter.IngredientsViewHolder> {

    private List<Ingredient> mIngredients;

    @NonNull
    @Override
    public IngredientsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_ingredient, viewGroup, false);
        IngredientsViewHolder holder = new IngredientsViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientsViewHolder holder, int i) {
        holder.mItemName.setText(mIngredients.get(i).getIngredient());
        String quantity = String.format(Locale.getDefault(), "%1$,.0f", mIngredients.get(i).getQuantity());
        holder.mItemQuantity.setText(quantity);
        holder.mItemUnits.setText(mIngredients.get(i).getMeasure());
    }

    @Override
    public int getItemCount() {
        if (mIngredients == null) return 0;
        return mIngredients.size();
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.mIngredients = ingredients;
        notifyDataSetChanged();
    }

    class IngredientsViewHolder extends RecyclerView.ViewHolder {
        TextView mItemName;
        TextView mItemQuantity;
        TextView mItemUnits;

        public IngredientsViewHolder(@NonNull View itemView) {
            super(itemView);
            mItemName = itemView.findViewById(R.id.item_name);
            mItemQuantity = itemView.findViewById(R.id.item_quantity);
            mItemUnits = itemView.findViewById(R.id.item_units);
        }
    }
}
