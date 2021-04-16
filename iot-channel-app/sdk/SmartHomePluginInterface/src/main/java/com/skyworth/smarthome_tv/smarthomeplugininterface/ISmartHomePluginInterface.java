package com.skyworth.smarthome_tv.smarthomeplugininterface;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import java.io.Serializable;

/**
 * @ClassName: SmartHomePluginInterface
 * @Author: XuZeXiao
 * @CreateDate: 2020/6/10 19:06
 * @Description:
 */
public interface ISmartHomePluginInterface extends Serializable {

    /***
     * 设置Context
     * @param pluginContext 插件Context
     */
    void onContextSet(Context pluginContext);

    /**
     * 插件加载完成，初始化回调。
     */
    void onPluginInit();

    /**
     * 插件提供主页卡片View。
     *
     * @param callback View内部焦点边界回调
     * @return
     */
    View getContentCardView(IViewBoundaryCallback callback);

    /**
     * 插件提供智慧家庭App列表PanelView。
     *
     * @param callback View内部焦点边界回调
     * @return
     */
    View getPanelView(IViewBoundaryCallback callback);

    /**
     * 插件提供主页卡片View生命周期回调，供宿主调用。
     *
     * @return
     */
    LifeCycleCallback getContentLifeCycleCallback();

    /**
     * 插件提供智慧家庭App列表PanelView生命周期回调，供宿主调用。
     *
     * @return
     */
    LifeCycleCallback getPanelLifeCycleCallback();

    /**
     * 提供其他进程通过主页通知插件，传数据的能力
     *
     * @param bundle
     */
    void onDeliverPluginMessage(Bundle bundle);

    /**
     * 提供connector
     *
     * @param connector
     */
    void setSmartHomeConnector(ISmartHomeConnector connector);

    /**
     * 内容区域状态变化（展开/收缩）
     */
    void onShortcutStateChanged(@ShortcutState int state);
}
