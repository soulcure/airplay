package com.coocaa.swaiotos.virtualinput.module.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.cocaa.swaiotos.virtualinput.R;
import com.coocaa.smartsdk.SmartApi;
import com.coocaa.smartsdk.SmartApiListener;
import com.coocaa.smartsdk.SmartApiListenerImpl;
import com.coocaa.smartsdk.object.IUserInfo;
import com.coocaa.swaiotos.virtualinput.utils.DimensUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import swaiotos.runtime.h5.core.os.H5RunType;
import swaiotos.sensor.client.ISmartApi;
import swaiotos.sensor.client.SensorClient;
import swaiotos.sensor.client.data.ClientBusinessInfo;
import swaiotos.sensor.data.AccountInfo;
import swaiotos.sensor.touch.InputTouchView;

public class RLaserPenFragment extends Fragment {
    public static final String TAG = "LaserPaint";

    //接收并将event事件给SensorClient
    private InputTouchView inputTouchView;
    //实际转发event给dongle的对象
    private SensorClient client;

    private ViewGroup mView;
    private Button button;
    private ImageView laserOffText, laserOnText, laserOffImg;
    private boolean isStart;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (getActivity() == null) return null;
        mView = (ViewGroup) inflater.inflate(R.layout.remote_laser_pen_fragment, container, false);


        ClientBusinessInfo clientBusinessInfo = new ClientBusinessInfo("client-laser-client",
                "client-laser-server", "激光笔", 0, 0);
        clientBusinessInfo.protoVersion = 0;
        //增加版本拉平
        client = new SensorClient(getActivity(), clientBusinessInfo, getAccountInfo());

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
        initView();
        return mView;
    }

    private void initView() {

        int width = DimensUtils.getDeviceWidth(getActivity()) - DimensUtils.dp2Px(getActivity(), 20);
        inputTouchView = (InputTouchView) client.getView();
        inputTouchView.setNeedTwoFinger(false);
        inputTouchView.setBackground(getResources().getDrawable(R.drawable.bg_b_16_round_12));
        inputTouchView.setEnabled(false);
        RelativeLayout.LayoutParams viewParams = new RelativeLayout.LayoutParams(width,
                ViewGroup.LayoutParams.MATCH_PARENT);
        viewParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        viewParams.bottomMargin = DimensUtils.dp2Px(getContext(), 25);
        viewParams.topMargin = DimensUtils.dp2Px(getContext(), 5);
        mView.addView(inputTouchView, 0, viewParams);

        laserOffImg = mView.findViewById(R.id.laser_off_img);
        laserOffText = mView.findViewById(R.id.laser_off_text_img);
        laserOnText = mView.findViewById(R.id.laser_on_text_img);
        button = mView.findViewById(R.id.btn_laser_switch);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchLaser();
            }
        });

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, ".. onViewCreated");
    }

    private void switchLaser() {
        if (isStart) {
            button.setBackgroundResource(R.drawable.bg_round_25_white_alpha0);
            button.setText("启用激光笔");
            button.setTextColor(Color.parseColor("#FF444444"));
            laserOffImg.setVisibility(View.VISIBLE);
            laserOffText.setVisibility(View.VISIBLE);
            laserOnText.setVisibility(View.INVISIBLE);
            if (client != null) client.stop();
            removeListener();
            inputTouchView.setEnabled(false);
            switchSuperScroller(false);
            isStart = !isStart;
        } else {
            button.setBackgroundResource(R.drawable.bg_round_25_white_alpha10);
            button.setText("关闭激光笔");
            button.setTextColor(Color.parseColor("#FFFFFFFF"));
            laserOffImg.setVisibility(View.INVISIBLE);
            laserOffText.setVisibility(View.INVISIBLE);
            laserOnText.setVisibility(View.VISIBLE);
            if (client != null) client.start();
            addListener();
            inputTouchView.setEnabled(true);
            switchSuperScroller(true);
            isStart = !isStart;
        }
    }

    private void switchSuperScroller(boolean isScroller) {
        Fragment fragment = getParentFragment();
        if (fragment == null) {
            return;
        }
        if (fragment instanceof RSpeakFragment) {
            RSpeakFragment rSpeakFragment = (RSpeakFragment) fragment;
            if (isScroller) {
                rSpeakFragment.switchOff();
            } else {
                rSpeakFragment.switchOn();
            }
        }
    }

    private AccountInfo getAccountInfo() {
        AccountInfo info = new AccountInfo();
        IUserInfo userInfo = SmartApi.getUserInfo();
        if (userInfo != null) {
            info.accessToken = userInfo.accessToken;
            info.avatar = userInfo.avatar;
            info.mobile = userInfo.mobile;
            info.open_id = userInfo.open_id;
            info.nickName = userInfo.nickName;
        }
        Log.e(TAG, "getAccountInfo()============" + info.toString());
        return info;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public void onResume() {
        Log.d(TAG, "++ onResume, isStart=" + isStart);
        super.onResume();
        //callStart();
        if (client != null && !isStart) {
            client.stop();
        }
    }

//    public void onShow() {
//        Log.d(TAG, "++ onShow, isShow=" + isShow);
//        //callStart();
//    }

//    private void callStart() {
////        if(!isShow) {
//        Log.d(TAG, "++ callStart");
//        isShow = true;
//        if (client != null) client.start();
//        addListener();
////        }
//    }

//    public void onHide() {
//        Log.d(TAG, "-- onHide");
//        if (client != null) client.stop();
//        removeListener();
//        isShow = false;
//    }

    @Override
    public void onStop() {
        Log.d(TAG, "-- onStop");
        super.onStop();
        if (client != null) client.stop();
        removeListener();
        isStart = false;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "~~ onDestroy");
        super.onDestroy();
        removeListener();
        isStart = false;
    }

    private void addListener() {
        SmartApi.addListener(smartListener);
    }

    private void removeListener() {
        SmartApi.removeListener(smartListener);
    }

    private SmartApiListener smartListener = new SmartApiListenerImpl() {

        @Override
        public void loginState(int code, String info) {
            Log.d(TAG, "onLoginStateChanged, code=" + code);
            if (code == 2 || code == 3) {
                if (client != null  && isStart) {
                    client.start();
                }
            }
        }
    };
}
