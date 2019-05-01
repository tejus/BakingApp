package com.tejus.bakingapp.ui.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.tejus.bakingapp.R;
import com.tejus.bakingapp.model.Recipe;

import java.util.List;

public class MainAdapter extends ArrayAdapter<Recipe> {

    private List<Recipe> mRecipes;

    MainAdapter(Context context, List<Recipe> recipes) {
        super(context, 0, recipes);
        mRecipes = recipes;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        }

        Recipe recipe = mRecipes.get(position);
        ((TextView) convertView.findViewById(android.R.id.text1)).setText(recipe.getName());
        ((TextView) convertView.findViewById(android.R.id.text2)).setText(getContext().getString(R.string.main_serves, recipe.getServings()));
        return convertView;
    }

    public void setRecipes(List<Recipe> recipes) {
        mRecipes = recipes;
    }
}
