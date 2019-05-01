package com.tejus.bakingapp.ui;

import android.content.Context;
import android.text.Layout;
import android.util.AttributeSet;

/*
 ** Author: Vitaliy Polchuk
 ** Link: https://stackoverflow.com/a/13203635
 */
public class WrapWidthTextView extends android.support.v7.widget.AppCompatTextView {
    public WrapWidthTextView(Context context) {
        super(context);
    }

    public WrapWidthTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WrapWidthTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        Layout layout = getLayout();
        if (layout != null) {
            int width = (int) Math.ceil(getMaxLineWidth(layout))
                    + getCompoundPaddingLeft() + getCompoundPaddingRight();
            int height = getMeasuredHeight();
            setMeasuredDimension(width, height);
        }
    }

    private float getMaxLineWidth(Layout layout) {
        float max_width = 0f;
        int lines = layout.getLineCount();
        for (int i = 0; i < lines; i++) {
            if (layout.getLineWidth(i) > max_width) {
                max_width = layout.getLineWidth(i);
            }
        }
        return max_width;
    }
}
