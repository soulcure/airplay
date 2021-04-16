package com.coocaa.tvpi.module.movie.decoration;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by WHY on 2018/4/8.
 */

public class EpisodesItemDecoration extends RecyclerView.ItemDecoration {

    private int spanCount;
    private int spacingH;
    private int spacingV;

    public EpisodesItemDecoration(int spanCount, int spacingH, int spacingV) {
        this.spanCount = spanCount;
        this.spacingH = spacingH;
        this.spacingV = spacingV;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view); // item position
        int column = position % spanCount; // item column

        outRect.left = column * spacingH / spanCount; // column * ((1f / spanCount) * spacing)
        outRect.right = spacingH - (column + 1) * spacingH / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)

        //注释if判断条件，设计给的图ui第一行也要有top间隔，但是滚动范围要能到最上边
//        if (position >= spanCount) {
            outRect.top = spacingV; // item top
//        }

        if (parent.getAdapter().getItemCount() - 1 == position) {
            outRect.bottom = spacingV;
        }

    }
}
