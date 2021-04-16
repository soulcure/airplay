package swaiotos.runtime.h5;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.coocaa.businessstate.object.BusinessState;
import com.coocaa.businessstate.object.User;
import com.coocaa.smartsdk.SmartApi;

import org.greenrobot.eventbus.EventBus;

import java.util.Map;

import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.SSChannelClient;
import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.runtime.AppletRuntimeManager;
import swaiotos.runtime.h5.common.bean.H5ContentBean;
import swaiotos.runtime.h5.common.bean.SsePushBean;
import swaiotos.runtime.h5.common.event.OnGameEngineInfo;
import swaiotos.runtime.h5.common.event.OnGameInfoCBData;
import swaiotos.runtime.h5.common.event.OnRemoteAppVersion;
import swaiotos.runtime.h5.common.event.OnRemoteStateData;
import swaiotos.runtime.h5.common.util.EmptyUtils;
import swaiotos.runtime.h5.common.util.LogUtil;
import swaiotos.runtime.h5.common.util.URLSplitUtil;
import swaiotos.runtime.h5.core.os.H5CoreOS;
import swaiotos.runtime.h5.core.os.H5RunType;


/**
 * @ClassName: AIOTSSClientService
 * @Author: AwenZeng
 * @CreateDate: 2020/4/1 20:26
 * @Description: AIOT推送接收service
 */
public class H5SSClientService extends SSChannelClient.SSChannelClientService {
    private static final String TAG = "H5SSClientService";

    public static String owner;

