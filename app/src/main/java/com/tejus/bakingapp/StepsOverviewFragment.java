package com.tejus.bakingapp;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tejus.bakingapp.model.Recipe;

public class StepsOverviewFragment extends Fragment {

    public StepsOverviewFragment() {
        // Required empty public constructor
    }

    public static Fragment newInstance(Recipe recipe) {
        Fragment fragment = new StepsOverviewFragment();
        if (recipe != null) {
            Bundle bundle = new Bundle();
            bundle.putParcelable("recipe", recipe);
            fragment.setArguments(bundle);
        }
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_steps_overview, container, false);
    }
}
