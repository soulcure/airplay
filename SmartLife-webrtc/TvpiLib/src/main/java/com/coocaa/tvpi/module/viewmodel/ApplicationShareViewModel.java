package com.coocaa.tvpi.module.viewmodel;


import android.text.TextUtils;
import android.util.Log;

import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.data.BaseData;
import com.coocaa.smartscreen.data.app.AppModel;
import com.coocaa.smartscreen.data.app.TvAppStateListResp;
import com.coocaa.smartscreen.data.app.TvAppStateModel;
import com.coocaa.smartscreen.data.channel.AppStoreParams;
import com.coocaa.smartscreen.data.channel.events.GetAppStateEvent;
import com.coocaa.smartscreen.utils.CmdUtil;
import com.coocaa.tvpi.base.mvvm.BaseViewModel;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import swaiotos.channel.iot.ss.device.Device;
import swaiotos.channel.iot.ss.device.DeviceInfo;
import swaiotos.channel.iot.ss.device.TVDeviceInfo;

/**
 * 整个app共享的状态（生命周期贯穿整个app）
 * Created by songxing on 2020/7/15
 */
public class ApplicationShareViewModel extends BaseViewModel {
    private static final String TAG = ApplicationShareViewModel.class.getSimpleName();

    //是否开启轮询正在下载apk的安装状态
    private final MutableLiveData<Boolean> isLoopInstallingAppStateLiveData = new MutableLiveData<>();
    //正在下载的apk的安装状态LiveData
    private MutableLiveData<List<TvAppStateModel>> installingAppStateLiveData = new MutableLiveData<>();
    //保存加入下载中app的Map
    private Map<String,List<AppModel>> installingAppMap = new HashMap<>();

    public ApplicationShareViewModel() {
        Log.d(TAG, "AppShareViewModel init");
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    public void getInstallingAppState() {
        Log.d(TAG, "getInstallingAppState");
        List<AppModel> installingAppList = installingAppMap.get(getDeviceActiveId());
        if(installingAppList != null && !installingAppList.isEmpty()) {
            JsonArray params = new JsonArray();
            for (AppModel appModel : installingAppList) {
                JsonObject object = new JsonObject();
                object.addProperty("pkgName", appModel.pkg);
                params.add(object);
            }
            String cmdString = AppStoreParams.CMD.SKY_COMMAND_APPSTORE_MOBILE_GET_APPSTATUS.toString();
            CmdUtil.sendAppCmd(cmdString, params.toString());
        }
    }

    public void addInstallingApp(AppModel appModel){
        Log.d(TAG, "addInstallingApp: " + appModel);
        String key = getDeviceActiveId();
        if(installingAppMap.containsKey(key)){
            List<AppModel> installingAppList = installingAppMap.get(key);
            if(installingAppList == null){
                installingAppList = new LinkedList<>();
            }
            if(!installingAppList.contains(appModel)) {
                installingAppList.add(appModel);
                Log.d(TAG, "addInstallingApp add :" + appModel);
                installingAppStateLiveData.setValue(adapter(installingAppList));
                startLoopInstallingAppState();
            }else {
                Log.d(TAG, "addInstallingApp: already downloading");
            }
        }else {
            List<AppModel> appModelList = new LinkedList<>();
            appModelList.add(appModel);
            installingAppMap.put(key,appModelList);
            Log.d(TAG, "addInstallingApp add :" + appModel);
            installingAppStateLiveData.setValue(adapter(appModelList));
            startLoopInstallingAppState();
        }
    }

    //从电视获取到应用安装状态
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GetAppStateEvent getAppStateEvent) {
        Log.d(TAG, "GetAppStateEvent onEvent: 获取应用安装状态，判断正在下载的应用安装状态\n"+getAppStateEvent);
        //这里只处理正在安装的应用状态
        List<AppModel> installingAppList = installingAppMap.get(getDeviceActiveId());
        if(installingAppList != null && !installingAppList.isEmpty()) {
            TvAppStateListResp resp = BaseData.load(getAppStateEvent.result, TvAppStateListResp.class);
            if (resp != null) {
                List<TvAppStateModel> appStateList = resp.result;
                //删除正在下载apk列表中已经下载好的
                for (TvAppStateModel tvAppStateModel : appStateList) {
                    if (tvAppStateModel.appinfo == null || TextUtils.isEmpty(tvAppStateModel.appinfo.pkgName)) {
                        continue;
                    }
                    Iterator<AppModel> iterator = installingAppList.iterator();
                    while (iterator.hasNext()) {
                        AppModel next = iterator.next();
                        if (tvAppStateModel.appinfo.pkgName.equals(next.pkg) && tvAppStateModel.installed) {
                            Log.d(TAG, "GetAppStateEvent onEvent: app install finish remove：" + next);
                            iterator.remove();
                        }
                    }
                }

                installingAppStateLiveData.setValue(adapter(installingAppList));
                //全部下载完成停止轮询下载中apk状态
                if (installingAppList.isEmpty()) {
                    Log.d(TAG, "GetAppStateEvent onEvent: all installing app install finish");
                    stopLoopInstallingAppState();
                }
            }
        }else {
            Log.d(TAG, "GetAppStateEvent onEvent: no installing app");
        }
    }

    public MutableLiveData<List<TvAppStateModel>> getInstallingAppStateLiveData() {
        return installingAppStateLiveData;
    }

    private void startLoopInstallingAppState() {
        Log.d(TAG, "startLoopInstallingAppState");
        isLoopInstallingAppStateLiveData.setValue(true);
    }

    private void stopLoopInstallingAppState() {
        Log.d(TAG, "stopLoopInstallingAppState");
        isLoopInstallingAppStateLiveData.setValue(false);
    }

    public LiveData<Boolean> isLoopInstallingAppState() {
        Log.d(TAG, "isLoopInstallingAppState");
        return isLoopInstallingAppStateLiveData;
    }

    private List<TvAppStateModel> adapter(List<AppModel> appModelList){
        //发送正在下载apk的初始安装状态
        List<TvAppStateModel> appStateList = new ArrayList<>();
        for (AppModel model : appModelList) {
            TvAppStateModel tvAppStateModel = new TvAppStateModel(model);
            appStateList.add(tvAppStateModel);
        }
        return appStateList;
    }

    private String getDeviceActiveId() {
        try {
            String activeId = "";
            Device device = SSConnectManager.getInstance().getDevice();
            DeviceInfo deviceInfo = device.getInfo();
            if (null != deviceInfo) {
                TVDeviceInfo tvDeviceInfo = (TVDeviceInfo) deviceInfo;
                activeId = tvDeviceInfo.activeId;
            }
            return activeId;
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "AppShareViewModel onCleared");
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }
}