    public H5SSClientService() {
        super("H5SSClientService");
        Log.d(TAG, "H5SSClientService() called");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "H5SSClientService onCreate called");
        if(isTVOrDongle()){
            LogUtil.w("isLargeScreen");
            H5CoreOS.initH5OSRunType(H5RunType.RunType.TV_RUNTYPE_ENUM);
        }
    }

    public static boolean isTVOrDongle() {

        return !SmartApi.isMobileRuntime();
    }

    @Override
    protected boolean handleIMMessage(IMMessage message, SSChannel channel) {
        LogUtil.androidLog("IOT-cloud push msg：" + message.getContent());
        handleSsePush(message);
        return true;
    }


    private void handleSsePush(IMMessage message){

        String content = message.getContent();
        String targetClient = message.getClientTarget();

        owner = message.getExtra("owner");
        LogUtil.d( "handleSsePush() called with: message = [" + message + "],owner = [" +owner +"]");

        H5ChannelInstance.OnReceiveMsg callback = H5ChannelInstance.getSingleton().getOnReceiveMsg();
        if (callback != null) {
            callback.onReceive(targetClient, content);
        }

        try {
            EventBus.getDefault().post(message);
            if(EmptyUtils.isNotEmpty(message.getContent())){
                JSONObject contentObj = JSON.parseObject(message.getContent());

                if(contentObj!=null ){
                    if(contentObj.containsKey("event")){
                        SsePushBean ssePushBean = JSON.parseObject(message.getContent(), SsePushBean.class);
                        if (ssePushBean == null) {
                            Log.w(TAG, "handleSsePush() ssePushBean == null");
                            return;
                        }
                        if (ssePushBean.getClientVersion() != null) {
                            if(!EventBus.getDefault().hasSubscriberForEvent(OnRemoteAppVersion.class)){
                                //TV或者手机没有打开webview，这个获取客户端版本号的消息会被丢失
                                LogUtil.e("OnRemoteAppVersion has not webView subscriber!");
                            }else{
                                EventBus.getDefault().post(new OnRemoteAppVersion("onRemoteAppVersion", ssePushBean.getClientVersion(), message.getClientSource()));
                            }
                        } else if (ssePushBean.getEvent() != null) {
                            switch (SsePushBean.EVENT_TYPE.valueOf(ssePushBean.getEvent())){
                                case DONGLEANDTV:
                                    //H5AppletActivity.start(this,ssePushBean.getData());
                                    // 接收到发给TV 的广播，h5-runtime需要判断这个广播是不是属于Dongle。
                                    if(H5RunType.RunType.TV_RUNTYPE_ENUM == H5CoreOS.getH5RunType()){
                                        sendSsePushEvent(SsePushBean.EVENT_TYPE.valueOf(ssePushBean.getEvent()),ssePushBean,owner);
                                    }
                                    break;
                                case ALL_DEVICES:
                                    sendSsePushEvent(SsePushBean.EVENT_TYPE.valueOf(ssePushBean.getEvent()),ssePushBean,owner);
                                    break;
                                default:
                                    break;
                            }
                        } else {
                            Log.w(TAG, "handleSsePush() ssePushBean == null");
                        }
                    }else if(contentObj.containsKey("type") && "custom_event".equalsIgnoreCase(contentObj.getString("type"))){
                        //状态回调
                        if(isTVOrDongle()){
                            //TV 上的webview才需要处理手机发过来的remote的逻辑
                            JSONObject paramObj = JSON.parseObject(contentObj.getString("param"));
                            String type=  paramObj.getString("type");
                            String cmd = paramObj.getString("cmd");
                            String param = paramObj.getString("param");
                            handleRemoteIntent(cmd,param,type);
                        }else{
                            Log.w(TAG, "mobile runtime do not need handle remote intent");
                        }
                    }else if(contentObj.containsKey("type") && "game_engine".equalsIgnoreCase(contentObj.getString("type"))){
                        OnGameEngineInfo engineInfo = OnGameEngineInfo.fromJSONString(message.getContent());
                        if(isTVOrDongle()){
                            //TV 上的webview才需要处理手机发过来的游戏控制器的消息

                            LogUtil.d("OnGameEngineInfo received event: " + engineInfo.engineEvent);

                            /**
                             *
                             * 说明：
                             * 1、游戏引擎远端的手机app能够支持以下几种事件：
                             * 1)、pause：暂停  //所有的player都能够暂停游戏；
                             * 2)、resume：恢复  //所有的player都能够恢复游戏；
                             * 3)、create：创建  //只有一个player能够创建游戏，创建游戏后作为host；
                             * 4)、join：加入  //所有的player都能够加入游戏，host创建游戏后默认加入游戏；
                             * 5)、leave：离开  //所有的player都能够离开游戏，如果游戏过程中有人离开游戏，游戏会默认变为暂停；
                             * 6)、start：启动  //只有游戏的创建者能够启动游戏；
                             * 7)、input：按键  //所有的player都能输入按键数据；
                             * 8)、sensor：传感  //所有的player都能输入传感数据；
                             * 9)、custom_data：自定义数据  //游戏与游戏之间的额外数据传输
                             *
                             */
                            OnGameInfoCBData data = new OnGameInfoCBData("onGameEngineCB",engineInfo);
                            if("create".equals(engineInfo.engineEvent)){
                                // 创建游戏
                                createGame(data,owner);
                            }else{
                                gameEngineToWebView(data);
                            }
                        }else if( !isTVOrDongle() && ("custom_data".equals(engineInfo.engineEvent)||"tv_destroyed".equals(engineInfo.engineEvent))){
                            // custom_data 是可以双向通信的
                            OnGameInfoCBData data = new OnGameInfoCBData("onGameEngineCB",engineInfo);
                            gameEngineToWebView(data);
                        }
                        else{
                            Log.w(TAG, "mobile runtime do not need handle game_engine intent");
                        }
                    } else{
                        Log.w(TAG, "handleSsePush() can not handle " + message.getContent());
                    }
                }else{
                    Log.w(TAG, "handleSsePush() contentObj == null");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createGame(OnGameInfoCBData engine,String owner){
        if(engine.gameInfo.tvGameUrl != null && engine.gameInfo.tvGameUrl!=null){

            Map<String, String> readyLoadUrlParams = URLSplitUtil.urlSplit(engine.gameInfo.tvGameUrl);
            String readyLoadUrlRuntimeTag = readyLoadUrlParams.get("h5-runtime-tag");

            String gameType = getPackageName()+"$"+readyLoadUrlRuntimeTag;
            Intent intent = new Intent();
            intent.setClassName(getPackageName(),"swaiotos.runtime.h5.app.H5TVAppletActivity");
            intent.putExtra("url", engine.gameInfo.tvGameUrl);
            intent.putExtra(H5RunType.RUNTIME_KEY, H5RunType.TV_RUNTYPE);
            if(owner!=null){
                User mUser = User.decode(owner);
                BusinessState state = new BusinessState.Builder().owner(mUser).id(gameType).build();
                intent.putExtra("r_state", BusinessState.encode(state));
            }
            intent.putExtra("game_engine",OnGameEngineInfo.toJSONString(engine.gameInfo));
            if (!(getBaseContext() instanceof Activity)) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            LogUtil.d("createGame  getBaseContext().startActivity(intent)  recUrl == " +  engine.gameInfo.tvGameUrl);
            getBaseContext().startActivity(intent);
        }else{
           LogUtil.e("engine.tvGameUrl is  must not be null");
        }
    }

    public void gameEngineToWebView(OnGameInfoCBData info){
        if(!EventBus.getDefault().hasSubscriberForEvent(OnGameInfoCBData.class)){
            // TV或手机没有打开webview
            LogUtil.e("OnGameInfoCBData has not webView subscriber");
        }else
        {
            EventBus.getDefault().post(info);
        }
    }

    private void handleRemoteIntent(String playCmd,String param,String type){
        OnRemoteStateData remoteStateBean = new OnRemoteStateData("onRemoteCtrl",playCmd,param,type);
        Log.d(TAG, "handleSsePush()! ");
        if(!EventBus.getDefault().hasSubscriberForEvent(OnRemoteStateData.class)){
            // TV或手机没有打开webview
            LogUtil.e("OnRemoteStateData has not webView subscriber");
        }else{
            EventBus.getDefault().post(remoteStateBean);
        }

    }

    /**
     * 需要处理远端发过来的事件
     * @param ssePushBean
     */
    private void sendSsePushEvent(SsePushBean.EVENT_TYPE type, SsePushBean ssePushBean,String owner){
        Intent intent = new Intent();
        boolean isTvOrDongle = isTVOrDongle();
        if(isTvOrDongle){
            if(owner!=null){
                User mUser = User.decode(owner);
                BusinessState state = new BusinessState.Builder().owner(mUser).id(getPackageName()+"$H5_ATMOSPHERE").build();
                intent.putExtra("r_state", BusinessState.encode(state));
            }
            intent.setClassName(getPackageName(),"swaiotos.runtime.h5.app.H5TVAppletActivity");
            LogUtil.d("swaiotos.runtime.h5.app.H5TVAppletActivity");
        }else{
            intent.setClassName(getPackageName(),"swaiotos.runtime.h5.H5NPAppletActivity");
            LogUtil.d("swaiotos.runtime.h5.H5NPAppletActivity");
        }
        H5ContentBean h5Bean = H5ContentBean.fromJSONString(ssePushBean.getData());
        String recUrl = h5Bean.getH5ReceivedUrl();
        LogUtil.d("recUrl == " + recUrl);
        if(recUrl!=null){
            if(recUrl.startsWith("http")||recUrl.startsWith("https")){
                intent.putExtra("url", recUrl);
                intent.putExtra(H5RunType.RUNTIME_KEY, isTvOrDongle ? H5RunType.TV_RUNTYPE : H5RunType.MOBILE_RUNTYPE);
                intent.putExtra("event",JSON.toJSONString(ssePushBean));
                if (!(getBaseContext() instanceof Activity)) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
//                LogUtil.d("  getBaseContext().startActivity(intent) recUrl == " + recUrl);
                if(isTvOrDongle) {
                    LogUtil.d("isTvOrDongle  getBaseContext().startActivity(intent)  recUrl == " + recUrl);
                    getBaseContext().startActivity(intent);
                } else {
                    LogUtil.d("isMobile  getBaseContext().startActivity(intent)  recUrl == " + recUrl);

                    if(!EventBus.getDefault().hasSubscriberForEvent(H5ContentBean.class)){
                        // 手机端没有打开webview
                        LogUtil.e("H5ContentBean has not webView subscriber!");
                    }else{
                        EventBus.getDefault().post(h5Bean);
                    }
                }
            }else {
                LogUtil.e("sendSsePushEvent recUrl !=http  or https "+ recUrl);
                try{
                    boolean startSuccess = AppletRuntimeManager.get(getBaseContext()).startApplet(Uri.parse(recUrl));
                    if(!startSuccess){
                        LogUtil.e("Error, H5 runtime open url failure, url = " + recUrl);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }else{
            LogUtil.e("Error, H5 runtime open url == null!");

        }
    }

}
