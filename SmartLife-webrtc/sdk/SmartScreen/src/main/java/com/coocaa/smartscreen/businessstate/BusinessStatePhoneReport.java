package com.coocaa.smartscreen.businessstate;

import android.content.Context;
import android.util.Log;

import com.coocaa.smartscreen.businessstate.object.BusinessState;
import com.coocaa.smartscreen.businessstate.object.CmdData;

import java.util.ArrayList;
import java.util.List;

import swaiotos.channel.iot.IOTChannel;
import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.ss.session.Session;

/**
 * Describe: 手机端-业务状态获取接口
 *
 *  1.业务初始接口：<br>
 *    接口定义：init(Context context)<br>
 *       描述：初始化环境变量<br>
 *   <br><br>
 *
 *  1.获取业务状态接口：<br>
 *       描述：主要为移动端向Dangle端请求当前业务状态，实际操作为向业务端发送一条CMD消息。<br>
 *
 *<br><br>
 * Created by AwenZeng on 2020/12/18
 */
public class BusinessStatePhoneReport implements IBusinessStatePhoneReport, IBusinessStateListener {

    private Context mContext;
    private static BusinessStatePhoneReport instance;
    private SSChannel mSSChannel;
    private Session mySession;
    private boolean isInit = false;
    private final byte[] lock = new byte[0];
    public static final String LOG_TAG = "BusinessState";
    public static final String IOT_CLIENT_ID = "ss-iotclientID-9527";
    public static final String SMARTSCRREEN_CLIENT_ID = "ss-clientID-SmartScreen";

    private List<IBusinessStateListener> mListeners = new ArrayList<>();

    public static BusinessStatePhoneReport getDefault() {
        if (instance == null) {
            synchronized (BusinessStatePhoneReport.class) {
                if (instance == null) {
                    instance = new BusinessStatePhoneReport();
                }
            }
        }
        return instance;
    }


    /**
     * 业务状态初始化
     * @param context 环境变量
     */
    @Override
    public  void init(Context context) {
        mContext = context.getApplicationContext();
        IOTChannel.mananger.open(mContext, "com.coocaa.smartscreen", new IOTChannel.OpenCallback() {
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
                mSSChannel = null;
                isInit = false;
            }
        });
    }

    /**
     * 获取业务当前状态
     */
    @Override
    public void getBusinessState() {
        if(!isInit){
            Log.e(LOG_TAG,"请先初始化接口");
            init(mContext);
            return ;
        }
        try{
            Log.d(LOG_TAG,"获取当前业务状态");
            if(mSSChannel!=null&&mSSChannel.getSessionManager().getConnectedSession()!=null){
                CmdData data = new CmdData("getBusinessState", CmdData.CMD_TYPE.STATE.toString(), "");
                String content = data.toJson();
                IMMessage imMessage = IMMessage.Builder.createTextMessage(mySession,mSSChannel.getSessionManager().getConnectedSession(), SMARTSCRREEN_CLIENT_ID,IOT_CLIENT_ID, content);
                mSSChannel.getIMChannel().send(imMessage);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onUdpateBusinessState(BusinessState businessState) {
        synchronized (lock){
            for (IBusinessStateListener listener : mListeners) {
                listener.onUdpateBusinessState(businessState);
            }
        }
    }

    @Override
    public  void addListener(IBusinessStateListener listener) {
        if (listener!=null&&!mListeners.contains(listener)){
            mListeners.add(listener);
        }
    }

    @Override
    public  void removeListener(IBusinessStateListener listener) {
        if (listener!=null&&mListeners.contains(listener)){
            mListeners.remove(listener);
        }
    }
}
