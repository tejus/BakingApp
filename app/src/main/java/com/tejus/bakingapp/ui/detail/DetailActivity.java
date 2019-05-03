package com.tejus.bakingapp.ui.detail;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

    @BindView(R.id.detail_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.viewpager_steps)
    ViewPager mViewPager;
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

    private boolean mTwoPane;
    private ActionBar mActionBar;
    private DetailPagerAdapter mPagerAdapter;
    private Recipe mRecipe;
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
            mViewPager.addOnPageChangeListener(mStepChangeCallback);
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        mPagerAdapter = new DetailPagerAdapter(fragmentManager, mRecipe);
        mViewPager.setAdapter(mPagerAdapter);
        DetailOverviewPagerAdapter overviewPagerAdapter =
                new DetailOverviewPagerAdapter(fragmentManager, mRecipe);
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

    private ViewPager.SimpleOnPageChangeListener mStepChangeCallback =
            new ViewPager.SimpleOnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    if (position == 0)
                        mActionBar.setSubtitle(getString(R.string.detail_intro));
                    else
                        mActionBar.setSubtitle(getString(R.string.detail_step_number, (position)));
                }
            };

    public void onBtnClickBack(View v) {
        if (!mTwoPane
                && mSheetState != ViewPagerBottomSheetBehavior.STATE_COLLAPSED)
            return;
        if (mViewPager.getCurrentItem() > 0)
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
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
        if (mViewPager.getCurrentItem() < mPagerAdapter.getCount() - 1)
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
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
        mViewPager.setCurrentItem(position);
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
}
