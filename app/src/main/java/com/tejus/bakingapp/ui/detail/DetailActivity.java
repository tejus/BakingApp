package com.tejus.bakingapp.ui.detail;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tejus.bakingapp.R;
import com.tejus.bakingapp.model.Recipe;

import biz.laenger.android.vpbs.BottomSheetUtils;
import biz.laenger.android.vpbs.ViewPagerBottomSheetBehavior;
import butterknife.BindView;
import butterknife.ButterKnife;

import static com.tejus.bakingapp.ingredientswidget.IngredientsWidgetService.startActionUpdateLatestRecipe;

public class DetailActivity extends AppCompatActivity implements StepsOverviewAdapter.OnStepAdapterClickListener {

    public static final String EXTRA_RECIPE_KEY = "recipe";
    public static final String EXTRA_CURRENT_STEP_KEY = "step";

    @BindView(R.id.detail_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.viewpager_overview)
    ViewPager mOverviewViewPager;
    @Nullable
    @BindView(R.id.include_bottom_sheet)
    LinearLayout mSheetLayout;
    @Nullable
    @BindView(R.id.tv_overview)
    TextView mTvOverview;
    @BindView(R.id.iv_overview_back)
    ImageView mIvBack;
    @BindView(R.id.iv_overview_forward)
    ImageView mIvForward;
    @BindView(R.id.tv_overview_ingredients)
    TextView mTvIngredients;
    @BindView(R.id.tv_overview_steps)
    TextView mTvSteps;

    private FragmentManager mFragmentManager;
    private boolean mTwoPane;
    private ActionBar mActionBar;
    private Recipe mRecipe;
    private int mTotalSteps;
    private int mCurrentStep;
    private ViewPagerBottomSheetBehavior mSheetBehavior;
    private int mSheetState;
    private boolean mFinished;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        //Load and verify recipe and current step, if any, from the launch intent
        getIntentData();

        //Proceed with view setup and variable initialisation only after verifying Intent data
        setupToolbar();

        mFinished = false;
        mFragmentManager = getSupportFragmentManager();

        restoreState(savedInstanceState);

        //Hide the back button if the activity launches to the first step
        if (mCurrentStep == 1) {
            mIvBack.setVisibility(View.INVISIBLE);
        }

        //Setup ViewPager for the ingredients and steps list
        DetailOverviewPagerAdapter overviewPagerAdapter =
                new DetailOverviewPagerAdapter(mFragmentManager, mRecipe);
        mOverviewViewPager.setAdapter(overviewPagerAdapter);
        mOverviewViewPager.addOnPageChangeListener(mOverviewTabChangeCallback);

