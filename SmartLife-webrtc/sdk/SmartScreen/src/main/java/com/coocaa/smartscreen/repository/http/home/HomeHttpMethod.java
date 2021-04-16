package com.coocaa.smartscreen.repository.http.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.constant.SmartConstans;
import com.coocaa.smartscreen.data.banner.BannerHttpData;
import com.coocaa.smartscreen.data.businessstate.SceneConfigBean;
import com.coocaa.smartscreen.data.businessstate.SceneConfigHttpData;
import com.coocaa.smartscreen.data.clientconfig.ClientConfigHttpData;
import com.coocaa.smartscreen.data.function.FunctionBean;
import com.coocaa.smartscreen.data.function.FunctionHttpData;
import com.coocaa.smartscreen.data.function.homepage.SSHomePageData;
import com.coocaa.smartscreen.data.function.homepage.SSHomePageResp;
import com.coocaa.smartscreen.data.panel.PanelBean;
import com.coocaa.smartscreen.data.panel.PanelHttpData;
import com.coocaa.smartscreen.network.util.MD5Util;
import com.coocaa.smartscreen.repository.http.HttpMethod;
import com.coocaa.smartscreen.repository.http.HttpServer;
import com.coocaa.smartscreen.utils.AndroidUtil;
import com.coocaa.tvpi.module.homepager.adapter.bean.PlayMethodBean;
import com.coocaa.tvpi.module.homepager.adapter.bean.PlayMethodHttpData;
import com.google.gson.Gson;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import swaiotos.channel.iot.ss.device.Device;
import swaiotos.channel.iot.ss.device.DeviceInfo;
import swaiotos.channel.iot.ss.device.TVDeviceInfo;

/**
 * @Author: yuzhan
 */
public class HomeHttpMethod extends HttpMethod<HomeHttpService> {

    private Map<String, String> header = new HashMap<>();
    private final static HomeHttpMethod instance = new HomeHttpMethod();

    private final static String APP_KEY = "81dbba5e74da4fcd8e42fe70f68295a6";
    private final static String SECRET = "50c08407916141aa878e65564321af5f";
//    private final static int SCENE_ID = 1;

    private final static String TAG = "TvpiHttp";

    private static List<FunctionBean> cachedFunctionList = new ArrayList<>(); //其他页面也需要用到金刚区数据，增加静态缓存

    private static List<SSHomePageData> ssHomePageDataList = new ArrayList<>();

    public static HomeHttpMethod getInstance() {
        return instance;
    }

    public HomeHttpMethod() {
        super();
    }

    @WorkerThread
    public boolean submitLog(CcLogData log) {
        try {
            String time = timestampInSecond();
            Map<String, String> map = new HashMap<>();
            map.put("time", time);
            map.put("appkey", APP_KEY);
            map.put("sign", getSign(map));
            Log.d("CCEvent", "submitLog222, log=" + JSON.toJSONString(log) + ", time=" + time + ", appkey=" + APP_KEY);
            String ret = getService().submitLog(map, log);
            Log.d("CCEvent", "submitLog, ret=" + ret);
        } catch (Exception e) {
            Log.d(TAG, "submitLog error=" + e.toString());
            AndroidUtil.printException(TAG, "submitLog error", e);
        }
        return false;
    }

    public List<FunctionBean> getCachedFunctionList() {
        return cachedFunctionList;
    }

    public List<SSHomePageData> getSSHomePageDataList() {
        return ssHomePageDataList;
    }

    private String getSpaceId() {
        String spaceId = "";
        Device device = SSConnectManager.getInstance().getHistoryDevice();
        if (device != null) {
            spaceId = device.getSpaceId();
        }
        return spaceId == null ? "" : spaceId;
    }

    private String getActiveId() {
        Device device = SSConnectManager.getInstance().getHistoryDevice();
        if (device == null || device.getInfo() == null)
            return "";
        DeviceInfo deviceInfo = device.getInfo();
        if (deviceInfo.type() == DeviceInfo.TYPE.TV) {
            TVDeviceInfo tvDeviceInfo = (TVDeviceInfo) deviceInfo;
            String activationId = tvDeviceInfo.activeId;
            if (activationId != null)
                return activationId;
        }
        return "";
    }

    private String getSceneType() {
        Device device = SSConnectManager.getInstance().getHistoryDevice();
        if (device != null) {
            String registerType = device.getZpRegisterType();
            if (TextUtils.isEmpty(registerType))
                return "tv";
            return registerType;
        }
        return "tv";
    }

