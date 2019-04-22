package com.tejus.bakingapp;

import android.content.Context;
import android.content.res.AssetManager;

import com.google.gson.Gson;
import com.tejus.bakingapp.model.Recipe;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Repository {

    static ArrayList<Recipe> getRecipes(Context context) {
        AssetManager assetManager = context.getAssets();
        String json;
        Gson gson = new Gson();

        try {
            InputStream inputStream = assetManager.open("baking.json");
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        Recipe[] recipes = gson.fromJson(json, Recipe[].class);
        return new ArrayList<>(Arrays.asList(recipes));
    }

    static Recipe getRecipe(Context context, int position) {
        List<Recipe> recipes = getRecipes(context);
        if (recipes != null && recipes.size() > position) {
            return recipes.get(position);
        }
        return null;
    }
}
