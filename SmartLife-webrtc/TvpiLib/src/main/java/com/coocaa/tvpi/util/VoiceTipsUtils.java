package com.coocaa.tvpi.util;

import android.text.TextUtils;
import android.util.Log;

import com.coocaa.publib.data.BaseData;
import com.coocaa.publib.data.VoiceTipsResp;
import com.coocaa.publib.network.NetWorkManager;
import com.coocaa.publib.network.util.ParamsUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DefaultObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

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

    private static final String [] examplesA =
            {"播放",
            "暂停",
            "快进1分钟",
            "快退1分钟",
            "快进到10分钟",};
    private static final String [] examplesB =
            {"打开主页",
            "打开影视中心",
            "打开网络设置",
            "打开应用圈",
            "打开个人中心",};
    private static final String [] examplesC =
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

    public static void getTips() {
        NetWorkManager
                .getInstance()
                .getApiService()
                .getVoiceTips(ParamsUtil.getQueryMap(null))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultObserver<ResponseBody>() {
                    @Override
                    public void onNext(ResponseBody response) {
                        try {
                            String string = response.string();
                            Log.d(TAG, string);
                            if (!TextUtils.isEmpty(string)) {
                                if (!TextUtils.isEmpty(string)) {
                                    voiceTipsResp = BaseData.load(string, VoiceTipsResp.class);
                                } else {
                                    voiceTipsResp = BaseData.load(defaultResponse, VoiceTipsResp.class);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: ");
                        voiceTipsResp = BaseData.load(defaultResponse, VoiceTipsResp.class);
                    }

                    @Override
                    public void onComplete() {

                    }
                });


        /*ParamsUtil paramsUtil = new ParamsUtil(ConstantsUrl.URL_VOICE_TIPS, ConstantsUrl.APP_KEY_TVPAI, ConstantsUrl.APP_SALT_TVPAI);
        ApiClient.get(paramsUtil.getFullRequestUrl(), new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
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
    }
}
