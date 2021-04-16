package com.coocaa.tvpi.module.app.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.data.app.AppModel;
import com.coocaa.smartscreen.data.app.TvAppStateModel;
import com.coocaa.tvpi.base.mvvm.BaseViewModelFragment;
import com.coocaa.tvpi.base.mvvm.view.LoadStateViewProvide;
import com.coocaa.tvpi.module.app.AppDetailActivity;
import com.coocaa.tvpi.module.app.AppSearchActivity;
import com.coocaa.tvpi.module.app.AppStoreListActivity;
import com.coocaa.tvpi.module.app.adapter.AppStoreAdapter;
import com.coocaa.tvpi.module.app.bean.AppStoreWrapBean;
import com.coocaa.tvpi.module.app.viewmodel.AppTabStoreViewModel;
import com.coocaa.tvpi.module.connection.ConnectDialogActivity;
import com.coocaa.tvpi.module.viewmodel.ApplicationShareViewModel;
import com.coocaa.tvpi.view.CommonTitleBar;
import com.coocaa.tvpi.view.decoration.CommonVerticalItemDecoration;
import com.coocaa.tvpilib.R;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.coocaa.tvpi.common.UMengEventId.APP_INSTALL;

/**
 * 应用-应用商店Tab
 * Created by songxing on 2020/7/16
 */
public class AppTabStoreFragment extends BaseViewModelFragment<AppTabStoreViewModel> {
    private static final String TAG = AppTabStoreFragment.class.getSimpleName();
    private CommonTitleBar titleBar;
    private LoadStateViewProvide loadStateView;
    private AppStoreAdapter appStoreAdapter;

    private ApplicationShareViewModel appShareViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_app_tab_store, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        setListener();
        getAppStoreList();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        //Tab切换时更新app的安装状态，防止应用Tab删除后这里还显示已安装
        if (!hidden) {
            Log.d(TAG, "onHiddenChanged: !hidden");
            resetAppStoreInstallState();
            getInstalledAppState();
            observerInstalledAppState();
            observerInstallingAppState();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(TAG);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(TAG);
    }

    @Override
    protected LoadStateViewProvide createLoadStateViewProvide() {
        return loadStateView;
    }

    private void initView(View view) {
        loadStateView = view.findViewById(R.id.loadStateView);
        titleBar = view.findViewById(R.id.titleBar);
        RecyclerView rvAppStore = view.findViewById(R.id.rvApp);
        CommonVerticalItemDecoration decoration = new CommonVerticalItemDecoration(DimensUtils.dp2Px(getContext(), 30));
        LinearLayoutManager manager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        appStoreAdapter = new AppStoreAdapter();
        rvAppStore.addItemDecoration(decoration);
        rvAppStore.setLayoutManager(manager);
        rvAppStore.setAdapter(appStoreAdapter);
        appShareViewModel = getAppViewModelProvider().get(ApplicationShareViewModel.class);
    }

    private void setListener() {
        titleBar.setOnClickListener(new CommonTitleBar.OnClickListener() {
            @Override
            public void onClick(CommonTitleBar.ClickPosition position) {
                if (position == CommonTitleBar.ClickPosition.LEFT) {
                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                } else {
                    AppSearchActivity.start(getContext());
                }
            }
        });

        appStoreAdapter.setAppStoreListener(new AppStoreAdapter.AppStoreListener() {
            @Override
            public void onMoreListClick(String classId, String className) {
                AppStoreListActivity.start(getContext(), classId, className);
            }

            @Override
            public void onChildItemClick(AppModel appModel) {
                AppDetailActivity.start(getContext(), appModel);
            }

            @Override
            public void onChildStateButtonClick(int pos, AppModel appModel) {
                if (!SSConnectManager.getInstance().isConnected()) {
                    ConnectDialogActivity.start(getActivity());
                    return;
                }
                if (appModel.status == AppModel.STATE_UNINSTALL) {
                    viewModel.installApp(appModel);
                    appModel.status = AppModel.STATE_INSTALLING;
                    appStoreAdapter.notifyItemChanged(pos);
                    appShareViewModel.addInstallingApp(appModel);
                } else if (appModel.status == AppModel.STATE_INSTALLED) {
                    viewModel.startApp(appModel);
                    submitInstallAppEvent();
                }
            }
        });
    }


