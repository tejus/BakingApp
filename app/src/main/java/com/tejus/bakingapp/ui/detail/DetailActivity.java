package com.tejus.bakingapp.ui.detail;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
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

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity {

    public static final String EXTRA_POSITION_KEY = "position";

    @BindView(R.id.detail_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.viewpager_steps)
    ViewPager mViewPager;
    @BindView(R.id.viewpager_overview)
    ViewPager mSheetViewPager;
    @BindView(R.id.include_bottom_sheet)
    LinearLayout mSheetLayout;
    @BindView(R.id.tv_overview)
    TextView mTvOverview;
    @BindView(R.id.iv_overview_back)
    ImageView mIvBack;
    @BindView(R.id.iv_overview_forward)
    ImageView mIvForward;
    @BindView(R.id.tv_bottom_sheet_ingredients)
    TextView mTvIngredients;
    @BindView(R.id.tv_bottom_sheet_steps)
    TextView mTvSteps;

    private ActionBar mActionBar;
    private FragmentManager mFragmentManager;
    private DetailPagerAdapter mPagerAdapter;
    private Recipe mRecipe;
    private BottomSheetBehavior mSheetBehavior;
    private int mSheetState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

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

        mFragmentManager = getSupportFragmentManager();
        mPagerAdapter = new DetailPagerAdapter(mFragmentManager, mRecipe);
        mViewPager.setAdapter(mPagerAdapter);
        setupBottomSheetBehavior();
    }

    private void setupBottomSheetBehavior() {
        DetailSheetPagerAdapter sheetPagerAdapter =
                new DetailSheetPagerAdapter(mFragmentManager, mRecipe);
        mSheetViewPager.setAdapter(sheetPagerAdapter);
        mSheetViewPager.addOnPageChangeListener(mSheetChangeCallback);

        mSheetBehavior = BottomSheetBehavior.from(mSheetLayout);
        mSheetState = mSheetBehavior.getState();
        mTvIngredients.post(() -> mTvIngredients.setTranslationY(mTvIngredients.getHeight()));
        mTvSteps.post(() -> mTvSteps.setTranslationY(mTvSteps.getHeight()));
        mSheetBehavior.setBottomSheetCallback(mSheetCallback);
    }

    private BottomSheetBehavior.BottomSheetCallback mSheetCallback =
            new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View view, int i) {
                    mSheetState = i;
                    if (mSheetState == BottomSheetBehavior.STATE_COLLAPSED) {
                        sheetCollapseAnimation();
                    } else if (mSheetState == BottomSheetBehavior.STATE_EXPANDED) {
                        sheetExpandAnimation();
                    }
                }

                @Override
                public void onSlide(@NonNull View view, float v) {
                }
            };

    private ViewPager.SimpleOnPageChangeListener mSheetChangeCallback =
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
                    mActionBar.setSubtitle("Step " + (position + 1));
                }
            };

    public void onClickBack(View v) {
        if (mViewPager.getCurrentItem() > 0
                && mSheetState == BottomSheetBehavior.STATE_COLLAPSED)
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
    }

    public void onClickOverview(View v) {
        if (mSheetState == BottomSheetBehavior.STATE_COLLAPSED) {
            mSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            sheetExpandAnimation();
        }
    }

    public void onClickForward(View v) {
        if (mViewPager.getCurrentItem() < mPagerAdapter.getCount() - 1
                && mSheetState == BottomSheetBehavior.STATE_COLLAPSED)
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
    }

    public void onClickIngredients(View v) {
        if (mSheetState == BottomSheetBehavior.STATE_EXPANDED) {
            mSheetViewPager.setCurrentItem(0);
        }
    }

    public void onClickSteps(View v) {
        if (mSheetState == BottomSheetBehavior.STATE_EXPANDED) {
            mSheetViewPager.setCurrentItem(1);
        }
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
        if (mSheetState != BottomSheetBehavior.STATE_COLLAPSED) {
            mSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            sheetCollapseAnimation();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (mSheetState != BottomSheetBehavior.STATE_COLLAPSED) {
                mSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                sheetCollapseAnimation();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
