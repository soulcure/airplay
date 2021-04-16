package swaiotos.runtime.h5;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.core.graphics.ColorUtils;
import swaiotos.runtime.base.AppletActivity;
import swaiotos.runtime.base.StatusBarHelper;
import swaiotos.runtime.base.style.AppletTitleStyle;

/**
 * @Author: yuzhan
 */
class H5MobileFloatAppletLayoutBuilder implements AppletActivity.LayoutBuilder, AppletActivity.HeaderHandler {

    Context context;
    private View root;
    private View headerLayout;
    LinearLayout leftLayout;
    LinearLayout rightLayout;

    private Runnable shareClickRunnable;
    private Runnable backButtonClickRunnable;

    private boolean isDarkMode = false;

    public H5MobileFloatAppletLayoutBuilder(Context context) {
        this.context = context;
    }

    @Override
    public View build(View content) {
        LayoutInflater inflater = LayoutInflater.from(context);
        root = inflater.inflate(R.layout.h5_float_layout_v2, null);
        ViewGroup contentHolder = root.findViewById(R.id.h5_float_layout_content);
        contentHolder.addView(content, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        headerLayout = root.findViewById(R.id.h5_float_layout_header_v2);

        leftLayout = headerLayout.findViewById(R.id.h5_float_layout_left);
        rightLayout = headerLayout.findViewById(R.id.h5_float_layout_right);
        init();
        return root;
    }

    private void init() {
        leftLayout.setBackgroundResource(R.mipmap.h5_float_left_back);
        rightLayout.setBackgroundResource(R.mipmap.h5_float_right_share);

        leftLayout.setClickable(true);
        rightLayout.setClickable(true);
        leftLayout.setOnClickListener(onClickListener);
        rightLayout.setOnClickListener(onClickListener);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v == leftLayout) {
                if(backButtonClickRunnable != null) {
                    backButtonClickRunnable.run();
                }
            }
            else if(v == rightLayout) {
                if(shareClickRunnable != null) {
                    shareClickRunnable.run();
                }
            }
        }
    };

    @Override
    public void setCustomHeaderLeftView(View view) {

    }

    @Override
    public void setHeaderVisible(boolean visible) {
        if(headerLayout == null) return ;
        headerLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setBackButtonIcon(Drawable drawable) {

    }

    @Override
    public void setBackButtonVisible(boolean visiable) {

    }

    @Override
    public void setBackButtonOnClickListener(Runnable callback) {
        backButtonClickRunnable = callback;
    }

    @Override
    public void setTitle(CharSequence title) {

    }

    @Override
    public void setTitle(CharSequence title, CharSequence subTitle) {

    }

    @Override
    public void setShareButtonOnClickListener(Runnable callback) {
        shareClickRunnable = callback;
    }

    @Override
    public void setExitButtonOnClickListener(Runnable callback) {

    }

    @Override
    public void setBackgroundColor(int color) {
        headerLayout.setBackgroundColor(color);
        StatusBarHelper.transparencyBar(((Activity) context).getWindow(), color);
        setDarkMode(!isLightColor(color));
    }

    @Override
    public void setDarkMode(boolean darkmode) {
        this.isDarkMode = darkmode;
        if(isDarkMode) {
            StatusBarHelper.setStatusBarDarkMode((Activity) context);
        } else {
            StatusBarHelper.setStatusBarLightMode((Activity) context);
        }
    }

    @Override
    public void setTitleAlpha(float alpha) {

    }

    @Override
    public void setNavigationBarColor(int color) {

    }

    @Override
    public void setTitleStyle(AppletTitleStyle style) {

    }

    @Override
    public void setExitButtonVisible(boolean visible) {
        if(rightLayout != null) {
            rightLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    private static boolean isLightColor(int color) {
        return ColorUtils.calculateLuminance(color) >= 0.5;
    }
}
