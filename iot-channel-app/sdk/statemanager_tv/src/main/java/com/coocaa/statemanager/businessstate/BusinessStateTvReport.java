package com.coocaa.statemanager.businessstate;

import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.coocaa.statemanager.data.AppResolver;
import com.coocaa.statemanager.common.bean.BusinessState;
import com.coocaa.statemanager.common.bean.CmdData;
import com.coocaa.statemanager.common.bean.SystemUpdateInfo;
import com.coocaa.statemanager.common.bean.User;
import com.coocaa.statemanager.common.utils.StateUtils;
import com.coocaa.statemanager.common.utils.SystemInfoUtil;

import java.util.ArrayList;
import java.util.Map;

import swaiotos.channel.iot.IOTChannel;
import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.ss.session.Session;
import swaiotos.channel.iot.utils.EmptyUtils;


/**
 * Describe: TV端-业务状态上报
 * Created by AwenZeng on 2020/12/18
 */
public class BusinessStateTvReport implements IBusinessStateTvReport {
    private volatile static BusinessStateTvReport instance;
    private SSChannel mSSChannel;
    private Session mySession;
    private boolean isInit = false;
    private String mOwner;
    private String mExtra;
    private Context mContext;
    private BusinessState mCurrentState;
    /**
     * 加入此filters的业务，不返回首页
     */
    private ArrayList<String> filters;
    public static final String LOG_TAG = "BusinessState";
    public static final String IOT_CLIENT_ID = "ss-iotclientID-9527";
    public static final String SMARTSCRREEN_CLIENT_ID = "ss-clientID-SmartScreen";

    public static BusinessStateTvReport getDefault() {
        if (instance == null) {
            synchronized (BusinessStateTvReport.class) {
                if (instance == null) {
                    instance = new BusinessStateTvReport();
                }
            }
        }
        return instance;
    }

    public BusinessStateTvReport() {
        filters = new ArrayList<>();
        filters.add("com.tianci.de");//投屏业务
        filters.add("com.swaiot.webrtc");//PC投电视业务
    }

    @Override
    public  void init(Context context){
        if(context == null){
            Log.e(LOG_TAG,"context为空");
            return;
        }
        mContext = context.getApplicationContext();
        IOTChannel.mananger.open(mContext, "swaiotos.channel.iot", new IOTChannel.OpenCallback() {
            @Override
            public void onConntected(SSChannel ssChannel) {
                try {
                    isInit = true;
                    mSSChannel = ssChannel;
                    mySession = mSSChannel.getSessionManager().getMySession();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String s) {
                isInit = false;
                mSSChannel = null;
                mySession = null;
            }
        });
    }

    @Override
    public void updateBusinessState(String id, String values) {
        if(!isInit){
            Log.e(LOG_TAG,"请先初始化接口");
            init(mContext);
            return;
        }
        try{
            mCurrentState = new BusinessState();
            mCurrentState.id = id;
            if(EmptyUtils.isNotEmpty(mOwner)){
                mCurrentState.owner = User.decode(mOwner);
            }
            mCurrentState.values = values;
            mCurrentState.extra = mExtra;
            if(mSSChannel!=null){
                mCurrentState.version = "0";
                mCurrentState.type = id;
                if(values == null){
                    mCurrentState.values = "{}";
                }
                Log.d(LOG_TAG, "updateBusinessState:"+JSONObject.toJSONString(mCurrentState));
                CmdData data = new CmdData("", CmdData.CMD_TYPE.STATE.toString(), JSONObject.toJSONString(mCurrentState));
                String content = data.toJson();
                IMMessage imMessage = IMMessage.Builder.createBroadcastTextMessage(mySession, IOT_CLIENT_ID, SMARTSCRREEN_CLIENT_ID, content);
                mSSChannel.getIMChannel().send(imMessage);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void exitBusiness() {
        if(!isInit){
            Log.e(LOG_TAG,"请先初始化接口");
            init(mContext);
            return;
        }
        try{
            mCurrentState = null;
            BusinessState state = new BusinessState();
            state.id = "DEFAULT";
            state.type = state.id;
            state.values = "{}";
            state.version = "0";
            Log.d(LOG_TAG, "updateBusinessState:"+JSONObject.toJSONString(state));
            if(mSSChannel!=null){
                CmdData data = new CmdData("", CmdData.CMD_TYPE.STATE.toString(), JSONObject.toJSONString(state));
                String content = data.toJson();
                IMMessage imMessage = IMMessage.Builder.createBroadcastTextMessage(mySession, IOT_CLIENT_ID,SMARTSCRREEN_CLIENT_ID, content);
                mSSChannel.getIMChannel().send(imMessage);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * 非dangle业务，返回默认遥控器
     */
    @Override
    public void getDangleTvBusinessState() {
        try {
            Log.d(LOG_TAG, "getDangleTvBusinessState");
            ComponentName cn = StateUtils.getTopComponet(mContext);
            if (EmptyUtils.isNotEmpty(cn)) {
                Log.d(LOG_TAG, "getDangleTvBusinessState pkg:" + cn.getPackageName() + " className:" + cn.getClassName());
                if (!AppResolver.isDongleCastBusiness( cn.getPackageName(), cn.getClassName())){
                    Log.d(LOG_TAG, "sendMessage:default");
                    exitBusiness();
                }else if(EmptyUtils.isNotEmpty(mCurrentState)){
                    updateBusinessState(mCurrentState.id,mCurrentState.values);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setCurrentBusinessState(BusinessState state) {
        mCurrentState = state;
    }

    @Override
    public void setOwner(String owner) {
        mOwner = owner;
    }

    @Override
    public void disconnectBackHome(String openId) {
        ComponentName cn = StateUtils.getTopComponet(mContext);
        if(EmptyUtils.isNotEmpty(cn)){
            if(filters.contains(cn.getPackageName())){
                return;
            }
        }
        if(EmptyUtils.isNotEmpty(mOwner)){
            User user = User.decode(mOwner);
            if(user.userID.equals(openId)){
                AppResolver.comebackHome();
            }
        }
    }

    @Override
    public void setExtra(Map<String, String> extra) {
        try{
            mExtra = JSONObject.toJSONString(extra);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void reportSystemUpdateState(IMMessage imMessage) {
        if(!isInit){
            Log.e(LOG_TAG,"请先初始化接口");
            init(mContext);
            return;
        }
        try{
            SystemUpdateInfo systemUpdateInfo = new SystemUpdateInfo();
            systemUpdateInfo.isSystemUpgradeExist = SystemInfoUtil.isSystemUpgradeExist();
            if(mSSChannel!=null){
                CmdData data = new CmdData("", CmdData.CMD_TYPE.DONGLE_INFO.toString(), JSONObject.toJSONString(systemUpdateInfo));
                mSSChannel.getIMChannel().send(IMMessage.Builder.replyTextMessage(imMessage,data.toJson()));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
