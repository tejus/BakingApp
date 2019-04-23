package com.tejus.bakingapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.tejus.bakingapp.model.Recipe;
import com.tejus.bakingapp.model.Step;

import java.util.ArrayList;
import java.util.List;

public class DetailPagerAdapter extends FragmentPagerAdapter {

    private Fragment ingredientFragment;
    private List<Fragment> fragmentList;

    DetailPagerAdapter(FragmentManager fm, Recipe recipe) {
        super(fm);
        ingredientFragment = IngredientsFragment.newInstance(recipe);
        fragmentList = new ArrayList<>();
        for (Step step : recipe.getSteps()) {
            Fragment fragment = StepFragment.newInstance(step);
            fragmentList.add(fragment);
        }
    }

    @Override
    public Fragment getItem(int i) {
        if (i == 0) {
            return ingredientFragment;
        }
        return fragmentList.get(i - 1);
    }

    @Override
    public int getCount() {
        return fragmentList.size() + 1;
    }
}
