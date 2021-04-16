package com.coocaa.swaiotos.virtualinput.module.fragment;

import android.content.Context;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.cocaa.swaiotos.virtualinput.R;
import com.coocaa.publib.utils.SpUtil;
import com.coocaa.smartscreen.businessstate.object.BusinessState;
import com.coocaa.smartscreen.businessstate.object.User;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.data.businessstate.SceneConfigBean;
import com.coocaa.smartsdk.SmartApi;
import com.coocaa.swaiotos.virtualinput.event.GlobalEvent;
import com.coocaa.swaiotos.virtualinput.iot.GlobalIOT;
import com.coocaa.swaiotos.virtualinput.utils.BrightnessTools;
import com.coocaa.swaiotos.virtualinput.utils.DimensUtils;
import com.coocaa.swaiotos.virtualinput.utils.VirtualInputUtils;
import com.coocaa.tvpi.module.io.HomeUIThread;

import java.util.HashMap;
import java.util.Map;

import swaiotos.runtime.h5.core.os.H5RunType;
import swaiotos.sensor.client.ISmartApi;
import swaiotos.sensor.client.SensorClient;
import swaiotos.sensor.client.data.ClientBusinessInfo;
import swaiotos.sensor.touch.InputTouchView;

public class RPhotoAlbumFragment extends BaseLazyFragment {
    public static final String TAG = "PhotoAlbum";
    //根布局
    private RelativeLayout rootLayout;
    //旋转布局
    private RelativeLayout rlRotate;
    //接收并将event事件给SensorClient
    private InputTouchView inputTouchView;
    //实际转发event给dongle的对象
    private SensorClient client;
    //控制器中间文字
    private ImageView ivCenterText;
    //旋转图标
    private ImageView ivRotate;
    //旋转文字
    private ImageView ivRotateText;
    //旋转背景
    private ImageView ivRotateBg;
    //旋转角度
    private int rotate;
    private String owner;
    //调用系统震动
    private boolean vibrate;
    //调用震动间隔时间
    private static final long VIBRATE_DURATION = 100L;
    private Context context;
    private float mCurAppBright = 0;//当前屏幕亮度


