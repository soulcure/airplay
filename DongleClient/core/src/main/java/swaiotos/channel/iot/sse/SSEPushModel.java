package swaiotos.channel.iot.sse;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.skyworthiot.iotssemsg.IotSSEMsgLib;


import java.io.File;
import java.io.IOException;
import java.util.List;

import swaiotos.channel.iot.utils.AppUtils;
import swaiotos.channel.iot.utils.ThreadManager;

import static com.skyworthiot.iotssemsg.IotSSEMsgLib.ReceivedFileResultEnum.RECEIVEFILE_FINISHED;
import static com.skyworthiot.iotssemsg.IotSSEMsgLib.SendFileResultEnum.SENDFILE_FINISHED;

public class SSEPushModel {
    private static final String TAG = "sse";
    private static final String APP_SALT = "e53fc2d3c4ca4177b280fcc1fbf69aa4";


    private Context mContext;
    private String mDeviceId;
    private ConnectHandler mConnectHandler;


    private List<LoginCallback> mLoginCallback;

    private SSEReceiver mReceiveListener;
    private SendMessageCallBack mSendMessageCallback;
    private UploadCallback mUploadCallback;
    private DownloadCallback mDownloadCallback;


    private int connectDelay = CONNECT_DELAY;

    private static final int CONNECT_DELAY = 1;   //SSE重连延时时间,单位秒
    private static final int CONNECT_INCREASE = 5;  //SSE重连增长时间,单位秒


    private static final int HANDLER_SSE_RECONNECT = 1;


    public interface LoginCallback {
        void onSuccess();

        void onFail();
    }

    public interface SSEReceiver {
        void onReceive(String tag, String message);
    }

    public interface SendMessageCallBack {
        void onSendErr(IotSSEMsgLib.SSESendResultEnum sseSendResultEnum);
    }

    public interface UploadCallback {
        void onFileUploaded(String fileKey);
    }

    public interface DownloadCallback {
        void onFileDownloaded(String fileKey, File file);
    }


    private IotSSEMsgLib mIotSSEMsgLib;


    public SSEPushModel(Context context) {
        mContext = context;
        mConnectHandler = new ConnectHandler(context.getMainLooper());
        mIotSSEMsgLib = new IotSSEMsgLib(context,
                new IotSSEMsgLib.IOTSSEMsgListener() {
                    @Override
                    public String appSalt() {
                        return APP_SALT;
                    }

                    @Override
                    public void onSSELibError(IotSSEMsgLib.SSEErrorEnum sseErrorEnum, String s) {
                        Log.e(TAG, "sseErrorEnum = " + sseErrorEnum + " msg = " + s);
                        //网络连接失败，重连SSE
                        switch (sseErrorEnum) {
                            case ConnectHostError:
                            case SSEDisconnectError:
                            case ConnectBosHostError:
                            case RegisterSSError:
                            case RegisterIotDeviceError:
                                reConnect();

                                for (LoginCallback item : mLoginCallback) {
                                    item.onFail();
                                }
                                break;
                        }
                    }

                    @Override
                    public void onSSEStarted() {
                        Log.d(TAG, "SSE Connect success sid:" + mDeviceId);
                        connectDelay = 1;
                        mConnectHandler.removeMessages(HANDLER_SSE_RECONNECT);

                        for (LoginCallback item : mLoginCallback) {
                            item.onSuccess();
                        }

                    }

                    @Override
                    public void onSendResult(IotSSEMsgLib.SSESendResultEnum sendResult, String destId, String msgId, String msgName, String message) {
                        Log.d(TAG, "onSendResult =" + sendResult + " destId=" + destId + " msgId=" + msgId + " msgName=" + msgName + " message= " + message);
                        if (mSendMessageCallback != null) {
                            mSendMessageCallback.onSendErr(sendResult);
                        }
                    }

                    @Override
                    public void onSendFileToCloud(IotSSEMsgLib.SendFileResultEnum sendFileResult, String fileKey, long currentSize, long totalSize, int respCode, String respMsg, String toDestId) {
                        Log.d(TAG, "onSendFileToCloud =" + sendFileResult + " fileKey=" + fileKey);
                        if (sendFileResult == SENDFILE_FINISHED) {
                            if (mUploadCallback != null) {
                                mUploadCallback.onFileUploaded(fileKey);
                            }
                        }
                    }

                    @Override
                    public void onReceivedFileFromCloud(IotSSEMsgLib.ReceivedFileResultEnum recFileResult, String fileKey, long currentSize, long totalSize, String url) {
                        Log.d(TAG, "onReceivedFileFromCloud =" + recFileResult + " fileKey=" + fileKey + " url=" + url);
                        if (recFileResult == RECEIVEFILE_FINISHED) {
                            if (mDownloadCallback != null) {
                                mDownloadCallback.onFileDownloaded(fileKey, new File(url));
                            }
                        }
                    }

                    @Override
                    public void onReceivedSSEMessage(String id, String event, String data) {
                        Log.d(TAG, "onReceivedSSEMessage id=" + id + " event=" + event + " data=" + data);
                        if (mReceiveListener != null) {
                            mReceiveListener.onReceive(event, data);
                        }
                    }
                });

    }


