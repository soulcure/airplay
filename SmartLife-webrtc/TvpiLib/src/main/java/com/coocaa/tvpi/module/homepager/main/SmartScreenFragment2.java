package com.coocaa.tvpi.module.homepager.main;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.tvpi.base.mvvm.BaseViewModelFragment;
import com.coocaa.tvpi.base.mvvm.view.DefaultLoadStateView;
import com.coocaa.tvpi.base.mvvm.view.LoadStateViewProvide;
import com.coocaa.tvpi.event.NetworkEvent;
import com.coocaa.tvpi.module.connection.WifiConnectActivity;
import com.coocaa.tvpi.module.homepager.adapter.bean.SmartScreenWrapBean;
import com.coocaa.tvpi.module.io.HomeIOThread;
import com.coocaa.tvpi.module.io.HomeUIThread;
import com.coocaa.tvpi.module.share.ShareCodeActivity;
import com.coocaa.tvpi.util.AppProcessUtil;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpilib.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import swaiotos.channel.iot.ss.device.Device;

public class SmartScreenFragment2 extends BaseViewModelFragment<SmartScreenViewModel2> {

    private static final String TAG = SmartScreenFragment2.class.getSimpleName();
    private static final String DARK_THEME = "dark";
    private static final String LIGHT_THEME = "light";

    private DefaultLoadStateView loadStateView;
    private ConnectStatusView connectStatusView;
    private TopBarView toolbarView;
    private SmartScreenFunctionAdapter2 adapter;
    private volatile String curDeviceType = null;
    private volatile String lastDeviceType = null;

