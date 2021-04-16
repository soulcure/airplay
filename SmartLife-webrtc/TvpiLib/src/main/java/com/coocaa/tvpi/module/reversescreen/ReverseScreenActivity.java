package com.coocaa.tvpi.module.reversescreen;

import android.content.Context;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;

import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.data.channel.events.ReverScreenExitEvent;
import com.coocaa.smartscreen.utils.CmdUtil;
import com.coocaa.smartscreen.utils.NetworkUtils;
import com.coocaa.tvpi.broadcast.NetWorkStateReceiver;
import com.coocaa.tvpi.event.NetworkEvent;
import com.coocaa.tvpi.module.reversescreen.view.ExitView;
import com.coocaa.tvpilib.R;
import com.swaiotos.skymirror.sdk.capture.MirManager;
import com.swaiotos.skymirror.sdk.reverse.IDrawListener;
import com.swaiotos.skymirror.sdk.reverse.IPlayerListener;
import com.swaiotos.skymirror.sdk.reverse.PlayerDecoder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * 同屏控制
 */
public class ReverseScreenActivity extends AppCompatActivity {
    public static final String TAG = ReverseScreenActivity.class.getSimpleName();
    private ReverseScreenTextureView screenTextureView;
    private int deviceWidth;
    private int deviceHeight;
    private int orientation = Configuration.ORIENTATION_LANDSCAPE;

    private NetWorkStateReceiver netWorkStateReceiver;

    private IPlayerListener iPlayerListener = new IPlayerListener() {
        @Override
        public void onError(int i, String s) {
            Log.d(TAG, "IPlayerListener onError: " + i + s);
            ToastUtils.getInstance().showGlobalLong("同屏异常:" + s);
            finish();
        }
    };

    IDrawListener drawListener = new IDrawListener() {

        @Override
        public void setHW(final int w, final int h, int rotate, PlayerDecoder decoder) {
           Log.d(TAG, "setHW w:" + w + " h:" + h + " rotate:" + rotate);
//
//            //电视端分辨率最高1920*1080，假设：电视屏幕宽dw,高dh
//            // if( w > h ) ---- 手机横屏  电视需横屏显示，//理论全屏充满
//            //                                         check(w < 1080 )
//            // if( w < h)  ---- 手机竖屏  电视需竖屏显示，取 1920 和 1080 中最小值 1080 做为高，宽需等比例缩放
//            //                                         高1080 宽 （w）
//
//            if (h > w) { //设备竖屏,竖屏显示
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        //取电视宽高最小值
//                        if (deviceWidth > deviceHeight) {
//                            //电视画面高 = （电视屏幕宽和电视屏幕高的最小值）
//                            //电视画面宽 = 传入宽 * （传入高 / 电视屏幕高和电视屏幕高的最小值）
//                            //换算成16/9
//                            float desWH = (deviceHeight * 1.0f / h);
//                            int newWidth = (int) (w * desWH - w * desWH % 16);
//                            ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(newWidth, deviceHeight);
////                            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
//                            screenTextureView.setLayoutParams(layoutParams);
//                            screenTextureView.requestLayout();
//                        } else {//反之
//                            float desWH = (deviceWidth * 1.0f / w);
//                            int newHeight = (int) (h * desWH - w * desWH % 16);
//                            ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(deviceWidth, newHeight);
////                            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
//                            screenTextureView.setLayoutParams(layoutParams);
//                            screenTextureView.requestLayout();
//                        }
//                    }
//                });
//            }
//
//            if (w > h) {//设备横屏，横屏显示
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        //取电视宽高最小值
//                        if (DLNACommonUtil.checkPermission(ReverseScreenActivity.this)) {
//                            if (deviceWidth > deviceHeight) {
//                                float desWH = (deviceWidth * 1.0f / w);
//                                int newHeight = (int) (h * desWH - w * desWH % 16);
//                                ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(deviceWidth, newHeight);
////                                screenTextureView.setLayoutParams(layoutParams);
//                                screenTextureView.requestLayout();
//                            } else {//反之
//                                float desWH = (deviceHeight * 1.0f / h);
//                                int newWidth = (int) (w * desWH - w * desWH % 16);
//                                ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(newWidth, deviceHeight);
////                                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
//                                screenTextureView.setLayoutParams(layoutParams);
//                                screenTextureView.requestLayout();
//                            }
//                        } else {
//                            if (deviceWidth > deviceHeight) {
//                                ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(deviceWidth, deviceHeight - deviceHeight % 16);
////                                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
//                                screenTextureView.setLayoutParams(layoutParams);
//                                screenTextureView.requestLayout();
//                            } else {
//                                ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(deviceHeight, deviceWidth);
////                                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
//                                screenTextureView.setLayoutParams(layoutParams);
//                                screenTextureView.setRotation(270);
//                                screenTextureView.requestLayout();
//                            }
//                        }
//
//                    }
//                });
//            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reverse_screen);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);//Android4.4以上
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE|
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|
                            View.SYSTEM_UI_FLAG_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION|
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        deviceWidth = dm.widthPixels;
        deviceHeight = dm.heightPixels;
        if (deviceWidth > deviceHeight) {
            Log.d(TAG, "STATUS_LANDSCAPE");
            orientation = Configuration.ORIENTATION_LANDSCAPE;
        } else {
            Log.d(TAG, "STATUS_PORTAIT");
            orientation = Configuration.ORIENTATION_PORTRAIT;
        }
        Log.d(TAG, "deviceWidth :" + deviceWidth + ",deviceHeight:" + deviceHeight);


        initView();
        MirManager.instance().init(this,
                new MirManager.InitListener() {
                    @Override
                    public void success() {
                        screenTextureView.startReverseScreen(iPlayerListener, drawListener);
                    }

                    @Override
                    public void fail() {
                        ToastUtils.getInstance().showGlobalLong("同屏服务异常");
                        finish();
                    }
                });

        EventBus.getDefault().register(this);

        netWorkStateReceiver = new NetWorkStateReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netWorkStateReceiver, filter);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        unregisterReceiver(netWorkStateReceiver);

        MirManager.instance().destroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(NetworkEvent event) {
        Log.d(TAG, "onEvent: 收到网络变化");
        if (!NetworkUtils.isAvailable(this)) {
            ToastUtils.getInstance().showGlobalShort("网络不可用");
            finish();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ReverScreenExitEvent event) {
        Log.d(TAG, "onEvent: 退出同屏控制");
        ToastUtils.getInstance().showGlobalShort("电视正忙");
        finish();
    }


    private void initView() {
        screenTextureView = findViewById(R.id.surface);
        ExitView exitView = findViewById(R.id.tv_exit);
        exitView.setOnExitClickListener(new ExitView.ExitListener() {
            @Override
            public void onExitClick() {
                finish();
            }
        });

        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CmdUtil.sendKey(KeyEvent.KEYCODE_BACK);
            }
        });

