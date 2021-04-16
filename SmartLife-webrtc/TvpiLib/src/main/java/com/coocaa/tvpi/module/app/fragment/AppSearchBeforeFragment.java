package com.coocaa.tvpi.module.app.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.data.app.AppModel;
import com.coocaa.smartscreen.data.app.TvAppStateModel;
import com.coocaa.tvpi.base.mvvm.BaseViewModelFragment;
import com.coocaa.tvpi.base.mvvm.view.DefaultLoadStateView;
import com.coocaa.tvpi.base.mvvm.view.LoadStateViewProvide;
import com.coocaa.tvpi.module.app.adapter.AppSearchBeforeAdapter;
import com.coocaa.tvpi.module.app.bean.AppSearchBeforeWrapBean;
import com.coocaa.tvpi.module.app.viewmodel.AppSearchBeforeViewModel;
import com.coocaa.tvpi.module.app.viewmodel.share.AppSearchShareViewModel;
import com.coocaa.tvpi.module.connection.ConnectDialogActivity;
import com.coocaa.tvpi.module.viewmodel.ApplicationShareViewModel;
import com.coocaa.tvpilib.R;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.coocaa.tvpi.common.UMengEventId.APP_INSTALL;

/**
 * 应用搜索搜索前Fragment
 * Created by songxing on 2020/8/10
 */
public class AppSearchBeforeFragment extends BaseViewModelFragment<AppSearchBeforeViewModel> {
    private static final String TAG = AppSearchBeforeFragment.class.getSimpleName();
    private DefaultLoadStateView loadStateView;
    private AppSearchBeforeAdapter searchBeforeAdapter;
    private AppSearchShareViewModel searchShareViewModel;   //搜索Activity共享的ViewModel
    private ApplicationShareViewModel appShareViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_app_search_before, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        getSearchBeforeList();
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
        if (getActivity() == null) return;
        loadStateView = view.findViewById(R.id.loadStateView);
        RecyclerView rvSearchBefore = view.findViewById(R.id.rvSearchBefore);
        searchBeforeAdapter = new AppSearchBeforeAdapter();
        RecyclerView.LayoutManager manager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        rvSearchBefore.setLayoutManager(manager);
        rvSearchBefore.setAdapter(searchBeforeAdapter);
        searchShareViewModel = ViewModelProviders.of(getActivity()).get(AppSearchShareViewModel.class);
        appShareViewModel = getAppViewModelProvider().get(ApplicationShareViewModel.class);
        searchBeforeAdapter.setSearchBeforeListener(new AppSearchBeforeAdapter.SearchBeforeListener() {
            @Override
            public void onAppClick(AppModel appModel) {
                if (!SSConnectManager.getInstance().isConnected()) {
                    ConnectDialogActivity.start(getActivity());
                    return;
                }
                if (appModel.status == AppModel.STATE_UNINSTALL) {
                    viewModel.installApp(appModel);
                    appModel.status = AppModel.STATE_INSTALLING;
                    searchBeforeAdapter.notifyDataSetChanged();
                    appShareViewModel.addInstallingApp(appModel);
                } else if (appModel.status == AppModel.STATE_INSTALLED) {
                    viewModel.startApp(appModel);
                    submitInstallAppEvent();
                }
            }

            @Override
            public void onHistorySearchClick(String history) {
                searchShareViewModel.setSearchKeyword(history);
                searchShareViewModel.setShowSearchBefore(false);
            }

            @Override
            public void clearHistorySearch() {
                viewModel.clearHistoryList();
                searchBeforeAdapter.getData().remove(0);
                searchBeforeAdapter.notifyDataSetChanged();
            }
        });
    }

    private void getSearchBeforeList() {
        viewModel.getSearchBeforeList().observe(getViewLifecycleOwner(), new Observer<List<AppSearchBeforeWrapBean>>() {
            @Override
            public void onChanged(List<AppSearchBeforeWrapBean> appSearchBeforeWrapBeans) {
                searchBeforeAdapter.setList(appSearchBeforeWrapBeans);
                getAppInstallState();
                observerInstalledAppState();
                observerInstallingAppState();
            }
        });
    }

    //获取app的安装状态
    private void getAppInstallState() {
        List<AppModel> appModelList = new ArrayList<>();
        List<AppSearchBeforeWrapBean> appSearchBeforeWrapBeans = searchBeforeAdapter.getData();
        for (AppSearchBeforeWrapBean appSearchBeforeWrapBean : appSearchBeforeWrapBeans) {
            appModelList.addAll(appSearchBeforeWrapBean.recommend);
        }
        viewModel.getAppState(appModelList);
    }


    //电视机返回的app的状态
    private void observerInstalledAppState() {
        viewModel.getAppInstallStateLiveData().observe(getViewLifecycleOwner(), new Observer<List<TvAppStateModel>>() {
            @Override
            public void onChanged(List<TvAppStateModel> tvAppStateModels) {
                Log.d(TAG, "observerAppState onChanged: " + tvAppStateModels);
                updateAppState(tvAppStateModels);
            }
        });
    }

    //正在下载app的安装状态
    private void observerInstallingAppState() {
        appShareViewModel.getInstallingAppStateLiveData().observe(getViewLifecycleOwner(), new Observer<List<TvAppStateModel>>() {
            @Override
            public void onChanged(List<TvAppStateModel> tvAppStateModels) {
                updateAppState(tvAppStateModels);
            }
        });
    }

    //更新应用列表安装状态
    private void updateAppState(List<TvAppStateModel> tvAppStateModels) {
        List<AppSearchBeforeWrapBean> searchBeforeList = searchBeforeAdapter.getData();
        for (AppSearchBeforeWrapBean appSearchBeforeWrapBean : searchBeforeList) {
            if (appSearchBeforeWrapBean != null) {
                for (AppModel appModel : appSearchBeforeWrapBean.getRecommend()) {
                    for (TvAppStateModel tvAppStateModel : tvAppStateModels) {
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
                }
            }
        }
        searchBeforeAdapter.notifyDataSetChanged();
    }

    private void submitInstallAppEvent() {
        Map<String, String> eventMap = new HashMap<>();
        eventMap.put("page", "search");
        MobclickAgent.onEvent(getContext(), APP_INSTALL, eventMap);
    }

}