        //Check if Two Pane layout has been inflated
        mTwoPane = findViewById(R.id.include_card_view) != null;
        //Setup the BottomSheetBehavior only if the layout is not Two Pane
        if (!mTwoPane)
            setupBottomSheetBehavior();
    }

    /**
     * Load and verify recipe and current step, if any, from the launch intent
     */
    private void getIntentData() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey(EXTRA_RECIPE_KEY)) {
            mRecipe = bundle.getParcelable(EXTRA_RECIPE_KEY);
            if (mRecipe == null) {
                throwError();
                return;
            }
            mTotalSteps = mRecipe.getSteps().size() - 1;
            if (bundle.containsKey(EXTRA_CURRENT_STEP_KEY)) {
                mCurrentStep = bundle.getInt(EXTRA_CURRENT_STEP_KEY);
            } else {
                //Start with the second step since we display
                //the Introduction in a Dialog in MainActivity
                mCurrentStep = 1;
            }
        } else {
            throwError();
        }
    }

    /**
     * Close activity and display a toast in case the recipe is invalid
     */
    private void throwError() {
        Toast.makeText(this, "Invalid recipe!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);
        mActionBar = getSupportActionBar();
        if (mActionBar != null)
            mActionBar.setDisplayHomeAsUpEnabled(true);
        if (mRecipe.getName() != null) {
            mActionBar.setTitle(mRecipe.getName());
        }
    }

    /**
     * Restore previous state in case of configuration change
     */
    private void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            loadFragment(mCurrentStep);
        } else {
            mCurrentStep = savedInstanceState.getInt(EXTRA_CURRENT_STEP_KEY);
            updateNavIcons(mCurrentStep);
            updateToolbar();
        }
    }

    /**
     * Setup BottomSheetBehavior and load callbacks for its state changes
     */
    private void setupBottomSheetBehavior() {
        if (mSheetLayout != null) {
            BottomSheetUtils.setupViewPager(mOverviewViewPager);
            mSheetBehavior = ViewPagerBottomSheetBehavior.from(mSheetLayout);
            mSheetState = mSheetBehavior.getState();
            mTvIngredients.post(() -> mTvIngredients.setTranslationY(mTvIngredients.getHeight()));
            mTvSteps.post(() -> mTvSteps.setTranslationY(mTvSteps.getHeight()));
            mSheetBehavior.setBottomSheetCallback(new ViewPagerBottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View view, int i) {
                    mSheetState = i;
                    if (mSheetState == ViewPagerBottomSheetBehavior.STATE_COLLAPSED) {
                        sheetCollapseAnimation();
                    } else if (mSheetState == ViewPagerBottomSheetBehavior.STATE_EXPANDED) {
                        sheetExpandAnimation();
                    }
                }

                @Override
                public void onSlide(@NonNull View view, float v) {
                }
            });

            //Add a bounce to the BottomSheet as a hint
            mSheetLayout.animate().translationY(-100).setDuration(500).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mSheetLayout.animate().translationY(0).setDuration(500);
                }
            });
        }
    }

    /**
     * Helper method to load StepFragments at a given step
     */
    private void loadFragment(int position) {
        mFragmentManager.beginTransaction()
                .replace(R.id.frame_steps, getStepFragment(position))
                .commit();
        updateNavIcons(position);
        mCurrentStep = position;
        updateToolbar();
    }

    /**
     * Helper method to retrieve StepFragment instances at a given step
     */
    private Fragment getStepFragment(int position) {
        return StepFragment.newInstance(mRecipe.getSteps().get(position), position);
    }

    /**
     * Helper method to display the correct navigation icons on the bottom bar
     */
    private void updateNavIcons(int position) {
        if (position == mTotalSteps) {
            mIvForward.setImageDrawable(
                    getDrawable(R.drawable.baseline_done_white_24)
            );
        } else if (mCurrentStep == mTotalSteps) {
            mIvForward.setImageDrawable(
                    getDrawable(R.drawable.baseline_chevron_right_white_24)
            );
        }

        if (position == 1) {
            mIvBack.animate()
                    .alpha(0f)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mIvBack.setVisibility(View.INVISIBLE);
                        }
                    });
        } else if (mCurrentStep == 1) {
            mIvBack.setVisibility(View.VISIBLE);
            mIvBack.animate()
                    .alpha(1f)
                    .setListener(null);
        }
    }

    /**
     * Helper method to update the toolbar with the current step number
     */
    private void updateToolbar() {
        mActionBar.setSubtitle(getString(R.string.detail_step_number, (mCurrentStep)));
    }

    private ViewPager.SimpleOnPageChangeListener mOverviewTabChangeCallback =
            new ViewPager.SimpleOnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
                    if (position == 0) {
                        mTvIngredients.setTextColor(getColor(R.color.colorAccent));
                        mTvSteps.setTextColor(getColor(R.color.colorGrey));
                    } else if (position == 1) {
                        mTvIngredients.setTextColor(getColor(R.color.colorGrey));
                        mTvSteps.setTextColor(getColor(R.color.colorAccent));
                    }
                }
            };

    public void onBtnClickBack(View v) {
        if (mCurrentStep == 1 || (!mTwoPane
                && mSheetState != ViewPagerBottomSheetBehavior.STATE_COLLAPSED))
            return;
        loadFragment(mCurrentStep - 1);
    }

    public void onBtnClickOverview(View v) {
        if (mSheetState == ViewPagerBottomSheetBehavior.STATE_COLLAPSED) {
            mSheetBehavior.setState(ViewPagerBottomSheetBehavior.STATE_EXPANDED);
            sheetExpandAnimation();
        }
    }

    public void onBtnClickForward(View v) {
        if (!mTwoPane
                && mSheetState != ViewPagerBottomSheetBehavior.STATE_COLLAPSED)
            return;
        if (mCurrentStep == mTotalSteps) {
            mFinished = true;
            finish();
            return;
        }
        loadFragment(mCurrentStep + 1);
    }

    public void onBtnClickIngredients(View v) {
        if (!mTwoPane
                && mSheetState != ViewPagerBottomSheetBehavior.STATE_EXPANDED)
            return;
        mOverviewViewPager.setCurrentItem(0);
    }

    public void onBtnClickSteps(View v) {
        if (!mTwoPane
                && mSheetState != ViewPagerBottomSheetBehavior.STATE_EXPANDED)
            return;
        mOverviewViewPager.setCurrentItem(1);
    }

    @Override
    public void onStepClick(int position) {
        loadFragment(position);
        if (!mTwoPane)
            onBackPressed();
    }

    /**
     * Method to animate the Bottom Sheet views when it is collapsing
     */
    private void sheetCollapseAnimation() {
        if (mTvOverview != null) {
            mIvBack.animate().translationX(0);
            mIvForward.animate().translationX(0);
            mTvOverview.animate().translationY(0);
            mTvIngredients.setClickable(false);
            mTvIngredients.setFocusable(false);
            mTvIngredients.animate().translationY(mTvIngredients.getHeight());
            mTvSteps.setClickable(false);
            mTvSteps.setFocusable(false);
            mTvSteps.animate().translationY(mTvSteps.getHeight());
        }
    }

    /**
     * Method to animate the Bottom Sheet views when it is expanding
     */
    private void sheetExpandAnimation() {
        if (mTvOverview != null) {
            mIvBack.animate().translationX(-mIvBack.getWidth());
            mIvForward.animate().translationX(mIvForward.getWidth());
            mTvOverview.animate().translationY(-mTvOverview.getHeight());
            mTvIngredients.setClickable(true);
            mTvIngredients.setFocusable(true);
            mTvIngredients.animate().translationY(0);
            mTvSteps.setClickable(true);
            mTvSteps.setFocusable(true);
            mTvSteps.animate().translationY(0);
        }
    }

    /**
     * Helper method to save the progress through the current recipe, if it is not finished
     */
    private void saveCurrentProgress() {
        SharedPreferences.Editor preferencesEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        if (mFinished) {
            preferencesEditor.clear();
        } else {
            preferencesEditor.putInt(getString(R.string.pref_recipe_id_key), mRecipe.getId());
            preferencesEditor.putInt(getString(R.string.pref_step_key), mCurrentStep);
        }
        preferencesEditor.apply();

        //Update all IngredientsWidget instances
        startActionUpdateLatestRecipe(this);
    }

    @Override
    public void onBackPressed() {
        //Capture the back press if the Bottom Sheet is expanded, to collapse it
        if (!mTwoPane
                && mSheetState != ViewPagerBottomSheetBehavior.STATE_COLLAPSED) {
            mSheetBehavior.setState(ViewPagerBottomSheetBehavior.STATE_COLLAPSED);
            sheetCollapseAnimation();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Capture the back press if the Bottom Sheet is expanded, to collapse it
        if (item.getItemId() == android.R.id.home) {
            if (!mTwoPane
                    && mSheetState != ViewPagerBottomSheetBehavior.STATE_COLLAPSED) {
                mSheetBehavior.setState(ViewPagerBottomSheetBehavior.STATE_COLLAPSED);
                sheetCollapseAnimation();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!TextUtils.equals(mRecipe.getName(), "Test recipe")) {
            saveCurrentProgress();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_CURRENT_STEP_KEY, mCurrentStep);
    }
}
