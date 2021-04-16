package swaiotos.channel.iot.ss.channel.im.cloud;

import android.util.Log;

import com.skyworthiot.iotssemsg.IotSSEMsgLib;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.SSContext;
import swaiotos.channel.iot.ss.analysis.ChannelStatistics;
import swaiotos.channel.iot.ss.analysis.UserBehaviorAnalysis;
import swaiotos.channel.iot.ss.analysis.data.SSeMsgError;
import swaiotos.channel.iot.ss.channel.base.BaseChannel;
import swaiotos.channel.iot.ss.channel.base.sse.SSEChannel;
import swaiotos.channel.iot.ss.channel.im.IMChannel;
import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.ss.channel.im.IMMessageCallback;
import swaiotos.channel.iot.ss.client.ClientManagerImpl;
import swaiotos.channel.iot.ss.client.Clients;
import swaiotos.channel.iot.ss.device.PhoneDeviceInfo;
import swaiotos.channel.iot.ss.session.Session;
import swaiotos.channel.iot.utils.AndroidLog;
import swaiotos.channel.iot.utils.CompressImage;
import swaiotos.channel.iot.utils.EmptyUtils;
import swaiotos.channel.iot.utils.HT;

/**
 * @ClassName: CloudIMChannel
 * @Author: lu
 * @CreateDate: 2020/4/10 3:54 PM
 * @Description:
 */
public class CloudIMChannel implements IMChannel, SSEChannel.Receiver, BaseChannel.Callback {
    public static final String SSE_TAG = "swaiot-os-iotchannel";
    private SSContext mSSContext;
    private SSEChannel mSseChannel;
    private Receiver mReceiver;

    private List<String> mClients = new ArrayList<>();
    private HT mHT = new HT("im-thread", true);
    private ChannelStatistics mChannelStatistics;

    public CloudIMChannel(SSContext context, SSEChannel sseChannel) {
        mSseChannel = sseChannel;
        mSSContext = context;
        mChannelStatistics = new ChannelStatistics(mSSContext, ChannelStatistics.CHANNEL.SSE);
    }

    @Override
    public String open() throws IOException {
        mSseChannel.addCallback(this);
        performOpen(false);

        return getAddress();
    }

    private void performOpen(boolean notify) {
        mSseChannel.addReceiver(SSE_TAG, this);
//        mSSContext.getController().getDeviceStateManager().updateConnective(type(), getAddress());
//        mSSContext.getSessionManager().updateMySession(type(), getAddress(), notify);
    }

    @Override
    public void setReceiver(Receiver receiver) {
        mReceiver = receiver;
    }

    @Override
    public String getAddress() {
        return mSseChannel.getAddress();
    }

    @Override
    public boolean available() {
        return mSseChannel.available();
    }

    @Override
    public void close() throws IOException {
        mSseChannel.removeCallback(this);
        performClose();
    }

    private void performClose() {
        //mSseChannel.removeReceiver(SSE_TAG);
        synchronized (mClients) {
            mClients.clear();
        }
    }

    @Override
    public void openClient(Session session, TcpClientResult callback) {
        String lsid = session.getId();
        synchronized (mClients) {
            if (!mClients.contains(lsid)) {
                mClients.add(lsid);
            }
        }
    }

    @Override
    public void reOpenLocalClient(Session session) {

    }

