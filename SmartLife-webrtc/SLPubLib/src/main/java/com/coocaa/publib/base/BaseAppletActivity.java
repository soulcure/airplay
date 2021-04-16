package com.coocaa.publib.base;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.coocaa.publib.R;
import com.coocaa.publib.utils.SpUtil;
import com.coocaa.publib.views.LoadingDialog;
import com.umeng.analytics.MobclickAgent;

import androidx.appcompat.app.AppCompatActivity;
import swaiotos.runtime.np.NPAppletActivity;


/**
 * Created by WHY on 2017/12/6.
 */

/*
 * Activity的基类
 */
public abstract class BaseAppletActivity extends NPAppletActivity {

    protected static String TAG = BaseActivity.class.getSimpleName();

    private static PowerManager.WakeLock mWakeLock = null;
    protected LoadingDialog mNecessaryLoadingDlg;

    private boolean mIsStarted = false;
    private boolean vibrate = false;
    private Runnable mTimeoutRunnable;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler(Looper.getMainLooper());
        AppManager.getInstance().addActivity(this);
        vibrate = SpUtil.getBoolean(this, SpUtil.Keys.REMOTE_VIBRATE, true);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mIsStarted = true;

        /* 如果不调用此方法，不仅会导致按照"几天不活跃"条件来推送失效，
         *  还将导致广播发送不成功以及设备描述红色等问题发生
         *  可以只在应用的主Activity中调用此方法，但是由于SDK的日志发送策略，
         *  有可能由于主activity的日志没有发送成功，而导致未统计到日活数据
         * */
//        PushAgent.getInstance(this).onAppStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // // [统计页面(仅有Activity的应用中SDK自动调用,不需要单独写。参数为页面名称,可自定义)]
//        MobclickAgent.onPageStart(TAG);

        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // [(仅有Activity的应用中SDK自动调用,不需要单独写)
        // 保证onPageEnd在onPause之前调用,因为onPause中会保存信息。参数页面名称,可自定义]
//        MobclickAgent.onPageEnd(TAG);

        MobclickAgent.onPause(this);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return super.dispatchTouchEvent(event);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mIsStarted = false;
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mNecessaryLoadingDlg != null) {
            mNecessaryLoadingDlg.dismiss();
            mNecessaryLoadingDlg = null;
        }
        AppManager.getInstance().removeActivity(this);
        stopTimeout();
    }

    protected boolean isShouldHideKeyboard(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0],
                    top = l[1],
                    bottom = top + v.getHeight(),
                    right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击EditText的事件，忽略它。
                return false;
            } else {
                return true;
            }
        }
        // 如果焦点不是EditText则忽略，这个发生在视图刚绘制完，第一个焦点不在EditText上，和用户用轨迹球选择其他的焦点
        return false;
    }

    /**
     * 获取InputMethodManager，隐藏软键盘
     *
     * @param token
     */
    protected void hideKeyboard(IBinder token) {
        if (token != null) {
            InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    protected void playVibrate() {
        if (vibrate) {
            Vibrator vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    public boolean onBackClicked() {
        return false;
    }


    /**
     * 用于有必须依赖的加载项的Activity。如果加载被框被取消，则关闭Activity。
     */
    public void showLoading(String title) {
        showLoading();
        mNecessaryLoadingDlg.setTitle(title);
    }

    /**
     * 用于有必须依赖的加载项的Activity。如果加载被框被取消，则关闭Activity。
     */
    public void showLoading(int titleId) {
        showLoading();
        mNecessaryLoadingDlg.setTitle(titleId);
    }

    /**
     * Loading Dialog被取消。
     *
     * @param dialog
     * @return
     */
    public boolean onLoadingCancelled(DialogInterface dialog) {
        return false;
    }

    /**
     * 显示Loading Dialog。
     */
    public void showLoading() {
        Log.d(TAG, "showLoading");
        if (mNecessaryLoadingDlg == null) {
            mNecessaryLoadingDlg = new LoadingDialog(this);
            mNecessaryLoadingDlg.setCancelable(true);
            mNecessaryLoadingDlg.setCanceledOnTouchOutside(false);
            mNecessaryLoadingDlg.setOnCancelListener(new DialogInterface.OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    Log.d(TAG, "showLoading.onCancel");
                    if (!onLoadingCancelled(dialog)) {
                        dialog.dismiss();

                        /*if (!AppManager.isExsitActivity(BaseActivity.this, HomeActivity2.class)) {
                            Intent i = new Intent(BaseActivity.this, HomeActivity2.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(i);
                        }*/
                        finish();
                    }
                }
            });
        }

        if (mIsStarted && !mNecessaryLoadingDlg.isShowing()) {
            mNecessaryLoadingDlg.show();
        }
    }

    public boolean isLoadingShow() {
        return mNecessaryLoadingDlg != null && mNecessaryLoadingDlg.isShowing();
    }

    /**
     * 关闭加载框。Activity可供使用了。
     */
    public void dismissLoading() {
        if (mNecessaryLoadingDlg != null) {
            mNecessaryLoadingDlg.dismiss();
            mNecessaryLoadingDlg = null;
        }
    }

    protected void setStatusBarTranslucent() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);//Android4.4以上
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    protected void setNoStatusBar() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }


    protected void setActivityAlphaIn() {
        overridePendingTransition(R.anim.dialog_alpha_enter, R.anim.dialog_alpha_outer);
    }

    protected void setActivityAlphaOut() {
        overridePendingTransition(R.anim.dialog_alpha_outer, R.anim.dialog_alpha_enter);
    }

    public static synchronized final void disableScreenSaver(Context context) {
        try {
            if (null == mWakeLock) {
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE | PowerManager.FULL_WAKE_LOCK, context.getClass().getName());  // TAG为 Your class name
                if (null != mWakeLock) {
                    mWakeLock.acquire();
                }
            }
        } catch (Exception e) {
        }
    }

    public static synchronized final void releaseScreenSaver() {
        try {
            if (mWakeLock != null) {
                mWakeLock.release();
                mWakeLock = null;
            }
        } catch (Exception e) {
        }
    }


    protected void startTimeout(final long timeout,Runnable timeoutRunnable) {
        this.mTimeoutRunnable = timeoutRunnable;
        if(mHandler != null) {
            mHandler.removeCallbacks(mTimeoutRunnable);
            mHandler.postDelayed(mTimeoutRunnable, timeout);
        }
    }

    protected void stopTimeout() {
        if(mHandler != null && mTimeoutRunnable != null) {
            mHandler.removeCallbacks(mTimeoutRunnable);
        }
    }
}
