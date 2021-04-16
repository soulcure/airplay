package com.coocaa.tvpi.module.mirror;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartsdk.SmartApi;
import com.coocaa.smartsdk.object.ISmartDeviceInfo;
import com.coocaa.tvpi.module.connection.ConnectDialogActivity;
import com.coocaa.tvpi.module.homepager.cotroller.MirrorScreenController;
import com.coocaa.tvpi.module.io.HomeIOThread;
import com.coocaa.tvpi.module.io.HomeUIThread;
import com.coocaa.tvpilib.R;
import com.umeng.analytics.MobclickAgent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static com.coocaa.smartscreen.connect.SSConnectManager.CONNECT_BOTH;
import static com.coocaa.smartscreen.connect.SSConnectManager.CONNECT_LOCAL;
import static com.coocaa.smartscreen.connect.SSConnectManager.CONNECT_NOTHING;
import static com.coocaa.tvpi.common.UMengEventId.MAIN_PAGE_CAST_PHONE;
import static com.coocaa.tvpi.module.homepager.cotroller.MirrorScreenController.MIRROR_SCREEN_REQUEST_CODE;

/**
 * @Author: yuzhan
 */
public class MirrorFragment extends Fragment {

    private MirrorScreenController mirrorScreenController;
    private RelativeLayout layout;
    private final String TAG = "SmartMirror";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(layout == null) {
            layout = new RelativeLayout(getContext());
            layout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            layout.setBackgroundColor(Color.parseColor("#40FFFF00"));
        }
        return layout;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated...");
        super.onViewCreated(view, savedInstanceState);
        mirrorScreenController = new MirrorScreenController(this, mirrorScreenListener);

        HomeIOThread.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                HomeUIThread.execute(new Runnable() {
                    @Override
                    public void run() {
                        mirrorScreen();
                    }
                });
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mirrorScreenController.resume();
    }

    @Override
    public void onDestroy() {
//        mirrorScreenController.destroy();
        super.onDestroy();
    }

    private void mirrorScreen() {
        Log.d(TAG, "mirrirScreen");
        MobclickAgent.onEvent(getContext(), MAIN_PAGE_CAST_PHONE);
        int connectState = SSConnectManager.getInstance().getConnectState();
        final ISmartDeviceInfo deviceInfo = SmartApi.getConnectDeviceInfo();
        Log.d(TAG, "pushToTv: connectState" + connectState);
        Log.d(TAG, "pushToTv: deviceInfo" + deviceInfo);
        //未连接
        if(connectState == CONNECT_NOTHING || deviceInfo == null){
            showConnectDialog();
            return;
        }
        //本地连接不通
        if(!(connectState == CONNECT_LOCAL || connectState == CONNECT_BOTH)){
            ToastUtils.getInstance().showGlobalShort(R.string.not_same_wifi_tips);
            return;
        }
        mirrorScreenController.switchMirrorScreen();
    }

    //转发到MirrorScreenController
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: " + requestCode + requestCode + data);
        if (requestCode == MIRROR_SCREEN_REQUEST_CODE && mirrorScreenController != null) {
            mirrorScreenController.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void updateMirrorState(boolean isMirror) {
//        shortcutCommandLayout.setMirrorScreenState(isMirror);
    }

    private void showConnectDialog() {
        ConnectDialogActivity.start(getActivity());
    }

    private MirrorScreenController.MirrorScreenListener mirrorScreenListener
            = new MirrorScreenController.MirrorScreenListener() {
        @Override
        public void onStartMirrorScreen() {
            updateMirrorState(false);
        }

        @Override
        public void onMirroringScreen() {
            updateMirrorState(true);
        }

        @Override
        public void onStopMirrorScreen() {
            updateMirrorState(false);
        }
    };

}