    @Override
    protected View getContentView() {
        context = getActivity();
        if (context == null) return null;

        rootLayout = new RelativeLayout(context);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        rootLayout.setLayoutParams(layoutParams);

        int width = DimensUtils.getDeviceWidth(context) - DimensUtils.dp2Px(context, 20);
        ClientBusinessInfo clientBusinessInfo = new ClientBusinessInfo("ss-pic-control-client",
                "ss-clientID-UniversalMediaPlayer", "照片共享控制", 0, 0);
        clientBusinessInfo.protoVersion = 0;//增加版本拉平
        client = new SensorClient(context, clientBusinessInfo,
                VirtualInputUtils.getAccountInfo());
        client.setSmartApi(new ISmartApi() {
            @Override
            public boolean isSameWifi() {
                return SmartApi.isSameWifi();
            }

            @Override
            public void startConnectSameWifi() {
                SmartApi.startConnectSameWifi(H5RunType.RUNTIME_NETWORK_FORCE_LAN);
            }
        });
        inputTouchView = (InputTouchView) client.getView();
        inputTouchView.setNeedTwoFinger(true);
        inputTouchView.setBackground(getResources().getDrawable(R.drawable.bg_round_12_black_alpha20));
        RelativeLayout.LayoutParams viewParams = new RelativeLayout.LayoutParams(width,
                ViewGroup.LayoutParams.MATCH_PARENT);
        viewParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        rootLayout.addView(inputTouchView, viewParams);

        //控制器中间文字
        {
            ivCenterText = new ImageView(context);
            ivCenterText.setImageResource(R.drawable.photo_album_center_text);
            viewParams = new RelativeLayout.LayoutParams(DimensUtils.dp2Px(context, 180),
                    DimensUtils.dp2Px(context, 70));
            viewParams.topMargin = DimensUtils.dp2Px(context, 180);
            viewParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            viewParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            rootLayout.addView(ivCenterText, viewParams);
        }
        //旋转布局
        {
            rlRotate = new RelativeLayout(context);
            viewParams = new RelativeLayout.LayoutParams(DimensUtils.dp2Px(context, 115),
                    DimensUtils.dp2Px(context, 50));
            viewParams.bottomMargin = DimensUtils.dp2Px(context, 25);
            viewParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            viewParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            rootLayout.addView(rlRotate, viewParams);
        }
        //旋转背景
        {
            ivRotateBg = new ImageView(context);
            ivRotateBg.setBackground(getResources().getDrawable(R.drawable.bg_round_15_black));
            viewParams = new RelativeLayout.LayoutParams(DimensUtils.dp2Px(context, 95),
                    DimensUtils.dp2Px(context, 30));
            viewParams.topMargin = DimensUtils.dp2Px(context, 10);
            viewParams.leftMargin = DimensUtils.dp2Px(context, 10);
            rlRotate.addView(ivRotateBg, viewParams);
        }
        //旋转图标
        {
            ivRotate = new ImageView(context);
            ivRotate.setImageResource(R.drawable.photo_album_rotate);
            viewParams = new RelativeLayout.LayoutParams(
                    DimensUtils.dp2Px(context, 20), DimensUtils.dp2Px(context, 20));
            viewParams.leftMargin = DimensUtils.dp2Px(context, 20);
            viewParams.topMargin = DimensUtils.dp2Px(context, 15);
            rlRotate.addView(ivRotate, viewParams);
        }
        //旋转文字
        {
            ivRotateText = new ImageView(context);
            ivRotateText.setImageResource(R.drawable.photo_album_rotate_text);
            viewParams = new RelativeLayout.LayoutParams(DimensUtils.dp2Px(context, 50),
                    DimensUtils.dp2Px(context, 20));
            viewParams.rightMargin = DimensUtils.dp2Px(context, 20);
            viewParams.topMargin = DimensUtils.dp2Px(context, 15);
            viewParams.addRule(RelativeLayout.ALIGN_PARENT_END);
            rlRotate.addView(ivRotateText, viewParams);
        }

        rlRotate.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        ivRotate.setAlpha(0.5f);
                        ivRotateText.setAlpha(0.5f);
                        if (SmartApi.isSameWifi()) {
                            playVibrate();
                            GlobalIOT.iot.sendCmd("rotate", "image", String.valueOf(rotate += 90),
                                    SSConnectManager.TARGET_CLIENT_MEDIA_PLAYER, owner);
                            submitLog();
                        } else {
                            SmartApi.startConnectSameWifi(H5RunType.RUNTIME_NETWORK_FORCE_LAN);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        ivRotate.setAlpha(1f);
                        ivRotateText.setAlpha(1f);
                        break;
                }
                return true;
            }
        });

        rotate = 0;
        vibrate = SpUtil.getBoolean(context, SpUtil.Keys.REMOTE_VIBRATE, true);
        return rootLayout;
    }

    private void playVibrate() {
        if (vibrate) {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        resetDefaultBrightness();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (client != null) client.stop();
        resetDefaultBrightness();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (client != null) client.start();
        delaySetAppBrightness();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (client != null) client.stop();
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        Log.i(TAG, "onWindowFocusChanged: " + hasFocus);
        if (hasFocus) {
            delaySetAppBrightness();
        } else {
            resetDefaultBrightness();
        }
    }

    public void dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            resetDefaultBrightness();
        } else if (ev.getAction() == MotionEvent.ACTION_UP) {
            delaySetAppBrightness();
        }
    }

    @Override
    protected int getContentViewId() {
        return 0;
    }

    @Override
    public void setFragmentData(BusinessState stateBean, SceneConfigBean sceneConfigBean) {
        super.setFragmentData(stateBean, sceneConfigBean);
        if (stateBean.owner != null) {
            owner = User.encode(stateBean.owner);
        }
    }

    private void submitLog() {
        Map<String, String> params = new HashMap<>();
        params.put("applet_id", "IMAGE");
        params.put("applet_name", "相册共享");
        params.put("btn_name", "旋转");
        params.put("tab_name", "当前内容");
        GlobalEvent.onEvent("remote_btn_clicked", params);
    }

    private Runnable mBrightnessRunable = new Runnable() {
        @Override
        public void run() {
            mCurAppBright = BrightnessTools.getAppBrightness(getContext());
            Log.i(TAG, "BrightnessRunable mCurAppBright: " + mCurAppBright);
            //降低屏幕亮度为默认的1/10
            BrightnessTools.setAppBrightness(getContext(), mCurAppBright / 10f);
        }
    };

    /**
     * 15秒无操作降低屏幕亮度
     */
    private void delaySetAppBrightness() {
        HomeUIThread.removeTask(mBrightnessRunable);
        HomeUIThread.execute(15 * 1000, mBrightnessRunable);
    }

    /**
     * 恢复屏幕亮度（跟随系统）
     */
    private void resetDefaultBrightness() {
        HomeUIThread.removeTask(mBrightnessRunable);
        float appBright = BrightnessTools.getAppBrightness(getContext());
        if (appBright != mCurAppBright) {
            Log.i(TAG, "resetDefaultBrightness --> curBri: " + appBright + "---lastBri: " + mCurAppBright);
            mCurAppBright = appBright;
            BrightnessTools.setAppBrightness(getContext(), -1);
        }
    }
}
