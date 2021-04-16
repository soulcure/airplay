package com.coocaa.tvpi.module.homepager.view;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.signature.ObjectKey;
import com.coocaa.publib.base.GlideApp;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.data.channel.events.ScreenshotEvent;
import com.coocaa.smartscreen.utils.CmdUtil;
import com.coocaa.tvpilib.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * 截屏同屏
 * Created by songxing on 2020/3/13
 */
public class ReverseScreenImageView extends RelativeLayout {
    private static final String TAG = ReverseScreenImageView.class.getSimpleName();
    //    private static final long SCREEN_SHOT_TIME = 15 * 1000;
    private static final long SCREEN_SHOT_TIME = 3 * 1000;

    private ImageView ivScreenshot;
    private Handler screenshotHandler;
    private boolean isScreenshot = false;

    public ReverseScreenImageView(@NonNull Context context) {
        this(context, null, 0);
    }

    public ReverseScreenImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ReverseScreenImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
        EventBus.getDefault().register(this);
        Log.d(TAG, "ReverseScreenImageView: register");
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
        Log.d(TAG, "onDetachedFromWindow: unregister");
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_reverse_screen_imageview, this, true);
        ivScreenshot = findViewById(R.id.iv_revers_screen_image);
    }

    public void startScreenshot() {
        if (isScreenshot) {
            return;
        }
        Log.d(TAG, "startScreenshot");
        doScreenshot();
        isScreenshot = true;
        if (screenshotHandler == null) {
            screenshotHandler = new Handler(Looper.getMainLooper());
        }
        screenshotHandler.postDelayed(screenshotRunnable, SCREEN_SHOT_TIME);
    }


    public void stopScreenshot() {
        if (!isScreenshot) {
            return;
        }
        Log.d(TAG, "stopScreenshot");
        isScreenshot = false;
        if (screenshotHandler != null) {
            screenshotHandler.removeCallbacks(screenshotRunnable);
        }
    }

    private void doScreenshot() {
        Log.d(TAG, "doScreenshot");
        CmdUtil.sendScreenshot();
    }


    private Runnable screenshotRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isScreenshot) {
               return;
            }
            Log.d(TAG, "ScreenshotRunnable running:" + screenshotRunnable);
            doScreenshot();
            screenshotHandler.postDelayed(this, SCREEN_SHOT_TIME);
        }
    };

    public void refreshScreenshot(String s) {
        if (!isScreenshot) {
            return;
        }
        Log.d(TAG, "refreshScreenshot: " + s);
        GlideApp.with(getContext())
                .load(s)
                .signature(new ObjectKey(System.currentTimeMillis()))
                .dontAnimate()
                .fitCenter()
                .placeholder(ivScreenshot.getDrawable())
                .into(ivScreenshot);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ScreenshotEvent ScreenshotEvent) {
        if(!isScreenshot) {
            return;
        }
        Log.d(TAG, "ScreenshotEvent: " + ScreenshotEvent.url);
        refreshScreenshot(ScreenshotEvent.url);
    }
}
