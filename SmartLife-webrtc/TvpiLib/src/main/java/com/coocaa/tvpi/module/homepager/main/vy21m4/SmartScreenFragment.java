package com.coocaa.tvpi.module.homepager.main.vy21m4;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.data.banner.BannerHttpData;
import com.coocaa.smartscreen.data.function.homepage.SSHomePageData;
import com.coocaa.swaiotos.virtualinput.utils.DimensUtils;
import com.coocaa.tvpi.base.mvvm.BaseViewModelFragment;
import com.coocaa.tvpi.base.mvvm.view.DefaultLoadStateView;
import com.coocaa.tvpi.base.mvvm.view.LoadStateViewProvide;
import com.coocaa.tvpi.event.AppAreaRefreshEvent;
import com.coocaa.tvpi.event.NetworkEvent;
import com.coocaa.tvpi.module.connection.WifiConnectActivity;
import com.coocaa.tvpi.module.homepager.main.ConnectStatusView;
import com.coocaa.tvpi.module.homepager.main.TopBarView;
import com.coocaa.tvpi.module.homepager.main.UIConnectCallbackAdapter;
import com.coocaa.tvpi.module.homepager.main.UIConnectStatus;
import com.coocaa.tvpi.module.homepager.main.vy21m4.adapter.SmartScreenNavAdapter;
import com.coocaa.tvpi.module.homepager.main.vy21m4.adapter.SmartScreenViewPagerAdapter;
import com.coocaa.tvpi.module.io.HomeUIThread;
import com.coocaa.tvpi.util.AppProcessUtil;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpi.util.Utils;
import com.coocaa.tvpilib.R;
import com.google.android.material.appbar.AppBarLayout;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.viewpager2.widget.ViewPager2;

import swaiotos.channel.iot.ss.device.Device;

public class SmartScreenFragment extends BaseViewModelFragment<SmartScreenViewModel> {

    private static final String TAG = SmartScreenFragment.class.getSimpleName();
    private static final String DARK_THEME = "dark";
    private static final String LIGHT_THEME = "light";
    private static final int NO_BANNER = 0;
    private static final int NORMAL_BANNER_STYLE = 1;
    private static final int ANIMATION_BANNER_STYLE = 2;
    private static final int IMAGE_BANNER_STYLE = 3;

    private DefaultLoadStateView loadStateView;
    private ConnectStatusView connectStatusView;
    private SmartScreenBanner smartScreenBanner;
    private AppBarLayout appBarLayout;
    private TopBarView toolbarView;
    private MagicIndicator indicator;
    private ViewPager2 viewPager2;
    private LinearLayout topLayout;

    private List<Fragment> fragmentList = new ArrayList<>();
    private List<String> titleList = new ArrayList<>();
    private volatile String curDeviceType = null;
    private volatile String lastDeviceType = null;

    private List<SSHomePageData> mSSHomePageDataList;

    private boolean hasInit = false;
    private boolean needRefresh = false;
    private SharedSpaceFragment sharedSpaceFragment;
    private BigScreenPlayFragment bigScreenPlayFragment;

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
        return inflater.inflate(R.layout.fragment_smartscreen_y21m4, container, false);
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
        getFunctionList();
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
        if (needRefresh) {
            needRefresh = false;
            getFunctionList();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SSConnectManager.getInstance().removeConnectCallback(uiConnectCallbackAdapter);
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(AppAreaRefreshEvent event) {
        Log.d(TAG, "onEvent: AppAreaRefreshEvent");
        needRefresh = true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(NetworkEvent event) {
        Log.d(TAG, "onEvent: NetworkEvent");
        uiConnectCallbackAdapter.updateDeviceInfoUI();
    }

    private void initView() {
        if (getView() == null || getContext() == null) {
            return;
        }
        loadStateView = getView().findViewById(R.id.load_state_view);
        toolbarView = getView().findViewById(R.id.toolbar);
        connectStatusView = getView().findViewById(R.id.home_connect_view);
        smartScreenBanner = getView().findViewById(R.id.smart_screen_banner);
        indicator = getView().findViewById(R.id.indicator);
        viewPager2 = getView().findViewById(R.id.viewpager2);
        appBarLayout = getView().findViewById(R.id.appbar_layout);
        topLayout = getView().findViewById(R.id.top_layout);
        initListener();
    }

    private void initListener() {
        loadStateView.setLoadTipsOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSmartScreenList(true);
            }
        });
        smartScreenBanner.setOnAnimalLoadListener(new SmartScreenBanner.OnAnimalLoadListener() {
            @Override
            public void onLoadError() {
                updateNormalBg();
            }

            @Override
            public void onLoadSuccess() {
                BannerHttpData.FunctionContent bannerData = smartScreenBanner.getBannerData();
                if (null == bannerData) {
                    return;
                }
                updateBg(bannerData);
            }
        });

