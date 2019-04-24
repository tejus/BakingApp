package com.tejus.bakingapp;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tejus.bakingapp.model.Recipe;
import com.tejus.bakingapp.model.Step;

import java.util.List;

public class StepsOverviewFragment extends Fragment {

    private static final String EXTRA_RECIPE_KEY = "recipe";

    private Recipe mRecipe;
    private List<Step> mSteps;

    public StepsOverviewFragment() {
        // Required empty public constructor
    }

    public static Fragment newInstance(Recipe recipe) {
        Fragment fragment = new StepsOverviewFragment();
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

        final View rootView = inflater.inflate(R.layout.fragment_steps_overview, container, false);

        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey(EXTRA_RECIPE_KEY)) {
            mRecipe = bundle.getParcelable(EXTRA_RECIPE_KEY);
        } else {
            throw new ClassCastException(rootView.getContext().toString()
                    + " must pass a valid Recipe object to fragment!");
        }
        if (mRecipe != null) {
            mSteps = mRecipe.getSteps();
        } else {
            throw new ClassCastException(rootView.getContext().toString()
                    + " must pass a valid Recipe object to fragment!");
        }

        TextView textView = rootView.findViewById(R.id.tv_steps_temp);

        textView.setText("Steps: \n");

        for (Step step : mSteps) {
            textView.append(step.getId() + 1 + " ");
            textView.append(step.getShortDescription() + "\n");
        }
        // Inflate the layout for this fragment
        return rootView;
    }
}
