package com.tejus.bakingapp;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.tejus.bakingapp.model.Recipe;

public class DetailActivity extends AppCompatActivity {

    public static final String EXTRA_POSITION_KEY = "position";

    private DetailPagerAdapter mPagerAdapter;
    private Recipe mRecipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ViewPager viewPager = findViewById(R.id.viewpager_detail);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey(EXTRA_POSITION_KEY)) {
            int position = bundle.getInt(EXTRA_POSITION_KEY);
            mRecipe = Repository.getRecipe(this, position);
        } else {
            Toast.makeText(this, "Invalid recipe!", Toast.LENGTH_SHORT).show();
            finish();
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(mRecipe.getName());
        }

        mPagerAdapter = new DetailPagerAdapter(getSupportFragmentManager(), mRecipe);
        viewPager.setAdapter(mPagerAdapter);
    }
}
