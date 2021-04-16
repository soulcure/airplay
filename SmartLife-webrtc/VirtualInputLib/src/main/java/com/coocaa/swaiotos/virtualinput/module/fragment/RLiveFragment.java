package com.coocaa.swaiotos.virtualinput.module.fragment;

import android.content.Context;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cocaa.swaiotos.virtualinput.R;
import com.coocaa.smartscreen.businessstate.object.BusinessState;
import com.coocaa.smartscreen.data.businessstate.SceneConfigBean;
import com.coocaa.smartsdk.SmartApi;
import com.coocaa.swaiotos.virtualinput.action.GlobalAction;
import com.coocaa.swaiotos.virtualinput.iot.GlobalIOT;
import com.coocaa.swaiotos.virtualinput.module.view.TouchControlView;

import swaiotos.runtime.h5.core.os.H5RunType;

public class RLiveFragment extends BaseLazyFragment implements TouchControlView.OnSlideCtrlListener {

    private View mView;
    private View liveListView;
    private TouchControlView mTouchControlView;
    private View upperPartView;
    private View lowerPartView;
    private ImageView volumeAdd;
    private ImageView volumeReduce;
    private TextView mute;

    private BusinessState mBusinessState;
    private SceneConfigBean mSceneConfigBean;
    private Vibrator vibrator;
    private long VIBRATE_DURATION = 60L;

    @Override
    protected int getContentViewId() {
        return R.layout.remote_live_fragment;
    }

    @Override
    protected void initView(View view) {
        super.initView(view);
        mView = view;
        liveListView = view.findViewById(R.id.live_list_layout);
        upperPartView = view.findViewById(R.id.upper_part);
        lowerPartView = view.findViewById(R.id.lower_part);
        volumeAdd = view.findViewById(R.id.volume_add_img);
        volumeReduce = view.findViewById(R.id.volume_subtract_img);
        mute = view.findViewById(R.id.mute_img);
        mTouchControlView = view.findViewById(R.id.live_touch_rl);
    }

    @Override
    protected void initEvent() {
        super.initEvent();
        liveListView.setOnClickListener(viewClickListener);
        upperPartView.setOnClickListener(viewClickListener);
        lowerPartView.setOnClickListener(viewClickListener);
        mTouchControlView.setOnSlideCtrlListener(this);
        volumeAdd.setOnClickListener(volumeClickListener);
        volumeReduce.setOnClickListener(volumeClickListener);
        mute.setOnClickListener(volumeClickListener);
    }

    @Override
    public void setFragmentData(BusinessState stateBean, SceneConfigBean sceneConfigBean) {
        super.setFragmentData(stateBean, sceneConfigBean);
        mSceneConfigBean = sceneConfigBean;
        mBusinessState = stateBean;
    }

    private View.OnClickListener viewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mSceneConfigBean != null) {
                if (v == liveListView) {
                    //发布会需求，不在同一wifi，弹连接wifi弹窗，防止被误点
                    if (!SmartApi.isSameWifi()) {
                        SmartApi.startConnectSameWifi(H5RunType.RUNTIME_NETWORK_FORCE_LAN);
                        return;
                    }

                    if (!TextUtils.isEmpty(mSceneConfigBean.appletUri)) {
                        GlobalAction.action.startActivity(getContext(), mSceneConfigBean.appletUri);
                        getActivity().finish();
                    }
                } else if (v == upperPartView) {
                    sendChangeCmd(true);
                    playVibrate();
                } else if (v == lowerPartView) {
                    sendChangeCmd(false);
                    playVibrate();
                }
            }
        }
    };

    private View.OnClickListener volumeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //发布会需求，不在同一wifi，弹连接wifi弹窗，防止被误点
            if (!SmartApi.isSameWifi()) {
                SmartApi.startConnectSameWifi(H5RunType.RUNTIME_NETWORK_FORCE_LAN);
                return;
            }

            if (v == volumeReduce) {
                sendKeyEvent(KeyEvent.KEYCODE_VOLUME_DOWN);
            } else if (v == volumeAdd) {
                sendKeyEvent(KeyEvent.KEYCODE_VOLUME_UP);
            } else if (v == mute) {
                sendKeyEvent(KeyEvent.KEYCODE_VOLUME_MUTE);
            }
        }
    };

    private void sendKeyEvent(int keyCode) {
        playVibrate();
        GlobalIOT.iot.sendKeyEvent(keyCode, KeyEvent.ACTION_DOWN);
    }

    private void playVibrate() {
        if (vibrator == null) {
            vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        }
        if (vibrator != null) {
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    private void sendChangeCmd(boolean isUpKeycode) {
        if (!SmartApi.isSameWifi()) {
            SmartApi.startConnectSameWifi(H5RunType.RUNTIME_NETWORK_FORCE_LAN);
            return;
        }
        if (isUpKeycode) {
            GlobalIOT.iot.sendKeyEvent(KeyEvent.KEYCODE_DPAD_UP, KeyEvent.ACTION_DOWN);
        } else {
            GlobalIOT.iot.sendKeyEvent(KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.ACTION_DOWN);
        }
    }

    @Override
    public void previous() {
        sendChangeCmd(true);
        playVibrate();
    }

    @Override
    public void next() {
        sendChangeCmd(false);
        playVibrate();
    }
}
