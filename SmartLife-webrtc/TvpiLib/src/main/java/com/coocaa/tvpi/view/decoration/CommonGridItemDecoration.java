package com.coocaa.tvpi.view.decoration;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;


/**
 * Created by WHY on 2018/4/8.
 */

public class CommonGridItemDecoration extends RecyclerView.ItemDecoration {

    private int spanCount;
    private int spacingH;
    private int spacingV;

    public CommonGridItemDecoration(int spanCount, int spacingH, int spacingV) {
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

        if (position >= spanCount) {
            outRect.top = spacingV; // item top
        }

        int childCount = parent.getAdapter().getItemCount();
        if (childCount  % spanCount == 0) {
            childCount = childCount - spanCount;
        } else {
            childCount = childCount - childCount % spanCount;
        }
//            outRect.bottom = DimensUtils.dp2Px(MyApplication.getContext(), 60f);


//        Log.d("wuhaiyuan", "position: " + position + "   colum:" + column);
//        Log.d("wuhaiyuan", "l:" + outRect.left+", "+ "r:" + outRect.right+", "
//                + "t:" + outRect.top+", "+ "b:" + outRect.bottom+", ");
        /*if (parent.getAdapter().getItemCount() - 1 == parent.getChildAdapterPosition(view)) {
            outRect.bottom = DimensUtils.dp2Px(MyApplication.getContext(), 60f);
        }*/

    }
}
