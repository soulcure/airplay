package com.coocaa.swaiotos.virtualinput.module.view.decoration;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by IceStorm on 2017/12/11.
 */

public class CommonVerticalItemDecoration extends RecyclerView.ItemDecoration {

    private int centerSpace;
    private int boundarySpace;

    private int headerSpace;
    private int footerSpace;

    public int getCenterSpace() {
        return centerSpace;
    }

    public int getBoundarySpace() {
        return boundarySpace;
    }

    public CommonVerticalItemDecoration(int centerSpace) {
        this.centerSpace = centerSpace;
    }

    public CommonVerticalItemDecoration(int boundarySpace, int centerSpace) {
        this.boundarySpace = boundarySpace;
        this.centerSpace = centerSpace;
    }

    public CommonVerticalItemDecoration(int headerSpace, int centerSpace, int footerSpace) {
        this.headerSpace = headerSpace;
        this.centerSpace = centerSpace;
        this.footerSpace = footerSpace;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
//        outRect.left=space;
//        outRect.right=centerSpace;
//        outRect.bottom=space;
        if(parent.getChildAdapterPosition(view)==0){
            outRect.top = boundarySpace == 0 ? headerSpace : boundarySpace;
        } else {
            outRect.top = 0;
        }
        if (parent.getAdapter().getItemCount() - 1 == parent.getChildAdapterPosition(view)) {
            outRect.bottom = boundarySpace == 0 ? footerSpace : boundarySpace;
        } else {
            outRect.bottom = centerSpace;
        }
    }
}
