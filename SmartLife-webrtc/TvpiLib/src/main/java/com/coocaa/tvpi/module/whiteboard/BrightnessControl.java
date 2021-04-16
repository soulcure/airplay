package com.coocaa.tvpi.module.whiteboard;

import android.content.Context;
import android.util.Log;

import com.coocaa.swaiotos.virtualinput.utils.BrightnessTools;
import com.coocaa.tvpi.module.io.HomeUIThread;

public class BrightnessControl {
    private Context context;
    private float mCurAppBright = 0;//当前屏幕亮度
    private final String TAG = "WBBrightness";

    public BrightnessControl(Context context) {
        this.context = context;
    }

    public void downBrightness() {
        delaySetAppBrightness();
    }

    public void resetBrightness() {
        resetDefaultBrightness();
    }

    /**
     * 15秒无操作降低屏幕亮度
     */
    private void delaySetAppBrightness() {
        HomeUIThread.removeTask(mBrightnessRunable);
        HomeUIThread.execute(15 * 1000, mBrightnessRunable);
    }

    private Runnable mBrightnessRunable = new Runnable() {
        @Override
        public void run() {
            mCurAppBright = BrightnessTools.getAppBrightness(context);
            Log.i(TAG, "BrightnessRunable mCurAppBright: " + mCurAppBright);
            //降低屏幕亮度为默认的1/10
            BrightnessTools.setAppBrightness(context, mCurAppBright / 10f);
        }
    };

    private void resetDefaultBrightness() {
        HomeUIThread.removeTask(mBrightnessRunable);
        float appBright = BrightnessTools.getAppBrightness(context);
        if (appBright != mCurAppBright) {
            Log.i(TAG, "resetDefaultBrightness --> curBri: " + appBright + "---lastBri: " + mCurAppBright);
            mCurAppBright = appBright;
            BrightnessTools.setAppBrightness(context, -1);
        }
    }
}
