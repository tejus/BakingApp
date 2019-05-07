package com.tejus.bakingapp.ui.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.button.MaterialButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tejus.bakingapp.R;
import com.tejus.bakingapp.data.Repository;
import com.tejus.bakingapp.model.Recipe;
import com.tejus.bakingapp.ui.detail.DetailActivity;
import com.tejus.bakingapp.ui.detail.StepFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class IntroDialogFragment extends DialogFragment {

    private static final String LOG_TAG = IntroDialogFragment.class.getSimpleName();
    private static final String EXTRA_RECIPE_POSITION_KEY = "position";

    @BindView(R.id.btn_intro_dialog_start)
    MaterialButton mBtnStartCooking;
    private Unbinder mUnbinder;

    private Context mContext;
    int mPosition;

    public static IntroDialogFragment newInstance(int position) {
        IntroDialogFragment dialogFragment = new IntroDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_RECIPE_POSITION_KEY, position);
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
        Recipe recipe = null;
        if (bundle != null && bundle.containsKey(EXTRA_RECIPE_POSITION_KEY)) {
            mPosition = bundle.getInt(EXTRA_RECIPE_POSITION_KEY);
            recipe = Repository.getRecipe(mContext, mPosition);
            if (recipe == null) throwError();
        } else {
            throwError();
        }

        FragmentManager fragmentManager = getChildFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.frame_intro_dialog, StepFragment.newInstance(recipe.getSteps().get(0), 0))
                .commit();

        mBtnStartCooking.setText(getString(R.string.intro_dialog_start, recipe.getName().toLowerCase()));
        mBtnStartCooking.setOnClickListener((v) -> {
            Intent intent = new Intent(mContext, DetailActivity.class);
            Bundle newBundle = new Bundle();
            newBundle.putInt(DetailActivity.EXTRA_RECIPE_POSITION_KEY, mPosition);
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
        Log.d(LOG_TAG, "onDestroy");
    }
}
