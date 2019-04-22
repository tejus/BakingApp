package com.tejus.bakingapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.tejus.bakingapp.model.Recipe;
import com.tejus.bakingapp.model.Step;

import java.util.ArrayList;
import java.util.List;

public class DetailPagerAdapter extends FragmentPagerAdapter {

    private List<Fragment> fragmentList;

    DetailPagerAdapter(FragmentManager fm, Recipe recipe) {
        super(fm);
        fragmentList = new ArrayList<>();
        for (Step step : recipe.getSteps()) {
            Fragment fragment = StepFragment.newInstance(step);
            fragmentList.add(fragment);
        }
    }

    @Override
    public Fragment getItem(int i) {
        return fragmentList.get(i);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }
}
