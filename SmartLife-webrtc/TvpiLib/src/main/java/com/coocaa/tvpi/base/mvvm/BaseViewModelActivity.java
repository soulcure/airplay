package com.coocaa.tvpi.base.mvvm;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.coocaa.tvpi.base.mvvm.view.LoadStateViewProvide;
import com.coocaa.tvpi.util.GenericsUtils;
import com.coocaa.tvpi.util.StatusBarHelper;

/**
 * Activity基类
 * 1.创建对应泛型类型的ViewModel实例{@link #initViewModel()}.
 * 2.注册BaseViewModel中有关加载状态的LiveData的观察{@link #initLoadStateObserver()}.
 * 3.实现加载状态接口 {@link LoadStateViewProvide}
 *
 * @param <VM> ViewModel类型
 * Created by songxing on 2020/7/12
 */
public abstract class BaseViewModelActivity<VM extends BaseViewModel> extends BaseViewModelProvideActivity implements LoadStateViewProvide {
    private static final String TAG = BaseViewModelActivity.class.getSimpleName();

    protected VM viewModel;
    protected LoadStateViewProvide loadStateViewProvide;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initStatusBar();
        initViewModel();
        initLoadStateObserver();
    }

    private void initStatusBar() {
        StatusBarHelper.translucent(this);
        StatusBarHelper.setStatusBarLightMode(this);
    }

    protected void initViewModel() {
        Class<VM> superClassGenericType = GenericsUtils.getSuperClassGenericType(getClass());
        viewModel = ViewModelProviders.of(this).get(superClassGenericType);
    }

    protected void initLoadStateObserver() {
        viewModel.loadStateLiveData.observe(this, new Observer<BaseViewModel.LoadState>() {
            @Override
            public void onChanged(BaseViewModel.LoadState state) {
                switch (state) {
                    case LOADING:
                        showLoadingView();
                        break;
                    case LOADING_DIALOG:
                        showLoadingDialog();
                        break;
                    case LOAD_FINISH:
                        showLoadFinishView();
                        dismissLoadingDialog();
                        break;
                    case LOAD_ERROR:
                        showLoadingErrorView("");
                        break;
                    case LOAD_LIST_EMPTY:
                        showListEmptyView();
                        break;
                    default:
                        break;
                }
            }
        });
    }


    @Override
    public void showLoadingView() {
        loadStateViewProvide = createLoadStateViewProvide();
        if (loadStateViewProvide != null) {
            loadStateViewProvide.showLoadingView();
        }
    }

    @Override
    public void showLoadFinishView() {
        loadStateViewProvide = createLoadStateViewProvide();
        if (loadStateViewProvide != null) {
            loadStateViewProvide.showLoadFinishView();
        }
    }

    @Override
    public void showLoadingErrorView(String errorMsg) {
        loadStateViewProvide = createLoadStateViewProvide();
        if (loadStateViewProvide != null) {
            loadStateViewProvide.showLoadingErrorView(errorMsg);
        }
    }

    @Override
    public void showListEmptyView() {
        loadStateViewProvide = createLoadStateViewProvide();
        if (loadStateViewProvide != null) {
            loadStateViewProvide.showListEmptyView();
        }
    }

    public void showLoadingDialog() {
        showLoading();
    }

    public void dismissLoadingDialog() {
        dismissLoading();
    }


    protected LoadStateViewProvide createLoadStateViewProvide() {
        return null;
    }
}
