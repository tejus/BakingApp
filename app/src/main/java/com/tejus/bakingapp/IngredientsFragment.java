package com.tejus.bakingapp;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tejus.bakingapp.model.Ingredient;
import com.tejus.bakingapp.model.Recipe;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class IngredientsFragment extends Fragment {

    private static final String EXTRA_RECIPE_KEY = "recipe";

    private Context mContext;

    private Unbinder mUnbinder;
    @BindView(R.id.rv_ingredients)
    RecyclerView mRecyclerView;

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
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_ingredients, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        Bundle bundle = getArguments();
        Recipe recipe;
        List<Ingredient> ingredients;
        if (bundle != null && bundle.containsKey(EXTRA_RECIPE_KEY)) {
            recipe = bundle.getParcelable(EXTRA_RECIPE_KEY);
        } else {
            throw new ClassCastException(mContext.toString()
                    + " must pass a valid Recipe object to fragment!");
        }
        if (recipe != null) {
            ingredients = recipe.getIngredients();
        } else {
            throw new ClassCastException(mContext.toString()
                    + " must pass a valid Recipe object to fragment!");
        }

        IngredientsAdapter adapter = new IngredientsAdapter();
        adapter.setIngredients(ingredients);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.setAdapter(adapter);
        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }
}
