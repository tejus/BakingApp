package com.tejus.bakingapp.ui.main;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tejus.bakingapp.R;
import com.tejus.bakingapp.model.Recipe;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainViewHolder> {

    private List<Recipe> mRecipes;
    private OnRecipeClickListener mClickListener;

    MainAdapter(OnRecipeClickListener clickListener) {
        mClickListener = clickListener;
    }

    void setRecipes(List<Recipe> recipes) {
        mRecipes = recipes;
    }

    @NonNull
    @Override
    public MainViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_recipe, viewGroup, false);
        return new MainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MainViewHolder holder, int i) {
        holder.mName.setText(mRecipes.get(i).getName());
        String serves = holder.itemView.getResources()
                .getString(R.string.main_serves, mRecipes.get(i).getServings());
        holder.mServes.setText(serves);
        holder.itemView.setOnClickListener(v -> mClickListener.onRecipeClick(i));
        Picasso.get()
                .load(Uri.parse(mRecipes.get(i).getImage()))
                .placeholder(R.color.colorPrimary)
                .error(R.color.colorGrey)
                .into(holder.mImageView);
    }

    @Override
    public int getItemCount() {
        if (mRecipes == null) return 0;
        return mRecipes.size();
    }

    class MainViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_recipe_image)
        ImageView mImageView;
        @BindView(R.id.item_recipe_name)
        TextView mName;
        @BindView(R.id.item_recipe_serves)
        TextView mServes;

        MainViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public interface OnRecipeClickListener {
        void onRecipeClick(int position);
    }
}