        smartScreenBanner.setOnViewShowListener(new SmartScreenBanner.OnViewShowListener() {
            @Override
            public void onViewHide() {
                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) viewPager2.getLayoutParams();
                params.setBehavior(null);
                params.topMargin = DimensUtils.dp2Px(getContext(), 41);
                viewPager2.setLayoutParams(params);
            }

            @Override
            public void onViewShow() {
                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) viewPager2.getLayoutParams();
                params.setBehavior(new AppBarLayout.ScrollingViewBehavior());
                params.topMargin = DimensUtils.dp2Px(getContext(), 0);
                viewPager2.setLayoutParams(params);
            }
        });

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (smartScreenBanner != null
                        && smartScreenBanner.getBannerData() != null
                        && smartScreenBanner.getBannerData().style != ANIMATION_BANNER_STYLE) {
                    return;
                }
                Log.d(TAG, "onOffsetChanged: " + verticalOffset);
                if (verticalOffset == 0) {
                    updateBgAlpha(1);
                    return;
                }
                float height = smartScreenBanner.getHeight();
                float fraction = Math.abs(verticalOffset) / height;
                Log.d(TAG, "onOffsetChanged: " + fraction);
                if (fraction > 1) {
                    fraction = 1;
                }
                updateBgAlpha(1 - fraction);
            }
        });

    }

    private void initFragment() {
        for (int i = 0; i < mSSHomePageDataList.size(); i++) {
            SSHomePageData ssHomePageData = mSSHomePageDataList.get(i);
            if (ssHomePageData.tab_id == 1) {
                sharedSpaceFragment = new SharedSpaceFragment();
                sharedSpaceFragment.setSSHomePageData(ssHomePageData);
                fragmentList.add(sharedSpaceFragment);
            } else if (mSSHomePageDataList.get(i).tab_id == 2) {
                bigScreenPlayFragment = new BigScreenPlayFragment();
                bigScreenPlayFragment.setSSHomePageData(ssHomePageData);
                fragmentList.add(bigScreenPlayFragment);
            }
            titleList.add(ssHomePageData.tab_name);
        }

        viewPager2.setAdapter(new SmartScreenViewPagerAdapter(getChildFragmentManager(), getLifecycle(), fragmentList));
        CommonNavigator commonNavigator = new CommonNavigator(getContext());
        SmartScreenNavAdapter smartScreenNavAdapter = new SmartScreenNavAdapter(fragmentList, titleList, getContext());
        smartScreenNavAdapter.setOnItemClickListener(new SmartScreenNavAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int pos) {
                viewPager2.setCurrentItem(pos);
            }
        });
        indicator.setNavigator(commonNavigator);
        commonNavigator.setAdapter(smartScreenNavAdapter);
        viewPager2.registerOnPageChangeCallback(onPageChangeCallback);
        hasInit = true;
    }

    private void updateFragment() {
        for (int i = 0; i < mSSHomePageDataList.size(); i++) {
            SSHomePageData ssHomePageData = mSSHomePageDataList.get(i);
            if (ssHomePageData.tab_id == 1) {
                sharedSpaceFragment.setSSHomePageData(ssHomePageData);
            } else if (mSSHomePageDataList.get(i).tab_id == 2) {
                bigScreenPlayFragment.setSSHomePageData(ssHomePageData);
            }
        }
    }

    private void getSmartScreenList(boolean showLoading) {
        Log.d(TAG, "getSmartScreenList, curDeviceType=" + curDeviceType + ", lastDeviceType=" + lastDeviceType);
        if (TextUtils.isEmpty(curDeviceType)) {
            curDeviceType = "default";//默认未连接设备
        }
        //考虑改成类型变化了，且没获取到数据才需要刷新数据
        if (!TextUtils.equals(lastDeviceType, curDeviceType)) {
            lastDeviceType = curDeviceType;
            viewModel.getBannerData(showLoading, curDeviceType)
                    .observe(getViewLifecycleOwner(), smartScreenFunctionListObserver);
        }
    }

    private final Observer<BannerHttpData.FunctionContent> smartScreenFunctionListObserver = new Observer<BannerHttpData.FunctionContent>() {
        @Override
        public void onChanged(BannerHttpData.FunctionContent bannerData) {
            Log.d(TAG, "onChanged: " + bannerData);
            if (bannerData != null) {
                smartScreenBanner.showBanner(bannerData);
                if (bannerData.style == NO_BANNER || bannerData.style == NORMAL_BANNER_STYLE) {
                    updateNormalBg();
                }
                if (bannerData.style == IMAGE_BANNER_STYLE) {
                    updateBg(bannerData);
                }
            } else {
                smartScreenBanner.hindView();
                updateNormalBg();
            }
        }
    };

    private void getFunctionList() {
        viewModel.getFunctionListAppAreaV2(false, curDeviceType)
                .observe(getViewLifecycleOwner(), ssHomePageDataObserver);
    }

    private Observer<List<SSHomePageData>> ssHomePageDataObserver = new Observer<List<SSHomePageData>>() {
        @Override
        public void onChanged(List<SSHomePageData> ssHomePageDataList) {
            if (ssHomePageDataList != null && !ssHomePageDataList.isEmpty()) {
                mSSHomePageDataList = ssHomePageDataList;
                if (hasInit) {
                    updateFragment();
                } else {
                    initFragment();
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

    private void updateBgAlpha(float fraction) {
        if (smartScreenBanner.getBannerData() != null
                && smartScreenBanner.getBannerData().bg != null
                && (smartScreenBanner.getBannerData().style != NO_BANNER || smartScreenBanner.getBannerData().style != NORMAL_BANNER_STYLE)) {

            try {
                topLayout.setBackgroundColor(Utils.changeAlpha(Color.parseColor(smartScreenBanner.getBannerData().bg), fraction));
            }catch (Exception e){
                Log.e(TAG, "updateBg: Unknown color");
                smartScreenBanner.hindView();
                updateNormalBg();
                return;
            }
        }
        boolean isReVise = checkRevise(fraction);
        if (isReVise) {
            reviseIcon();
        }
    }

    private boolean checkRevise(float fraction) {
        if (smartScreenBanner == null
                || smartScreenBanner.getBannerData() == null
                || smartScreenBanner.getBannerData().theme == null) {
            return false;
        }
        if (fraction < 0.5) {
            return smartScreenBanner.getBannerData().theme.equals(DARK_THEME) == toolbarView.isDark();
        } else {
            return smartScreenBanner.getBannerData().theme.equals(DARK_THEME) != toolbarView.isDark();
        }
    }

    //通知栏theme反置
    private void reviseIcon() {
        if (toolbarView.isDark()) {
            toolbarView.setThemeDark(false);
            StatusBarHelper.setStatusBarLightMode(getActivity());
        } else {
            toolbarView.setThemeDark(true);
            StatusBarHelper.translucent(getActivity());
        }
    }

    private void updateBg(BannerHttpData.FunctionContent bannerData) {
        if (bannerData.style == ANIMATION_BANNER_STYLE || bannerData.style == IMAGE_BANNER_STYLE) {
            //颜色格式不对会有异常
            try {
                topLayout.setBackgroundColor(Color.parseColor(bannerData.bg));
            } catch (Exception e) {
                Log.e(TAG, "updateBg: Unknown color");
                smartScreenBanner.hindView();
                updateNormalBg();
                return;
            }
        } else {
            updateNormalBg();
        }

        if (DARK_THEME.equals(bannerData.theme)) {
            toolbarView.setThemeDark(true);
            StatusBarHelper.translucent(getActivity());
        } else if (LIGHT_THEME.equals(bannerData.theme)) {
            toolbarView.setThemeDark(false);
            StatusBarHelper.setStatusBarLightMode(getActivity());
        }
    }

    private void updateNormalBg() {
        topLayout.setBackgroundColor(Color.parseColor("#F4F4F4"));
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

    private final ViewPager2.OnPageChangeCallback onPageChangeCallback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            indicator.onPageScrolled(position, positionOffset, positionOffsetPixels);
            Log.d(TAG, "onPageScrolled: ");
        }

        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            indicator.onPageSelected(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            super.onPageScrollStateChanged(state);
            indicator.onPageScrollStateChanged(state);
        }
    };
}
