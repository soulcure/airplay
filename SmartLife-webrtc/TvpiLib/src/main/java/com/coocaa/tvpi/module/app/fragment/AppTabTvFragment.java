package com.coocaa.tvpi.module.app.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.data.app.AppModel;
import com.coocaa.smartscreen.data.app.TvAppModel;
import com.coocaa.tvpi.base.mvvm.BaseViewModelFragment;
import com.coocaa.tvpi.base.mvvm.view.DefaultLoadStateView;
import com.coocaa.tvpi.base.mvvm.view.LoadStateViewProvide;
import com.coocaa.tvpi.module.app.AppDetailActivity;
import com.coocaa.tvpi.module.app.adapter.AppTvAdapter;
import com.coocaa.tvpi.module.app.viewmodel.AppTabTvViewModel;
import com.coocaa.tvpi.module.app.viewmodel.share.AppHomeShareViewModel;
import com.coocaa.tvpi.module.app.widget.AppEditLayout;
import com.coocaa.tvpi.module.connection.ConnectDialogActivity;
import com.coocaa.tvpi.view.CommonTitleBar;
import com.coocaa.tvpilib.R;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.coocaa.tvpi.common.UMengEventId.APP_UNSTALL;

/**
 * 应用——电视应用Tab
 * Created by songxing on 2020/7/16
 */
public class AppTabTvFragment extends BaseViewModelFragment<AppTabTvViewModel> {
    private static final String TAG = AppTabTvFragment.class.getSimpleName();
    private DefaultLoadStateView loadStateView;
    private CommonTitleBar titleBar;
    private AppEditLayout appEditLayout;
    private RecyclerView rvApp;
    private AppTvAdapter appTvAdapter;

    private boolean isEditState;
    private AppHomeShareViewModel homeShareViewModel;

    @Override
    protected LoadStateViewProvide createLoadStateViewProvide() {
        return loadStateView;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_app_tab_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        setListener();
        getEditState();
        getInstalledApp(true);
        observerInstalledApp();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            Log.d(TAG, "onHiddenChanged: !hidden");
            getInstalledApp(false);
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

    private void initView(View view) {
        if(getActivity() == null) return;
        titleBar = view.findViewById(R.id.titleBar);
        loadStateView = view.findViewById(R.id.loadStateView);
        appEditLayout = view.findViewById(R.id.editLayout);
        rvApp = view.findViewById(R.id.rvApp);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
        appTvAdapter = new AppTvAdapter();
        rvApp.setLayoutManager(layoutManager);
        rvApp.setAdapter(appTvAdapter);
        homeShareViewModel = ViewModelProviders.of(getActivity()).get(AppHomeShareViewModel.class);
        homeShareViewModel.setEditState(false);
        showEditButton(false);
    }

    private void setListener(){
        titleBar.setOnClickListener(new CommonTitleBar.OnClickListener() {
            @Override
            public void onClick(CommonTitleBar.ClickPosition position) {
                if (position == CommonTitleBar.ClickPosition.LEFT) {
                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                } else if (position == CommonTitleBar.ClickPosition.RIGHT) {
                    homeShareViewModel.setEditState(!isEditState);
                }
            }
        });

        appTvAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                TvAppModel appModel = (TvAppModel) adapter.getData().get(position);
                AppDetailActivity.start(getContext(), new AppModel(appModel));
            }
        });

        appEditLayout.setEditListener(new AppEditLayout.EditListener() {
            @Override
            public void onUninstallClick() {
                if (!SSConnectManager.getInstance().isConnected()) {
                    showConnectDialog();
                    return;
                }

                List<TvAppModel> selectedApp = getSelectedApp();
                notifyItemRemove(selectedApp);
                viewModel.uninstallApp(selectedApp);
                homeShareViewModel.setEditState(false);
                MobclickAgent.onEvent(getContext(), APP_UNSTALL);
            }

            @Override
            public void onSelectAllClick(boolean isSelect) {
                List<TvAppModel> data = appTvAdapter.getData();
                for (TvAppModel datum : data) {
                    datum.isSelected = isSelect;
                }
                appTvAdapter.notifyDataSetChanged();
            }
        });

        appTvAdapter.setItemSelectListener(new AppTvAdapter.ItemSelectListener() {
            @Override
            public void onItemSelect() {
                int selectedSize = getSelectedApp().size();
                int totalSize = appTvAdapter.getData().size();
                appEditLayout.onSelectItemChange(selectedSize, totalSize);
            }
        });
    }

    private void getEditState() {
        homeShareViewModel.isEditState().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean editState) {
                Log.d(TAG, "isEditState onChanged: " + editState);
                isEditState = editState;
                titleBar.setText(CommonTitleBar.TextPosition.RIGHT_BUTTON, isEditState ? "完成" : "编辑");
                appEditLayout.setVisibility(isEditState ? View.VISIBLE : View.GONE);
                appTvAdapter.setEditState(isEditState);
            }
        });
    }

    private void getInstalledApp(boolean showLoading) {
        if (!SSConnectManager.getInstance().isConnected()) {
            showConnectDialog();
            return;
        }
        if(showLoading){
            viewModel.getInstalledAppWithLoading();
        }else {
            viewModel.getInstalledApp();
        }
    }

    //已安装的电视应用
    private void observerInstalledApp() {
        viewModel.getInstalledAppLiveData().observe(getViewLifecycleOwner(), installedAppObserver);
    }

    private  Observer<List<TvAppModel>> installedAppObserver = new Observer<List<TvAppModel>>() {
        @Override
        public void onChanged(List<TvAppModel> tvAppModels) {
            Log.d(TAG, "observerInstallApp onChanged: " + tvAppModels);
            showEditButton(tvAppModels != null && !tvAppModels.isEmpty());
            if (tvAppModels != null) {
                appTvAdapter.setList(tvAppModels);
            }
        }
    };

    private void notifyItemRemove(List<TvAppModel> selectedApp){
        List<TvAppModel> data = appTvAdapter.getData();
        for (TvAppModel tvAppModel : selectedApp) {
            data.remove(tvAppModel);
        }
        appTvAdapter.notifyDataSetChanged();
    }

    private void showEditButton(boolean show) {
        titleBar.setText(CommonTitleBar.TextPosition.RIGHT_BUTTON, show ? "编辑" : "");
    }

    private void showConnectDialog() {
        ConnectDialogActivity.start(getActivity());
    }

    private List<TvAppModel> getSelectedApp() {
        List<TvAppModel> selectedApp = new ArrayList<>();
        List<TvAppModel> data = appTvAdapter.getData();
        for (TvAppModel datum : data) {
            if (datum.isSelected) {
                selectedApp.add(datum);
            }
        }
        return selectedApp;
    }
}
