package com.coocaa.tvpi.base.mvvm.view;

/**
 * 数据加载状态接口
 * Created by songxing on 2020/7/12
 */
public interface LoadStateViewProvide {
    /**
     * 显示加载中视图
     */
    void showLoadingView();

    /**
     * 显示加载完成视图
     */
    void showLoadFinishView();

    /**
     * 显示加载列表为空的界面
     */
    void showListEmptyView();

    /**
     * 显示加载异常视图
     *
     * @param errorMsg 异常信息
     */
    void showLoadingErrorView(String errorMsg);

}
