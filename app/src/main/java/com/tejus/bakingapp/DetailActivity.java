package com.tejus.bakingapp;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.tejus.bakingapp.model.Recipe;

public class DetailActivity extends AppCompatActivity {

    private DetailPagerAdapter mPagerAdapter;
    private Recipe mRecipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ViewPager viewPager = findViewById(R.id.viewpager_detail);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey("position")) {
            int position = bundle.getInt("position");
            mRecipe = Repository.getRecipe(this, position);
        } else {
            Toast.makeText(this, "Invalid recipe!", Toast.LENGTH_SHORT).show();
            finish();
        }

        mPagerAdapter = new DetailPagerAdapter(getSupportFragmentManager(), mRecipe);
        viewPager.setAdapter(mPagerAdapter);
    }
}
