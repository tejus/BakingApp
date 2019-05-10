package com.tejus.bakingapp.ingredientswidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.tejus.bakingapp.R;
import com.tejus.bakingapp.model.Recipe;
import com.tejus.bakingapp.ui.detail.DetailActivity;
import com.tejus.bakingapp.ui.main.MainActivity;

public class IngredientsWidgetProvider extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, Recipe recipe, int currentStep) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.ingredients_widget);

        //Setup an intent to launch MainActivity
        Intent launchMainActivityIntent;
        launchMainActivityIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntentMainActivity = PendingIntent.getActivity(context, 1256,
                launchMainActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (recipe != null && currentStep > 0) {
            //Setup an intent to launch DetailActivity at the current recipe and step
            Intent launchDetailActivityIntent;
            launchDetailActivityIntent = new Intent(context, DetailActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable(DetailActivity.EXTRA_RECIPE_KEY, recipe);
            bundle.putInt(DetailActivity.EXTRA_CURRENT_STEP_KEY, currentStep);
            launchDetailActivityIntent.putExtras(bundle);

            //Add DetailActivity's parent into the TaskStack so that MainActivity launches on exiting
            //the DetailActivity launched with the intent above
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntentWithParentStack(launchDetailActivityIntent);
            PendingIntent pendingIntent = stackBuilder.getPendingIntent(578,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            //Set the widget top bar to launch DetailActivity at the current recipe and step
            views.setTextViewText(R.id.tv_widget_heading, recipe.getName());
            views.setOnClickPendingIntent(R.id.tv_widget_heading, pendingIntent);
        } else {
            //Set the widget top bar to launch MainActivity when there is no current recipe and step
            views.setTextViewText(R.id.tv_widget_heading, context.getString(R.string.app_name));
            views.setOnClickPendingIntent(R.id.tv_widget_heading, pendingIntentMainActivity);
        }

        //Setup the ingredients ListView
        Intent listIntent = new Intent(context, IngredientsListWidgetService.class);
        views.setRemoteAdapter(R.id.list_widget_ingredients, listIntent);

        //Setup an empty view for the ListView to launch MainActivity
        views.setEmptyView(R.id.list_widget_ingredients, R.id.empty_view);
        views.setOnClickPendingIntent(R.id.empty_view, pendingIntentMainActivity);

        appWidgetManager.updateAppWidget(appWidgetId, views);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.list_widget_ingredients);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        //Start the widget update service on the widget's auto-update period
        IngredientsWidgetService.startActionUpdateLatestRecipe(context);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    /**
     * Helper method to update each instance of IngredientsWidget from IngredientsWidgetService
     */
    public static void updateWidget(Context context, AppWidgetManager appWidgetManager,
                                    int[] appWidgetIds, Recipe recipe, int currentStep) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, recipe, currentStep);
        }
    }
}