    /**
     * 通用banner数据获取
     * ak  非必须
     * uid 非必须
     */
    @WorkerThread
    public BannerHttpData.FunctionContent getOperationData(@NonNull String type, String ak, String uid) {
        BannerHttpData.FunctionContent ret = null;
        try {
            Map<String, String> queryMap = new HashMap<>();
            queryMap.put("appkey", APP_KEY);
            queryMap.put("time", timestampInSecond());
            queryMap.put("version", appVersion());
            queryMap.put("activation_id", getActiveId());
            queryMap.put("operation_type", type);
            if (!TextUtils.isEmpty(ak)) {
                queryMap.put("ak", ak);
            }
            if (!TextUtils.isEmpty(uid)) {
                queryMap.put("uid", uid);
            }
            queryMap.put("sign", getSign(queryMap));
            String bannerListStr = getService().getOperationData(queryMap);
            Log.d(TAG, "get getBannerList http=" + bannerListStr);
            ret = new Gson().fromJson(bannerListStr, BannerHttpData.class).data;
            //ret = JSON.parseObject(bannerListStr, BannerHttpData.class).data;
        } catch (Exception e) {
            Log.d(TAG, "getBannerList error=" + e.toString());
            AndroidUtil.printException(TAG, "getBannerList error", e);
        }
        return ret;
    }

    @WorkerThread
    public List<FunctionBean> getBannerList() {
        Log.d(TAG, "call getBannerList");
        List<FunctionBean> ret = null;
        try {
            Map<String, String> queryMap = new HashMap<>();
            queryMap.put("time", timestampInSecond());
            queryMap.put("version", appVersion());
            queryMap.put("appkey", APP_KEY);
            queryMap.put("spaceId", getSpaceId());
            queryMap.put("activationId", getActiveId());
            queryMap.put("sceneType", getSceneType());
            queryMap.put("sign", getSign(queryMap));
            String bannerListStr = getService().getBannerList(queryMap);
            Log.d(TAG, "get getBannerList http=" + bannerListStr);
            ret = JSON.parseObject(bannerListStr, FunctionHttpData.class).data.content;
        } catch (Exception e) {
            Log.d(TAG, "getBannerList error=" + e.toString());
            AndroidUtil.printException(TAG, "getBannerList error", e);
        }
        return ret;
    }


