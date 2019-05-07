package com.tejus.bakingapp.ui.main;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;

import com.tejus.bakingapp.R;
import com.tejus.bakingapp.data.Repository;
import com.tejus.bakingapp.model.Recipe;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements MainAdapter.OnRecipeClickListener {

    @BindView(R.id.main_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.rv_main)
    RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        List<Recipe> recipes = Repository.getRecipes(this);
        MainAdapter adapter = new MainAdapter(this);
        adapter.setRecipes(recipes);
        int columnCount = columnCount();
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, columnCount));
        mRecyclerView.addItemDecoration(new GridLayoutItemDecoration(gridSpacing(), columnCount));
        mRecyclerView.setAdapter(adapter);
    }

    private int gridSpacing() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int spacing = (int) (getResources()
                .getDimension(R.dimen.main_vertical_spacing) / displayMetrics.density);
        Log.d("MainActivity", "Grid Spacing is: " + spacing);
        return spacing;
    }

    private int columnCount() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int scalingFactor = (int) (getResources()
                .getDimension(R.dimen.main_minimum_width) / displayMetrics.density);
        Log.d("MainActivity", "Minimum width is: " + scalingFactor + " for display of width " + dpWidth);
        int columnCount = (int) (dpWidth / scalingFactor);
        if (columnCount < 1) {
            columnCount = 1;
        }
        return columnCount;
    }

    @Override
    public void onRecipeClick(int position) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            transaction.remove(prev);
        }
        transaction.addToBackStack(null);

        DialogFragment newDialog = IntroDialogFragment.newInstance(position);
        newDialog.show(transaction, "dialog");
    }
}
