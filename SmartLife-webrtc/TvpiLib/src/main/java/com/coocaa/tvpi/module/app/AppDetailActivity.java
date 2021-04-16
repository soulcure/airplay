package com.coocaa.tvpi.module.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.data.app.AppModel;
import com.coocaa.smartscreen.data.app.TvAppStateModel;
import com.coocaa.tvpi.base.mvvm.BaseViewModelActivity;
import com.coocaa.tvpi.base.mvvm.view.DefaultLoadStateView;
import com.coocaa.tvpi.base.mvvm.view.LoadStateViewProvide;
import com.coocaa.tvpi.module.connection.ConnectDialogActivity;
import com.coocaa.tvpi.module.app.adapter.AppDetailAdapter;
import com.coocaa.tvpi.module.app.bean.AppDetailWrapBean;
import com.coocaa.tvpi.module.app.viewmodel.AppDetailViewModel;
import com.coocaa.tvpi.module.viewmodel.ApplicationShareViewModel;
import com.coocaa.tvpi.view.CommonTitleBar;
import com.coocaa.tvpilib.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 应用详情
 * Created by songxing on 2020/9/18
 */
public class AppDetailActivity extends BaseViewModelActivity<AppDetailViewModel> {
    private static final String TAG = AppDetailActivity.class.getSimpleName();

    private CommonTitleBar titleBar;
    private DefaultLoadStateView loadStateView;
    private AppDetailAdapter appDetailAdapter;
    private AppModel appModel;

    private ApplicationShareViewModel appShareViewModel;

    public static void start(Context context, AppModel appModel) {
        Intent starter = new Intent(context, AppDetailActivity.class);
        starter.putExtra("appModel", appModel);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_detail);
        parserIntent();
        initView();
        setListener();
        getAppDetail();
    }

    @Override
    protected LoadStateViewProvide createLoadStateViewProvide() {
        return loadStateView;
    }

    private void parserIntent() {
        if (getIntent() != null) {
            appModel = (AppModel) getIntent().getSerializableExtra("appModel");
        }
    }

    private void initView() {
        titleBar = findViewById(R.id.titleBar);
        loadStateView = findViewById(R.id.loadStateView);
        RecyclerView rvDetail = findViewById(R.id.rvDetail);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        rvDetail.setLayoutManager(manager);
        appDetailAdapter = new AppDetailAdapter();
        rvDetail.setAdapter(appDetailAdapter);
        appShareViewModel = getAppViewModelProvider().get(ApplicationShareViewModel.class);
    }

    private void setListener() {
        titleBar.setOnClickListener(new CommonTitleBar.OnClickListener() {
            @Override
            public void onClick(CommonTitleBar.ClickPosition position) {
                if(position == CommonTitleBar.ClickPosition.LEFT){
                    finish();
                }
            }
        });

        appDetailAdapter.setStateButtonListener(new AppDetailAdapter.StateButtonClickListener() {
            @Override
            public void onStateButtonClick(AppModel appModel, int position) {
                if (!SSConnectManager.getInstance().isConnected()) {
                    ConnectDialogActivity.start(AppDetailActivity.this);
                    return;
                }
                if (appModel.status == AppModel.STATE_UNINSTALL) {
                    viewModel.installApp(appModel);
                    appModel.status = AppModel.STATE_INSTALLING;
                    appDetailAdapter.notifyItemChanged(position);
                    //加入正在安装的应用轮询列表中
                    appShareViewModel.addInstallingApp(appModel);
                } else if (appModel.status == AppModel.STATE_INSTALLED) {
                    viewModel.startApp(appModel);
                }
            }
        });
    }

    private void getAppDetail() {
        viewModel.getAppDetail(appModel).observe(this, new Observer<List<AppDetailWrapBean>>() {
            @Override
            public void onChanged(List<AppDetailWrapBean> appDetailWrapBeans) {
                appDetailAdapter.setList(appDetailWrapBeans);
                getAppInstallState();
                observerInstalledAppState();
                observerInstallingAppState();
            }
        });
    }

    //获取app的安装状态
    private void getAppInstallState() {
        List<AppDetailWrapBean> data = appDetailAdapter.getData();
        List<AppModel> appModelList = new ArrayList<>();
        for (AppDetailWrapBean datum : data) {
            if (datum.getAppModel() != null) {
                appModelList.add(datum.getAppModel());
            } else {
                if (datum.getRecommendDataList() != null && !datum.getRecommendDataList().isEmpty()) {
                    appModelList.addAll(datum.getRecommendDataList());
                }
            }
        }
        viewModel.getAppState(appModelList);
    }

    //电视机返回的app的状态
    private void observerInstalledAppState() {
        viewModel.getAppInstallStateLiveData().observe(this, new Observer<List<TvAppStateModel>>() {
            @Override
            public void onChanged(List<TvAppStateModel> tvAppStateModels) {
                Log.d(TAG, "observerInstalledAppState onChanged: " + tvAppStateModels);
                updateAppState(tvAppStateModels);
            }
        });
    }

    //下载中app的安装状态
    private void observerInstallingAppState() {
        appShareViewModel.getInstallingAppStateLiveData().observe(this, new Observer<List<TvAppStateModel>>() {
            @Override
            public void onChanged(List<TvAppStateModel> tvAppStateModels) {
                Log.d(TAG, "observerInstallingAppState onChanged: " + tvAppStateModels);
                updateAppState(tvAppStateModels);
            }
        });
    }

    //更新应用列表安装状态
    private void updateAppState(List<TvAppStateModel> tvAppStateModels) {
        for (TvAppStateModel tvAppStateModel : tvAppStateModels) {
            if(tvAppStateModel.appinfo == null || TextUtils.isEmpty(tvAppStateModel.appinfo.pkgName)){
                continue;
            }
            List<AppDetailWrapBean> data = appDetailAdapter.getData();
            for (AppDetailWrapBean wrapBean : data) {
                //更新详情安装状态
                if (wrapBean.getAppModel() != null) {
                    if (wrapBean.getAppModel().pkg.equals(tvAppStateModel.appinfo.pkgName)) {
                        if (tvAppStateModel.installed) {
                            wrapBean.getAppModel().status = AppModel.STATE_INSTALLED;
                        } else {
                            if (tvAppStateModel.downloadStatus == 1) {
                                wrapBean.getAppModel().status = AppModel.STATE_INSTALLING;
                            }
                        }
                        break;
                    }
                    //更新推荐列表安装状态
                } else if (wrapBean.getRecommendDataList() != null && !wrapBean.getRecommendDataList().isEmpty()) {
                    for (AppModel appModel : wrapBean.getRecommendDataList()) {
                        if (appModel.pkg.equals(tvAppStateModel.appinfo.pkgName)) {
                            if (tvAppStateModel.installed) {
                                appModel.status = AppModel.STATE_INSTALLED;
                            } else {
                                if (tvAppStateModel.downloadStatus == 1) {
                                    appModel.status = AppModel.STATE_INSTALLING;
                                }
                            }
                            break;
                        }
                    }
                } else {
                    Log.d(TAG, "updateAppState: error");
                }
            }
        }
        appDetailAdapter.notifyDataSetChanged();

    }

}
