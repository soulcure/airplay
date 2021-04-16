package swaiotos.channel.iot.ss.channel.base.sse.model;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.skyworthiot.iotssemsg.IotSSEMsgLib;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import swaiotos.channel.iot.ss.SSContext;
import swaiotos.channel.iot.ss.analysis.UserBehaviorAnalysis;
import swaiotos.channel.iot.ss.channel.base.sse.SSEChannel;
import swaiotos.channel.iot.ss.channel.base.sse.SSEMsgInfo;
import swaiotos.channel.iot.ss.client.event.ConnectEvent;
import swaiotos.channel.iot.ss.server.utils.Constants;
import swaiotos.channel.iot.utils.AndroidLog;
import swaiotos.channel.iot.utils.EmptyUtils;
import swaiotos.channel.iot.utils.LogUtil;
import swaiotos.channel.iot.utils.ThreadManager;

import static com.skyworthiot.iotssemsg.IotSSEMsgLib.ReceivedFileResultEnum.RECEIVEFILE_FINISHED;

/**
 * @ClassName: SsePushModel
 * @Author: AwenZeng
 * @CreateDate: 2020/3/9 17:47
 * @Description: Sse推送model
 */
public class SSEPushModel implements ISSEPushModel {
    private Context mContext;
    private IotSSEMsgLib mIotSSEMsgLib;
    private String mDeviceId = "";
    private ConnectHandler mConnectHandler;
    private SSEReceiver mReceiveListener;
    private SSEChannel.DownloadCallback mDownloadCallback;
    private SSEChannel.UploadCallback mUploadCallback;
    private SSEChannel.SendMessageCallBack mSendMessageCallback;
    private Map<String, SSEMsgInfo> mSendMessageCallbackMap;
    private int connectDelay = CONNECT_DELAY;
    private SSContext SSContext;

    private static final int CONNECT_DELAY = 1;   //SSE重连延时时间,单位秒
    private static final int CONNECT_INCREASE = 5;  //SSE重连增长时间,单位秒

    private static final String APP_SALT = "e53fc2d3c4ca4177b280fcc1fbf69aa4";

    private static final int HANDLER_SSE_RECONNECT = 1;
    private long sseInitTime;

