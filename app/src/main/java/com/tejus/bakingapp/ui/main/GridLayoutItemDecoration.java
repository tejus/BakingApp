package com.tejus.bakingapp.ui.main;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class GridLayoutItemDecoration extends RecyclerView.ItemDecoration {

    private int spacing;
    private int numColumns;

    public GridLayoutItemDecoration(int spacing, int numColumns) {
        this.spacing = spacing;
        this.numColumns = numColumns;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int position = parent.getChildAdapterPosition(view);

        /*
         ** Author: liangzhitao
         ** Link: https://gist.github.com/liangzhitao/e57df3c3232ee446d464
         */
        if (position >= 0) {
            int columnNumber = position % numColumns;

            outRect.left = spacing - columnNumber * spacing / numColumns;
            outRect.right = (columnNumber + 1) * spacing / numColumns;
            if (position < numColumns)
                outRect.top = spacing;
            outRect.bottom = spacing;
        }
    }
}