        findViewById(R.id.iv_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CmdUtil.sendKey(KeyEvent.KEYCODE_HOME);
            }
        });

        findViewById(R.id.iv_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CmdUtil.sendKey(KeyEvent.KEYCODE_MENU);
            }
        });

        findViewById(R.id.iv_c).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CmdUtil.sendKey(962);
            }
        });
    }

    private void setNetWorkCallback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            // 请注意这里会有一个版本适配bug，所以请在这里添加非空判断
            if (connectivityManager != null) {
                connectivityManager.requestNetwork(new NetworkRequest.Builder().build(), new ConnectivityManager.NetworkCallback() {

                    /**
                     * 网络可用的回调
                     * */
                    @Override
                    public void onAvailable(Network network) {
                        super.onAvailable(network);
                        Log.e("lzp", "onAvailable");
                    }

                    /**
                     * 网络丢失的回调
                     * */
                    @Override
                    public void onLost(Network network) {
                        super.onLost(network);
                        Log.e("lzp", "onLost");
                    }

                    /**
                     * 当建立网络连接时，回调连接的属性
                     * */
                    @Override
                    public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
                        super.onLinkPropertiesChanged(network, linkProperties);
                        Log.e("lzp", "onLinkPropertiesChanged");
                    }

                    /**
                     *  按照官方的字面意思是，当我们的网络的某个能力发生了变化回调，那么也就是说可能会回调多次
                     *
                     *  之后在仔细的研究
                     * */
                    @Override
                    public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                        super.onCapabilitiesChanged(network, networkCapabilities);
                        Log.e("lzp", "onCapabilitiesChanged");
                    }

                    /**
                     * 在网络失去连接的时候回调，但是如果是一个生硬的断开，他可能不回调
                     * */
                    @Override
                    public void onLosing(Network network, int maxMsToLive) {
                        super.onLosing(network, maxMsToLive);
                        Log.e("lzp", "onLosing");
                    }

                    /**
                     * 按照官方注释的解释，是指如果在超时时间内都没有找到可用的网络时进行回调
                     * */
                    @Override
                    public void onUnavailable() {
                        super.onUnavailable();
                        Log.e("lzp", "onUnavailable");
                    }

                });
            }
        }
    }
}
