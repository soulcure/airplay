package swaiotos.runtime.base;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.graphics.ColorUtils;
import swaiotos.runtime.base.style.AppletTitleStyle;

/**
 * @Author: yuzhan
 */
class AppletLayoutBuilder implements AppletActivity.LayoutBuilder, AppletActivity.HeaderHandler {
    private Context context;
    private boolean fitSystemWindow;
    private Window window;

    private ImageView backButton;
    private boolean isBackButtonDrawableChanged = false;
//    private Runnable shareClickRunnable;
    private Runnable backButtonClickRunnable;
    private Runnable rawBackButtonClickRunnable;
//    private View shareButton;
    private View exitButton;
    private Runnable exitButtonClickRunnable;
    private TextView titleTextView;
    protected View headerLayout;
    //
    protected LinearLayout headerLeftLayout, headerCenterLayout, headerRightLayout;
    private boolean hasInited = false;
    private View root;
    private boolean isDarkMode = false;

    private boolean hasNavigationBarColor = false;

    public AppletLayoutBuilder(Context context) {
        this(context, false);
    }

    public AppletLayoutBuilder(Context context, boolean fitSystemWindow) {
        this(context, fitSystemWindow, null);
    }

    public AppletLayoutBuilder(Context context, boolean fitSystemWindow, Window window) {
        this.context = context;
        this.fitSystemWindow = fitSystemWindow;
        this.window = window;
    }

