package com.tejus.bakingapp;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.tejus.bakingapp.data.Repository;
import com.tejus.bakingapp.model.Recipe;
import com.tejus.bakingapp.ui.detail.DetailActivity;
import com.tejus.bakingapp.ui.main.MainActivity;
import com.tejus.bakingapp.utilities.AppExecutors;

import java.util.List;

/**
 * Implementation of App Widget functionality.
 */
public class IngredientsWidgetProvider extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.ingredients_widget);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int recipeId = preferences.getInt(context.getString(R.string.pref_recipe_id_key), -1);
        if (recipeId > -1) {
            int currentStep = preferences.getInt(context.getString(R.string.pref_step_key), -1);
            if (currentStep > 0) {
                AppExecutors.getInstance().networkIO().execute(() -> {
                    List<Recipe> recipes = Repository.getRecipes();
                    if (recipes != null) {
                        AppExecutors.getInstance().mainThread().execute(() -> {
                            Intent intent = new Intent(context, DetailActivity.class);

                            Bundle bundle = new Bundle();
                            bundle.putParcelable(DetailActivity.EXTRA_RECIPE_KEY, Recipe.getById(recipes, recipeId));
                            bundle.putInt(DetailActivity.EXTRA_CURRENT_STEP_KEY, currentStep);
                            intent.putExtras(bundle);

                            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                            stackBuilder.addNextIntentWithParentStack(intent);
                            PendingIntent pendingIntent = stackBuilder.getPendingIntent(578, PendingIntent.FLAG_UPDATE_CURRENT);
                            //PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
                            views.setOnClickPendingIntent(R.id.widget_ingredients, pendingIntent);
                            appWidgetManager.updateAppWidget(appWidgetId, views);
                        });
                    }
                });
            }
        } else {
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            views.setOnClickPendingIntent(R.id.widget_ingredients, pendingIntent);
        }

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}