    @Override
    public void reOpenSSE() {
        try {
            mSseChannel.reOpen(mSSContext.getLSID());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean available(Session session) {
        String lsid = session.getId();
        synchronized (mClients) {
            return mSseChannel.available() && mClients.contains(lsid);
        }
    }

    @Override
    public void closeClient(Session session, boolean forceClose) {
        String lsid = session.getId();
        synchronized (mClients) {
            mClients.remove(lsid);
        }
    }

    @Override
    public void send(final IMMessage message, final IMMessageCallback callback) throws Exception {
        send(message.getTarget(), message, callback);
    }

    @Override
    public void send(IMMessage message) throws Exception {
        try {
            send(message, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void send(Session target, IMMessage message) throws Exception {
        send(target, message, null);
    }

    private long curTime;

    @Override
    public void send(final Session target, final IMMessage message, final IMMessageCallback callback) throws Exception {
        final String targetId = target.getId();
        AndroidLog.androidLog("--sse-send message start:" + message.getId() + " :" + System.currentTimeMillis() + " --" + Thread.currentThread().getId());
        mChannelStatistics.sendMessage(message);
        switch (message.getType()) {
            case CTR:
            case DIALOG:
            case CONFIRM:
            case CANCEL:
            case PROGRESS:
            case RESULT:
            case PROTO:
            case TEXT: {
                String data = message.encode();
                mSseChannel.send(targetId, message.getId(), SSE_TAG, data, new SSEChannel.SendMessageCallBack() {
                    @Override
                    public void onSendErro(IotSSEMsgLib.SSESendResultEnum sseSendResultEnum, String iMMessageStr) {
                        try {
                            IMMessage imMessage = IMMessage.Builder.decode(iMMessageStr);
                            switch (sseSendResultEnum) {
                                case TARGETKNOWERROR:
                                case TARGETOFFLINEERROR:
                                    if (EmptyUtils.isNotEmpty(callback)) {
                                        callback.onEnd(imMessage, -1, sseSendResultEnum.toString());
                                    }
//                                    mChannelStatistics.sseSendMessageError(imMessage);

                                    //SSE通道消息事件错误
                                    UserBehaviorAnalysis.reportSSeMsgError(mSSContext.getLSID(), targetId, imMessage.getId(), imMessage.getType().toString(), sseSendResultEnum.toString(), SSeMsgError.SENDMSG, iMMessageStr);
                                    break;
                                case TARGETONLINESUCCESS:
                                    if (EmptyUtils.isNotEmpty(callback)) {
                                        callback.onEnd(imMessage, 0, sseSendResultEnum.toString());
                                    }
                                    mChannelStatistics.receiverMessage(imMessage, 30 * 1000);
                                    break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                break;
            }
            case VIDEO:
            case IMAGE:
            case AUDIO:
            case DOC:
                if (callback != null) {
                    callback.onStart(message);
                }
                AndroidLog.androidLog("target.getId():" + target.getId() + " message.getContent():" + message.getContent());
                delayUploadFile(target, message, callback);
                break;
            default: {
                break;
            }
        }
    }

    @Override
    public boolean serverSend(IMMessage message, IMMessageCallback callback) throws Exception {
        //do nothing
        return false;
    }

    @Override
    public List<String> serverSendList() throws Exception {
        return null;
    }

    @Override
    public void removeServerConnect(String sid) {

    }

    private synchronized void delayUploadFile(final Session target, final IMMessage message, final IMMessageCallback callback) {
        long time = System.currentTimeMillis() - curTime;
        AndroidLog.androidLog("target.getId():" + target.getId() + " message.getContent():" + message.getContent() + "time:" + time + " curTime:" + curTime);
        if (time >= 3000) {
            mHT.post(new Runnable() {
                @Override
                public void run() {
                    upLoadFile(target, message, callback);
                    curTime = System.currentTimeMillis();
                }
            });
        } else {
            mHT.postDelay(new Runnable() {
                @Override
                public void run() {
                    upLoadFile(target, message, callback);
                    curTime = System.currentTimeMillis();
                }
            }, time);
        }
    }


    private void upLoadFile(Session target, final IMMessage message, final IMMessageCallback callback) {
        try {
            final String targetId = target.getId();
            String path = message.getContent();
            final File file;
            final boolean isImage = message.getType() == IMMessage.TYPE.IMAGE && CompressImage.isImageFile(path);

            if (isImage) {
                Log.e("yao", "图片压缩前---size=" + new File(path).length());
                String url = CompressImage.compressImage(path);//压缩图片
                file = new File(url);
                Log.e("yao", "图片压缩后---size=" + file.length());
            } else {
                file = new File(path);
            }

            if (!file.exists() || file.length() == 0) {
                Log.e("yao", "不是有效的文件，不上传文件服务器");
                return;
            }


            mSseChannel.uploadFile(targetId, file, message.getSource().getId(),
                    new SSEChannel.UploadCallback() {
                        @Override
                        public void onFileUploaded(String fileKey) {
                            try {
                                if (isImage) {
                                    Log.e("yao", "删除压缩后的图片--" + file.length());
                                    file.delete();
                                    //file.deleteOnExit();
                                }

                                IMMessage message1 = IMMessage.Builder.modifyContent(message, fileKey);
                                String data = message1.encode();
                                mSseChannel.send(targetId, message.getId(), SSE_TAG, data, new SSEChannel.SendMessageCallBack() {
                                    @Override
                                    public void onSendErro(IotSSEMsgLib.SSESendResultEnum sseSendResultEnum, String iMMessageStr) {
                                        try {
                                            IMMessage imMessage = IMMessage.Builder.decode(iMMessageStr);
                                            switch (sseSendResultEnum) {
                                                case TARGETKNOWERROR:
                                                case TARGETOFFLINEERROR:
                                                    if (EmptyUtils.isNotEmpty(callback)) {
                                                        callback.onEnd(imMessage, -1, sseSendResultEnum.toString());
                                                    }
                                                    break;
                                                case TARGETONLINESUCCESS:
                                                    if (EmptyUtils.isNotEmpty(callback)) {
                                                        callback.onEnd(imMessage, 0, sseSendResultEnum.toString());
                                                    }
                                                    break;
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                    }
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public String type() {
        return SSChannel.IM_CLOUD;
    }

    @Override
    public void onReceive(String content) {
        try {
            final IMMessage message = IMMessage.Builder.decode(content);
            switch (message.getType()) {
                case VIDEO:
                case IMAGE:
                case AUDIO:
                case DOC: {
                    String fileKey = message.getContent();
                    mSseChannel.downloadFile(fileKey, new SSEChannel.DownloadCallback() {
                        @Override
                        public void onFileDownloaded(String fileKey, File file) {
                            IMMessage message1 = null;
                            try {
                                message1 = IMMessage.Builder.modifyContent(message, mSSContext.getWebServer().uploadFile(file));
                                if (mReceiver != null) {
                                    mReceiver.onReceive(CloudIMChannel.this, message1);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    break;
                }
                default: {
                    if (mReceiver != null) {
                        mReceiver.onReceive(this, message);
                    }
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetConnections() {
        try {
            List<Session> sessions = mSSContext.getSessionManager().getServerSessions();
            for (Session session : sessions) {
                openClient(session, null);
            }
            Session connectSession = mSSContext.getSessionManager().getConnectedSession();
            if (connectSession != null && mSSContext.getDeviceInfo() != null) {
                mSSContext.getController().connectSSE(connectSession.getId(), 10000, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnected(BaseChannel channel) {
        mSSContext.post(new Runnable() {
            @Override
            public void run() {
                performOpen(true);
                resetConnections();
            }
        });
    }

    @Override
    public void onDisconnected(BaseChannel channel) {
        mSSContext.post(new Runnable() {
            @Override
            public void run() {
                performClose();
            }
        });
    }
}
