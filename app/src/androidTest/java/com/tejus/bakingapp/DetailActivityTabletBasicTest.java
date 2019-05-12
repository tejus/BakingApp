package com.tejus.bakingapp;

import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.tejus.bakingapp.model.Recipe;
import com.tejus.bakingapp.ui.detail.DetailActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class DetailActivityTabletBasicTest {

    @Rule
    public ActivityTestRule<DetailActivity> mActivityTestRule =
            new ActivityTestRule<>(DetailActivity.class, false, false);

    /**
     * Tests the Ingredient and Step ViewPager on layouts with width more than 600dp
     * when landscape and more than 720dp when portrait. Set a delay between clicks for
     * animations to complete, even on slow emulators
     */
    @Test
    public void clickOverviewStep_ShowsStepRecyclerView() {
        Recipe recipe = Recipe.getTestRecipe();
        Intent testLaunchIntent = new Intent();
        testLaunchIntent.putExtra(DetailActivity.EXTRA_RECIPE_KEY, recipe);
        mActivityTestRule.launchActivity(testLaunchIntent);
        onView(withId(R.id.tv_step_heading)).check(matches(isDisplayed()));
        onView(withId(R.id.rv_ingredients)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_overview_steps)).perform(click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.rv_steps)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_overview_ingredients)).perform(click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.rv_ingredients)).check(matches(isDisplayed()));
    }
}
