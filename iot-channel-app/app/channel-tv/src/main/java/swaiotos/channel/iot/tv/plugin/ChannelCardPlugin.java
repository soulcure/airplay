package swaiotos.channel.iot.tv.plugin;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.skyworth.smarthome_tv.smarthomeplugininterface.ISmartHomeConnector;
import com.skyworth.smarthome_tv.smarthomeplugininterface.ISmartHomePluginInterface;
import com.skyworth.smarthome_tv.smarthomeplugininterface.IViewBoundaryCallback;
import com.skyworth.smarthome_tv.smarthomeplugininterface.LifeCycleCallback;

import swaiotos.channel.iot.utils.AndroidLog;

/**
 * @ProjectName: iot-channel-app
 * @Package: swaiotos.channel.iot.tv.plugin
 * @ClassName: ChannelCardPlugin
 * @Description: java类作用描述
 * @Author: wangyuehui
 * @CreateDate: 2020/8/20 10:27
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/8/20 10:27
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class ChannelCardPlugin implements ISmartHomePluginInterface, LifeCycleCallback {
    private Context mPluginContext;
    private ChannelCardView mChannelCardView;
    private ChannelPanelView mChannelPanelView;

    @Override
    public void onContextSet(Context pluginContext) {
        mPluginContext =  pluginContext;
    }

    @Override
    public void onPluginInit() {

    }

    @Override
    public View getContentCardView(IViewBoundaryCallback callback) {
        AndroidLog.androidLog("---getContentCardView---:"+mChannelCardView);
        if (mChannelCardView == null)
            mChannelCardView= new ChannelCardView(mPluginContext,callback);
        return mChannelCardView;
    }

    @Override
    public View getPanelView(IViewBoundaryCallback callback) {
        AndroidLog.androidLog("---getPanelView---:"+mChannelCardView);
        if (mChannelPanelView == null)
            mChannelPanelView = new ChannelPanelView(mPluginContext,callback);
        return mChannelPanelView;
    }

    @Override
    public LifeCycleCallback getContentLifeCycleCallback() {
        return this;
    }

    @Override
    public LifeCycleCallback getPanelLifeCycleCallback() {
        return this;
    }

    @Override
    public void onDeliverPluginMessage(Bundle bundle) {
        AndroidLog.androidLog(" ------- "+bundle);
        String push_TYPE = bundle.getString("PUSH_TYPE");//省略，添加自己需要的其他数据
        if (mChannelCardView != null && !TextUtils.isEmpty(push_TYPE) && "swaiotos.channel.iot".equals(push_TYPE))
            mChannelCardView.onDeliverPluginMessage(bundle);
    }

    @Override
    public void setSmartHomeConnector(ISmartHomeConnector connector) {

    }

    @Override
    public void onShortcutStateChanged(int state) {
        AndroidLog.androidLog(" -------state:"+state);
        if (mChannelCardView != null)
            mChannelCardView.onShortcutDataChanged(state);

        if (mChannelPanelView != null)
            mChannelPanelView.onShortcutDataChanged(state);
    }

    @Override
    public void onResume() {
        if (mChannelCardView != null)
            mChannelCardView.onResume();
        if (mChannelPanelView != null)
            mChannelPanelView.onResume();
    }

    @Override
    public void onPause() {
        if (mChannelCardView != null)
            mChannelCardView.onPause();
        if (mChannelPanelView != null)
            mChannelPanelView.onPause();
    }

    @Override
    public void onStop() {
        if (mChannelCardView != null)
            mChannelCardView.onStop();

        if (mChannelPanelView != null)
            mChannelPanelView.onStop();
    }

    @Override
    public void onDestroy() {
        if (mChannelCardView != null)
            mChannelCardView.onDestroy();

        if (mChannelPanelView != null)
            mChannelPanelView.onDestroy();

        mChannelCardView = null;
        mChannelPanelView = null;
    }
}
