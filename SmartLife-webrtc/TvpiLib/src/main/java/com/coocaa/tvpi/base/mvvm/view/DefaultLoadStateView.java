package com.coocaa.tvpi.base.mvvm.view;

import android.content.Context;
import android.util.AttributeSet;

import com.coocaa.tvpi.view.LoadTipsView;


public class DefaultLoadStateView extends LoadTipsView implements LoadStateViewProvide {


    public DefaultLoadStateView(Context context) {
        super(context);
    }

    public DefaultLoadStateView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DefaultLoadStateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public void showLoadingView() {
        setLoadTipsIV(LoadTipsView.TYPE_LOADING);
        setVisibility(VISIBLE);
    }

    @Override
    public void showLoadingErrorView(String errorMsg) {
        setTipsText(errorMsg);
        setLoadTipsIV(LoadTipsView.TYPE_FAILED);
        setVisibility(VISIBLE);
    }

    @Override
    public void showLoadFinishView() {
        showLoadingComplete();
        setVisibility(GONE);
    }

    @Override
    public void showListEmptyView() {
        setLoadTipsIV(LoadTipsView.TYPE_NODATA);
        setVisibility(VISIBLE);
    }
}
