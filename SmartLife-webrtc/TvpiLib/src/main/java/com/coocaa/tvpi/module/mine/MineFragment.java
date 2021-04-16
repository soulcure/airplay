package com.coocaa.tvpi.module.mine;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.data.account.CoocaaUserInfo;
import com.coocaa.smartsdk.SmartApi;
import com.coocaa.smartsdk.SmartApiListener;
import com.coocaa.smartsdk.SmartApiListenerImpl;
import com.coocaa.smartsdk.object.ISmartDeviceInfo;
import com.coocaa.tvpi.event.UserLoginEvent;
import com.coocaa.tvpi.module.connection.ScanActivity;
import com.coocaa.tvpi.module.feedback.FeedbackActivity;
import com.coocaa.tvpi.module.login.LoginActivity;
import com.coocaa.tvpi.module.login.UserInfoCenter;
import com.coocaa.tvpi.module.mine.about.AboutActivity;
import com.coocaa.tvpi.module.mine.lab.SmartLabActivity;
import com.coocaa.tvpi.module.mine.userinfo.UserInfoActivity;
import com.coocaa.tvpi.util.FastClick;
import com.coocaa.tvpi.util.Utils;
import com.coocaa.tvpi.util.permission.PermissionListener;
import com.coocaa.tvpi.util.permission.PermissionsUtil;
import com.coocaa.tvpilib.R;
import com.makeramen.roundedimageview.RoundedImageView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


public class MineFragment extends Fragment {

    private final static String TAG = MineFragment.class.getSimpleName();

    private RoundedImageView mMineHeadImg;
    private TextView mTvUsername;
    private TextView mTvCurrentDevice;
    private RelativeLayout mRlLogin;
    private RelativeLayout mRlAdvice;
    private RelativeLayout mRlScan;
    private RelativeLayout mRlAbout;
    private RelativeLayout mLab;
    private TextView tvVersionName;

    private FastClick fastClick = new FastClick();

    public MineFragment() {
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        if (UserInfoCenter.getInstance().isLogin()) {
            updateLoginView(UserInfoCenter.getInstance().getCoocaaUserInfo());
        } else {
            updateUnLoginView();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

//    @Override
//    public void setUserVisibleHint(boolean isVisibleToUser) {
//        super.setUserVisibleHint(isVisibleToUser);
//        Log.d(TAG, "setUserVisibleHint: " + isVisibleToUser);
//        if (UserInfoCenter.getInstance().isLogin() && isVisibleToUser) {
//            updateLoginView(UserInfoCenter.getInstance().getCoocaaUserInfo());
//        } else {
//            updateUnLoginView();
//        }
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine, container, false);
        initView(view);
        initListener();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SmartApi.addListener(smartApiListener);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("SmartMine", "onDestroy");
        SmartApi.removeListener(smartApiListener);
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onEventMainThread(UserLoginEvent userLoginEvent) {
        if(userLoginEvent.isLogin) {
            //这里通过onresume刷新了，首页是eventbus刷新的。
            //Log.d(TAG, "onEvent: refresh avatar...");
        }
    }

    SmartApiListener smartApiListener = new SmartApiListenerImpl() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onDeviceConnect(ISmartDeviceInfo deviceInfo) {
            Log.d("SmartMine", "onDeviceConnect : " + deviceInfo);
            if(deviceInfo != null) {
                mTvCurrentDevice.setText("当前已连接“" + deviceInfo.deviceName + "”");
            }
        }

        @Override
        public void onDeviceDisconnect() {
            Log.d("SmartMine", "onDeviceDisconnect");
            mTvCurrentDevice.setText("暂未连接设备");
        }
    };

    private void initView(View view) {
        mMineHeadImg = view.findViewById(R.id.mine_head_img);
        mTvUsername = view.findViewById(R.id.tv_username);
        mTvCurrentDevice = view.findViewById(R.id.tv_current_device);
        mRlLogin = view.findViewById(R.id.rl_login);
        mRlAdvice = view.findViewById(R.id.rl_advice);
        mRlScan = view.findViewById(R.id.rl_scan);
        mRlAbout = view.findViewById(R.id.rl_about);
        tvVersionName = view.findViewById(R.id.tv_version_name);
        tvVersionName.setText(String.format("V%s", Utils.getAppVersionName(getContext())));
        mLab = view.findViewById(R.id.rl_lab);
    }