    @Override
    public View build(View content) {
        if(hasInited)
            return root;
        hasInited = true;
        LayoutInflater inflater = LayoutInflater.from(context);
        root = inflater.inflate(layoutId(), null);
//        root.setFitsSystemWindows(true);//这个会导致无法实现沉浸式状态栏
        ViewGroup contentHolder = root.findViewById(R.id.applet_layout_content);
        contentHolder.addView(content, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        headerLayout = root.findViewById(R.id.applet_layout_header);
//
        headerLeftLayout = headerLayout.findViewById(R.id.applet_header_left);
        headerCenterLayout = headerLayout.findViewById(R.id.applet_header_center);
        headerRightLayout = headerLayout.findViewById(R.id.applet_header_right);

        initLeft();
        initCenter();
        initRight();

        return root;
    }

    private void initLeft() {
        backButton = new ImageView(context);
        backButton.setImageResource(R.drawable.applet_back);
        //让返回箭头更容易被触控到
        backButton.setPadding(0, (int) context.getResources().getDimension(R.dimen.runtime_title_padding),
                (int) context.getResources().getDimension(R.dimen.runtime_left_container_padding),
                (int) context.getResources().getDimension(R.dimen.runtime_title_padding));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        backButton.setLayoutParams(params);
        backButton.setVisibility(View.GONE);
        headerLeftLayout.addView(backButton);
        backButton.setClickable(true);
        backButton.setOnClickListener(onClickListener);
    }

    private void initRight() {
//        shareButton = headerLayout.findViewById(R.id.applet_header_share);
        exitButton = headerLayout.findViewById(R.id.applet_header_right);
//        shareButton.setOnClickListener(onClickListener);
        exitButton.setOnClickListener(onClickListener);
//        shareButton.setOnTouchListener(onTouchListener);
        exitButton.setOnTouchListener(onTouchListener);
    }

    private void initCenter() {
        titleTextView = new TextView(context);
        titleTextView.setTextColor(Color.BLACK);
        titleTextView.setTextSize(18);
        titleTextView.setGravity(Gravity.CENTER);
        titleTextView.setSingleLine();
        titleTextView.setPadding(0, 0, (int) context.getResources().getDimension(R.dimen.runtime_title_padding), 0);
        titleTextView.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleTextView.setLayoutParams(params);
        headerCenterLayout.addView(titleTextView);
    }

    @Override
    public void setCustomHeaderLeftView(View view) {
        if(headerLeftLayout == null) return ;
        headerLeftLayout.removeAllViews();
        headerLeftLayout.addView(view);
    }

    @Override
    public void setBackButtonVisible(final boolean visiable) {
        if(backButton == null) return ;
        backButton.setVisibility(visiable ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setBackButtonOnClickListener(final Runnable callback) {
        //app可能会动态修改返回键的行为
        if(rawBackButtonClickRunnable == null && callback != null) {
            rawBackButtonClickRunnable = callback;
        }
        backButtonClickRunnable = callback;
    }

    @Override
    public void setShareButtonOnClickListener(Runnable callback) {
//        this.shareClickRunnable = callback;
    }


    @Override
    public void setTitleAlpha(float alpha){
        if(titleTextView == null) return ;
        titleTextView.setAlpha(alpha);
    }

    @Override
    public void setNavigationBarColor(int color) {
        if(window != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                hasNavigationBarColor = true;
                window.setNavigationBarColor(color);
            }
        }
    }

    @Override
    public void setTitleStyle(AppletTitleStyle style) {
        if(style == null) return ;
        if(style.getAlpha() != null) {
            titleTextView.setAlpha(style.getAlpha());
        }
        if(style.getColor() != null) {
            titleTextView.setTextColor(style.getColor());
        }
        if(style.isFakeBold() != null) {
            titleTextView.getPaint().setFakeBoldText(style.isFakeBold());
        }
        if(style.getTextSize() != null) {
            titleTextView.setTextSize(style.getTextSize());
        }
    }

    @Override
    public void setExitButtonVisible(boolean visible) {
        if(exitButton != null) {
            exitButton.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        if(titleTextView == null) return ;
        titleTextView.setText(title);
    }

    @Override
    public void setTitle(CharSequence title, CharSequence subTitle) {
        if(titleTextView == null) return ;
        if(!TextUtils.isEmpty(subTitle)) {
            StringBuilder sb = new StringBuilder(title).append("\n").append(subTitle);
            String concat = sb.toString();
            SpannableString spannableString = new SpannableString(concat);
            spannableString.setSpan(new AbsoluteSizeSpan(12, true), title.length()+1, concat.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            titleTextView.setSingleLine(false);
            setTitle(spannableString);
        } else {
            titleTextView.setSingleLine();
            setTitle(title);
        }
    }

    @Override
    public void setExitButtonOnClickListener(final Runnable callback) {
        exitButtonClickRunnable = callback;
    }

    @Override
    public void setBackgroundColor(int color) {
        headerLayout.setBackgroundColor(color);
        StatusBarHelper.transparencyBar(((Activity) context).getWindow(), color);
        setDarkMode(!isLightColor(color));
    }

    private static boolean isLightColor(int color) {
        return ColorUtils.calculateLuminance(color) >= 0.5;
    }

    @Override
    public void setHeaderVisible(boolean visiable) {
        if(headerLayout == null) return ;
        headerLayout.setVisibility(visiable ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setBackButtonIcon(Drawable drawable) {
        if(backButton == null) {
            Log.e("AppletLayoutBuilder", "setBackButtonIcon() called backButton == null");
            return;
        }
        if(drawable == null) {
            backButton.setImageResource(isDarkMode ? R.drawable.applet_back_dark : R.drawable.applet_back);
            isBackButtonDrawableChanged = false;
            backButton.setPadding(0, (int) context.getResources().getDimension(R.dimen.runtime_title_padding),
                    (int) context.getResources().getDimension(R.dimen.runtime_left_container_padding),
                    (int) context.getResources().getDimension(R.dimen.runtime_title_padding));
        } else {
            backButton.setImageDrawable(drawable);
            isBackButtonDrawableChanged = true;
            backButton.setPadding(0, 0, 0, 0);
        }
    }

    public void changeHeaderHeight(int height) {
        ViewGroup.LayoutParams layoutParams = headerLayout.getLayoutParams();
        if(layoutParams == null)
            layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
        layoutParams.height = height;
        headerLayout.setLayoutParams(layoutParams);
    }

    @Override
    public void setDarkMode(boolean b) {
        this.isDarkMode = b;
        if(isDarkMode) {
            StatusBarHelper.setStatusBarDarkMode((Activity) context);
            headerRightLayout.setBackgroundResource(R.drawable.applet_right_dark);
            titleTextView.setTextColor(Color.WHITE);
        } else {
            StatusBarHelper.setStatusBarLightMode((Activity) context);
            headerRightLayout.setBackgroundResource(R.drawable.applet_right);
            titleTextView.setTextColor(Color.BLACK);
        }
        if(!isBackButtonDrawableChanged) {
            backButton.setImageResource(isDarkMode ? R.drawable.applet_back_dark : R.drawable.applet_back);
        }
        if(window != null && !hasNavigationBarColor) { //适配全面屏底部
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.setNavigationBarColor(isDarkMode ? Color.BLACK : Color.TRANSPARENT);
            }
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v == exitButton) {
                if(exitButtonClickRunnable != null) {
                    exitButtonClickRunnable.run();
                }
            }
//            else if(v == shareButton) {
//                if(shareClickRunnable != null) {
//                    shareClickRunnable.run();
//                }
//            }
            else if(v == backButton) {
                if(backButtonClickRunnable != null) {
                    backButtonClickRunnable.run();
                } else if(rawBackButtonClickRunnable != null) {
                    rawBackButtonClickRunnable.run();
                }
            }
        }
    };

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(event.getAction() == MotionEvent.ACTION_DOWN) {
                if(v == exitButton) {
//                    headerRightLayout.setBackgroundResource(isDarkMode ? R.drawable.applet_right_exit_press_dark : R.drawable.applet_right_exit_press);
                    headerRightLayout.setBackgroundResource(isDarkMode ? R.drawable.applet_right_press_dark : R.drawable.applet_right_press);
                }
//                else if(v == shareButton) {
//                    headerRightLayout.setBackgroundResource(isDarkMode ? R.drawable.applet_right_share_press_dark : R.drawable.applet_right_share_press);
//                }
            } else if(event.getAction() == MotionEvent.ACTION_UP) {
                if(v == exitButton) {
                    headerRightLayout.setBackgroundResource(isDarkMode ? R.drawable.applet_right_dark : R.drawable.applet_right);
//                    headerRightLayout.setBackgroundResource(isDarkMode ? R.drawable.applet_right_dark : R.drawable.applet_right);
                }
//                else if(v == shareButton) {
//                    headerRightLayout.setBackgroundResource(isDarkMode ? R.drawable.applet_right_dark : R.drawable.applet_right);
//                }
            }
            return false;
        }
    };

    protected int layoutId() {
        return R.layout.layout_applet_activity;
    }
}
