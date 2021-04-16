package com.coocaa.smartscreen.connect.service;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.coocaa.smartscreen.businessstate.BusinessStatePhoneReport;
import com.coocaa.smartscreen.businessstate.object.BusinessState;
import com.coocaa.smartscreen.businessstate.object.User;
import com.coocaa.smartscreen.data.BaseData;
import com.coocaa.smartscreen.data.channel.AppInfo;
import com.coocaa.smartscreen.data.channel.events.AppInfoEvent;
import com.coocaa.smartscreen.data.channel.AppStoreParams;
import com.coocaa.smartscreen.data.channel.CmdData;
import com.coocaa.smartscreen.data.channel.MirrorScreenParams;
import com.coocaa.smartscreen.data.channel.ReverseScreenParams;
import com.coocaa.smartscreen.data.channel.events.AccountEvent;
import com.coocaa.smartscreen.data.channel.events.DongleInfoEvent;
import com.coocaa.smartscreen.data.channel.events.GetAppStateEvent;
import com.coocaa.smartscreen.data.channel.events.GetInstallApkEvent;
import com.coocaa.smartscreen.data.channel.events.MirrorScreenEvent;
import com.coocaa.smartscreen.data.channel.events.ProgressEvent;
import com.coocaa.smartscreen.data.channel.events.ReverScreenExitEvent;
import com.coocaa.smartscreen.data.channel.events.ScreenshotEvent;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.Map;
import java.util.Set;

import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.SSChannelClient;
import swaiotos.channel.iot.ss.channel.im.IMMessage;

public class MainSSClientService extends SSChannelClient.SSChannelClientService {
    private static final String TAG = MainSSClientService.class.getSimpleName();
    //    public static final String AUTH = "swaiotos.channel.iot.mobile";
    public static final String AUTH = "ss-clientID-SmartScreen";

    private Handler mHandler;
    private Context mContext;

    /**
     * interface
     */
    public interface ScreenShotCallback {
        void onSuccess(String url);

        void onFailure(String error);
    }

    private ScreenShotCallback screenShotCallback;

    public void setScreenShotCallback(ScreenShotCallback screenShotCallback) {
        this.screenShotCallback = screenShotCallback;
    }

    public MainSSClientService() {
        super("MainSSClientService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        mHandler = new Handler(Looper.getMainLooper());
        mContext = this;
        /*if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            //数字是随便写的“40”，
            nm.createNotificationChannel(new NotificationChannel("40", "App Service", NotificationManager.IMPORTANCE_DEFAULT));
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "40");

            //其中的2，是也随便写的，正式项目也是随便写
            startForeground(2 ,builder.build());
        }*/
    }

    @Override
    protected boolean handleIMMessage(IMMessage message, SSChannel channel) {
        //Log.d(TAG, "handleIMMessage " + message);
        if (null == message) {
            Log.d(TAG, "message is null !!!");
            return false;
        }
        Log.d(TAG, "chen handleIMMessage: "+message.toString());
        Log.d(TAG, "handleIMMessage type= " + message.getType());
        SSMsgDispatcher.dispatch(message);

        ProgressEvent progressEvent = new ProgressEvent();

        if (message.getType() == IMMessage.TYPE.PROGRESS) {
            int progress = Integer.parseInt(message.getContent());
            if (progress >= 0 && progress <= 100) {
                Log.d(TAG, "chandleIMMessage content= " + message.getContent());
                progressEvent.setProgress(progress);
                progressEvent.setType(IMMessage.TYPE.PROGRESS);
                EventBus.getDefault().post(progressEvent);
                MsgProgressEventObserver.onProgressLoading(progressEvent);
            }
        }

        if (message.getType() == IMMessage.TYPE.RESULT) {
            //EventBus.getDefault().post(new ProgressEvent("",false,50,message.getContent()));
            Log.d(TAG, "handleIMMessage content= " + message.getContent());
            JsonElement jsonElement = new JsonParser().parse(message.getContent());
            Set<Map.Entry<String, JsonElement>> es = jsonElement.getAsJsonObject().entrySet();
            for (Map.Entry<String, JsonElement> en : es) {
                Log.d(TAG, "onSuccess: " + en.getKey() + " " + en.getValue().toString());
                if ("result".equals(en.getKey())) {
                    if ("true".equals(en.getValue().toString())) {
                        progressEvent.setResult(true);
                    } else if ("false".equals(en.getValue().toString())) {
                        progressEvent.setResult(false);
                    }
                }

                if ("info".equals(en.getKey())) {
                    progressEvent.setInfo(en.getValue().toString());
                }
            }
            progressEvent.setProgress(-1);
            progressEvent.setType(IMMessage.TYPE.RESULT);
            EventBus.getDefault().post(progressEvent);
            MsgProgressEventObserver.onProgressResult(progressEvent);
        }

        String response = message.getExtra("response");
        if (!TextUtils.isEmpty(response)) {
            CmdData cmdData = BaseData.load(response, CmdData.class);
            if (cmdData == null) {
                return false;
            }
            if (CmdData.CMD_TYPE.SCREEN_SHOT.toString().equals(cmdData.type)) {
                Log.d(TAG, "handleIMMessage: 收到截屏");
                ScreenshotEvent event = new ScreenshotEvent();
                event.url = message.getContent();
                event.msg = message.getExtra("result");
                EventBus.getDefault().post(event);
            } else if (CmdData.CMD_TYPE.MIRROR_SCREEN.toString().equals(cmdData.type)) {
                if (MirrorScreenParams.CMD.START_MIRROR.toString().equals(cmdData.cmd)) {
                    Log.d(TAG, "handleIMMessage: 收到开始屏幕镜像");

                    MirrorScreenEvent mirrorScreenEvent = new MirrorScreenEvent();
                    mirrorScreenEvent.setResult(message.getContent());
                    EventBus.getDefault().post(mirrorScreenEvent);
                } else if (MirrorScreenParams.CMD.STOP_MIRROR.toString().equals(cmdData.cmd)) {
                    Log.e(TAG, "handleIMMessage: 收到结束屏幕镜像");
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext, "目标已经被屏幕镜像了", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else if (CmdData.CMD_TYPE.REVERSE_SCREEN.toString().equals(cmdData.type)) {
                if (ReverseScreenParams.CMD.STOP_REVERSE.toString().equals(cmdData.cmd)) {
                    Log.e(TAG, "handleIMMessage: 收到结束同屏控制");
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext, "目标已经被同屏控制了", Toast.LENGTH_SHORT).show();
                            EventBus.getDefault().post(new ReverScreenExitEvent());
                        }
                    });

                }
            } else if (CmdData.CMD_TYPE.APP_STORE.toString().equals(cmdData.type)) {
                Log.d(TAG, "handleIMMessage: GetInstallApkEvent ");
                if (AppStoreParams.CMD.SKY_COMMAND_APPSTORE_MOBILE_GET_INSTALLED_APPS.toString().equals(cmdData.cmd)) {
                    EventBus.getDefault().post(new GetInstallApkEvent(message.getContent()));
                } else if (AppStoreParams.CMD.SKY_COMMAND_APPSTORE_MOBILE_GET_APPSTATUS.toString().equals(cmdData.cmd)) {
                    Log.d(TAG, "handleIMMessage: GetAppStateEvent");
                    EventBus.getDefault().post(new GetAppStateEvent(message.getContent()));

                }
            } /*else if (CmdData.CMD_TYPE.MEDIA.toString().equals(cmdData.type)) {
                String videoSource = message.getContent();
                Log.d(TAG, "videoSource: " + videoSource);
                Log.d(TAG, "call: 发送广播");
                Intent intent = new Intent("swaiot.intent.action.VIDEO_SOURCE");
                intent.putExtra("swaiot_video_source_key", videoSource);
                intent.setPackage("com.ccos.padlauncher");
                sendBroadcast(intent);
            }*/ else if (CmdData.CMD_TYPE.ACCOUNT.toString().equals(cmdData.type)) {
                Log.d(TAG, "handleIMMessage: 获取到电视账号");
                AccountEvent accountEvent = BaseData.load(message.getContent(), AccountEvent.class);
                if (null != accountEvent) {
                    EventBus.getDefault().post(accountEvent);
                }
            }
        }