    private void initListener() {
        mRlScan.setOnClickListener(v -> {
            if(!fastClick.isFaskClick()) {
                startScan();
            }
        });
        mRlAdvice.setOnClickListener(v -> {
            if(!fastClick.isFaskClick()) {
                startFeedback();
            }
        });
        mRlLogin.setOnClickListener(v -> {
            if(!fastClick.isFaskClick()) {
                if (!UserInfoCenter.getInstance().isLogin()) {
                    startLogin();
                } else {
                    Intent intent = new Intent();
                    intent.setClass(Objects.requireNonNull(getContext()), UserInfoActivity.class);
                    startActivity(intent);
                }
            }
        });
        mRlAbout.setOnClickListener(v -> {
            if(!fastClick.isFaskClick()) {
                startAbout();
            }
        });
        mLab.setOnClickListener(v -> {
            if(!fastClick.isFaskClick()) {
                Intent intent = new Intent();
                intent.setClass(Objects.requireNonNull(getContext()), SmartLabActivity.class);
                startActivity(intent);
            }
        });
    }

    private void startAbout() {
        Intent intent = new Intent(getActivity(), AboutActivity.class);
        startActivity(intent);
    }

    private void startScan() {
        if(UserInfoCenter.getInstance().isLogin()){
            PermissionsUtil.getInstance().requestPermission(getContext(), new PermissionListener() {
                @Override
                public void permissionGranted(String[] permission) {
                    Intent intent = new Intent(getActivity(), ScanActivity.class);
                    startActivity(intent);
                }

                @Override
                public void permissionDenied(String[] permission) {
                    ToastUtils.getInstance().showGlobalLong("将无法扫描二维码");
                }
            }, Manifest.permission.CAMERA);
        }else{
            startLogin();
        }


    }

    private void startFeedback() {
        if(UserInfoCenter.getInstance().isLogin()){
            Intent intent = new Intent(getActivity(), FeedbackActivity.class);
            startActivity(intent);
        }else {
            startLogin();
        }

    }


    private void startLogin() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
    }

    @SuppressLint("SetTextI18n")
    private void updateLoginView(CoocaaUserInfo coocaaUserInfo) {
        if(coocaaUserInfo == null){
            updateUnLoginView();
            return;
        }
        ISmartDeviceInfo deviceInfo = SmartApi.getConnectDeviceInfo();
        if (deviceInfo == null) {
            mTvCurrentDevice.setText("暂未连接设备");
        } else {
            mTvCurrentDevice.setText("当前已连接“" + deviceInfo.deviceName + "”");
        }
        if (coocaaUserInfo.getMobile() != null) {
            String mobile = coocaaUserInfo.getMobile().substring(0, 3) + "****" + coocaaUserInfo.getMobile().substring(7);
            mTvUsername.setText(mobile);
        }

        String avatar = coocaaUserInfo.getAvatar();
        if (TextUtils.isEmpty(avatar)) {
            Glide.with(this).load(R.drawable.icon_mine_default_unhead).into(mMineHeadImg);
        } else if (avatar.startsWith("https")) {
            Glide.with(this).load(avatar).error(R.drawable.icon_mine_default_unhead).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).into(mMineHeadImg);
        } else if (avatar.startsWith("http")) {
            String avatar2 = avatar.replaceFirst("http", "https");
            Glide.with(this).load(avatar2).error(R.drawable.icon_mine_default_unhead).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).into(mMineHeadImg);
        }
    }

    private void updateUnLoginView() {
        if (mTvUsername != null && mTvCurrentDevice != null) {
            mTvCurrentDevice.setText("点击注册登录");
            mTvUsername.setText("未登录");
            Glide.with(this).load(R.drawable.icon_mine_default_head).into(mMineHeadImg);
        }
    }

    private boolean isConnected() {
        return SSConnectManager.getInstance().isConnected();
    }

}