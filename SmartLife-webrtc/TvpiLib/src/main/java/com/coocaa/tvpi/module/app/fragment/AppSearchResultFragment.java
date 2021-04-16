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
import com.coocaa.tvpi.base.mvvm.view.DefaultLoadStateView;
import com.coocaa.tvpi.base.mvvm.view.LoadStateViewProvide;
import com.coocaa.tvpi.module.app.adapter.AppStoreListAdapter;
import com.coocaa.tvpi.module.app.viewmodel.AppSearchResultViewModel;
import com.coocaa.tvpi.module.app.viewmodel.share.AppSearchShareViewModel;
import com.coocaa.tvpi.module.connection.ConnectDialogActivity;
import com.coocaa.tvpi.module.viewmodel.ApplicationShareViewModel;
import com.coocaa.tvpi.view.decoration.CommonVerticalItemDecoration;
import com.coocaa.tvpilib.R;
import com.umeng.analytics.MobclickAgent;

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
 * 应用搜索结果界面
 * Created by songxing on 2020/8/10
 */
public class AppSearchResultFragment extends BaseViewModelFragment<AppSearchResultViewModel> {
    private static final String TAG = AppSearchResultFragment.class.getSimpleName();
    private DefaultLoadStateView loadStateView;
    private AppStoreListAdapter appAdapter;
    private AppSearchShareViewModel searchShareViewModel;
    private ApplicationShareViewModel appShareViewModel;

    private String keyword;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_app_search_result, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        search();
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
        if(getActivity() == null) return;
        loadStateView = view.findViewById(R.id.loadStateView);
        RecyclerView rvSearch = view.findViewById(R.id.rvSearchResult);
        LinearLayoutManager manager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        CommonVerticalItemDecoration decoration = new CommonVerticalItemDecoration(DimensUtils.dp2Px(getContext(), 10));
        appAdapter = new AppStoreListAdapter();
        rvSearch.setLayoutManager(manager);
        rvSearch.addItemDecoration(decoration);
        rvSearch.setAdapter(appAdapter);
        appAdapter.setEmptyView(LayoutInflater.from(getContext()).inflate(R.layout.empty_search_result,rvSearch,false));
        searchShareViewModel = ViewModelProviders.of(getActivity()).get(AppSearchShareViewModel.class);
        appShareViewModel = getAppViewModelProvider().get(ApplicationShareViewModel.class);
        appAdapter.setStateButtonClickListener(new AppStoreListAdapter.StateButtonClickListener() {
            @Override
            public void onStateButtonClick(AppModel appModel, int position) {
                if (!SSConnectManager.getInstance().isConnected()) {
                    ConnectDialogActivity.start(getActivity());
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

    private void search() {
        searchShareViewModel.getKeywordLiveData().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                keyword = s;
                doSearch();
            }
        });
    }

    private void doSearch() {
        viewModel.search(keyword).observe(getViewLifecycleOwner(), searchResultObserver);
    }

    private Observer<List<AppModel>> searchResultObserver = new Observer<List<AppModel>>() {
        @Override
        public void onChanged(List<AppModel> appModels) {
            Log.d(TAG, "searchResultObserver onChanged: " + appModels);
            appAdapter.setList(appModels);
            getAppInstallState();
            observerInstalledAppState();
            observerInstallingAppState();
        }
    };


    //获取app的安装状态
    private void getAppInstallState(){
        viewModel.getAppState(appAdapter.getData());
    }

    //电视机返回的app的状态
    private void observerInstalledAppState(){
        viewModel.getAppInstallStateLiveData().observe(getViewLifecycleOwner(), installedAppStateObserver);
    }

    private Observer<List<TvAppStateModel>> installedAppStateObserver = new Observer<List<TvAppStateModel>>() {
        @Override
        public void onChanged(List<TvAppStateModel> tvAppStateModels) {
            Log.d(TAG, "installedAppStateObserver onChanged: " + tvAppStateModels);
            updateAppState(tvAppStateModels);
        }
    };

    //下载中的app的安装状态
    private void observerInstallingAppState(){
        appShareViewModel.getInstallingAppStateLiveData().observe(getViewLifecycleOwner(), installingAppStateObserver);
    }

    private Observer<List<TvAppStateModel>> installingAppStateObserver = new Observer<List<TvAppStateModel>>() {
        @Override
        public void onChanged(List<TvAppStateModel> tvAppStateModels) {
            Log.d(TAG, "installingAppStateObserver onChanged: " + tvAppStateModels);
            updateAppState(tvAppStateModels);
        }
    };


    //更新应用列表安装状态
    private  void updateAppState(List<TvAppStateModel> tvAppStateModels) {
        List<AppModel> searchResultList = appAdapter.getData();
        for (AppModel resultApp : searchResultList) {
            for (TvAppStateModel tvAppStateModel : tvAppStateModels) {
                if (resultApp.pkg.equals(tvAppStateModel.appinfo.pkgName)) {
                    if(tvAppStateModel.installed){
                        resultApp.status = AppModel.STATE_INSTALLED;
                    }else {
                        if(tvAppStateModel.downloadStatus == 1){
                            resultApp.status = AppModel.STATE_INSTALLING;
                        }
                    }
                    break;
                }
            }
        }
        appAdapter.notifyDataSetChanged();
    }

    private void submitInstallAppEvent(){
        Map<String, String> eventMap = new HashMap<>();
        eventMap.put("page", "search");
        MobclickAgent.onEvent(getContext(), APP_INSTALL, eventMap);
    }
}
