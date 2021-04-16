package com.coocaa.tvpi.module.reversescreen;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;

import com.coocaa.smartscreen.data.channel.ReverseScreenParams;
import com.coocaa.smartscreen.utils.CmdUtil;
import com.swaiotos.skymirror.sdk.capture.MirManager;
import com.swaiotos.skymirror.sdk.reverse.IDrawListener;
import com.swaiotos.skymirror.sdk.reverse.IPlayerListener;


public class ReverseScreenTextureView extends TextureView {
    private static final String TAG = ReverseScreenTextureView.class.getSimpleName();

    private interface InitCallBack {
        void onInit();
    }

    private InitCallBack callBack;

    public ReverseScreenTextureView(Context context) {
        this(context, null, 0);
    }


    public ReverseScreenTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ReverseScreenTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setSurfaceTextureListener(new SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                if (callBack != null) {
                    callBack.onInit();
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

            }
        });


    }

    public synchronized void startReverseScreen(final IPlayerListener listener, final IDrawListener drawListener) {
        Log.d(TAG, "startReverse: ");
        if (getSurfaceTexture() != null) {
            doReverseScreen(listener,drawListener);
        } else {
            callBack = new InitCallBack() {
                @Override
                public void onInit() {
                    doReverseScreen(listener,drawListener);
                }
            };
        }
    }

    private void doReverseScreen(IPlayerListener listener, final IDrawListener drawListener) {
        Log.d(TAG, "doReverseScreen: ");
        MirManager.instance().startReverseScreen(new Surface(getSurfaceTexture()), listener,drawListener);
        CmdUtil.sendReveseScreenCmd(ReverseScreenParams.CMD.START_REVERSE.toString());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        MirManager.instance().sendMotionEvent(event);
        return true;
    }
}
