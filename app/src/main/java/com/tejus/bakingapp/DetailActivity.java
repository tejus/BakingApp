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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.tejus.bakingapp.model.Recipe;

public class DetailActivity extends AppCompatActivity {

    public static final String EXTRA_POSITION_KEY = "position";

    private FragmentManager mFragmentManager;
    private DetailPagerAdapter mPagerAdapter;
    private Fragment mIngredientsFragment;
    private Recipe mRecipe;

    private LinearLayout mSheetLayout;
    private BottomSheetBehavior mBottomSheet;
    private int mSheetState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mFragmentManager = getSupportFragmentManager();
        ViewPager viewPager = findViewById(R.id.viewpager_detail);

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

        mPagerAdapter = new DetailPagerAdapter(mFragmentManager, mRecipe);
        viewPager.setAdapter(mPagerAdapter);

        mIngredientsFragment = IngredientsFragment.newInstance(mRecipe);

        mFragmentManager.beginTransaction()
                .add(R.id.frame_ingr, mIngredientsFragment)
                .commit();

        mSheetLayout = findViewById(R.id.include_bottom_sheet);

        mBottomSheet = BottomSheetBehavior.from(mSheetLayout);
        mBottomSheet.setBottomSheetCallback(mSheetCallback);
        mSheetState = mBottomSheet.getState();

        RelativeLayout sheetBar = findViewById(R.id.bar_bottom_sheet);
        sheetBar.setOnClickListener(v -> {
            if (mSheetState == BottomSheetBehavior.STATE_EXPANDED) {
                mBottomSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
            } else if (mSheetState == BottomSheetBehavior.STATE_COLLAPSED) {
                mBottomSheet.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

    }

    private BottomSheetBehavior.BottomSheetCallback mSheetCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View view, int i) {
            mSheetState = i;
        }

        @Override
        public void onSlide(@NonNull View view, float v) {

        }
    };

    @Override
    public void onBackPressed() {
        if (mSheetState == BottomSheetBehavior.STATE_EXPANDED) {
            mBottomSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
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
                    mBottomSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    return true;
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}
