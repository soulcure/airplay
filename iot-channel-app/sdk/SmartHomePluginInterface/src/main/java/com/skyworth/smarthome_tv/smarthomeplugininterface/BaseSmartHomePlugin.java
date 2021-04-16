package com.skyworth.smarthome_tv.smarthomeplugininterface;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

/**
 * @Description: 智家插件抽象基类，使用者可直接继承或者实现ISmartHomePluginInterface
 * @Author: wzh
 * @CreateDate: 2020/7/12
 */
public abstract class BaseSmartHomePlugin implements ISmartHomePluginInterface {

    protected Context pluginContext;
    protected ISmartHomeConnector connector;

    @Override
    public void onContextSet(Context pluginContext) {
        this.pluginContext = pluginContext;
    }

    @Override
    public void setSmartHomeConnector(ISmartHomeConnector connector) {
        this.connector = connector;
    }

    @Override
    public abstract void onPluginInit();

    @Override
    public View getContentCardView(IViewBoundaryCallback callback) {
        return null;
    }

    @Override
    public View getPanelView(IViewBoundaryCallback callback) {
        return null;
    }

    @Override
    public LifeCycleCallback getContentLifeCycleCallback() {
        return null;
    }

    @Override
    public LifeCycleCallback getPanelLifeCycleCallback() {
        return null;
    }

    @Override
    public void onDeliverPluginMessage(Bundle bundle) {

    }

    @Override
    public void onShortcutStateChanged(@ShortcutState int state) {

    }
}
