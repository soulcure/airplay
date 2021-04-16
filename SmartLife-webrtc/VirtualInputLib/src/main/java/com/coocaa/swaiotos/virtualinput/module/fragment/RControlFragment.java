package com.coocaa.swaiotos.virtualinput.module.fragment;

import android.Manifest;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.cocaa.swaiotos.virtualinput.R;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.publib.views.SDialog;
import com.coocaa.smartscreen.businessstate.object.BusinessState;
import com.coocaa.smartscreen.data.businessstate.SceneConfigBean;
import com.coocaa.smartsdk.SmartApi;
import com.coocaa.smartsdk.object.ISmartDeviceInfo;
import com.coocaa.swaiotos.virtualinput.iot.GlobalIOT;
import com.coocaa.swaiotos.virtualinput.module.view.siriwave.VoiceView;
import com.coocaa.swaiotos.virtualinput.utils.VirtualInputUtils;
import com.coocaa.swaiotos.virtualinput.utils.permission.PermissionListener;
import com.coocaa.swaiotos.virtualinput.utils.permission.PermissionsUtil;
import com.coocaa.tvpi.module.io.HomeUIThread;
import com.google.gson.Gson;
import com.skyworth.ai.speech.svs.SVSSDKProxy;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import swaiotos.runtime.h5.core.os.H5RunType;

/**
 * @ClassName RControlFragment
 * @Description TODO (write something)
 * @User heni
 * @Date 2020/12/17
 */
public class RControlFragment extends Fragment {
    private static final String TAG = RControlFragment.class.getSimpleName();
    private View mView;
    private ImageView directionIV, centerIV;
    private View powerBtn, volumeDownBtn, volumeUpBtn, signalBtn, homeBtn, backBtn, menuBtn, voiceBtn;
    private RelativeLayout topLayout, centerLayout;
    private RelativeLayout backLayout;
    private VoiceView voiceView;