    public SSEPushModel(Context context, final SSContext ssContext) {
        mContext = context;
        SSContext = ssContext;

        mSendMessageCallbackMap = new ConcurrentHashMap<>();

        mConnectHandler = new ConnectHandler(context.getMainLooper());
        mIotSSEMsgLib = new IotSSEMsgLib(context, new IotSSEMsgLib.IOTSSEMsgListener() {

            @Override
            public String appSalt() {
                return APP_SALT;
            }

            @Override
            public void onSSELibError(IotSSEMsgLib.SSEErrorEnum sseErrorEnum, String s) {
                Log.e("sse", "sseErrorEnum = " + sseErrorEnum + " msg = " + s);
                //网络连接失败，重连SSE
                switch (sseErrorEnum) {
                    case ConnectHostError:
                    case SSEDisconnectError:
                    case ConnectBosHostError:
                    case RegisterSSError:
                    case RegisterIotDeviceError:
                        if (connectDelay > CONNECT_DELAY + CONNECT_INCREASE * 2) {
                            EventBus.getDefault().post(new ConnectEvent(false));
                        }

                        SSContext.getSessionManager().connectChannelSessionState(Constants.COOCAA_IOT_CHANNEL_TYPE_SSE,
                                Constants.COOCAA_IOT_CHANNEL_STATE_DISCONNECT);
                        AndroidLog.androidLog("sse-mChannelConnectState-sseError:"+sseErrorEnum);
                        reConnect(connectDelay * 1000);
                        //上报sse初始化异常的情况
                        UserBehaviorAnalysis.reportSSeInitError(mDeviceId,sseErrorEnum.name(),s);
                        break;
                }
            }

            @Override
            public void onSSEStarted() {
                Log.d("sse", "SSE Connect success sid:" + mDeviceId);
                if (connectDelay > CONNECT_DELAY) {
                    EventBus.getDefault().post(new ConnectEvent(true));
                }
                connectDelay = 1;
                mConnectHandler.removeMessages(HANDLER_SSE_RECONNECT);
                ssContext.getDeviceManager().sseLoginSuccess();

                /*SSContext.getSessionManager().connectChannelSessionState(Constants.COOCAA_IOT_CHANNEL_TYPE_SSE,
                        Constants.COOCAA_IOT_CHANNEL_STATE_CONNECT);*/

                //上报sse初始化成功的日志
                UserBehaviorAnalysis.reportSSeInitTime(mDeviceId,System.currentTimeMillis() - sseInitTime);

                String res = "SEE Login Success Sid=" + mDeviceId;
                Log.d("logfile", res);
//                LogFile.inStance().toFile(res);
            }

            @Override
            public void onSendResult(IotSSEMsgLib.SSESendResultEnum sendResult, String destId, String msgId, String msgName, String message) {
                LogUtil.androidLog("onSendResult =" + sendResult + " destId=" + destId + " msgId=" + msgId + " msgName=" + msgName + " message= " + message);

                try {
                    if (mSendMessageCallbackMap != null && mSendMessageCallbackMap.containsKey(msgId) && mSendMessageCallbackMap.size() > 0) {
                        SSEMsgInfo sseMsgInfo = mSendMessageCallbackMap.get(msgId);
                        if (sseMsgInfo != null) {
                            SSEChannel.SendMessageCallBack sendMessageCallBack = sseMsgInfo.getMessageCallBack();
                            if (sendMessageCallBack != null)
                                sendMessageCallBack.onSendErro(sendResult, message);
                            mSendMessageCallbackMap.remove(msgId);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    if (mSendMessageCallbackMap != null && mSendMessageCallbackMap.size() > 0) {
                        //过滤超过8s的数据
                        int size = mSendMessageCallbackMap.size();
                        String[] sseMsgIds = mSendMessageCallbackMap.keySet().toArray(new String[size]);
                        for (int k = 0; k < sseMsgIds.length; k++) {
                            SSEMsgInfo sseMsgInfoK = mSendMessageCallbackMap.get(sseMsgIds[k]);
                            if (sseMsgInfoK != null) {
                                long timeDiff = System.currentTimeMillis() - sseMsgInfoK.getTime();
                                if (timeDiff > 8000) {
                                    mSendMessageCallbackMap.remove(sseMsgIds[k]);
                                }
                            }
                        }
                        AndroidLog.androidLog("----mSendMessageCallbackMap-size:"+mSendMessageCallbackMap.size());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onSendFileToCloud(IotSSEMsgLib.SendFileResultEnum sendFileResult, String fileKey, long currentSize, long totalSize, int respCode, String respMsg, String toDestId) {
                LogUtil.androidLog("onSendFileToCloud sendFileResult=" + sendFileResult + "  fileKey=" + fileKey + " currentSize=" + currentSize + " totalSize=" + totalSize + " respCode" + respCode + " respMsg" + respMsg + " toDestId" + toDestId);
                if (EmptyUtils.isNotEmpty(mUploadCallback)) {
                    switch (sendFileResult) {
                        case SENDFILE_FINISHED: {
                            mUploadCallback.onFileUploaded(fileKey);
                        }
                        break;
                    }
                }
            }

            @Override
            public void onReceivedFileFromCloud(IotSSEMsgLib.ReceivedFileResultEnum receFileResult, String fileKey, long currentSize, long totalSize, String url) {
                if (receFileResult == RECEIVEFILE_FINISHED) {
                    if (EmptyUtils.isNotEmpty(mDownloadCallback)) {
                        mDownloadCallback.onFileDownloaded(fileKey, new File(url));
                    }
                }
            }

            @Override
            public void onReceivedSSEMessage(String id, String event, String data) {
                LogUtil.androidLog("onReceivedSSEMessage id=" + id + " event=" + event + " data=" + data);
                if (EmptyUtils.isNotEmpty(mReceiveListener)) {
                    AndroidLog.androidLog("sse-back-1data:"+data);
                    try {
                        mReceiveListener.onReceive(event, data);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void setReceiveListener(SSEReceiver receiveListener) {
        mReceiveListener = receiveListener;
    }

    @Override
    public boolean isSSEConnected() {
        return mIotSSEMsgLib.isSSEConnected();
    }

    @Override
    public boolean isSSEStarted() {
        return mIotSSEMsgLib.isSSEConnected();
    }


    @Override
    public boolean connectSSE(String deviceId) {
        Log.d("SmartScreenImpl", "SmartScreenImpl deviceId:" + deviceId);
        return initPushSEE(deviceId);
    }


    private void sseReConnect() {
        if (TextUtils.isEmpty(mDeviceId)) {
            Log.e("sse", "sse mDeviceId is null and core exit");
            System.exit(0);
        } else {
            if (isNetworkConnected()) {
                sseInitTime = System.currentTimeMillis();

                mIotSSEMsgLib.reConnectSSEAsSmartScreen(mDeviceId);
                connectDelay = connectDelay + 5;
                Log.e("sse", "sse 开始重连，不成功下次重连延时秒---" + connectDelay);
            } else {
                Log.e("sse", "sse 网络不可用...等待网络可用");
            }
        }
    }

    public void disconnect() {
        mIotSSEMsgLib.close();
    }


    public void reConnect() {
        mConnectHandler.sendEmptyMessage(HANDLER_SSE_RECONNECT);
    }

    public void reConnect(long delay) {
        mConnectHandler.sendEmptyMessageDelayed(HANDLER_SSE_RECONNECT, delay);
    }

    @Override
    public void sendSSEMessage(String toDeviceId, String msgId, String msgName, String message, SSEChannel.SendMessageCallBack callBack) throws IOException {
//        mSendMessageCallback = callBack;
        AndroidLog.androidLog("sse client sendMessage=" + message);
        if (mIotSSEMsgLib.isSSEConnected()) {

            try {
                if (callBack != null && !TextUtils.isEmpty(msgId)) {
                    SSEMsgInfo sseMsgInfo = new SSEMsgInfo();
                    sseMsgInfo.setMessageCallBack(callBack);
                    sseMsgInfo.setTime(System.currentTimeMillis());
                    mSendMessageCallbackMap.put(msgId,sseMsgInfo);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            boolean res = mIotSSEMsgLib.sendMessage(toDeviceId, msgId, msgName, message);
            AndroidLog.androidLog("sse client sendMessage result=" + res);
            if (!res) {
                AndroidLog.androidLog("sse client sendMessage fail and reConnect");
                reConnect();
            }
        } else {
            initPushSEE(mDeviceId);
            //throw new IOException();
        }
    }

    @Override
    public void uploadFile(String target, File file, String uid, SSEChannel.UploadCallback callback) throws IOException {
        if (mIotSSEMsgLib.isSSEConnected()) {
            mIotSSEMsgLib.syncFileToCloud(target, file, uid);
            mUploadCallback = callback;
        } else {
            initPushSEE(mDeviceId);
            //throw new IOException();
        }
    }

    @Override
    public void downloadFile(String fileKey, SSEChannel.DownloadCallback callback) {
        mIotSSEMsgLib.syncFileFromCloud(fileKey);
        mDownloadCallback = callback;
    }

    /**
     * SSE初始化
     */
    public boolean initPushSEE(String deviceId) {
        if (EmptyUtils.isNotEmpty(deviceId)) {
            connectDelay = 1;
            mDeviceId = deviceId;

            String Start = "initPushSEE Start Sid=" + deviceId;
            Log.d("logfile", Start);
//            LogFile.inStance().toFile(Start);

            sseInitTime = System.currentTimeMillis();
            mIotSSEMsgLib.connectSSEAsSmartScreen(mDeviceId);

            LogUtil.androidLog("SSE推送初始化--参数：DeviceID:" + mDeviceId);
            return true;
        } else {
            LogUtil.androidLog("设备ID或userId为空----参数：DeviceID:" + mDeviceId);
            return false;
        }

    }

    /**
     * SSE切换新账号 for mobile
     *
     * @param deviceId
     */
    public void reConnectSSE(String deviceId) {
        if (EmptyUtils.isNotEmpty(deviceId)) {
            connectDelay = 1;
            mDeviceId = deviceId;
            mIotSSEMsgLib.close();
            mIotSSEMsgLib.connectSSEAsSmartScreen(deviceId);
            LogUtil.androidLog("SSE 重连新sid--参数：DeviceID:" + deviceId);
        }
    }


    /**
     * 网络是否连接
     *
     * @return
     */
    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (EmptyUtils.isNotEmpty(networkInfo)) {
            return networkInfo.isConnected();
        }
        return false;
    }

    private class ConnectHandler extends Handler {

        public ConnectHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == HANDLER_SSE_RECONNECT) {
                ThreadManager.getInstance().ioThread(new Runnable() {
                    @Override
                    public void run() {
                        sseReConnect();
                    }
                });

            }


        }
    }
}
