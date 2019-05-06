package com.tejus.bakingapp.ui.detail;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tejus.bakingapp.R;
import com.tejus.bakingapp.data.Repository;
import com.tejus.bakingapp.model.Recipe;

import biz.laenger.android.vpbs.BottomSheetUtils;
import biz.laenger.android.vpbs.ViewPagerBottomSheetBehavior;
import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity implements StepsOverviewAdapter.OnStepAdapterClickListener {

    public static final String EXTRA_POSITION_KEY = "position";
    public static final String CURRENT_STEP_KEY = "step";

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

    FragmentManager mFragmentManager;
    private boolean mTwoPane;
    private ActionBar mActionBar;
    private Recipe mRecipe;
    private int mCurrentStep;
    private ViewPagerBottomSheetBehavior mSheetBehavior;
    private int mSheetState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        mTwoPane = findViewById(R.id.include_card_view) != null;

        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey(EXTRA_POSITION_KEY)) {
            int position = bundle.getInt(EXTRA_POSITION_KEY);
            mRecipe = Repository.getRecipe(this, position);
        } else {
            Toast.makeText(this, "Invalid recipe!", Toast.LENGTH_SHORT).show();
            finish();
        }

        setSupportActionBar(mToolbar);
        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setTitle(mRecipe.getName());
        }

        mFragmentManager = getSupportFragmentManager();
        mCurrentStep = (savedInstanceState != null) ? savedInstanceState.getInt(CURRENT_STEP_KEY) : 0;
        if (savedInstanceState == null) {
            loadFragment(mCurrentStep);
        }

        DetailOverviewPagerAdapter overviewPagerAdapter =
                new DetailOverviewPagerAdapter(mFragmentManager, mRecipe);
        mOverviewViewPager.setAdapter(overviewPagerAdapter);
        mOverviewViewPager.addOnPageChangeListener(mOverviewTabChangeCallback);

        if (!mTwoPane)
            setupBottomSheetBehavior();
    }

    private void setupBottomSheetBehavior() {
        BottomSheetUtils.setupViewPager(mOverviewViewPager);
        mSheetBehavior = ViewPagerBottomSheetBehavior.from(mSheetLayout);
        mSheetState = mSheetBehavior.getState();
        mTvIngredients.post(() -> mTvIngredients.setTranslationY(mTvIngredients.getHeight()));
        mTvSteps.post(() -> mTvSteps.setTranslationY(mTvSteps.getHeight()));
        mSheetBehavior.setBottomSheetCallback(mSheetCallback);
    }

    private void loadFragment(int position) {
        mFragmentManager.beginTransaction()
                .replace(R.id.frame_steps, getStepFragment(position))
                .commit();
        updateToolbar();
    }

    private Fragment getStepFragment(int position) {
        return StepFragment.newInstance(mRecipe.getSteps().get(position));
    }

    private void updateToolbar() {
        if (mCurrentStep == 0)
            mActionBar.setSubtitle(getString(R.string.detail_intro));
        else
            mActionBar.setSubtitle(getString(R.string.detail_step_number, (mCurrentStep)));
    }

    private ViewPagerBottomSheetBehavior.BottomSheetCallback mSheetCallback =
            new ViewPagerBottomSheetBehavior.BottomSheetCallback() {
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
            };

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
        if (mCurrentStep == 0 || (!mTwoPane
                && mSheetState != ViewPagerBottomSheetBehavior.STATE_COLLAPSED))
            return;
        loadFragment(--mCurrentStep);
    }

    public void onBtnClickOverview(View v) {
        if (mSheetState == ViewPagerBottomSheetBehavior.STATE_COLLAPSED) {
            mSheetBehavior.setState(ViewPagerBottomSheetBehavior.STATE_EXPANDED);
            sheetExpandAnimation();
        }
    }

    public void onBtnClickForward(View v) {
        if (mCurrentStep == mRecipe.getSteps().size() - 1 || (!mTwoPane
                && mSheetState != ViewPagerBottomSheetBehavior.STATE_COLLAPSED))
            return;
        loadFragment(++mCurrentStep);
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
        mCurrentStep = position;
        loadFragment(mCurrentStep);
        if (!mTwoPane)
            onBackPressed();
    }

    private void sheetCollapseAnimation() {
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

    private void sheetExpandAnimation() {
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

    @Override
    public void onBackPressed() {
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CURRENT_STEP_KEY, mCurrentStep);
    }
}
