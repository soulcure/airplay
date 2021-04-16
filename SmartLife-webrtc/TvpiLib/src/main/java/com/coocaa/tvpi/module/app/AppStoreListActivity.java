package com.coocaa.tvpi.module.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.data.app.AppModel;
import com.coocaa.smartscreen.data.app.TvAppStateModel;
import com.coocaa.tvpi.base.mvvm.BaseViewModelActivity;
import com.coocaa.tvpi.base.mvvm.view.DefaultLoadStateView;
import com.coocaa.tvpi.base.mvvm.view.LoadStateViewProvide;
import com.coocaa.tvpi.module.app.adapter.AppStoreListAdapter;
import com.coocaa.tvpi.module.app.viewmodel.AppStoreListViewModel;
import com.coocaa.tvpi.module.connection.ConnectDialogActivity;
import com.coocaa.tvpi.module.viewmodel.ApplicationShareViewModel;
import com.coocaa.tvpi.view.CommonTitleBar;
import com.coocaa.tvpi.view.CustomFooter;
import com.coocaa.tvpi.view.CustomHeader;
import com.coocaa.tvpi.view.decoration.CommonVerticalItemDecoration;
import com.coocaa.tvpilib.R;
import com.liaoinstan.springview.widget.SpringView;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.coocaa.tvpi.common.UMengEventId.APP_INSTALL;

/**
 * 应用商城更多列表
 * Created by songxing on 2020/7/16
 */
public class AppStoreListActivity extends BaseViewModelActivity<AppStoreListViewModel> {
    private static final String TAG = AppStoreListActivity.class.getSimpleName();
    private static final int FIRST_PAGE_INDEX = 1;

    private DefaultLoadStateView loadStateView;
    private CommonTitleBar titleBar;
    private SpringView springView;
    private RecyclerView rvAppStore;

    private AppStoreListAdapter appAdapter;
    private String classId;
    private String className;
    private int pageIndex = FIRST_PAGE_INDEX;
    private int pageSize = 15;
    private boolean isHasMoreData;

    private List<AppModel> appStoreModelList = new ArrayList<>();   //列表数据

    private ApplicationShareViewModel appShareViewModel;

    public static void start(Context context, String classId, String className) {
        Intent starter = new Intent(context, AppStoreListActivity.class);
        starter.putExtra("classId", classId);
        starter.putExtra("className", className);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_store_list);
        parserIntent();
        initView();
        setListener();
        getAppStoreList(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(TAG);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(TAG);
    }

    @Override
    protected LoadStateViewProvide createLoadStateViewProvide() {
        return loadStateView;
    }

    private void parserIntent() {
        if (getIntent() != null) {
            classId = getIntent().getStringExtra("classId");
            className = getIntent().getStringExtra("className");
        }
    }

