package com.tejus.bakingapp.ui.detail;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tejus.bakingapp.R;
import com.tejus.bakingapp.model.Ingredient;
import com.tejus.bakingapp.ui.WrapWidthTextView;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class IngredientsAdapter extends RecyclerView.Adapter<IngredientsAdapter.IngredientsViewHolder> {

    private List<Ingredient> mIngredients;
    private int mMaxWidth;

    IngredientsAdapter(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        mMaxWidth = metrics.widthPixels * 2 / 3;
    }

    @NonNull
    @Override
    public IngredientsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_ingredient, viewGroup, false);
        IngredientsViewHolder holder = new IngredientsViewHolder(view);
        holder.mItemName.setMaxWidth(mMaxWidth);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientsViewHolder holder, int i) {
        holder.mItemName.setText(mIngredients.get(i).getIngredient());
        String quantity = String.format(Locale.getDefault(), "%1$,.0f", mIngredients.get(i).getQuantity());
        quantity = quantity.concat(" " + mIngredients.get(i).getMeasure().toLowerCase());
        holder.mItemQuantity.setText(quantity);
    }

    @Override
    public int getItemCount() {
        if (mIngredients == null) return 0;
        return mIngredients.size();
    }

    void setIngredients(List<Ingredient> ingredients) {
        this.mIngredients = ingredients;
        notifyDataSetChanged();
    }

    class IngredientsViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_ingredient_name)
        WrapWidthTextView mItemName;
        @BindView(R.id.item_ingredient_quantity)
        TextView mItemQuantity;

        IngredientsViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
