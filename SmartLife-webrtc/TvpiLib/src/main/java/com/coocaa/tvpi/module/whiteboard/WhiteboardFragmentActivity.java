package com.coocaa.tvpi.module.whiteboard;

import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.fragment.app.FragmentTransaction;

import com.alibaba.fastjson.JSON;
import com.coocaa.publib.base.BaseActionBarAppletActivity;
import com.coocaa.tvpi.module.base.UnVirtualInputable;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.whiteboard.client.WhiteBoardClientSSEvent;
import com.coocaa.whiteboard.config.WhiteBoardConfig;
import com.coocaa.whiteboard.server.WhiteBoardServerSSCmd;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import swaiotos.sensor.channel.ChannelMsgSender;
import swaiotos.sensor.server.data.ServerInfo;

/**
 * 使用fragment实现的，备份使用
 */
public class WhiteboardFragmentActivity extends BaseActionBarAppletActivity implements UnVirtualInputable {

    FrameLayout layout;
    private ChannelMsgSender msgSender;
    WhiteBoardSplashFragment splashFragment;
    private final static int ID_CONTENT = 0x1001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = "WBClient";
        layout = new FrameLayout(this);
        layout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        layout.setId(ID_CONTENT);
        setContentView(layout);
        msgSender = new ChannelMsgSender(this, WhiteBoardConfig.CLIENT_SS_ID);

        initView();
        StatusBarHelper.translucent(this);
        StatusBarHelper.setStatusBarLightMode(this);
    }

    @Override
    protected void onResume() {
        if(!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
        super.onResume();
    }

    @Override
    protected void onStop() {
        if(EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(WhiteBoardClientSSEvent event) {
        Log.d(TAG, "receive onEvent : " + event);
        if(event.info != null && WhiteBoardServerSSCmd.CMD_SERVER_REPLY_START.equals(event.info.cmd)) {
            try {
                ServerInfo serverInfo = JSON.parseObject(event.info.content, ServerInfo.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected boolean isFloatHeader() {
        return true;
    }

    private void initView() {
        if(splashFragment == null) {
            splashFragment = new WhiteBoardSplashFragment();
            splashFragment.setHeaderHandler(mHeaderHandler);
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(ID_CONTENT, splashFragment);
        transaction.commit();
    }
}