    private final UIConnectCallbackAdapter uiConnectCallbackAdapter = new UIConnectCallbackAdapter(new UIConnectCallbackAdapter.UIConnectCallback() {
        @Override
        public void onConnectStatusChange(boolean showDialog, @NotNull UIConnectStatus status) {
            Log.d(TAG, "onConnectStatusChange: " + status + " showDialog:" + showDialog);
            if (getActivity() == null) {
                Log.d(TAG, "onConnectStatusChange: activity is null");
                return;
            }
            //刷新界面连接状态
            updateUIByConnectStatus(status);
            //若设备和手机不在同一WiFi显示连接wifi弹窗
            if (showDialog && status == UIConnectStatus.CONNECT_NOT_SAME_WIFI) {
                showConnectWifiDialog();
            }
        }

        @Override
        public void onDeviceTypeChange(@Nullable String deviceType) {
            Log.d(TAG, "onDeviceTypeChange: " + deviceType);
            if (getActivity() == null) {
                Log.d(TAG, "onConnectStatusChange: activity is null");
                return;
            }
            //连接类型切换时更新数据
            curDeviceType = deviceType;
            getSmartScreenList(false);
        }

        @Override
        public void onDeviceChange(@NotNull Device<?> device) {
            Log.d(TAG, "onDeviceChange: " + device);
            updateDevice(device);
        }
    });


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_smartscreen, container, false);
    }

    @Override
    protected LoadStateViewProvide createLoadStateViewProvide() {
        return loadStateView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SSConnectManager.getInstance().addConnectCallback(uiConnectCallbackAdapter);
        EventBus.getDefault().register(this);
        initView();
        getSmartScreenList(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        uiConnectCallbackAdapter.updateDeviceInfoUI();
        if (toolbarView != null) {
            if (toolbarView.isDark()) {
                StatusBarHelper.translucent(getActivity());
            } else {
                StatusBarHelper.setStatusBarLightMode(getActivity());
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(NetworkEvent event) {
        Log.d(TAG, "onEvent: NetworkEvent");
        uiConnectCallbackAdapter.updateDeviceInfoUI();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SSConnectManager.getInstance().removeConnectCallback(uiConnectCallbackAdapter);
        EventBus.getDefault().unregister(this);
    }


    private void initView() {
        if (getView() == null || getContext() == null) return;
        loadStateView = getView().findViewById(R.id.load_state_view);
        toolbarView = getView().findViewById(R.id.toolbar);
        connectStatusView = new ConnectStatusView(getContext(), null);
        RecyclerView recyclerView = getView().findViewById(R.id.recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new SmartScreenFunctionAdapter2();
        adapter.addHeaderView(connectStatusView);
        recyclerView.setAdapter(adapter);
        loadStateView.setLoadTipsOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSmartScreenList(true);
            }
        });
        adapter.setOnAnimalLoadListener(new SmartScreenFunctionAdapter2.OnAnimalLoadListener() {
            @Override
            public void onLoadError() {
                updateNormalBg();
            }

            @Override
            public void onLoadSuccess() {
                List<SmartScreenWrapBean> smartScreenWrapBeans = adapter.getData();
                if (null == smartScreenWrapBeans) {
                    return;
                }
                for (SmartScreenWrapBean smartScreenWrapBean : smartScreenWrapBeans) {
                    if (smartScreenWrapBean.getBannerList() != null) {
                        updateBg(smartScreenWrapBean);
                    }
                }
            }
        });
    }

    private void getSmartScreenList(boolean showLoading) {
        Log.d(TAG, "getSmartScreenList, curDeviceType=" + curDeviceType + ", lastDeviceType=" + lastDeviceType);
        if (TextUtils.isEmpty(curDeviceType)) {
            curDeviceType = "default";//默认未连接设备
        }
        //考虑改成类型变化了，且没获取到数据才需要刷新数据
        if (!TextUtils.equals(lastDeviceType, curDeviceType)) {
            lastDeviceType = curDeviceType;
            viewModel.getSmartScreenList(showLoading, curDeviceType)
                    .observe(getViewLifecycleOwner(), smartScreenFunctionListObserver);
        }
    }

    private final Observer<List<SmartScreenWrapBean>> smartScreenFunctionListObserver = new Observer<List<SmartScreenWrapBean>>() {
        @Override
        public void onChanged(List<SmartScreenWrapBean> smartScreenWrapBeans) {
            Log.d(TAG, "onChanged: " + smartScreenWrapBeans);
            if (smartScreenWrapBeans != null) {
                adapter.setList(smartScreenWrapBeans);
                for (SmartScreenWrapBean smartScreenWrapBean : smartScreenWrapBeans) {
                    if (smartScreenWrapBean.getBannerList() != null) {
                        if (smartScreenWrapBean.style == 1) {
                            updateNormalBg();
                            return;
                        } else if (smartScreenWrapBean.style == 3) {
                            updateBg(smartScreenWrapBean);
                            return;
                        }
                    }
                    updateNormalBg();
                }
            }
        }
    };

    private void updateUIByConnectStatus(UIConnectStatus status) {
        Log.d(TAG, "updateUIByConnectStatus: " + status);
        Log.d(TAG, "updateUIByConnectStatus: session = " + SSConnectManager.getInstance().getConnectSession());
        Log.d(TAG, "updateUIByConnectStatus: historyDevice = " + SSConnectManager.getInstance().getHistoryDevice());
        HomeUIThread.execute(new Runnable() {
            @Override
            public void run() {
                connectStatusView.setUIConnectStatus(status);
            }
        });
    }

    private void updateDevice(Device device) {
        Log.d(TAG, "updateDevice: " + device);
        connectStatusView.setDeviceName(device);
    }

    private void updateBg(SmartScreenWrapBean smartScreenWrapBean) {
        if (smartScreenWrapBean.style == 2 || smartScreenWrapBean.style == 3) {
            //颜色格式不对会有异常
            try {
                connectStatusView.setBackgroundColor(Color.parseColor(smartScreenWrapBean.bg));
                toolbarView.setBackgroundColor(Color.parseColor(smartScreenWrapBean.bg));
            } catch (Exception e) {
                Log.e(TAG, "updateBg: Unknown color");
                return;
            }
        }

        if (DARK_THEME.equals(smartScreenWrapBean.theme)) {
            toolbarView.setThemeDark(true);
            StatusBarHelper.translucent(getActivity());
        } else if (LIGHT_THEME.equals(smartScreenWrapBean.theme)) {
            toolbarView.setThemeDark(false);
            StatusBarHelper.setStatusBarLightMode(getActivity());
        }
    }

    private void updateNormalBg() {
        connectStatusView.setBackgroundColor(Color.parseColor("#F4F4F4"));
        toolbarView.setBackgroundColor(Color.parseColor("#F4F4F4"));
        toolbarView.setThemeDark(false);
        StatusBarHelper.setStatusBarLightMode(getActivity());
    }


    private void showConnectWifiDialog() {
        Log.d(TAG, "showConnectWifiDialog");
        if (AppProcessUtil.isAppBackground) {
            Log.d(TAG, "showConnectWifiDialog: app is background");
            return;
        }

        if (SSConnectManager.getInstance().getDevice() != null
                && isTv(SSConnectManager.getInstance().getDevice())) {
            Log.d(TAG, "showConnectWifiDialog:  is not dongle device");
            return;
        }

        if (SSConnectManager.getInstance().isSameWifi()) {
            Log.d(TAG, "showConnectWifiDialog: is same wifi");
            return;
        }

        WifiConnectActivity.start(getActivity());
    }

    private boolean isTv(Device device) {
        return "tv".equalsIgnoreCase(SSConnectManager.getInstance().getDevice().getZpRegisterType());
    }
}
