package com.coocaa.whiteboard.ui.pad;

import android.content.Context;
import android.util.AttributeSet;

import com.coocaa.whiteboard.ui.R;
import com.coocaa.whiteboard.ui.toollayer.tvcontroller.TvControllerView;

/**
 * @ClassName PadTvControllerView
 * @Description TODO (write something)
 * @User heni
 * @Date 2021/3/16
 */
public class PadTvControllerView extends TvControllerView {

    public PadTvControllerView(Context context) {
        super(context);
    }

    public PadTvControllerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public int getLayoutId() {
        return R.layout.pad_whiteboard_tv_controller_layout;
    }
}
