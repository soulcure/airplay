package com.coocaa.tvpi.util;

import android.util.Log;

import com.coocaa.smartscreen.data.movie.Episode;
import com.coocaa.smartscreen.repository.Repository;
import com.coocaa.smartscreen.repository.service.MovieRepository;
import com.coocaa.tvpi.base.BaseRepositoryCallback;

import java.util.Map;

public class ReportUtil {
    private static final String TAG = ReportUtil.class.getSimpleName();

    private static final String KEY_BEHAVIOR_ID_PROGRESS = "KEY_BEHAVIOR_ID_PROGRESS";
    private static final String KEY_BEHAVIOR_ID_DISLIKE = "KEY_BEHAVIOR_ID_DISLIKE";

    public static void reportVideoProgress(String video_id, String progress) {
      /*  ParamsUtil paramsUtil = new ParamsUtil(ConstantsUrl.URL_DATA_COLLECT, ConstantsUrl.APP_KEY_TVPAI, ConstantsUrl.APP_SALT_TVPAI);

        ReportData reportData = new ReportData();
        reportData.tv_mac = SharedData.getInstance().getString(SharedData.Keys.MAC_DEVICE_CONNECT, "");

        Behavior behavior = new Behavior();
        behavior.id = getBehaviorId();
        behavior.pre_id = SpUtil.getString(MyApplication.getContext(), KEY_BEHAVIOR_ID_PROGRESS, "");
        behavior.type = 1;
        behavior.video_id = video_id;

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("progress", progress);
        behavior.params = jsonObject.toString();

        reportData.behavior = behavior;
        Log.d(TAG,"reportData: " + new Gson().toJson(reportData));
        reportProgressData(paramsUtil.getFullRequestUrl(), new Gson().toJson(reportData), behavior.id);*/
    }

    private static void reportProgressData(String url, String json, final String behaviorId) {
        /*ApiClient.postString(url, json, new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                IRLog.d(TAG, "onError.response" + e);
            }

            @Override
            public void onResponse(String response, int id) {
                IRLog.d(TAG, "onSuccess. response = " + response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getInt("code") == 0) {
                        SpUtil.putString(MyApplication.getContext(), KEY_BEHAVIOR_ID_PROGRESS, behaviorId);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });*/
    }

    public static void reportPushHistory(Episode episode, String videoType) {
        Repository.get(MovieRepository.class)
                .addPushHistory(videoType,episode)
                .setCallback(new BaseRepositoryCallback<Void>(){
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess. response = " + aVoid);
                    }

                    @Override
                    public void onFailed(Throwable e) {
                       Log.d(TAG, "onError.response" + e);
                    }
                });
    }

    private static void reportData(String url, String json) {
       /* ApiClient.postString(url, json, new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                IRLog.d(TAG, "onError.response" + e);
            }

            @Override
            public void onResponse(String response, int id) {
                IRLog.d(TAG, "onSuccess. response = " + response);
            }
        });*/
    }

    private static void reportData(String url, Map<String, String> map, final String behaviorId) {
       /* ApiClient.post(url, map, new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                IRLog.d(TAG, "onError.response" + e);
            }

            @Override
            public void onResponse(String response, int id) {
                IRLog.d(TAG, "onSuccess. response = " + response);

//                SpUtil.putString(MyApplication.getContext(), KEY_BEHAVIOR_ID_PROGRESS, behaviorId);
            }
        });*/
    }

    private static final String getBehaviorId() {
     /*   String behaviorId = null;
        try {
            String vuid = DeviceInfo.getUniquePsuedoID(); //VUIDHelper.getVUID();
            String time = String.valueOf(System.currentTimeMillis());
            Log.d(TAG, "vuid: " + vuid);
            Log.d(TAG, "time: " + time);
            String tmp = vuid + time;
            Log.d(TAG, "tmp: " + tmp);
            behaviorId = MD5Util.getMD5String(tmp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "behaviorId: " + behaviorId);
        return behaviorId;*/
     return "";
    }

    public static void reportDislikeLabel(String video_id, String dislikeTagIds) {
       /* ParamsUtil paramsUtil = new ParamsUtil(ConstantsUrl.URL_DATA_COLLECT, ConstantsUrl.APP_KEY_TVPAI, ConstantsUrl.APP_SALT_TVPAI);

        ReportData reportData = new ReportData();
        reportData.tv_mac = SharedData.getInstance().getString(SharedData.Keys.MAC_DEVICE_CONNECT, "");

        Behavior behavior = new Behavior();
        behavior.id = getBehaviorId();
        behavior.pre_id = SpUtil.getString(MyApplication.getContext(), KEY_BEHAVIOR_ID_DISLIKE, "");
        behavior.type = 2; //行为类型 1：播放play; 2：不喜欢标签；
        behavior.video_id = video_id;
        behavior.params = dislikeTagIds;

        reportData.behavior = behavior;
        Log.d(TAG,"reportData: " + new Gson().toJson(reportData));
        reportDislikeData(paramsUtil.getFullRequestUrl(), new Gson().toJson(reportData), behavior.id);*/
    }

    private static void reportDislikeData(String url, String json, final String behaviorId) {
        /*ApiClient.postString(url, json, new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                IRLog.d(TAG, "onError.response" + e);
            }

            @Override
            public void onResponse(String response, int id) {
                IRLog.d(TAG, "onSuccess. response = " + response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getInt("code") == 0) {
                        SpUtil.putString(MyApplication.getContext(), KEY_BEHAVIOR_ID_DISLIKE, behaviorId);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });*/
    }

    /**
     * 上报数据给第三方平台
     * @param eventId
     * @param map
     */
    public static void reportEventToThird(String eventId, Map<String, String> map) {
       /* //上传给umeng
        if (null == map) {
            Log.d(TAG, "reportEventToThird: map is null !!!");
            return;
        }
        MobclickAgent.onEvent(MyApplication.getContext(), eventId, map);

        //上传给个数
        try {
            JSONObject jsonObject = new JSONObject();
            for(Map.Entry<String, String> entry : map.entrySet()){
                String mapKey = entry.getKey();
                String mapValue = entry.getValue();
                Log.d(TAG, "reportEventToThird: key: " + mapKey + " value: " + mapValue);
                jsonObject.put(mapKey, mapValue);
            }
            GsManager.getInstance().onEvent(eventId, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }*/
    }

    /**
     * 上报数据给第三方平台
     * @param eventId
     */
    public static void reportEventToThird(String eventId) {
      /*  //上传给umeng
        MobclickAgent.onEvent(MyApplication.getContext(), eventId);
        //上传给个数
        GsManager.getInstance().onEvent(eventId);*/
    }

}
