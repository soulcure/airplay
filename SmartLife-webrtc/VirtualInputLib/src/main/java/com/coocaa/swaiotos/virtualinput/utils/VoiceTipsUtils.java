package com.coocaa.swaiotos.virtualinput.utils;

import android.text.TextUtils;
import android.util.Log;

import com.coocaa.publib.data.VoiceTipsResp;
import com.coocaa.smartscreen.data.voice.VoiceAdviceInfo;
import com.coocaa.smartscreen.repository.Repository;
import com.coocaa.smartscreen.repository.callback.RepositoryCallback;
import com.coocaa.smartscreen.repository.service.VoiceControlRepository;
import com.coocaa.smartscreen.repository.utils.Preferences;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import okhttp3.Call;

/**
 * Created by WHY on 2018/4/4.
 */

public class VoiceTipsUtils {
    private static final String TAG = VoiceTipsUtils.class.getSimpleName();

    /*
    examplesA = @[@"播放",@"暂停",@"快进",@"快退",@"快进1分钟",@"快退1分钟",@"快进到10分钟",
                      @"快退到10分钟"];
        examplesB = @[@"打开主页",@"打开影视中心",@"打开网络设置",@"打开应用圈",@"打开个人中心"
                      ];
        examplesC = @[@"湖南卫视",@"播放周杰伦的歌",@"今天天气怎么样",
                      @"播放声临其境",@"播放远大前程",@"播放熊出没",@"我想看鹿晗",@"今日大盘指数",
                      @"给我讲个笑话"];
    */

    private static final String[] examplesA =
            {"播放",
                    "暂停",
                    "快进1分钟",
                    "快退1分钟",
                    "快进到10分钟",};
    private static final String[] examplesB =
            {"打开主页",
                    "打开影视中心",
                    "打开网络设置",
                    "打开应用圈",
                    "打开个人中心",};
    private static final String[] examplesC =
            {"湖南卫视",
                    "播放周杰伦的歌",
                    "今天天气怎么样",
                    "播放声临其境",
                    "播放熊出没",
                    "我想看鹿晗",
                    "今日大盘指数",
                    "给我讲个笑话",
                    "播放远大前程",};

    public static List<String> getTipsString() {
        List<String> tips = new ArrayList<>();

        Random rand = new Random();

        int i = rand.nextInt(5);
        tips.add(examplesA[i]);

        i = rand.nextInt(5);
        tips.add(examplesB[i]);

        List<Integer> indexList = new ArrayList<>();
        while (indexList.size() < 3) {
            i = rand.nextInt(9);
            if (!indexList.contains(i)) {
                tips.add(examplesC[i]);
                indexList.add(i);
            }
        }

        return tips;
    }

    public static VoiceTipsResp voiceTipsResp;
    public static int tipIndex = -1;
    private static String defaultResponse = "{\"code\":0,\"data\":[[\"'播放声临其境'\",\" '暂停'\",\" '给我讲个笑话'\",\" '打开个人中心'\",\" '我想看鹿晗'\"],[\" '打开主页'\",\" '快退1分钟'\",\" '快进1分钟'\",\" '播放周杰伦的歌'\",\" '播放熊出没'\"],[\" '打开网络设置'\",\" '今日大盘指数'\",\" '播放'\",\" '今天天气怎么样'\",\" '快进到10分钟'\"],[\" '打开应用圈'\",\" '打开影视中心'\",\" '湖南卫视'\",\" '播放远大前程'\"]],\"msg\":\"成功\"}";

    public static String[] getTips() {
        String[] list = null;
        if (Preferences.VoiceAdvice.getUpdateTime() == null) {
            loadData();
        } else if (System.currentTimeMillis() - Long.valueOf(Preferences.VoiceAdvice.getUpdateTime()) < 60 * 60 * 12 * 1000) {
            String advice = Preferences.VoiceAdvice.getVoiceAdvice();
            if (advice != null) {
                advice = advice.replace("\"", "").replace("|", " ");
                Log.d(TAG, "showAdvice: " + advice);
                list = advice.split(" ");
            }
        } else {
            loadData();
            String advice = Preferences.VoiceAdvice.getVoiceAdvice();
            if (advice != null) {
                advice = advice.replace("\"", "").replace("|", " ");
                Log.d(TAG, "showAdvice: " + advice);
                list = advice.split(" ");
            }
        }

        if (list != null && list.length >= 5) {
            return list;
        } else {
            list = new String[]{"今天天气怎么样", "播放《赘婿》第三集", "音量调节到 12", "10 分钟后提醒我开会", "给我讲个笑话"};
            return list;
        }

    }



    /* ParamsUtil paramsUtil = new ParamsUtil(ConstantsUrl.URL_VOICE_TIPS, ConstantsUrl.APP_KEY_TVPAI, ConstantsUrl.APP_SALT_TVPAI);
     ApiClient.get(paramsUtil.getFullRequestUrl(), new StringCallback() {
         @Override
         public void onError(Call call, Exception e, int id) {
             Log.d(TAG, "onError: ");
             voiceTipsResp = BaseData.load(defaultResponse, VoiceTipsResp.class);
         }

         @Override
         public void onResponse(String response, int id) {
             Log.d(TAG, "onResponse: " + response);
             if (!TextUtils.isEmpty(response)) {
                 voiceTipsResp = BaseData.load(response, VoiceTipsResp.class);
             } else {
                 voiceTipsResp = BaseData.load(defaultResponse, VoiceTipsResp.class);
             }
         }
     });*/


    private static void loadData() {
        Repository.get(VoiceControlRepository.class)
                .getAdvice()
                .setCallback(new RepositoryCallback<VoiceAdviceInfo>() {
                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onSuccess(VoiceAdviceInfo adviceInfo) {
                        JsonElement jsonElement = new JsonParser().parse(adviceInfo.getValue());
                        Set<Map.Entry<String, JsonElement>> es = jsonElement.getAsJsonObject().entrySet();
                        for (Map.Entry<String, JsonElement> en : es) {
                            Log.d(TAG, "onSuccess: " + en.getKey() + " " + en.getValue().toString());
                            if(en.getKey().equals("description")) {
                                Preferences.VoiceAdvice.saveVoiceAdvice(en.getValue().toString());
                                Preferences.VoiceAdvice.saveUpdateTime(String.valueOf(System.currentTimeMillis()));
                            }
                        }
                    }

                    @Override
                    public void onFailed(Throwable e) {
                        Preferences.VoiceAdvice.saveUpdateTime(String.valueOf(System.currentTimeMillis()));
                    }
                });
    }
}
