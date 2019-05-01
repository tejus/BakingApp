package com.tejus.bakingapp.ui.detail;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tejus.bakingapp.R;
import com.tejus.bakingapp.model.Step;

public class StepFragment extends Fragment {

    private static final String EXTRA_STEP_KEY = "step";

    public StepFragment() {
        // Required empty public constructor
    }

    public static Fragment newInstance(Step step) {
        Fragment fragment = new StepFragment();
        if (step != null) {
            Bundle bundle = new Bundle();
            bundle.putParcelable(EXTRA_STEP_KEY, step);
            fragment.setArguments(bundle);
        }
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_step, container, false);

        Bundle bundle = getArguments();
        Step step;
        if (bundle != null && bundle.containsKey(EXTRA_STEP_KEY)) {
            step = bundle.getParcelable(EXTRA_STEP_KEY);
        } else {
            throw new ClassCastException(rootView.getContext().toString()
                    + " must pass a Step object to fragment!");
        }

        ((TextView) rootView.findViewById(R.id.tv_step_heading))
                .setText(step.getShortDescription());

        ((TextView) rootView.findViewById(R.id.tv_step_tempurl))
                .setText(step.getVideoURL());

        ((TextView) rootView.findViewById(R.id.tv_step_desc))
                .setText(step.getDescription());
        // Inflate the layout for this fragment
        return rootView;
    }
}
