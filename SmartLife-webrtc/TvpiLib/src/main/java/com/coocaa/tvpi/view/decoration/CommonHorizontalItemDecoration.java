package com.coocaa.tvpi.view.decoration;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by IceStorm on 2017/12/11.
 */

public class CommonHorizontalItemDecoration extends RecyclerView.ItemDecoration {

    private int centerSpace;
    private int boundarySpace;

    public int getCenterSpace() {
        return centerSpace;
    }

    public int getBoundarySpace() {
        return boundarySpace;
    }

    public CommonHorizontalItemDecoration(int centerSpace) {
        this.boundarySpace = 0;
        this.centerSpace = centerSpace;
    }

    public CommonHorizontalItemDecoration(int boundarySpace, int centerSpace) {
        this.boundarySpace = boundarySpace;
        this.centerSpace = centerSpace;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
//        outRect.left=space;
//        outRect.right=centerSpace;
//        outRect.bottom=space;
        if(parent.getChildAdapterPosition(view)==0){
            outRect.left = boundarySpace;
        } else {
            outRect.left = 0;
        }
        if (parent.getAdapter().getItemCount() - 1 == parent.getChildAdapterPosition(view)) {
            outRect.right=boundarySpace;
        } else {
            outRect.right=centerSpace;
        }
    }
}