    private SVSSDKProxy svssdkProxy;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.remote_controller_fragment, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        initVoice();
        initListener();
    }


    @Override
    public void onResume() {
        super.onResume();

        if(isTv()){
            powerBtn.setVisibility(View.VISIBLE);
        }else {
            powerBtn.setVisibility(View.GONE);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (svssdkProxy != null) {
            svssdkProxy.stopListening();
            svssdkProxy.cancelListening();
            svssdkProxy = null;
        }
    }

    private void initView() {
        //control
        centerIV = mView.findViewById(R.id.center_iv);
        directionIV = mView.findViewById(R.id.direction_iv);

        powerBtn = mView.findViewById(R.id.power_off);
        volumeDownBtn = mView.findViewById(R.id.volume_down);
        volumeUpBtn = mView.findViewById(R.id.volume_up);
        signalBtn = mView.findViewById(R.id.signal);
        homeBtn = mView.findViewById(R.id.home);
        backBtn = mView.findViewById(R.id.back);
        menuBtn = mView.findViewById(R.id.menu);
        voiceBtn = mView.findViewById(R.id.voice);
        voiceView = mView.findViewById(R.id.remote_view_voice_view);

        topLayout = mView.findViewById(R.id.top_layout);
        centerLayout = mView.findViewById(R.id.remote_view_center_layout);
        backLayout = mView.findViewById(R.id.back_layout);
    }

    private void initListener() {
        powerBtn.setOnTouchListener(mOnTouchListener);
        volumeDownBtn.setOnTouchListener(mOnTouchListener);
        volumeUpBtn.setOnTouchListener(mOnTouchListener);
        signalBtn.setOnTouchListener(mOnTouchListener);
        homeBtn.setOnTouchListener(mOnTouchListener);
        backBtn.setOnTouchListener(mOnTouchListener);
        menuBtn.setOnTouchListener(mOnTouchListener);
        voiceBtn.setOnTouchListener(mOnTouchListener);
        centerIV.setOnTouchListener(mOnTouchListener);
        directionIV.setOnTouchListener(onDirectionTouchListener);
    }

    private void initVoice() {
        if (!SmartApi.isDeviceConnect()) {
            ToastUtils.getInstance().showGlobalLong("请连接设备");
            return;
        }
        ISmartDeviceInfo info = SmartApi.getConnectDeviceInfo();
        if (info == null) {
            ToastUtils.getInstance().showGlobalLong("请连接正确设备");
            return;
        }
        Log.e(TAG, "initVoice: " + info.toString());
        svssdkProxy = new SVSSDKProxy
                .Builder()
                .setContext(getContext())
//                //被绑定电视的系统版本
//                .setClientVersion(info.cTcVersion)
//                //被绑定电视的mac地址
//                .setMac(info.MAC)
//                //被绑定电视的mac机芯
//                .setModel(info.mChip)
//                //被绑定电视的mac机型
//                .setType(info.mModel)
                //被绑定电视的激活id，当激活id为空时，会出现异常。
                .setSn(info.deviceId)
                .setSVSSDKCallBack(sVSSDKCallBack)
                .createSVSSDKProxy();
    }

    private void sendKeyEvent(int keyCode) {
        VirtualInputUtils.playVibrate();
        GlobalIOT.iot.sendKeyEvent(keyCode, KeyEvent.ACTION_DOWN);
    }

    private boolean isTv() {
        boolean result = false;
        ISmartDeviceInfo deviceInfo = SmartApi.getConnectDeviceInfo();
        if (deviceInfo != null) {
            result = "tv".equalsIgnoreCase(deviceInfo.zpRegisterType);
            Log.d(TAG, "isTv: " + result);
            return result;
        }
        return result;
    }

    private void startRecord() {
//        viewControlHanlder.sendEmptyMessageDelayed(LISTEN_CHECK, 10000);
//        viewControlHanlder.sendEmptyMessageDelayed(LISTEN_LIMIT, 30000);
        PermissionsUtil.getInstance().requestPermission(getContext(), new PermissionListener() {
            @Override
            public void permissionGranted(String[] permission) {
                HashMap<String, Object> params = new HashMap<>();
                params.put("isVad", false);
                //关闭自动语音发送
                if (svssdkProxy != null) {
                    Log.d(TAG, "startRecord: ");
                    svssdkProxy.startListening(params);
                }
            }

            @Override
            public void permissionDenied(String[] permission) {

            }
        }, Manifest.permission.RECORD_AUDIO);
    }

    View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            //发布会需求，不在同一wifi，不响应点击。防止其他人不在同一wifi误点
            if (!SmartApi.isSameWifi()) {
                SmartApi.startConnectSameWifi(H5RunType.RUNTIME_NETWORK_FORCE_LAN);
                return true;
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (v == powerBtn) {
                        powerBtn.setBackgroundResource(R.drawable.icon_remote_power_off_touch);
                        showConfirmDialog();
                    } else if (v == volumeDownBtn) {
                        mView.findViewById(R.id.volume_up_down).setBackgroundResource(R.drawable.icon_remote_volume_down);
                        sendKeyEvent(KeyEvent.KEYCODE_VOLUME_DOWN);
                    } else if (v == volumeUpBtn) {
                        mView.findViewById(R.id.volume_up_down).setBackgroundResource(R.drawable.icon_remote_volume_up);
                        sendKeyEvent(KeyEvent.KEYCODE_VOLUME_UP);
                    } else if (v == signalBtn) {
                        signalBtn.setBackgroundResource(R.drawable.icon_remote_signal_touch);
                        //如果是tv，发信号源键，如果是dongle，发962
                        if (isTv()) {
                            sendKeyEvent(KeyEvent.KEYCODE_TV_INPUT);
                        } else {
                            sendKeyEvent(962);
                        }
                    } else if (v == homeBtn) {
                        homeBtn.setBackgroundResource(R.drawable.icon_remote_home_touch);
                        sendKeyEvent(KeyEvent.KEYCODE_HOME);
                    } else if (v == backBtn) {
                        backBtn.setBackgroundResource(R.drawable.icon_remote_back_touch);
                        sendKeyEvent(KeyEvent.KEYCODE_BACK);
                    } else if (v == menuBtn) {
                        menuBtn.setBackgroundResource(R.drawable.icon_remote_settings_touch);
                        sendKeyEvent(KeyEvent.KEYCODE_MENU);
                    } else if (v == centerIV) {
                        centerIV.setBackgroundResource(R.drawable.bg_remote_btn_selected);
                        sendKeyEvent(KeyEvent.KEYCODE_DPAD_CENTER);
                    } else if (v == voiceBtn) {
                        voiceBtn.setBackgroundResource(R.drawable.icon_remote_voice_touch);
                        voiceViewShow();
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    if (v == powerBtn) {
                        powerBtn.setBackgroundResource(R.drawable.icon_remote_power_off);
                    } else if (v == volumeDownBtn) {
                        mView.findViewById(R.id.volume_up_down).setBackgroundResource(R.drawable.icon_remote_volume);
                    } else if (v == volumeUpBtn) {
                        mView.findViewById(R.id.volume_up_down).setBackgroundResource(R.drawable.icon_remote_volume);
                    } else if (v == signalBtn) {
                        signalBtn.setBackgroundResource(R.drawable.icon_remote_signal);
                    } else if (v == homeBtn) {
                        homeBtn.setBackgroundResource(R.drawable.icon_remote_home);
                    } else if (v == backBtn) {
                        backBtn.setBackgroundResource(R.drawable.icon_remote_back);
                    } else if (v == menuBtn) {
                        menuBtn.setBackgroundResource(R.drawable.icon_remote_settings);
                    } else if (v == centerIV) {
                        centerIV.setBackgroundResource(R.drawable.bg_remote_btn_normal);
                    } else if (v == voiceBtn) {
                        voiceBtn.setBackgroundResource(R.drawable.icon_remote_voice);
                        voiceViewHind();
                    }
                    return true;
            }
            return false;
        }
    };

    private void showConfirmDialog() {

        ISmartDeviceInfo deviceInfo = SmartApi.getConnectDeviceInfo();
        if (deviceInfo == null) {
            ToastUtils.getInstance().showGlobalShort("请先连接设备");
            return;
        }


        new SDialog(getContext(), "是否确定关闭电视", "取消", "关闭",
                new SDialog.SDialog2Listener() {
                    @Override
                    public void onClick(boolean left, View view) {
                        if (!left) {
                            sendKeyEvent(KeyEvent.KEYCODE_POWER);
                        }
                    }
                }).show();

    }

    private void voiceViewShow() {
        topLayout.setVisibility(View.INVISIBLE);
        centerLayout.setVisibility(View.INVISIBLE);
        backLayout.setVisibility(View.INVISIBLE);
        voiceView.setVisibility(View.VISIBLE);
        voiceView.show();
        voiceView.showWaveInAnim();
        startRecord();
    }

    private void voiceViewHind() {
        voiceView.showWaveOutAnim();
        voiceView.setVoiceCallback(new VoiceView.VoiceCallback() {
            @Override
            public void onHide() {
                topLayout.setVisibility(View.VISIBLE);
                centerLayout.setVisibility(View.VISIBLE);
                backLayout.setVisibility(View.VISIBLE);
            }
        });
        voiceView.hide();
        svssdkProxy.stopListening();
    }

    View.OnTouchListener onDirectionTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            //发布会需求，不在同一wifi，不响应点击。防止其他人不在同一wifi误点
            if (!SmartApi.isSameWifi()) {
                SmartApi.startConnectSameWifi(H5RunType.RUNTIME_NETWORK_FORCE_LAN);
                return true;
            }

            float x = event.getX();
            float y = event.getY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.d(TAG, "ACTION_DOWN: ");
                    directionDown(x, y);
                    VirtualInputUtils.playVibrate();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    Log.d(TAG, "ACTION_MOVE: ");
                    return true;
                case MotionEvent.ACTION_UP:
                    Log.d(TAG, "ACTION_UP: ");
                    directionIV.setBackgroundResource(R.drawable.bg_remote_direction);
                    return true;
            }
            return false;
        }
    };

    private void directionDown(float x, float y) {
        int w = directionIV.getWidth();
        int h = directionIV.getHeight();
        if ((y / x) < (h / w) && (y + h * x / w) < h) {
            Log.d(TAG, "onClick: 上");
            directionIV.setBackgroundResource(R.drawable.bg_remote_direction_up);
            sendKeyEvent(KeyEvent.KEYCODE_DPAD_UP);
        } else if ((y / x) > (h / w) && (y + h * x / w) > h) {
            Log.d(TAG, "onClick: 下");
            directionIV.setBackgroundResource(R.drawable.bg_remote_direction_down);
            sendKeyEvent(KeyEvent.KEYCODE_DPAD_DOWN);
        } else if ((y / x) > (h / w) && (y + h * x / w) < h) {
            Log.d(TAG, "onClick: 左");
            directionIV.setBackgroundResource(R.drawable.bg_remote_direction_left);
            sendKeyEvent(KeyEvent.KEYCODE_DPAD_LEFT);
        } else if ((y / x) < (h / w) && (y + h * x / w) > h) {
            Log.d(TAG, "onClick: 右");
            directionIV.setBackgroundResource(R.drawable.bg_remote_direction_right);
            sendKeyEvent(KeyEvent.KEYCODE_DPAD_RIGHT);
        }
    }

    private void updateContent(final String content) {
        HomeUIThread.execute(new Runnable() {
            @Override
            public void run() {
                if (TextUtils.isEmpty(content)) {
                    return;
                }
                voiceView.hindTips();
                voiceView.setVoiceContent(content);
            }
        });
    }

    /*private void submitClickEvent(String btnName) {
        HashMap<String, String> map = new HashMap<>();
        if (mSceneConfigBean != null) {
            map.put("applet_id", mSceneConfigBean.id);
            map.put("applet_name", mSceneConfigBean.appletName);
        }
        map.put("btn_name", btnName);
        map.put("tab_name", "控制中心");
        GlobalEvent.onEvent("remote_btn_clicked", map);
    }

    private void submitDragEvent() {
        HashMap<String, String> map = new HashMap<>();
        if (mSceneConfigBean != null) {
            map.put("applet_id", mSceneConfigBean.id);
            map.put("applet_name", mSceneConfigBean.appletName);
        }
        map.put("tab_name", "控制中心");
        GlobalEvent.onEvent("remote_drag_progress", map);
    }*/

    private SVSSDKProxy.ISVSSDKCallBack sVSSDKCallBack = new SVSSDKProxy.ISVSSDKCallBack() {
        //开始录音
        @Override
        public void onBeginningOfSpeech() {
            Log.d(TAG, "onBeginningOfSpeech: ");
        }

        //录音大小 db 只回调
        @Override
        public void onRmsChanged(float v) {
            Log.d(TAG, "onRmsChanged: " + (int) v);
            if (voiceView != null) {
                voiceView.setVolume((int) v);
            }
        }

        //停止录音
        @Override
        public void onEndOfSpeech() {
            Log.d(TAG, "onEndOfSpeech: ");
        }

        //语音识别局部识别结果
        @Override
        public void onPartialResults(String s) {
            Log.d(TAG, "onPartialResults: " + s);
            updateContent(s + "............");
        }

        //语音识别最终识别结果
        @Override
        public void onResults(String s) {
            Log.d(TAG, "onResults: " + s);
            updateContent(s);
        }

        /**
         * 语音识别过程中的异常错误码
         * ISpeechRecognizer.ERROR_AUDIO 表示声音采集出错.
         * ISpeechRecognizer.ERROR_NETWORK_TIMEOUT 网络超时错误.1
         * ISpeechRecognizer.ERROR_NO_MATCH 没有识别到语音结果.
         */
        @Override
        public void onError(int i) {
        }

        /** push 语音识别结果到电视端的回调。
         *  true 表示 成功 push 到后台服务器，但是不能保证电视可以正常收到消息。
         *  false 表示 没有 push 到后台服务器。
         */
        @Override
        public void onPushResult(boolean isSucceed) {
            Log.d(TAG, "onPushResult: " + isSucceed);
        }
    };


}
