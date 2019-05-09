package com.tejus.bakingapp.ui.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.button.MaterialButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tejus.bakingapp.R;
import com.tejus.bakingapp.model.Recipe;
import com.tejus.bakingapp.ui.detail.DetailActivity;
import com.tejus.bakingapp.ui.detail.StepFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class IntroDialogFragment extends DialogFragment {

    private static final String LOG_TAG = IntroDialogFragment.class.getSimpleName();
    private static final String EXTRA_RECIPE_KEY = "recipe";

    @BindView(R.id.btn_intro_dialog_start)
    MaterialButton mBtnStartCooking;
    private Unbinder mUnbinder;

    private Context mContext;
    Recipe mRecipe;

    public static IntroDialogFragment newInstance(Recipe recipe) {
        IntroDialogFragment dialogFragment = new IntroDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_RECIPE_KEY, recipe);
        dialogFragment.setArguments(bundle);
        return dialogFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_intro_dialog, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey(EXTRA_RECIPE_KEY)) {
            mRecipe = bundle.getParcelable(EXTRA_RECIPE_KEY);
            if (mRecipe == null) throwError();
        } else {
            throwError();
        }

        if (savedInstanceState == null) {
            FragmentManager fragmentManager = getChildFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.frame_intro_dialog, StepFragment.newInstance(mRecipe.getSteps().get(0), 0))
                    .commit();
        }

        mBtnStartCooking.setText(getString(R.string.intro_dialog_start, mRecipe.getName().toLowerCase()));
        mBtnStartCooking.setOnClickListener((v) -> {
            Intent intent = new Intent(mContext, DetailActivity.class);
            Bundle newBundle = new Bundle();
            newBundle.putParcelable(DetailActivity.EXTRA_RECIPE_KEY, mRecipe);
            intent.putExtras(newBundle);
            startActivity(intent);
            dismiss();
        });

        return rootView;
    }

    private void throwError() {
        throw new ClassCastException(mContext.toString()
                + " must pass a Step object to fragment!");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }
}
