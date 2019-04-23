package com.tejus.bakingapp;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.tejus.bakingapp.model.Recipe;

public class DetailActivity extends AppCompatActivity {

    public static final String EXTRA_POSITION_KEY = "position";

    private FragmentManager mFragmentManager;
    private ViewPager mViewPager;
    private DetailPagerAdapter mPagerAdapter;
    private Fragment mIngredientsFragment;
    private Recipe mRecipe;

    private LinearLayout mSheetLayout;
    private BottomSheetBehavior mSheetBehavior;
    private int mSheetState;
    private ImageView mIvBack;
    private ImageView mIvForward;
    private View mBgView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

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

        mViewPager = findViewById(R.id.viewpager_detail);
        mSheetLayout = findViewById(R.id.include_bottom_sheet);
        mIvBack = findViewById(R.id.iv_bottom_sheet_back);
        mIvForward = findViewById(R.id.iv_bottom_sheet_forward);
        mBgView = findViewById(R.id.background_dim_detail);

        mFragmentManager = getSupportFragmentManager();

        mPagerAdapter = new DetailPagerAdapter(mFragmentManager, mRecipe);
        mViewPager.setAdapter(mPagerAdapter);

        mIngredientsFragment = IngredientsFragment.newInstance(mRecipe);
        mFragmentManager.beginTransaction()
                .add(R.id.frame_ingr, mIngredientsFragment)
                .commit();

        mSheetBehavior = BottomSheetBehavior.from(mSheetLayout);
        mSheetState = mSheetBehavior.getState();
        mSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int i) {
                mSheetState = i;
                if (mSheetState == BottomSheetBehavior.STATE_COLLAPSED) {
                    mBgView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {
                mBgView.setVisibility(View.VISIBLE);
                mBgView.setAlpha(v);
            }
        });

        RelativeLayout sheetBar = findViewById(R.id.bar_bottom_sheet);
        sheetBar.setOnClickListener(v -> {
            if (mSheetState == BottomSheetBehavior.STATE_EXPANDED) {
                mSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            } else if (mSheetState == BottomSheetBehavior.STATE_COLLAPSED) {
                mSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        mIvForward.setOnClickListener(v -> {
            if (mViewPager.getCurrentItem() < mPagerAdapter.getCount() - 1)
                mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
        });
        mIvBack.setOnClickListener(v -> {
            if (mViewPager.getCurrentItem() > 0)
                mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
        });
    }

    @Override
    public void onBackPressed() {
        if (mSheetState == BottomSheetBehavior.STATE_EXPANDED) {
            mSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (mSheetState == BottomSheetBehavior.STATE_EXPANDED) {
                Rect rect = new Rect();
                mSheetLayout.getGlobalVisibleRect(rect);

                if (!rect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    mSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    return true;
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}
