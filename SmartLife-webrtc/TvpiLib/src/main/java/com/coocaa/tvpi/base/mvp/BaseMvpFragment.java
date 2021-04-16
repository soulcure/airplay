package com.coocaa.tvpi.base.mvp;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.coocaa.tvpi.base.BaseFragment;
import com.coocaa.tvpi.base.mvp.proxy.ProxyFragment;


public abstract class BaseMvpFragment extends BaseFragment implements IBaseView {

    private ProxyFragment mProxyFragment;

    protected abstract @LayoutRes
    int setLayout();

    protected abstract void initViews(@Nullable Bundle savedInstanceState);

    protected abstract void initData();

    @SuppressWarnings("ConstantConditions")
    protected <T extends View> T $(@IdRes int viewId) {
        return this.getView().findViewById(viewId);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(setLayout(), container, false);

        mProxyFragment = createProxyFragment();
        mProxyFragment.bindPresenter();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(savedInstanceState);
        initData();
    }

    @SuppressWarnings("unchecked")
    private ProxyFragment createProxyFragment() {
        if (mProxyFragment == null) {
            return new ProxyFragment<>(this);
        }
        return mProxyFragment;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mProxyFragment.unbindPresenter();
    }
}
