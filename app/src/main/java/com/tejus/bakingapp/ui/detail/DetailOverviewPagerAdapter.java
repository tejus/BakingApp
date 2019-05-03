package com.tejus.bakingapp.ui.detail;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.tejus.bakingapp.model.Recipe;

public class DetailOverviewPagerAdapter extends FragmentPagerAdapter {

    private Fragment mIngredientsFragment;
    private Fragment mStepsFragment;

    DetailOverviewPagerAdapter(FragmentManager fm, Recipe recipe) {
        super(fm);
        mIngredientsFragment = IngredientsFragment.newInstance(recipe);
        mStepsFragment = StepsOverviewFragment.newInstance(recipe);
    }

    @Override
    public Fragment getItem(int i) {
        return i == 0 ?
                mIngredientsFragment : mStepsFragment;
    }

    @Override
    public int getCount() {
        return 2;
    }
}