    private void initView() {
        loadStateView = findViewById(R.id.loadStateView);
        titleBar = findViewById(R.id.titleBar);
        springView = findViewById(R.id.springView);
        rvAppStore = findViewById(R.id.rvAppStore);
        titleBar.setText(CommonTitleBar.TextPosition.TITLE, className);
        springView.setHeader(new CustomHeader(this));
        springView.setFooter(new CustomFooter(this));
        CommonVerticalItemDecoration decoration = new CommonVerticalItemDecoration(
                DimensUtils.dp2Px(this, 15),
                DimensUtils.dp2Px(this, 15));
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                RecyclerView.VERTICAL, false);
        appAdapter = new AppStoreListAdapter();
        rvAppStore.addItemDecoration(decoration);
        rvAppStore.setLayoutManager(layoutManager);
        rvAppStore.setAdapter(appAdapter);
        appShareViewModel = getAppViewModelProvider().get(ApplicationShareViewModel.class);
    }

    private void setListener() {
        titleBar.setOnClickListener(new CommonTitleBar.OnClickListener() {
            @Override
            public void onClick(CommonTitleBar.ClickPosition position) {
                if (position == CommonTitleBar.ClickPosition.LEFT) {
                    finish();
                }
            }
        });

        springView.setListener(new SpringView.OnFreshListener() {
            @Override
            public void onRefresh() {
                pageIndex = FIRST_PAGE_INDEX;
                getAppStoreList(false);
            }

            @Override
            public void onLoadmore() {
                if (isHasMoreData) {
                    pageIndex++;
                    getAppStoreList(false);
                } else {
                    springView.onFinishFreshAndLoad();
                    ToastUtils.getInstance().showGlobalShort(getResources().getString(R.string.pull_no_more_msg));
                }
            }
        });

        appAdapter.setStateButtonClickListener(new AppStoreListAdapter.StateButtonClickListener() {
            @Override
            public void onStateButtonClick(AppModel appModel, int position) {
                if (!SSConnectManager.getInstance().isConnected()) {
                    ConnectDialogActivity.start(AppStoreListActivity.this);
                    return;
                }
                if (appModel.status == AppModel.STATE_UNINSTALL) {
                    viewModel.installApp(appModel);
                    appModel.status = AppModel.STATE_INSTALLING;
                    appAdapter.notifyItemChanged(position);
                    appShareViewModel.addInstallingApp(appModel);
                } else if (appModel.status == AppModel.STATE_INSTALLED) {
                    viewModel.startApp(appModel);
                    submitInstallAppEvent();
                }
            }
        });
    }

    private void getAppStoreList(boolean showLoading) {
        viewModel.getAppStoreList(showLoading, classId, pageIndex, pageSize).observe(this, appStoreObserver);
    }

    //获取商城电视应用列表
    private Observer<List<AppModel>> appStoreObserver = new Observer<List<AppModel>>() {
        @Override
        public void onChanged(List<AppModel> appModels) {
            if (pageIndex == FIRST_PAGE_INDEX) {     //下拉刷新
                appStoreModelList.clear();
            }
            if (appModels != null) {
                appStoreModelList.addAll(appModels);
                appAdapter.setList(appStoreModelList);
                getAppInstallState();
                observerInstalledAppState();
                observerInstallingAppState();
            }
            isHasMoreData = appModels != null && appModels.size() == pageSize;
            springView.onFinishFreshAndLoad();
        }
    };

    //获取app的安装状态
    private void getAppInstallState() {
        viewModel.getAppState(appStoreModelList);
    }

    //获取app的状态
    private void observerInstalledAppState() {
        viewModel.getAppInstallStateLiveData().observe(this,installedAppStateObserver);
    }

    private Observer<List<TvAppStateModel>> installedAppStateObserver = new Observer<List<TvAppStateModel>>() {
        @Override
        public void onChanged(List<TvAppStateModel> tvAppStateModels) {
            Log.d(TAG, "installedAppStateObserver onChanged: " + tvAppStateModels);
            updateAppState(tvAppStateModels);
        }
    };

    //获取下载中的app的安装状态
    private void observerInstallingAppState() {
        appShareViewModel.getInstallingAppStateLiveData().observe(this,installingAppStateObserver);
    }

    private Observer<List<TvAppStateModel>> installingAppStateObserver = new Observer<List<TvAppStateModel>>() {
        @Override
        public void onChanged(List<TvAppStateModel> tvAppStateModels) {
            Log.d(TAG, "onChanged: installingAppStateObserver" + tvAppStateModels);
            updateAppState(tvAppStateModels);
        }
    };

    //更新应用列表安装状态
    private void updateAppState(List<TvAppStateModel> tvAppStateModels) {
        for (AppModel storeApp : appStoreModelList) {
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
        appAdapter.notifyDataSetChanged();
    }

    private void submitInstallAppEvent() {
        Map<String, String> eventMap = new HashMap<>();
        eventMap.put("page", "category");
        MobclickAgent.onEvent(AppStoreListActivity.this, APP_INSTALL, eventMap);
    }
}
