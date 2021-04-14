package com.swaiotos.skymirror.sdk.reverse;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.swaiotos.skymirror.sdk.R;
import com.swaiotos.skymirror.sdk.capture.MirManager;

/**
 * @ClassName: PlayerActivity
 * @Description: java类作用描述
 * @Author: lfz
 * @Date: 2020/4/15 9:38
 */
public class PlayerActivity extends Activity implements TextureView.SurfaceTextureListener {

    private String TAG = PlayerActivity.class.getSimpleName();

    private static IPlayerInitListener sInitListener;

    private TextureView textureView;
    private int deviceWidth;
    private int deviceHeight;
    private int mViewWidth = 1080;
    private int mViewHeight = 1920;

    private Context mContext;


    private interface InitCallBack {
        void onInit();
    }

    private InitCallBack callBack;

    public static void obtainPlayer(Context context, IPlayerInitListener listener) {
        sInitListener = listener;
        Intent intent = new Intent(context, PlayerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


    IPlayerListener playerListener = new IPlayerListener() {
        @Override
        public void onError(int code, String errorMessage) {
            Log.e(TAG, "onError:" + code + "&errorMessage:" + errorMessage);
            Toast.makeText(mContext, errorMessage, Toast.LENGTH_SHORT).show();
            finish();
        }
    };


    IDrawListener drawListener = new IDrawListener() {

        @Override
        public void setHW(final int w, final int h, int rotate, PlayerDecoder decoder) {
            Log.d(TAG, "setUIHw w " + w + " h " + h + " angle " + rotate);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //取电视宽高最小值
                    if (w * deviceHeight > h * deviceWidth) {//宽度填满，上下留黑边
                        int newHeight = (h * deviceWidth) / w;
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) textureView.getLayoutParams();
                        params.width = deviceWidth;
                        params.height = newHeight;
                        textureView.setLayoutParams(params);
                        textureView.requestLayout();
                    } else {//高度填满，左右留黑边
                        int newWidth = (w * deviceHeight) / h;
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) textureView.getLayoutParams();
                        params.width = newWidth;
                        params.height = deviceHeight;
                        textureView.setLayoutParams(params);
                        textureView.requestLayout();
                    }
                }
            });
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_player);

        mContext = this;
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        deviceWidth = dm.widthPixels;
        deviceHeight = dm.heightPixels;

        Log.d(TAG, "deviceWidth :" + deviceWidth + ",deviceHeight:" + deviceHeight);


        textureView = findViewById(R.id.playerView);
        textureView.setSurfaceTextureListener(this);
        textureView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.e(TAG, "onTouch: 11111111 --- " + event.getAction());
                return sendMotionEvent(v, event);
            }
        });

        MirManager.instance().init(this,
                new MirManager.InitListener() {
                    @Override
                    public void success() {
                        startReverseScreen();
                    }

                    @Override
                    public void fail() {
                        finish();
                        Toast.makeText(mContext, "解码服务绑定失败", Toast.LENGTH_SHORT).show();
                    }
                });

    }


    @Override
    protected void onResume() {
        super.onResume();
        MirManager.instance().setReverseRunning(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MirManager.instance().setReverseRunning(false);
        MirManager.instance().stopAll(this);
    }

    private void doReverseScreen() {
        Log.d(TAG, "doReverseScreen...");
        MirManager.instance().setPlayerInitListener(sInitListener);

        Surface surface = new Surface(textureView.getSurfaceTexture());
        MirManager.instance().startReverseScreen(surface, playerListener, drawListener);
    }


    public synchronized void startReverseScreen() {
        Log.d(TAG, "startReverseScreen...");

        if (textureView.getSurfaceTexture() != null) {
            doReverseScreen();
        } else {
            callBack = new InitCallBack() {
                @Override
                public void onInit() {
                    doReverseScreen();
                }
            };
        }
    }


    private boolean sendMotionEvent(View v, MotionEvent motionEvent) {
        MirManager.instance().sendMotionEvent(motionEvent);//send
        return true;
    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.e(TAG, "onSurfaceTextureAvailable: " + width + " ----- " + height);

        mViewWidth = width;
        mViewHeight = height;

        if (callBack != null) {
            callBack.onInit();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.e(TAG, "onSurfaceTextureSizeChanged: " + width + " ----- " + height);
        mViewWidth = width;
        mViewHeight = height;
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }


    private void ChangeLayoutOnBroadCast() {
        runOnUiThread(new Runnable() {
            public void run() {
                int w = mViewWidth;
                int h = mViewHeight;
                textureView.setRotation(270);
                textureView.setScaleX((float) mViewHeight / (float) mViewWidth);//1080/1920            1920 resize to 1080 right
                textureView.setScaleY((float) mViewWidth / (float) mViewHeight);//1920/886
                Log.d(TAG, "ChangeLayoutOnBroadCast initial w " + mViewWidth + " h " + mViewHeight);
                setUILayout(w, h);
            }
        });
    }

    public void setUILayout(int w, int h) {

    }


    @Override
    protected void onStop() {
        super.onStop();
        MirManager.instance().stopReverseScreen();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MirManager.instance().destroy();
        MirManager.instance().stopAll(this);
    }


    /**
     * 上次点击返回键的时间
     */
    private long lastBackPressTime = -1L;


    @Override
    public void onBackPressed() {
        long currentTIme = System.currentTimeMillis();
        if (lastBackPressTime == -1L || currentTIme - lastBackPressTime >= 2000) {
            // 显示提示信息
            showBackPressTip();
            // 记录时间
            lastBackPressTime = currentTIme;
        } else {
            //退出应用
            finish();
        }
    }

    private void showBackPressTip() {
        Toast.makeText(this, "再按一次退出屏幕镜像", Toast.LENGTH_SHORT).show();
    }
}
