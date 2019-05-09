package com.tejus.bakingapp.ui.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tejus.bakingapp.R;
import com.tejus.bakingapp.data.Repository;
import com.tejus.bakingapp.model.Recipe;
import com.tejus.bakingapp.ui.detail.DetailActivity;
import com.tejus.bakingapp.utilities.AppExecutors;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements MainAdapter.OnRecipeClickListener {

    @BindView(R.id.bar_continue)
    LinearLayout mBarContinue;
    @BindView(R.id.tv_continue)
    TextView mTvContinue;
    @BindView(R.id.main_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.rv_main)
    RecyclerView mRecyclerView;

    private List<Recipe> mRecipes;
    private MainAdapter mAdapter;
    private boolean mRecipesLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        mAdapter = new MainAdapter(this);
        int columnCount = columnCount();
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, columnCount));
        mRecyclerView.addItemDecoration(new GridLayoutItemDecoration(gridSpacing(), columnCount));
        mRecyclerView.setAdapter(mAdapter);
        mRecipesLoaded = false;
    }

    private void loadRecipes() {
        if (mRecipesLoaded) {
            checkPreviousProgress();
            return;
        }

        AppExecutors.getInstance().networkIO().execute(() -> {
            mRecipes = Repository.getRecipes();
            AppExecutors.getInstance().mainThread().execute(() -> {
                if (mRecipes != null) {
                    mAdapter.setRecipes(mRecipes);
                    mRecipesLoaded = true;
                    checkPreviousProgress();
                }
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecipes();
    }

    private int gridSpacing() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int spacing = (int) (getResources()
                .getDimension(R.dimen.main_vertical_spacing) / displayMetrics.density);
        return spacing;
    }

    private int columnCount() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int scalingFactor = (int) (getResources()
                .getDimension(R.dimen.main_minimum_width) / displayMetrics.density);
        int columnCount = (int) (dpWidth / scalingFactor);
        if (columnCount < 1) {
            columnCount = 1;
        }
        return columnCount;
    }

    private void checkPreviousProgress() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int recipeId = preferences.getInt(getString(R.string.pref_recipe_id_key), -1);
        if (recipeId > -1) {
            int currentStep = preferences.getInt(getString(R.string.pref_step_key), -1);
            if (currentStep > 0) {
                Recipe recipe = Recipe.getById(mRecipes, recipeId);
                final Intent intent = new Intent(this, DetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable(DetailActivity.EXTRA_RECIPE_KEY, recipe);
                bundle.putInt(DetailActivity.EXTRA_CURRENT_STEP_KEY, currentStep);
                intent.putExtras(bundle);
                String name = recipe.getName();
                mBarContinue.setVisibility(View.VISIBLE);
                mTvContinue.setText(getString(R.string.main_continue, name, currentStep));
                mBarContinue.setOnClickListener((v) -> startActivity(intent));
                return;
            }
        } else {
            mBarContinue.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRecipeClick(int position) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            transaction.remove(prev);
        }
        transaction.addToBackStack(null);

        DialogFragment newDialog = IntroDialogFragment.newInstance(mRecipes.get(position));
        newDialog.show(transaction, "dialog");
    }
}