    /**
     * SSE初始化
     */
    public boolean initPushSEE(final String deviceId) {
        return initPushSEE(deviceId, null);
    }


    /**
     * SSE初始化
     */
    public boolean initPushSEE(final String deviceId, List<LoginCallback> callback) {
        if (TextUtils.isEmpty(deviceId)) {
            Log.e(TAG, "initPushSEE deviceId is empty");
            for (LoginCallback item : callback) {
                item.onFail();
            }
            return false;
        }

        mDeviceId = deviceId;
        mLoginCallback = callback;
        ThreadManager.getInstance().ioThread(new Runnable() {
            @Override
            public void run() {
                connectDelay = 1;
                mIotSSEMsgLib.connectSSEAsSmartScreen(mDeviceId);
            }
        });
        return true;
    }

    /**
     * SSE切换新账号 for mobile
     *
     * @param deviceId
     */
    public void reConnectSSE(String deviceId) {
        if (TextUtils.isEmpty(deviceId)) {
            Log.e(TAG, "reConnectSSE deviceId is empty");
        }

        mDeviceId = deviceId;
        ThreadManager.getInstance().ioThread(new Runnable() {
            @Override
            public void run() {
                connectDelay = 1;
                mIotSSEMsgLib.close();
                mIotSSEMsgLib.connectSSEAsSmartScreen(mDeviceId);
            }
        });
    }


    public void setReceiveListener(SSEReceiver receiveListener) {
        mReceiveListener = receiveListener;
    }

    public boolean isSSEConnected() {
        return mIotSSEMsgLib.isSSEConnected();
    }


    private void sseReConnect() {
        if (TextUtils.isEmpty(mDeviceId)) {
            Log.e(TAG, "sse mDeviceId is null and core exit");
            System.exit(0);
        } else {
            if (AppUtils.isNetworkConnected(mContext)) {
                mIotSSEMsgLib.reConnectSSEAsSmartScreen(mDeviceId);
                connectDelay = connectDelay + 5;
                Log.e(TAG, "sse 开始重连，不成功下次重连延时秒---" + connectDelay);
            } else {
                Log.e(TAG, "sse 网络不可用...等待网络可用");
            }
        }
    }

    public void disconnect() {
        mDeviceId = null;
        mIotSSEMsgLib.close();
    }


    public void reConnect() {
        mConnectHandler.sendEmptyMessageDelayed(HANDLER_SSE_RECONNECT, connectDelay * 1000);
    }


    public void sendSSEMessage(String toDeviceId, String msgId, String msgName, String message,
                               SendMessageCallBack callBack) throws IOException {
        mSendMessageCallback = callBack;
        Log.d(TAG, "sendSSEMessage:" + message);
        if (mIotSSEMsgLib.isSSEConnected()) {
            boolean res = mIotSSEMsgLib.sendMessage(toDeviceId, msgId, msgName, message);
            Log.d(TAG, "mIotSSEMsgLib sendMessage result:" + res);
        } else {
            initPushSEE(mDeviceId);
            throw new IOException();
        }
    }

    public void uploadFile(String target, File file, String uid,
                           UploadCallback callback) throws IOException {
        if (mIotSSEMsgLib.isSSEConnected()) {
            mUploadCallback = callback;
            mIotSSEMsgLib.syncFileToCloud(target, file, uid);
        } else {
            initPushSEE(mDeviceId);
            throw new IOException();
        }
    }

    public void downloadFile(String fileKey, DownloadCallback callback) {
        mIotSSEMsgLib.syncFileFromCloud(fileKey);
        mDownloadCallback = callback;
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
