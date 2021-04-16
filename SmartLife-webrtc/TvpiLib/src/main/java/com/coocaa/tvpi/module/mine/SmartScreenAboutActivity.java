package com.coocaa.tvpi.module.mine;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.coocaa.publib.base.BaseActivity;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.data.channel.AppInfo;
import com.coocaa.smartscreen.data.channel.CmdData;
import com.coocaa.smartscreen.data.channel.events.AppInfoEvent;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpi.view.CommonTitleBar;
import com.coocaa.tvpilib.R;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import swaiotos.channel.iot.ss.device.Device;
import swaiotos.channel.iot.ss.device.DeviceInfo;
import swaiotos.channel.iot.ss.device.TVDeviceInfo;

import static com.coocaa.smartscreen.connect.SSConnectManager.TARGET_APPSTATE;

public class SmartScreenAboutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_screen_about);
        StatusBarHelper.translucent(this);
        StatusBarHelper.setStatusBarLightMode(this);

        
        initView();
        EventBus.getDefault().register(this);
        getAppInfos();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAppInfoEvent(AppInfoEvent appInfoEvent) {
        Log.d(TAG, "onAppInfoEvent: ");
        if (null != appInfoEvent 
                && null != appInfoEvent.appInfos
               && appInfoEvent.appInfos.size() > 0) {
            TextView tvLauncherVersion = findViewById(R.id.tvLauncherVersion);
            TextView tvChannelVersion = findViewById(R.id.tvChannelVersion);
            TextView tvH5Version = findViewById(R.id.tvH5Version);
            TextView tvDocAppVersion = findViewById(R.id.tvDocAppVersion);

            for (AppInfo appInfo :
                    appInfoEvent.appInfos) {
                if (appInfo.pkgName.equals("com.coocaa.dongle.launcher")) {
                    tvLauncherVersion.setText(appInfo.versionName);
                } else if (appInfo.pkgName.equals("swaiotos.channel.iot")) {
                    tvChannelVersion.setText(appInfo.versionName);
                } else if (appInfo.pkgName.equals("swaiotos.runtime.h5.app")) {
                    tvH5Version.setText(appInfo.versionName);
                } else if (appInfo.pkgName.equals("com.yozo.office.education")) {
                    tvDocAppVersion.setText(appInfo.versionName);
                }
            }
        }
    }
    
    private void initView() {
        CommonTitleBar titleBar = findViewById(R.id.titleBar);
        TextView tvName = findViewById(R.id.tvName);
        TextView tvRegisterId = findViewById(R.id.tvRegisterId);
        TextView tvMac = findViewById(R.id.tvMac);
        TextView tvSysVersion = findViewById(R.id.tvSysVersion);

        Device device = SSConnectManager.getInstance().getDevice();
        if (device != null) {
            DeviceInfo deviceInfo = device.getInfo();
            if (null != deviceInfo) {
                if (deviceInfo.type() == DeviceInfo.TYPE.TV) {
                    TVDeviceInfo tvDeviceInfo = (TVDeviceInfo) deviceInfo;
                    tvRegisterId.setText(tvDeviceInfo.activeId);
                    tvMac.setText(tvDeviceInfo.MAC);
                    tvSysVersion.setText(tvDeviceInfo.cTcVersion);
                }
            }
            tvName.setText(SSConnectManager.getInstance().getDeviceName(SSConnectManager.getInstance().getHistoryDevice()));
        }

        titleBar.setOnClickListener(new CommonTitleBar.OnClickListener() {
            @Override
            public void onClick(CommonTitleBar.ClickPosition position) {
                if(position == CommonTitleBar.ClickPosition.LEFT){
                    finish();
                }
            }
        });
    }

    public void getAppInfos() {
        List<String> packageList = new ArrayList<>();
        //主页
        packageList.add("com.coocaa.dongle.launcher");
        //通道
        packageList.add("swaiotos.channel.iot");
        //h5
        packageList.add("swaiotos.runtime.h5.app");
        //文档app
        packageList.add("com.yozo.office.education");
        String param = new Gson().toJson(packageList);

        CmdData data = new CmdData("getAppInfos", CmdData.CMD_TYPE.APP_INFOS.toString(), param);
        String cmd = data.toJson();
        SSConnectManager.getInstance().sendTextMessage(cmd, TARGET_APPSTATE);
    }
}