        String content = message.getContent();
        Log.d(TAG, "handleIMMessage     content:" + content);
        if (!TextUtils.isEmpty(content)) {
            try {
                if (content.contains("SIGNALING_ANSWER")
                        || content.contains("SIGNALING_CANDIDATE")) {
                    MirrorScreenEvent mirrorScreenEvent = new MirrorScreenEvent();
                    mirrorScreenEvent.setContent(content);
                    EventBus.getDefault().post(mirrorScreenEvent);
					return true;
                }

                CmdData cmdData = BaseData.load(content, CmdData.class);
                if (cmdData == null) {
                    return false;
                }
                if (CmdData.CMD_TYPE.STATE.toString().equals(cmdData.type)){
                    BusinessState state = BusinessState.decode(cmdData.param);
                    Log.d(TAG, "handleIMMessage     state" + cmdData.param);
                    //新版本dangele 发的老数据version为3，新版本屏蔽掉老数据
                    if(!state.version.equals("3")){
                        if(!TextUtils.isEmpty(state.extra)){
                            JSONObject jsonObject = JSONObject.parseObject(state.extra);
                            String owner = jsonObject.getString("owner");
                            if(!TextUtils.isEmpty(owner)){
                                User user = User.decode(owner);
                                state.owner = user;
                            }
                        }
                        Log.d(BusinessStatePhoneReport.LOG_TAG, "handleIMMessage     state" + cmdData.param);
                        BusinessStatePhoneReport.getDefault().onUdpateBusinessState(state);
                    }
                } else if (CmdData.CMD_TYPE.APP_INFOS.toString().equals(cmdData.type)){
                    Log.d(TAG, "handleIMMessage: 获取到APP_INFOS = " + cmdData.param);
                    List<AppInfo> appInfos = new Gson().fromJson(cmdData.param,
                            new TypeToken<List<AppInfo>>(){}.getType());
                    EventBus.getDefault().post(new AppInfoEvent(appInfos));
                    MsgAppInfoEventObserver.onAppInfoLoaded(appInfos);
                } else if (CmdData.CMD_TYPE.DONGLE_INFO.toString().equals(cmdData.type)) {
                    DongleInfoEvent dongleInfoEvent = new Gson().fromJson(cmdData.param,DongleInfoEvent.class);
                    EventBus.getDefault().post(dongleInfoEvent);
                }
            } catch (Exception e) {
            }
        }
        return true;
    }

}
