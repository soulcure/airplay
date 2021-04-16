package com.coocaa.tvpi.module.app.viewmodel;

import android.util.Log;

import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartscreen.data.BaseData;
import com.coocaa.smartscreen.data.app.AppModel;
import com.coocaa.smartscreen.data.app.TvAppModel;
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

import java.util.List;

import androidx.lifecycle.MutableLiveData;

/**
 * 应用模块ViewModel基类
 * Created by songxing on 2020/9/9
 */
public class BaseAppViewModel extends BaseViewModel {
    private String tag = "BaseAppViewModel";
    private MutableLiveData<List<TvAppStateModel>> appStateLiveData = new MutableLiveData<>();

    public BaseAppViewModel() {
    }

    public BaseAppViewModel(String tag) {
        Log.d(tag, "BaseAppViewModel: init");
        this.tag = tag;
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    public void getInstalledApp() {
        Log.d(tag, "getInstalledApp");
        String cmdString = AppStoreParams.CMD.SKY_COMMAND_APPSTORE_MOBILE_GET_INSTALLED_APPS.toString();
        AppStoreParams params = new AppStoreParams();
        CmdUtil.sendAppCmd(cmdString, params.toJson());
    }

    public void getAppState(List<AppModel> appModelList) {
        Log.d(tag, "getAppState");
        JsonArray params = new JsonArray();
        for (AppModel appModel : appModelList) {
            JsonObject object = new JsonObject();
            object.addProperty("pkgName", appModel.pkg);
            params.add(object);
        }
        String cmdString = AppStoreParams.CMD.SKY_COMMAND_APPSTORE_MOBILE_GET_APPSTATUS.toString();
        CmdUtil.sendAppCmd(cmdString, params.toString());
    }

    public void installApp(AppModel appModel) {
        Log.d(tag, "installApp");
        String cmdString = AppStoreParams.CMD.SKY_COMMAND_APPSTORE_MOBILE_DOWNLOAD_SKYAPP.toString();
        AppStoreParams params = new AppStoreParams();
        params.appId = String.valueOf(appModel.appId);
        CmdUtil.sendAppCmd(cmdString, params.toJson());
        ToastUtils.getInstance().showGlobalShort("指令已发送，请在电视端查看");
    }

    public void uninstallApp(List<TvAppModel> list) {
        Log.d(tag, "uninstallApp: " + list);
        for (TvAppModel appModel : list) {
            String cmdString = AppStoreParams.CMD.SKY_COMMAND_APPSTORE_MOBILE_UNINSTALL_APP.toString();
            AppStoreParams params = new AppStoreParams();
            params.pkgName = appModel.pkgName;
            CmdUtil.sendAppCmd(cmdString, params.toJson());
        }
        ToastUtils.getInstance().showGlobalShort("指令已发送，请在电视端查看");
    }


    public void startApp(AppModel appModel) {
        Log.d(tag, "startApp");
        String cmdString = AppStoreParams.CMD.SKY_COMMAND_APPSTORE_MOBILE_START_APP.toString();
        AppStoreParams params = new AppStoreParams();
        params.pkgName = appModel.pkg;
        params.mainACtivity = appModel.mainActivity;
        CmdUtil.sendAppCmd(cmdString, params.toJson());
        ToastUtils.getInstance().showGlobalShort("指令已发送，请在电视端查看");
    }

    //从电视获取到应用安装状态
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GetAppStateEvent getAppStateEvent) {
        Log.d(tag, "GetAppStateEvent onEvent: 获取应用安装状态，判断列表应用安装状态\n" + getAppStateEvent);
        TvAppStateListResp resp = BaseData.load(getAppStateEvent.result, TvAppStateListResp.class);
        if (resp != null) {
            appStateLiveData.setValue(resp.result);
        }
    }

    public MutableLiveData<List<TvAppStateModel>> getAppInstallStateLiveData() {
        return appStateLiveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(tag, "BaseAppViewModel onCleared ");
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }
}