    //订阅商城数据列表
    private void getAppStoreList() {
        viewModel.getAppStoreList().observe(getViewLifecycleOwner(), new Observer<List<AppStoreWrapBean>>() {
            @Override
            public void onChanged(List<AppStoreWrapBean> appStoreWrapBeans) {
                Log.d(TAG, "getAppStoreList onChanged: " + appStoreWrapBeans);
                if (appStoreWrapBeans != null) {
                    appStoreAdapter.setList(appStoreWrapBeans);
                    getInstalledAppState();
                    observerInstalledAppState();
                    observerInstallingAppState();
                }
            }
        });
    }

    //获取app的安装状态
    private void getInstalledAppState() {
        List<AppModel> appModelList = new ArrayList<>();
        List<AppStoreWrapBean> data = appStoreAdapter.getData();
        for (AppStoreWrapBean wrapBean : data) {
            appModelList.addAll(wrapBean.appList);
        }
        viewModel.getAppState(appModelList);
    }

    //电视机返回的app的状态
    private void observerInstalledAppState() {
        viewModel.getAppInstallStateLiveData().observe(getViewLifecycleOwner(),installedAppStateObserver);
    }

    private Observer<List<TvAppStateModel>> installedAppStateObserver = new Observer<List<TvAppStateModel>>() {
        @Override
        public void onChanged(List<TvAppStateModel> tvAppStateModels) {
            Log.d(TAG, "observerInstalledAppState onChanged: " + tvAppStateModels);
            updateAppState(tvAppStateModels);
        }
    };

    //下载中的app的安装状态
    private void observerInstallingAppState() {
        appShareViewModel.getInstallingAppStateLiveData().observe(getViewLifecycleOwner(), installingAppStateObserver);
    }

    private Observer<List<TvAppStateModel>> installingAppStateObserver = new Observer<List<TvAppStateModel>>() {
        @Override
        public void onChanged(List<TvAppStateModel> tvAppStateModels) {
            Log.d(TAG, "observerInstallingAppState onChanged: " + tvAppStateModels);
            updateAppState(tvAppStateModels);
        }
    };


    //更新应用列表安装状态
    private void updateAppState(List<TvAppStateModel> tvAppStateModels) {
        List<AppStoreWrapBean> appStoreModelList = appStoreAdapter.getData();
        for (AppStoreWrapBean wrapBean : appStoreModelList) {
            if (wrapBean.appList != null) {
                for (AppModel storeApp : wrapBean.appList) {
                    for (TvAppStateModel tvAppStateModel : tvAppStateModels) {
                        if (storeApp.pkg.equals(tvAppStateModel.appinfo.pkgName)) {
                            if (tvAppStateModel.installed) {
                                storeApp.status = AppModel.STATE_INSTALLED;
                            } else {
                                if (tvAppStateModel.downloadStatus == 1) {
                                    storeApp.status = AppModel.STATE_INSTALLING;
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
        appStoreAdapter.notifyDataSetChanged();
    }

    private void resetAppStoreInstallState(){
        List<AppStoreWrapBean> data = appStoreAdapter.getData();
        for (AppStoreWrapBean datum : data) {
            for (AppModel appModel : datum.appList) {
                appModel.status = AppModel.STATE_UNINSTALL;
            }
        }
    }

    private void submitInstallAppEvent() {
        Map<String, String> eventMap = new HashMap<>();
        eventMap.put("page", "main");
        MobclickAgent.onEvent(getContext(), APP_INSTALL, eventMap);
    }
}
