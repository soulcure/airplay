package com.swaiotos.skymirror.sdk.capture;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.swaiotos.skymirror.sdk.R;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class DialogActivity extends Activity {

    private static final String TAG = "MirClientService";
    //TextView tv_msg;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O && isTranslucentOrFloating()) {
            fixOrientation();
        }
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.dimAmount = 0.0f; //设置窗口之外部分透明程度
        attributes.x = 0;
        attributes.y = 0;
        attributes.width = WindowManager.LayoutParams.MATCH_PARENT;
        attributes.height = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(attributes);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);

        setContentView(R.layout.activity_dialog);

        //Toast.makeText(this, "多屏互动开始!", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "DialogActivity onCreate");
    }


    private boolean fixOrientation() {
        try {
            Field field = Activity.class.getDeclaredField("mActivityInfo");
            field.setAccessible(true);
            ActivityInfo o = (ActivityInfo) field.get(this);
            o.screenOrientation = -1;
            field.setAccessible(false);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    private boolean isTranslucentOrFloating() {
        boolean isTranslucentOrFloating = false;
        try {
            int[] styleableRes = (int[]) Class.forName("com.android.internal.R$styleable").getField("Window").get(null);
            final TypedArray ta = obtainStyledAttributes(styleableRes);
            Method m = ActivityInfo.class.getMethod("isTranslucentOrFloating", TypedArray.class);
            m.setAccessible(true);
            isTranslucentOrFloating = (boolean) m.invoke(null, ta);
            m.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isTranslucentOrFloating;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "DialogActivity onStart");
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "DialogActivity onResume");
        finish();
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "DialogActivity onPause");
    }


    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "DialogActivity onStop");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "DialogActivity onDestroy");
    }
}
