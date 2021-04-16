package swaiotos.channel.iot.ss.channel.base.sse;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import swaiotos.channel.iot.ss.SSContext;
import swaiotos.channel.iot.ss.channel.base.sse.model.ISSEPushModel;
import swaiotos.channel.iot.ss.channel.base.sse.model.SSEPushModel;
import swaiotos.channel.iot.utils.AndroidLog;
import swaiotos.channel.iot.utils.NetUtils;
import swaiotos.channel.iot.utils.ThreadManager;

/**
 * @ClassName: SSEChannelImpl
 * @Author: lu
 * @CreateDate: 2020/4/10 4:47 PM
 * @Description:
 */
public class SSEChannelImpl implements SSEChannel, ISSEPushModel.SSEReceiver {
    private static final String TYPE = "base-sse";

    private SSEPushModel mSsePushModel;
    private final Map<String, List<Receiver>> mReceivers = new LinkedHashMap<>();
    private String mLSID;
    private List<Callback> mCallbacks = new ArrayList<>();
    private Context mContext;

    public SSEChannelImpl(Context context, SSContext ssContext) {
        mContext = context;
        mSsePushModel = new SSEPushModel(context, ssContext);
    }


    @Override
    public void addCallback(Callback callback) {
        synchronized (mCallbacks) {
            if (!mCallbacks.contains(callback)) {
                mCallbacks.add(callback);
            }
        }
    }

    @Override
    public void removeCallback(Callback callback) {
        synchronized (mCallbacks) {
            mCallbacks.remove(callback);
        }
    }

    @Override
    public void send(String target, String msgId, String tag, String message, SendMessageCallBack callBack) throws IOException {
        mSsePushModel.sendSSEMessage(target, msgId, tag, message, callBack);
    }

    @Override
    public void uploadFile(String target, File file, String uid, UploadCallback callback) throws IOException {
        mSsePushModel.uploadFile(target, file, uid, callback);
    }

    @Override
    public void downloadFile(String fileKey, DownloadCallback callback) {
        mSsePushModel.downloadFile(fileKey, callback);
    }

    @Override
    public void addReceiver(String tag, Receiver receiver) {
        synchronized (mReceivers) {
            List<Receiver> receivers = mReceivers.get(tag);
            if (receivers == null) {
                receivers = new ArrayList<>();
                mReceivers.put(tag, receivers);
            }
            if (!receivers.contains(receiver)) {
                receivers.add(receiver);
            }
        }
    }

    @Override
    public void removeReceiver(String tag) {
        synchronized (mReceivers) {
            List<Receiver> receivers = mReceivers.get(tag);
            if (receivers != null) {
                receivers.clear();
            }
            mReceivers.remove(tag);
        }
    }

    @Override
    public String open(String lsid) throws IOException {
        mLSID = lsid;
        String r = open();

        NetUtils.NetworkReceiver.register(mContext, mNetworkReceiver);
        return r;
    }

    @Override
    public String open() throws IOException {
        mSsePushModel.setReceiveListener(this);
        if (mSsePushModel.connectSSE(mLSID)) {
            return mLSID;
        }
        throw new IOException();
    }


    @Override
    public void reOpen(String lsid) throws IOException {
        mLSID = lsid;
        NetUtils.NetworkReceiver.register(mContext, mNetworkReceiver);
        mSsePushModel.setReceiveListener(this);

        mSsePushModel.reConnectSSE(lsid);
    }


    @Override
    public String getAddress() {
        return mLSID;
    }

    @Override
    public boolean available() {
        return mSsePushModel.isSSEConnected();
    }

    @Override
    public void close() throws IOException {
        NetUtils.NetworkReceiver.unregister(mContext, mNetworkReceiver);
        mSsePushModel.setReceiveListener(null);
    }

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public void onReceive(String tag, String message) {
        synchronized (mReceivers) {
            List<Receiver> receivers = mReceivers.get(tag);
            AndroidLog.androidLog("---sse:" + receivers + " message:" + message);
            if (receivers != null) {
                for (Receiver receiver : receivers) {
                    try {
                        receiver.onReceive(message);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    private NetUtils.NetworkReceiver mNetworkReceiver = new NetUtils.NetworkReceiver() {
        @Override
        public void onConnected() {
            Log.e("sse", "sse 网络可用...立即重连");
            ThreadManager.getInstance().ioThread(new Runnable() {
                @Override
                public void run() {
                    mSsePushModel.reConnect();
                }
            });
            synchronized (mCallbacks) {
                for (Callback callback : mCallbacks) {
                    callback.onConnected(SSEChannelImpl.this);
                }
            }
        }

        @Override
        public void onDisconnected() {
            synchronized (mCallbacks) {
                ThreadManager.getInstance().ioThread(new Runnable() {
                    @Override
                    public void run() {
                        mSsePushModel.disconnect();
                    }
                });

                for (Callback callback : mCallbacks) {
                    callback.onDisconnected(SSEChannelImpl.this);
                }
            }
        }
    };

}
