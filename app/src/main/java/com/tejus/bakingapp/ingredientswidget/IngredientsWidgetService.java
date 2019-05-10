package com.tejus.bakingapp.ingredientswidget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.tejus.bakingapp.R;
import com.tejus.bakingapp.data.Repository;
import com.tejus.bakingapp.model.Recipe;

import java.util.List;

public class IngredientsWidgetService extends IntentService {

    public static final String ACTION_UPDATE_LATEST_RECIPE = "com.tejus.bakingapp.action.update_latest_recipe";

    public IngredientsWidgetService() {
        super(IngredientsWidgetService.class.getSimpleName());
    }

    public static void startActionUpdateLatestRecipe(Context context) {
        Intent intent = new Intent(context, IngredientsWidgetService.class);
        intent.setAction(ACTION_UPDATE_LATEST_RECIPE);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPDATE_LATEST_RECIPE.equals(action)) {
                updateLatestRecipe();
            }
        }
    }

    /**
     * Method to find the current recipe and step, if any, and update all IngredientsWidget
     * instances with them
     */
    private void updateLatestRecipe() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        //Set default values so we can detect when there is no current recipe and step
        Recipe recipe = null;
        int currentStep = -1;
        int recipeId = preferences.getInt(getString(R.string.pref_recipe_id_key), -1);
        if (recipeId > -1) {
            currentStep = preferences.getInt(getString(R.string.pref_step_key), -1);
            if (currentStep > 0) {
                //Network call to fetch recipes. Since this class extends from IntentService,
                //we do not need to worry about spawning a new thread for this.
                List<Recipe> recipes = Repository.getRecipes();
                recipe = Recipe.getById(recipes, recipeId);
            }
        }
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, IngredientsWidgetProvider.class));
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_ingredients);
        //Call the widget provider class' method to update all widgets
        IngredientsWidgetProvider.updateWidget(this, appWidgetManager, appWidgetIds, recipe, currentStep);
    }
}
