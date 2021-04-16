package swaiotos.runtime.h5;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import swaiotos.runtime.base.FloatAppletLayoutBuilder;
import swaiotos.runtime.base.StatusBarHelper;

/**
 * @Author: yuzhan
 */
public class H5FloatNPAppletActivity extends H5NPAppletActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        StatusBarHelper.translucent(this);
//        if(mHeaderHandler != null) {
//            mHeaderHandler.setDarkMode(true);
//        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            getWindow().setNavigationBarColor(Color.BLACK);
//        }
    }

    @Override
    protected LayoutBuilder createLayoutBuilder() {
        return new FloatAppletLayoutBuilder(this, needFitsSystemWindows(), getWindow());
    }

    @Override
    protected boolean needFitsSystemWindows() {
        return false;
    }
}
