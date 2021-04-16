package com.coocaa.whiteboard.ui.pad;

import android.content.Context;
import android.util.AttributeSet;

import com.coocaa.whiteboard.ui.R;
import com.coocaa.whiteboard.ui.toollayer.WBToolLayerView;


/**
 * @ClassName IOverLayout
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 3/2/21
 * @Version TODO (write something)
 */
public class PadToolLayerView extends WBToolLayerView {

    public PadToolLayerView(Context context) {
        this(context, null);
    }

    public PadToolLayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public int getLayoutId() {
        return R.layout.pad_whiteboard_tool_layer_layout;
    }
}
