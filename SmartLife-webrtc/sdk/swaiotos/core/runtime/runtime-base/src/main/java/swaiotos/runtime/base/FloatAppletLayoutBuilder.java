package swaiotos.runtime.base;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.Window;

/**
 * @Author: yuzhan
 */
public class FloatAppletLayoutBuilder extends AppletLayoutBuilder {

    public FloatAppletLayoutBuilder(Context context) {
        this(context, false, null);
    }

    public FloatAppletLayoutBuilder(Context context, boolean fitSystemWindow, Window window) {
        super(context, fitSystemWindow, window);
    }

    @Override
    public View build(View content) {
        View ret = super.build(content);
        headerLayout.setBackgroundColor(Color.TRANSPARENT);
        return ret;
    }

    @Override
    protected int layoutId() {
        return R.layout.float_layout_applet_activity;
    }
}
