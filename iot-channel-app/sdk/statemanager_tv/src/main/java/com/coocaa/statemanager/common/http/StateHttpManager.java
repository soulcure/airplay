package com.coocaa.statemanager.common.http;



import android.content.Context;
import android.util.Log;

import com.coocaa.statemanager.StateManager;
import com.coocaa.statemanager.common.bean.MiracastAppInfo;
import com.coocaa.statemanager.common.bean.ScreenApps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import swaiotos.channel.iot.ss.SSChannelService;
import swaiotos.channel.iot.ss.server.http.HttpServiceConfig;
import swaiotos.channel.iot.ss.server.http.api.HttpManager;
import swaiotos.channel.iot.ss.server.http.api.HttpResult;
import swaiotos.channel.iot.ss.server.utils.Constants;
import swaiotos.channel.iot.ss.server.utils.MD5;
import swaiotos.sal.SAL;
import swaiotos.sal.SalModule;

/**
 * @ClassName: AppStoreHttpManager
 * @Author: AwenZeng
 * @CreateDate: 2019/12/23 14:32
 * @Description: 应用HttpManager
 */
public class StateHttpManager extends HttpManager<IStateHttpMethod> {

    public static final StateHttpManager SERVICE = new StateHttpManager();

    @Override
    protected Class<IStateHttpMethod> getServiceClass() {
        return IStateHttpMethod.class;
    }

    @Override
    protected Map<String, String> getHeaders() {
        return HttpServiceConfig.HEADER_LOADER.getHeader();
    }

    @Override
    protected String getBaseUrl() {
        return Constants.getIOTServer(SSChannelService.getContext());
    }

    public static Map<String, String> getBaseUrlParams() {
        String appKey = Constants.getLogAppKey(StateManager.INSTANCE.getContext());
        Map<String, String> map = new HashMap<>();
        map.put("appkey", appKey);
        map.put("time", String.valueOf(System.currentTimeMillis() / 1000));
        return map;
    }

    private static String sign(Map<String, String> map) {
       String secret = Constants.LOG_SECRET;
        List<String> keys = new ArrayList<String>(map.keySet());
        Collections.sort(keys);
        String temStr = "";
        for (String key : keys) {
            temStr += key + map.get(key);
        }
        Log.d("sign", " temStr:" + temStr);
        String mysign = "";
        try {
            mysign = MD5.getMd5(temStr + secret);
        } catch (Exception e) {

        }
        Log.d("sign", " mysign:" + mysign);
        return mysign;
    }

    /***
     * 获取投屏业务的配置
     * @param c
     * @return
     */
    public Call<HttpResult<ScreenApps>> getScreenApps(Context c) {
        Map<String, String> map = StateHttpManager.getBaseUrlParams();
        return httpLog(c).getScreenApps(map.get("appkey"),map.get("time"),
                StateHttpManager.sign(map));
    }


    /**
     * 获取需要禁用的投屏业务
     * @param c
     * @return
     */
    public Call<HttpResult<List<MiracastAppInfo>>> getDongleConfig(Context c) {
        Map<String, String> map = StateHttpManager.getBaseUrlParams();
        map.put("activation_id", SAL.getModule(c, SalModule.SYSTEM).getActiveId());
        map.put("config_type","cast");
        return httpLog(c).getDongleConfig(map.get("appkey")
                ,map.get("time"),
                map.get("activation_id")
                ,map.get("config_type"),
                StateHttpManager.sign(map));
    }
}
