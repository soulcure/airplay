package com.coocaa.tvpi.module.local.document.fragment;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpilib.R;


/**
 * @ClassName ConnectDialogFragment
 * @Description 文档帮助视频弹框
 * @User luoxi
 * @Date 2020-12-2
 * @Version TODO (write something)
 */
public class BottomBaseDialogFragment extends DialogFragment {
    protected  String DIALOG_FRAGMENT_TAG = BottomBaseDialogFragment.class.getSimpleName();
    protected AppCompatActivity mActivity;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);

    }
    public BottomBaseDialogFragment(AppCompatActivity mActivity){
        this.mActivity = mActivity;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getDialog().getWindow().setWindowAnimations(R.style.animate_dialog);
//        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.color_black_a50)));
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().getWindow().getAttributes().windowAnimations = R.style.bottom_dialog_anim;
        StatusBarHelper.translucent(getDialog().getWindow());
        StatusBarHelper.setStatusBarLightMode(getDialog().getWindow());
    }

    protected void initViews(View view) {
    }

    @Override
    public void onStart() {
        super.onStart();
        // 设置dialog的layout
        if (getDialog() == null || getDialog().getWindow() == null || getActivity() == null) {
            return;
        }
        WindowManager.LayoutParams layoutParams = getDialog().getWindow().getAttributes();
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = Gravity.BOTTOM;
        getDialog().getWindow().setAttributes(layoutParams);
    }

    public void show() {
        DIALOG_FRAGMENT_TAG=getClass().getSimpleName()+"_"+hashCode();
        this.show(mActivity.getSupportFragmentManager(), DIALOG_FRAGMENT_TAG);
    }

    public void dismissDialog() {
        Fragment prev = getFragmentManager().findFragmentByTag(DIALOG_FRAGMENT_TAG);
        if (prev != null) {
            DialogFragment df = (DialogFragment) prev;
            df.dismissAllowingStateLoss();
        }
        Log.d("SmartApi", "dismissDialog, listener=");

    }
    //获取底部导航栏高度
    public static int getNavigationBarHeight(Activity activity) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Display display = activity.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            Point realSize = new Point();
            display.getSize(size);
            display.getRealSize(realSize);
            Resources resources = activity.getResources();
            int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            int height = resources.getDimensionPixelSize(resourceId);
            //超出系统默认的导航栏高度以上，则认为存在虚拟导航
            if ((realSize.y - size.y) > (height - 10)) {
                return height;
            }

            return 0;
        } else {
            boolean menu = ViewConfiguration.get(activity).hasPermanentMenuKey();
            boolean back = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
            if (menu || back) {
                return 0;
            } else {
                Resources resources = activity.getResources();
                int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
                int height = resources.getDimensionPixelSize(resourceId);
                return height;
            }
        }

    }
}
