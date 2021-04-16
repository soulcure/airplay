package com.coocaa.tvpi.module.mine.lab;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartsdk.SmartApi;
import com.coocaa.smartsdk.object.ISmartDeviceInfo;
import com.coocaa.tvpi.module.connection.ConnectDialogActivity;
import com.coocaa.tvpi.module.homepager.cotroller.MirrorScreenController;
import com.coocaa.tvpilib.R;
import com.swaiot.webrtcc.video.WebRTCVideoManager;
import com.swaiotos.skymirror.sdk.capture.MirManager;
import com.umeng.analytics.MobclickAgent;

import static com.coocaa.smartscreen.connect.SSConnectManager.CONNECT_BOTH;
import static com.coocaa.smartscreen.connect.SSConnectManager.CONNECT_LOCAL;
import static com.coocaa.smartscreen.connect.SSConnectManager.CONNECT_NOTHING;
import static com.coocaa.tvpi.common.UMengEventId.MAIN_PAGE_CAST_PHONE;
import static com.coocaa.tvpi.module.homepager.cotroller.MirrorScreenController.MIRROR_SCREEN_REQUEST_CODE;

public class MirrorFragment extends Fragment {

    private static final String TAG = MirrorFragment.class.getSimpleName();
    private static final int MIRROR_OFF = 0;
    private static final int MIRROR_LOADING = 1;
    private static final int MIRROR_ON = 2;

    private ImageView imgMirrorOff;
    private ImageView imgMirrorOn;
    private LinearLayout btnMirrorSwitch;
    private ProgressBar mirrorLoadingBar;
    private TextView tvMirrorState;

    private MirrorScreenController mirrorScreenController;
    private int mirrorState = 0;
    private String mirrorOff = "打开手机镜像";
    private String mirrorLoading = "正在启动手机镜像";
    private String mirrorOn = "关闭手机镜像";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_mirror, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        initListener();
        initMirrorView();
    }

    private void initMirrorView() {
        boolean isMirroring;
        try {
            isMirroring = MirManager.instance().isMirRunning()
                    || WebRTCVideoManager.instance().isStart();
        }catch (Exception e){
            isMirroring = false;
        }

        Log.d(TAG, "initMirrorView: " + isMirroring);
        if (isMirroring) {
            updateMirrorState(MIRROR_ON);
        } else {
            updateMirrorState(MIRROR_OFF);
        }
    }

    //转发到MirrorScreenController
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", data=" + data);
        if (requestCode == MIRROR_SCREEN_REQUEST_CODE && mirrorScreenController != null) {
            mirrorScreenController.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void initView() {
        if (getView() == null) {
            return;
        }
        mirrorScreenController = new MirrorScreenController(this, mirrorScreenListener);
        imgMirrorOff = getView().findViewById(R.id.mirror_off_img);
        imgMirrorOn = getView().findViewById(R.id.mirror_on_img);
        btnMirrorSwitch = getView().findViewById(R.id.mirror_switch_layout);
        mirrorLoadingBar = getView().findViewById(R.id.mirror_loading_progressbar);
        tvMirrorState = getView().findViewById(R.id.tv_mirror_state);
    }

    private void initListener() {
        btnMirrorSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mirrorScreen();
            }
        });
    }

    private void mirrorScreen() {
        Log.d(TAG, "start switch mirrorScreen, cur is mirroring=" + mirrorState);
        MobclickAgent.onEvent(getContext(), MAIN_PAGE_CAST_PHONE);

        int connectState = SSConnectManager.getInstance().getConnectState();
        final ISmartDeviceInfo deviceInfo = SmartApi.getConnectDeviceInfo();
        Log.d(TAG, "pushToTv: connectState" + connectState);
        Log.d(TAG, "pushToTv: deviceInfo" + deviceInfo);
        //未连接
        if (connectState == CONNECT_NOTHING || deviceInfo == null) {
            showConnectDialog();
            return;
        }
        //本地连接不通
        if (!(connectState == CONNECT_LOCAL || connectState == CONNECT_BOTH)) {
            ToastUtils.getInstance().showGlobalShort(R.string.not_same_wifi_tips);
            return;
        }
        mirrorScreenController.switchMirrorScreen();
    }

    private void showConnectDialog() {
        ConnectDialogActivity.start(getContext());
    }

    private MirrorScreenController.MirrorScreenListener mirrorScreenListener
            = new MirrorScreenController.MirrorScreenListener() {
        @Override
        public void onStartMirrorScreen() {
            Log.d(TAG, "onStartMirrorScreen: MIRROR_OFF");
            updateMirrorState(MIRROR_LOADING);
        }

        @Override
        public void onMirroringScreen() {
            Log.d(TAG, "onMirroringScreen: MIRROR_LOADING");
            updateMirrorState(MIRROR_ON);
        }

        @Override
        public void onStopMirrorScreen() {
            Log.d(TAG, "onStopMirrorScreen: MIRROR_ON");
            updateMirrorState(MIRROR_OFF);
        }
    };

    private void updateMirrorState(int state) {
        Log.d(TAG, "updateMirrorState, _isMirror=" + state);
        mirrorState = state;
        updateMirrorUI();
    }

    private void updateMirrorUI() {
        //更新屏幕镜像状态
        switch (mirrorState) {
            case MIRROR_OFF:
                tvMirrorState.setText(mirrorOff);
                tvMirrorState.setTextColor(Color.parseColor("#FFFFFFFF"));
                btnMirrorSwitch.setBackgroundResource(R.drawable.bg_blue_round_12);
                mirrorLoadingBar.setVisibility(View.GONE);
                imgMirrorOff.setVisibility(View.VISIBLE);
                imgMirrorOn.setVisibility(View.INVISIBLE);
                break;
            case MIRROR_LOADING:
                tvMirrorState.setText(mirrorLoading);
                tvMirrorState.setTextColor(Color.parseColor("#FFFFFFFF"));
                btnMirrorSwitch.setBackgroundResource(R.drawable.bg_blue_round_12);
                mirrorLoadingBar.setVisibility(View.VISIBLE);
                imgMirrorOff.setVisibility(View.VISIBLE);
                imgMirrorOn.setVisibility(View.INVISIBLE);
                break;
            case MIRROR_ON:
                tvMirrorState.setText(mirrorOn);
                tvMirrorState.setTextColor(Color.parseColor("#CC000000"));
                btnMirrorSwitch.setBackgroundResource(R.drawable.bg_light_gray_round_12);
                mirrorLoadingBar.setVisibility(View.GONE);
                imgMirrorOff.setVisibility(View.INVISIBLE);
                imgMirrorOn.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }

}
