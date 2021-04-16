package com.coocaa.swaiotos.virtualinput.module.fragment;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cocaa.swaiotos.virtualinput.R;
import com.coocaa.publib.utils.SpUtil;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartsdk.SmartApi;
import com.coocaa.smartsdk.object.ISmartDeviceInfo;
import com.coocaa.swaiotos.virtualinput.iot.GlobalIOT;
import com.coocaa.swaiotos.virtualinput.utils.permission.PermissionListener;
import com.coocaa.swaiotos.virtualinput.utils.permission.PermissionsUtil;
import com.swaiot.webrtcc.sound.WebRTCSoundManager;

import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.ss.channel.im.IMMessageCallback;
import swaiotos.runtime.h5.H5ChannelInstance;


public class RMicroFragment extends Fragment {

    private static final String TAG = RMicroFragment.class.getSimpleName();

    private static final int STATE_NORMAL = 0;
    //未镜像
    private static final int STATE_CONNECTING = 1;
    //镜像连接中
    private static final int STATE_MIRRORING = 2;
    //正在镜像
    private static final long VIBRATE_DURATION = 100L;
    //震动时间

    private int mirrorState = STATE_NORMAL;
    private String TARGET = "com.coocaa.webrtc.airplay.voice";
    private View mView;
    private Button btnMicroSwitch;
    private ImageView imgVolumeAdd;
    private ImageView imgVolumeSubtract;
    private boolean isSay = false;
    private boolean vibrate;
    private ImageView imgMicroState;

    private final WebRTCSoundManager.SenderImpl sender = new WebRTCSoundManager.SenderImpl() {
        @Override
        public void onSend(String content) {
            Log.d(TAG, "WebRTCSoundManager onSend=" + content);
            H5ChannelInstance.getSingleton().sendWebRTCVoice(content);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.remote_micro_fragment, container, false);
        initView();
        initListener();
        return mView;
    }

    private void requestPermission() {
        PermissionsUtil.getInstance().requestPermission(getContext(), new PermissionListener() {
            @Override
            public void permissionGranted(String[] permission) {

            }

            @Override
            public void permissionDenied(String[] permission) {

            }
        }, Manifest.permission.RECORD_AUDIO);
    }

    private void initWebRtc() {

//        WebRTCSoundManager.instance().init(getActivity(), new WebRTCSoundManager.InitListener() {
//            @Override
//            public void success() {
//                Log.d(TAG, "success: " );
//            }
//
//            @Override
//            public void fail() {
//                Log.d(TAG, "fail: ");
//            }
//        });

    }

    private void initView() {
        btnMicroSwitch = mView.findViewById(R.id.btn_micro_switch);
        imgMicroState = mView.findViewById(R.id.micro_state_img);
        imgVolumeSubtract = mView.findViewById(R.id.volume_subtract_img);
        imgVolumeAdd = mView.findViewById(R.id.volume_add_img);
        vibrate = SpUtil.getBoolean(getContext(), SpUtil.Keys.REMOTE_VIBRATE, true);
        if (WebRTCSoundManager.instance() == null) {
            return;
        }
        try {
            if (WebRTCSoundManager.instance().isStart()) {
                imgMicroState.setImageResource(R.drawable.icon_micro_on);
                btnMicroSwitch.setText("关闭扩音器");
                isSay = true;
            } else {
                imgMicroState.setImageResource(R.drawable.icon_micro_off);
                btnMicroSwitch.setText("打开扩音器");
            }
        } catch (Exception e) {
            //initWebRtc();
            imgMicroState.setImageResource(R.drawable.icon_micro_off);
            btnMicroSwitch.setText("打开扩音器");
        }

    }

    private void initListener() {
        btnMicroSwitch.setOnClickListener(btnMicroSwitchListener);
        imgVolumeSubtract.setOnClickListener(btnVolumeSubListener);
        imgVolumeAdd.setOnClickListener(btnVolumeAddListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void startRecord() {
        WebRTCSoundManager.instance().init(getActivity(), new WebRTCSoundManager.InitListener() {
            @Override
            public void success() {
                WebRTCSoundManager.instance().setSender(sender);
                WebRTCSoundManager.instance().setResult(result);
                WebRTCSoundManager.instance().start();  //开始麦克风
                ToastUtils.getInstance().showGlobalShort("初始化成功");
                isSay = true;
            }

            @Override
            public void fail() {
                Log.d(TAG, "fail: ");
            }
        });
    }

    private void stopRecord() {
        WebRTCSoundManager.instance().stop();
        WebRTCSoundManager.instance().destroy();
    }

    private void playVibrate() {
        if (vibrate) {
            Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    private final View.OnClickListener btnMicroSwitchListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            requestPermission();
            playVibrate();
            if (isSay) {
                stopRecord();
                isSay = false;
                imgMicroState.setImageResource(R.drawable.icon_micro_off);
                btnMicroSwitch.setText("打开扩音器");
            } else {
                startRecord();
                imgMicroState.setImageResource(R.drawable.icon_micro_on);
                btnMicroSwitch.setText("关闭扩音器");
            }
        }
    };

    private final View.OnClickListener btnVolumeAddListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            sendKeyEvent(KeyEvent.KEYCODE_VOLUME_UP);
        }
    };

    private final View.OnClickListener btnVolumeSubListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            sendKeyEvent(KeyEvent.KEYCODE_VOLUME_DOWN);
        }
    };

    private void sendKeyEvent(int keyCode) {
        playVibrate();
        GlobalIOT.iot.sendKeyEvent(keyCode, KeyEvent.ACTION_DOWN);
    }

    private final WebRTCSoundManager.WebRtcResult result = new WebRTCSoundManager.WebRtcResult() {
        @Override
        public void onResult(int i, String s) {
            if (i == 0) {  //成功
                mirrorState = STATE_MIRRORING;
                ToastUtils.getInstance().showGlobalLong("扩音启动");
                Log.d(TAG, ": STATE_MIRRORING");
            } else if (i == 1) { //正在连接
                mirrorState = STATE_CONNECTING;
                Log.d(TAG, ": STATE_CONNECTING");
            } else {  //失败
                mirrorState = STATE_NORMAL;
                Log.d(TAG, ": STATE_NORMAL");
            }
        }
    };

}
