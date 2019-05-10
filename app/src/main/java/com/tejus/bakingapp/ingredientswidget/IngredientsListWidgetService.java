package com.tejus.bakingapp.ingredientswidget;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.tejus.bakingapp.R;
import com.tejus.bakingapp.data.Repository;
import com.tejus.bakingapp.model.Ingredient;
import com.tejus.bakingapp.model.Recipe;

import java.util.List;

public class IngredientsListWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new IngredientsListRemoteViewsFactory(this.getApplicationContext());
    }
}

class IngredientsListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context mContext;
    private List<Ingredient> mIngredients;

    IngredientsListRemoteViewsFactory(Context context) {
        mContext = context;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        mIngredients = null;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        int recipeId = preferences.getInt(mContext.getString(R.string.pref_recipe_id_key), -1);
        if (recipeId > -1) {
            List<Recipe> recipes = Repository.getRecipes();
            Recipe recipe = Recipe.getById(recipes, recipeId);
            if (recipe != null) {
                mIngredients = recipe.getIngredients();
            }
        }
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        if (mIngredients != null) {
            return mIngredients.size();
        }
        return 0;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        Ingredient ingredient = mIngredients.get(position);
        RemoteViews view = new RemoteViews(mContext.getPackageName(), R.layout.item_ingredient_widget);
        view.setTextViewText(R.id.item_ingredient_widget_name, ingredient.getIngredient());
        String quantity = mContext.getString(R.string.detail_ingredient_quantity,
                ingredient.getQuantity(),
                ingredient.getMeasure().toLowerCase());
        view.setTextViewText(R.id.item_ingredient_widget_quantity, quantity);
        return view;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