    @WorkerThread
    public List<FunctionBean> getFunctionList(String _deviceType) {
        Log.d(TAG, "call getFunctionList _deviceType=" + _deviceType);
        List<FunctionBean> ret = null;
        try {
            String deviceType = getDeviceType(_deviceType);
            String time = timestampInSecond();
            Map<String, String> map = new HashMap<>();
            map.put("deviceType", deviceType);
            map.put("time", time);
            map.put("version", appVersion());
            map.put("appkey", APP_KEY);
            map.put("spaceId", getSpaceId());
            map.put("version", appVersion());
            map.put("sign", getSign(map));
            String function = null;
            try {
                Log.d(TAG, "get getFunctionList by new map");
                function = getService().getFunctionList(map);
            } catch (Exception e) {//网络异常处理
                AndroidUtil.printException(TAG, "getFunctionList error", e);
            }
            Log.d(TAG, "get getFunctionList http=" + function);
            if (!TextUtils.isEmpty(function))
                ret = JSON.parseObject(function, FunctionHttpData.class).data.content;
            if (ret != null && !ret.isEmpty()) {
                cachedFunctionList.clear();
                cachedFunctionList.addAll(ret);
                saveSp("cache_function_list", function);
            } else {
                String cache = getSp("cache_function_list");
                if (!TextUtils.isEmpty(cache))
                    ret = JSON.parseObject(cache, FunctionHttpData.class).data.content;
                if (ret != null && !ret.isEmpty()) {
                    Log.d(TAG, "get getFunctionList from cache : " + cache);
                    cachedFunctionList.clear();
                    cachedFunctionList.addAll(ret);
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "get function error=" + e.toString());
            AndroidUtil.printException(TAG, "getFunctionList error", e);
        }
        return ret;
    }

    /**
     * 通用banner数据获取
     * ak  非必须
     * uid 非必须
     */
    @WorkerThread
    public List<FunctionBean> getFunctionList(String ak, String uid) {
        List<FunctionBean> ret = null;
        try {
            Map<String, String> queryMap = new HashMap<>();
            queryMap.put("appkey", APP_KEY);
            queryMap.put("time", timestampInSecond());
            queryMap.put("version", appVersion());
            queryMap.put("activation_id", getActiveId());
            queryMap.put("operation_type", "app_area");
            if (!TextUtils.isEmpty(ak)) {
                queryMap.put("ak", ak);
            }
            if (!TextUtils.isEmpty(uid)) {
                queryMap.put("uid", uid);
            }
            queryMap.put("sign", getSign(queryMap));
            String function = null;
            try {
                Log.d(TAG, "get getFunctionList by new map");
                function = getService().getOperationData(queryMap);
            } catch (Exception e) {
                AndroidUtil.printException(TAG, "getFunctionList error", e);
            }
            Log.d(TAG, "get getFunctionList http=" + function);
            if (!TextUtils.isEmpty(function)){
                ret = JSON.parseObject(function, FunctionHttpData.class).data.content;
            }
            if (ret != null && !ret.isEmpty()){
                cachedFunctionList.clear();
                cachedFunctionList.addAll(ret);
                saveSp("cache_function_list", function);
            } else {
                String cache = getSp("cache_function_list");
                if (!TextUtils.isEmpty(cache)) {
                    ret = JSON.parseObject(cache, FunctionHttpData.class).data.content;
                }
                if (ret != null && !ret.isEmpty()) {
                    Log.d(TAG, "get getFunctionList from cache : " + cache);
                    cachedFunctionList.clear();
                    cachedFunctionList.addAll(ret);
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "getFunctionList error=" + e.toString());
        }
        return ret;
    }    
    
    /**
     * 通用banner数据获取
     * ak  非必须
     * uid 非必须
     */
    @WorkerThread
    public List<SSHomePageData> getFunctionListAppAreaV2(String ak, String uid) {
        String cacheKey = "cache_app_area_v2";
        List<SSHomePageData> ret = null;
        try {
            Map<String, String> queryMap = new HashMap<>();
            queryMap.put("appkey", APP_KEY);
            queryMap.put("time", timestampInSecond());
            queryMap.put("version", appVersion());
            queryMap.put("activation_id", getActiveId());
            queryMap.put("operation_type", "app_area_v2");
            if (!TextUtils.isEmpty(ak)) {
                queryMap.put("ak", ak);
            }
            if (!TextUtils.isEmpty(uid)) {
                queryMap.put("uid", uid);
            }
            queryMap.put("sign", getSign(queryMap));
            String resp = null;
            try {
                Log.d(TAG, "get getFunctionListAppAreaV2 by new map");
                resp = getService().getOperationData(queryMap);
            } catch (Exception e) {
                AndroidUtil.printException(TAG, "getFunctionListAppAreaV2 error", e);
            }
            Log.d(TAG, "get getFunctionListAppAreaV2 http=" + resp);
            if (!TextUtils.isEmpty(resp)){
                ret = JSON.parseObject(resp, SSHomePageResp.class).data;
            }
            if (ret != null && !ret.isEmpty()){
                saveSp(cacheKey, resp);
                //如果需要，存储一份本地数据变量SSHomePageData
                ssHomePageDataList.clear();
                ssHomePageDataList.addAll(ret);
            } else {
                String cache = getSp(cacheKey);
                if (!TextUtils.isEmpty(cache)) {
                    ret = JSON.parseObject(cache, SSHomePageResp.class).data;
                }
                if (ret != null && !ret.isEmpty()){
                    Log.d(TAG, "get getFunctionListAppAreaV2 from cache : " + cache);
                    //如果需要，存储一份本地数据变量SSHomePageData
                    ssHomePageDataList.clear();
                    ssHomePageDataList.addAll(ret);
                } else {
                    Log.d(TAG, "getFunctionListAppAreaV2: 服务器数据为空，从本地json拿");
                    //暂时从本地文件获取
                    String normalDataString = AndroidUtil.readAssetFile("function_list_app_area_v2.json");
                    Log.d(TAG, "normalDataString: " + normalDataString);
                    ret = JSON.parseObject(normalDataString, SSHomePageResp.class).data;
                    if (ret != null && !ret.isEmpty()){
                        ssHomePageDataList.clear();
                        ssHomePageDataList.addAll(ret);
                    }
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "getFunctionListAppAreaV2 error=" + e.toString());
        }
        return ret;
    }

    private String getSp(String key) {
        SharedPreferences sp = AndroidUtil.getAppContext().getSharedPreferences("http_cache", Context.MODE_PRIVATE);
        return sp.getString(key, null);
    }

    private void saveSp(String key, String value) {
        SharedPreferences sp = AndroidUtil.getAppContext().getSharedPreferences("http_cache", Context.MODE_PRIVATE);
        sp.edit().putString(key, value).apply();
    }

    public List<PlayMethodBean> getPanelContentList() {
        List<PlayMethodBean> ret = null;
        try {
            PanelBean panelBean = getFirstPanel();
            Map<String, String> queryMap = new HashMap<>();
            queryMap.put("appkey", APP_KEY);
            queryMap.put("time", timestampInSecond());
            queryMap.put("version", appVersion());
            queryMap.put("activationId", getActiveId());
            //获取app玩法
            queryMap.put("operation_type", "play");
            queryMap.put("tab_id", panelBean.id);
            queryMap.put("sign", getSign(queryMap));
            String panelContentString = getService().getOperationData(queryMap);
            Log.d(TAG, "get panelContentString http=" + panelContentString);
            ret = JSON.parseObject(panelContentString, PlayMethodHttpData.class).data.content;
        } catch (Exception e) {
            Log.d(TAG, "getPanelContentList error=" + e.toString());
            AndroidUtil.printException(TAG, "getPanelContentList error", e);
        }
        return ret;
    }

    //从标题列表中取第一个
    private PanelBean getFirstPanel() {
        PanelBean ret = null;
        try {
            Map<String, String> queryMap = new HashMap<>();
            queryMap.put("appkey", APP_KEY);
            queryMap.put("time", timestampInSecond());
            queryMap.put("version", appVersion());
            queryMap.put("activationId", getActiveId());
            //获取app玩法tab
            queryMap.put("operation_type", "play_tab");
            queryMap.put("sign", getSign(queryMap));
            String tabString = getService().getOperationData(queryMap);
            Log.d(TAG, "getFirstPanel http=" + tabString);
            if (!TextUtils.isEmpty(tabString)) {
                ret = JSON.parseObject(tabString, PanelHttpData.class).data.content.get(0);
            }

        } catch (Exception e) {
            AndroidUtil.printException(TAG, "getFirstPanel http error", e);
        }
        if (ret == null) {
            String localJson = AndroidUtil.readAssetFile("panel_title_list.json");
            if (!TextUtils.isEmpty(localJson)) {
                try {
                    ret = JSON.parseObject(localJson, PanelHttpData.class).data.content.get(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return ret;
    }

    public List<PlayMethodBean> getPanelContentList(String _deviceType) {
        List<PlayMethodBean> ret = null;
        try {
            String deviceType = getDeviceType(_deviceType);
            PanelBean panelBean = getFirstPanel(deviceType);
            String time = timestampInSecond();
            Map<String, String> map = new HashMap<>();
            map.put("deviceType", deviceType);
            map.put("time", time);
            map.put("version", appVersion());
            map.put("appkey", APP_KEY);
            map.put("tabId", panelBean.id);
            map.put("spaceId", getSpaceId());
            map.put("activation_id", getActiveId());
            map.put("version", appVersion());
            map.put("sign", getSign(map));
            String panelContentString = getService().getPanelContentList(map);
            Log.d(TAG, "get panelContentString http=" + panelContentString);
            ret = JSON.parseObject(panelContentString, PlayMethodHttpData.class).data.content;
        } catch (Exception e) {
            Log.d(TAG, "getPanelContentList error=" + e.toString());
            AndroidUtil.printException(TAG, "getPanelContentList error", e);
        }
        return ret;
    }

    //从标题列表中取第一个
    private PanelBean getFirstPanel(String _deviceType) {
        PanelBean ret = null;
        try {
            String deviceType = getDeviceType(_deviceType);
            String time = timestampInSecond();
            Map<String, String> map = new HashMap<>();
            map.put("deviceType", deviceType);
            map.put("time", time);
            map.put("version", appVersion());
            map.put("appkey", APP_KEY);
            map.put("spaceId", getSpaceId());
            map.put("activation_id", getActiveId());
            map.put("version", appVersion());
            map.put("sign", getSign(map));

            String tabString = getService().getTabList(map);
            Log.d(TAG, "getFirstPanel http=" + tabString);
            if (!TextUtils.isEmpty(tabString)) {
                ret = JSON.parseObject(tabString, PanelHttpData.class).data.content.get(0);
            }

        } catch (Exception e) {
            AndroidUtil.printException(TAG, "getFirstPanel http error", e);
        }
        if (ret == null) {
            String localJson = AndroidUtil.readAssetFile("panel_title_list.json");
            if (!TextUtils.isEmpty(localJson)) {
                try {
                    ret = JSON.parseObject(localJson, PanelHttpData.class).data.content.get(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return ret;
    }


    @WorkerThread
    public List<SceneConfigBean> getSceneControlConfig() {
        Log.d(TAG, "call getSceneControlConfig");
        List<SceneConfigBean> ret = null;
        try {
            String time = timestampInSecond();
            Map<String, String> map = new HashMap<>();
            map.put("time", time);
            map.put("version", appVersion());
            map.put("appkey", APP_KEY);
            map.put("sign", getSign(map));
            String function = getService().getSceneControlConfig(map);
            Log.d(TAG, "get getSceneControlConfig http=" + function);
            ret = JSON.parseObject(function, SceneConfigHttpData.class).data.content;
        } catch (Exception e) {
            Log.d(TAG, "get getSceneControlConfig error=" + e.toString());
            AndroidUtil.printException(TAG, "getSceneControlConfig error", e);
        }
        return ret;
    }

    /**
     * 获取后台配置的资源列表（例如教程视频）
     *
     * @return
     */
    public ClientConfigHttpData.ClientConfigData getClientConfig() {
        ClientConfigHttpData.ClientConfigData configData = null;
        try {
            String time = timestampInSecond();
            String key = "androidVideoResource";
            Map<String, String> map = new HashMap<>();
            map.put("time", time);
            map.put("appkey", APP_KEY);
            map.put("key", key);
            map.put("sign", getSign(map));
            String function = getService().getClientConfig(map);
            Log.d(TAG, "getClientConfig http=" + function);
            configData = JSON.parseObject(function, ClientConfigHttpData.class).data;
        } catch (Exception e) {
            Log.d(TAG, "getClientConfig error=" + e.toString());
            AndroidUtil.printException(TAG, "getClientConfig error", e);
        }
        return configData;
    }

    private String getDeviceType(String raw) {
        if (TextUtils.isEmpty(raw)) {
            return "tv";
        }
        return raw;
    }

    @Override
    protected String getBaseUrl() {
        return HttpServer.getInstance().getServerUrl();
    }

    @Override
    protected int getTimeOut() {
        return 10;
    }

    @Override
    protected Class<HomeHttpService> getServiceClazz() {
        return HomeHttpService.class;
    }

    @Override
    protected Map<String, String> getHeaders() {
        return header;
    }

    private String timestampInSecond() {
        return String.valueOf(System.currentTimeMillis() / 1000);
    }

    private String appVersion() {
        return String.valueOf(SmartConstans.getBuildInfo().versionCode);
    }

    private static final String md5(final String s) {
        final String MD5 = "MD5";
        try {
            MessageDigest digest = MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String getSign(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        Map<String, String> sortMap = sortMapByKey(map);
        Iterator<Map.Entry<String, String>> iterator = sortMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            sb.append(entry.getKey());
            sb.append(entry.getValue());
        }
        sb.append(SECRET);
        String signStr = sb.toString();
        Log.d(TAG, "signStr=" + signStr);
        String sign = MD5Util.getMd5(signStr).toLowerCase();
        Log.d(TAG, "sign=" + sign);
        return sign;
    }

    private static Map<String, String> sortMapByKey(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        Map<String, String> sortMap = new TreeMap<String, String>(new MapComparator());
        sortMap.putAll(map);
        return sortMap;
    }

    static class MapComparator implements Comparator<String> {
        @Override
        public int compare(String str1, String str2) {
            return str1.compareTo(str2);
        }
    }

    private static String getDeviceActiveId(Device device) {
        if (null == device) {
            return "";
        }
        DeviceInfo deviceInfo = device.getInfo();
        if (null != deviceInfo) {
            switch (deviceInfo.type()) {
                case TV:
                    TVDeviceInfo tvDeviceInfo = (TVDeviceInfo) deviceInfo;
                    return tvDeviceInfo.activeId;
            }
        }
        return "";
    }

    public static void main(String[] args) {
        String appkey = "81dbba5e74da4fcd8e42fe70f68295a6";
        String id = "1";
        String time = "1603453714";
        String versino = "1";
        String SECRET = "50c08407916141aa878e65564321af5f";
        String signStr = "appkey81dbba5e74da4fcd8e42fe70f68295a6id1time1603453714version150c08407916141aa878e65564321af5f";
        System.out.println("signStr = " + signStr);
        String sign1 = MD5Util.getMd5(signStr).toLowerCase();
        String sign2 = md5(signStr).toLowerCase();
        System.out.println("s1 = " + sign1);
        System.out.println("s2 = " + sign2);
    }
}
