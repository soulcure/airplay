package com.coocaa.tvpi.module.movie.decoration;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.publib.PublibHelper;
import com.coocaa.publib.utils.DimensUtils;

/**
 * Created by WHY on 2018/4/8.
 */

public class LongVideoItemDecoration extends RecyclerView.ItemDecoration {

    public LongVideoItemDecoration() {

    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

        if (parent.getAdapter().getItemCount() - 1 == parent.getChildAdapterPosition(view)) {
            outRect.bottom = DimensUtils.dp2Px(PublibHelper.getContext(), 60f);
        }

    }
}
