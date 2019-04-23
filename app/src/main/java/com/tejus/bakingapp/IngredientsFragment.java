package com.tejus.bakingapp;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tejus.bakingapp.model.Ingredient;
import com.tejus.bakingapp.model.Recipe;

import java.util.List;

public class IngredientsFragment extends Fragment {

    private static final String EXTRA_RECIPE_KEY = "recipe";

    private Recipe mRecipe;

    public IngredientsFragment() {
        // Required empty public constructor
    }

    public static Fragment newInstance(Recipe recipe) {
        Fragment fragment = new IngredientsFragment();
        if (recipe != null) {
            Bundle bundle = new Bundle();
            bundle.putParcelable(EXTRA_RECIPE_KEY, recipe);
            fragment.setArguments(bundle);
        }
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_ingredients, container, false);

        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey(EXTRA_RECIPE_KEY)) {
            mRecipe = bundle.getParcelable(EXTRA_RECIPE_KEY);
        } else {
            throw new ClassCastException(rootView.getContext().toString()
                    + " must pass a Recipe object to fragment!");
        }

        List<Ingredient> ingredients = mRecipe.getIngredients();

        TextView textView = rootView.findViewById(R.id.tv_ingr_temp);

        textView.setText("Ingredients: \n");

        for (Ingredient ingredient : ingredients) {
            textView.append(ingredient.getIngredient() + "\n");
        }

        // Inflate the layout for this fragment
        return rootView;
    }
}
