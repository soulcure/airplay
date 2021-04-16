package swaiotos.channel.iot.webrtc;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.skyworth.dpclientsdk.ConnectState;
import com.skyworth.dpclientsdk.StreamSourceCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.webrtc.DataChannel;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.ConcurrentHashMap;

import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.SSContext;
import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.ss.session.Session;
import swaiotos.channel.iot.utils.AppUtils;
import swaiotos.channel.iot.webrtc.config.Constant;
import swaiotos.channel.iot.webrtc.entity.FileDescription;
import swaiotos.channel.iot.webrtc.entity.FileProgress;
import swaiotos.channel.iot.webrtc.entity.Model;
import swaiotos.channel.iot.webrtc.entity.SSEEvent;
import swaiotos.channel.iot.webrtc.observer.DateChannelObserverImpl;
import swaiotos.channel.iot.webrtc.observer.PeerConnObserverImpl;
import swaiotos.channel.iot.webrtc.observer.SdpObserverImpl;

public class DataChannelClient {
    private static final String TAG = DataChannelClient.class.getSimpleName();

    public static final String SOURCE_CLIENT = "com.coocaa.webrtc.datachannel.client";
    public static final String TARGET_CLIENT = "com.coocaa.webrtc.datachannel.server";

    private static final int PEER_FILE_BLOCK_SIZE = 1472;    //UDP MTU max size

    private PeerConnection localPeer;
    private PeerConnectionFactory peerConnectionFactory;
    private PeerConnection.RTCConfiguration configuration;
    private DataChannel dataChannel;
    private PeerConnection.IceConnectionState mState;

    private ProcessHandler mProcessHandler;

    private long outfileCurPos;
    private FileChannel outfileChannel;
    private final SSContext mSSContext;
    private final ConcurrentHashMap<String, File> sendFileMap;
    private final ConcurrentHashMap<String, IMMessage> sendFileMsgMap;

    public StreamSourceCallback streamSourceCallback;

    private MediaConstraints sdpConstraints;
    private String wifiInfoSSID;

    public DataChannelClient(SSContext ssContext, StreamSourceCallback callback) {
        mSSContext = ssContext;
        streamSourceCallback = callback;

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        initPeerConnectionFactory(ssContext.getContext());

        initHandler();
        sendFileMap = new ConcurrentHashMap<>();
        sendFileMsgMap = new ConcurrentHashMap<>();

        Log.d(TAG, "DataChannelClient create...");
    }


    public boolean isOpen() {
        return mState == PeerConnection.IceConnectionState.CONNECTED;
    }

    public void close() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        hangup();
    }

    private final PeerConnObserverImpl peerConnObserver = new PeerConnObserverImpl() {
        @Override
        public void onIceCandidate(IceCandidate iceCandidate) {
            super.onIceCandidate(iceCandidate);
            setIceCandidate(iceCandidate);
        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState state) {
            super.onIceConnectionChange(state);
            mState = state;
            Log.e(TAG, "PeerConnection.IceConnectionState :" + state
                    + "  hash=" + DataChannelClient.this.hashCode());

            if (state == PeerConnection.IceConnectionState.CLOSED
                    || state == PeerConnection.IceConnectionState.DISCONNECTED) {
                if (streamSourceCallback != null) {
                    streamSourceCallback.onConnectState(ConnectState.DISCONNECT);
                }
            } else if (state == PeerConnection.IceConnectionState.CONNECTED) {
                if (streamSourceCallback != null) {
                    streamSourceCallback.onConnectState(ConnectState.CONNECT);
                }
            } else if (state == PeerConnection.IceConnectionState.FAILED) {
                if (streamSourceCallback != null) {
                    streamSourceCallback.onConnectState(ConnectState.ERROR);
                }
            } else if (state == PeerConnection.IceConnectionState.CHECKING) {
            }
        }

        @Override
        public void onDataChannel(DataChannel dc) {
            super.onDataChannel(dc);
            dc.registerObserver(dateChannelObserverImpl);
            String channelName = dc.label();
            Log.d(TAG, "onDataChannel channelName=" + channelName);
        }
    };

    private final SdpObserverImpl sdpObserverImpl = new SdpObserverImpl() {
        @Override
        public void onCreateSuccess(SessionDescription sessionDescription) {
            //将会话描述设置在本地
            localPeer.setLocalDescription(this, sessionDescription);
            SessionDescription localDescription = localPeer.getLocalDescription();
            SessionDescription.Type type = localDescription.type;
            Log.d(TAG, "onCreateSuccess == " + " type == " + type);

            //接下来使用之前的WebSocket实例将offer发送给服务器
            if (type == SessionDescription.Type.OFFER) {
                //呼叫
                sendOffer(sessionDescription);
            } else if (type == SessionDescription.Type.ANSWER) {
                //应答
                sendAnswer(sessionDescription);
            }
        }
    };

    private final DateChannelObserverImpl dateChannelObserverImpl = new DateChannelObserverImpl() {
        @Override
        public void onBufferedAmountChange(long l) {
            super.onBufferedAmountChange(l);
        }

        @Override
        public void onStateChange() {
            super.onStateChange();
        }

        @Override
        public void onMessage(DataChannel.Buffer buffer) {
            super.onMessage(buffer);
            read(buffer.data, buffer.binary);
        }
    };


    /**
     * DataChannel.Init 可配参数说明：
     * ordered：是否保证顺序传输；
     * maxRetransmitTimeMs：重传允许的最长时间；
     * maxRetransmits：重传允许的最大次数；
     */
    private void initDataChannel() {
        if (dataChannel == null) {
            DataChannel.Init dcInit = new DataChannel.Init();
            dcInit.id = 1;
            dataChannel = localPeer.createDataChannel(Constant.CHANNEL, dcInit);
        }
    }


    public boolean sendChannelData(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        return dataChannel.send(new DataChannel.Buffer(buffer, false));
    }

    public boolean sendChannelData(String data) {
        ByteBuffer buffer = ByteBuffer.wrap(data.getBytes());
        return dataChannel.send(new DataChannel.Buffer(buffer, false));
    }

    public void sendChannelData(File file, IMMessage message) {
        String msgId = message.getId();

        FileDescription fileDescription = new FileDescription();
        fileDescription.setFile(file);
        fileDescription.setMsgId(msgId);
        fileDescription.setSendFilePath(file.getAbsolutePath());

        fileDescription.setCheckFile();

        Log.e(TAG, "put sendFileMap fileName=" + fileDescription.getFileName());
        sendFileMap.put(fileDescription.getMd5(), file);
        sendFileMsgMap.put(fileDescription.getMd5(), message);

        String checkFile = fileDescription.toJson();
        sendChannelData(checkFile);
    }


    private void progress(String md5, int progress) {
        IMMessage msg = sendFileMsgMap.get(md5);
        IMMessage imMessage = IMMessage.Builder.sendProtoProgress(msg, progress);

        FileProgress fileProgress = new FileProgress();
        fileProgress.imMessage = imMessage;

        EventBus.getDefault().post(fileProgress);
    }


    private void result(String md5, boolean b, String info) {
        try {
            IMMessage msg = sendFileMsgMap.get(md5);
            if (msg == null) {
                return;
            }
            IMMessage imMessage = IMMessage.Builder.sendProtoResult(msg, b, info);

            FileProgress fileProgress = new FileProgress();
            fileProgress.imMessage = imMessage;

            EventBus.getDefault().post(fileProgress);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void sendFileByWorkThread(final FileDescription fileDescription) {
        mProcessHandler.post(() -> {
            sendFile(fileDescription);
        });
    }


    /**
     * dongle端答复
     *
     * @param fileDescription
     */
    private void answerCheckSend(FileDescription fileDescription) {
        if (fileDescription.checkHasFile()) {
            fileDescription.setHasFile();
            String hasFile = fileDescription.toJson();
            sendChannelData(hasFile);
        } else {
            fileDescription.setNoFile();
            String noFile = fileDescription.toJson();
            sendChannelData(noFile);
        }
    }


    private void receiveSendFile(FileDescription fileDescription) {
        sendFileByWorkThread(fileDescription);
    }

    private void receiveNoSendFile(FileDescription fileDescription) {
        String md5 = fileDescription.getMd5();
        sendFileMap.remove(md5);

        IMMessage message = sendFileMsgMap.get(md5);
        message.setContent(fileDescription.getReceiveFilePath());
        sendChannelData(message.encode());
        sendFileMsgMap.remove(md5);
    }


    private void sendFile(FileDescription fileDescription) {
        fileDescription.setStart();
        String startJson = fileDescription.toJson();
        sendChannelData(startJson);
        File file = new File(fileDescription.getSendFilePath());
        String md5 = fileDescription.getMd5();

        if (file.exists() && file.isFile()) {
            try {
                FileChannel fileChannel = new FileInputStream(file).getChannel();
                Log.d(TAG, "sendFile size:" + fileChannel.size());

                ByteBuffer byteBuffer = ByteBuffer.allocate(PEER_FILE_BLOCK_SIZE);

                int size = 0;
                long total = 0;
                long fileSize = fileDescription.getFileSize();
                int isSendRate = 0;

                while ((size = fileChannel.read(byteBuffer)) != -1) {
                    byteBuffer.flip();
                    dataChannel.send(new DataChannel.Buffer(byteBuffer, true));
                    byteBuffer.clear();
                    total += size;
                    int rate = (int) (total * 100 / fileSize);
                    if (rate % 5 == 0 && isSendRate != rate) {
                        progress(md5, rate);
                        isSendRate = rate;
                    }

                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                fileChannel.close();

                result(md5, true, "send success");

            } catch (Exception e) {
                e.printStackTrace();
                result(md5, false, "send fail:" + e.getMessage());
            }
        }

        fileDescription.setEnd();
        String endJson = fileDescription.toJson();
        sendChannelData(endJson);
        sendFileMap.remove(md5);

        IMMessage message = sendFileMsgMap.get(md5);
        message.setContent(fileDescription.getReceiveFilePath());
        sendChannelData(message.encode());
        sendFileMsgMap.remove(md5);
    }

    public void read(ByteBuffer data, boolean binary) {
        if (binary) {
            try {
                if (outfileChannel != null) {
                    outfileChannel.write(data);
                    outfileCurPos += data.limit();

                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            byte[] bytes = new byte[data.remaining()];
            data.get(bytes);
            String command = new String(bytes);
            Log.d(TAG, "DataChannel onMessage=" + command);

            if (command.contains("dataType")) {
                FileDescription fileDescription = new Gson().fromJson(command, FileDescription.class);
                if (fileDescription != null) {
                    if (fileDescription.isCheckFile()) { //文件接收方消息
                        answerCheckSend(fileDescription);
                    } else if (fileDescription.needSendFile()) {  //文件发送方消息
                        receiveSendFile(fileDescription);
                    } else if (fileDescription.noNeedSendFile()) { //文件发送方消息
                        receiveNoSendFile(fileDescription);
                    } else if (fileDescription.isStart()) { //文件接收方消息
                        try {
                            File file = new File(fileDescription.getFileName());
                            outfileChannel = new FileOutputStream(file).getChannel();
                            outfileCurPos = 0;
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    } else if (fileDescription.isEnd()) { //文件接收方消息
                        if (outfileCurPos == fileDescription.getFileSize()) {
                            try {
                                outfileChannel.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            outfileChannel = null;
                            outfileCurPos = 0;

                            File file = new File(fileDescription.getFileName());
                            if (AppUtils.checkFileMd5(file, fileDescription.getMd5())) {
                                //done
                            }
                        }
                    }

                }

            } else {  //for 通道消息
                if (streamSourceCallback != null) {
                    streamSourceCallback.onData(command);
                }
            }

        }
    }


    //黏性事件 发送了该事件之后再订阅者依然能够接收到的事件
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(final SSEEvent event) {
        String type = event.getMsgType();
        Model model = event.getModel();

        Log.d(TAG, "DataChannelClient onEvent type= " + type);
        switch (type) {
            case Constant.OFFER: {
                SessionDescription sdp = model.getPayload().getSdp();
                //收到对方offer sdp
                Log.d(TAG, "EventBus Received offer sdp:\n" + sdp.description);
                Log.d(TAG, "EventBus Received offer type:" + sdp.type.toString());
                localPeer.setRemoteDescription(sdpObserverImpl, sdp);
                localPeer.createAnswer(sdpObserverImpl, new MediaConstraints());
            }
            break;
            case Constant.ANSWER: {
                SessionDescription sdp = model.getPayload().getSdp();
                Log.d(TAG, "EventBus Received answer sdp:\n" + sdp.description);
                Log.d(TAG, "EventBus Received answer type:" + sdp.type.toString());
                localPeer.setRemoteDescription(sdpObserverImpl, sdp);
            }
            break;
            case Constant.CANDIDATE: {
                //服务端 发送 接收方sdpAnswer
                IceCandidate iceCandidate = model.getPayload().getIceCandidate();
                if (iceCandidate != null && localPeer != null) {
                    Log.d(TAG, "EventBus Received iceCandidate sdpMid=" + iceCandidate.sdpMid);
                    Log.d(TAG, "EventBus Received iceCandidate sdpMLineIndex=" + iceCandidate.sdpMLineIndex);
                    Log.d(TAG, "EventBus Received iceCandidate sdp=" + iceCandidate.sdp);
                    localPeer.addIceCandidate(iceCandidate);
                }
            }
            break;
        }
    }


    private void sendData(String content) {
        try {
            Session mySession = mSSContext.getSessionManager().getMySession();
            Session targetSession = mSSContext.getSessionManager().getConnectedSession();

            if (targetSession != null) {
                IMMessage message = IMMessage.Builder.createTextMessage(mySession, targetSession,
                        SOURCE_CLIENT, TARGET_CLIENT, content);
                message.putExtra(SSChannel.FORCE_SSE, "true");//强制云端

                mSSContext.getIMChannel().send(message);
                Log.d(TAG, "send Data by sse content=" + content);
            } else {
                Log.e(TAG, "send Data fail by targetSession is null");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * 呼叫
     *
     * @param sdpDescription
     */
    private void sendOffer(SessionDescription sdpDescription) {
        Log.d(TAG, "log sendOffer sdp:\n" + sdpDescription.description);

        Model.PayLoad payLoad = new Model.PayLoad();
        payLoad.setSdp(sdpDescription);

        Model model = new Model();
        model.setType(Constant.OFFER);

        model.setPayload(payLoad);
        model.setSsid(wifiInfoSSID);

        String text = new Gson().toJson(model);

        Log.d(TAG, "log sendOffer : " + text);
        sendData(text);

        wifiInfoSSID = null;
    }

    /**
     * 呼叫
     *
     * @param sdpDescription
     */
    private void sendAnswer(SessionDescription sdpDescription) {
        Log.e(TAG, "sendAnswer sdp:\n" + sdpDescription.description);

        Model.PayLoad payLoad = new Model.PayLoad();
        payLoad.setSdp(sdpDescription);

        Model model = new Model();

        model.setType(Constant.ANSWER);
        model.setPayload(payLoad);

        String text = new Gson().toJson(model);

        Log.d(TAG, "sendAnswer : " + text);
        sendData(text);
    }


    /**
     * 设置 IceCandidate
     *
     * @param iceCandidate ice
     */
    private void setIceCandidate(IceCandidate iceCandidate) {
        Log.d(TAG, "sdp= " + iceCandidate.sdp);
        Log.d(TAG, "sdpMid = " + iceCandidate.sdpMid);
        Log.d(TAG, "sdpMLineIndex = " + iceCandidate.sdpMLineIndex);
        Log.d(TAG, "adapterType = " + iceCandidate.adapterType.toString());
        Log.d(TAG, "serverUrl = " + iceCandidate.serverUrl);

        Model.PayLoad payLoad = new Model.PayLoad();
        payLoad.setIceCandidate(iceCandidate);

        Model model = new Model();

        model.setType(Constant.CANDIDATE);
        model.setPayload(payLoad);
        String text = new Gson().toJson(model);

        Log.d(TAG, "setIceCandidate : " + text);
        sendData(text);
    }


    public void call() {
        initPeerConnect();
        initDataChannel();
        localPeer.createOffer(sdpObserverImpl, sdpConstraints);
    }


    public void offer(String ssid) {
        wifiInfoSSID = ssid;
        call();
    }


    public void iceRestart() {
        Log.e(TAG, "DataChannel iceRestart...");
        sdpConstraints.optional.add(new MediaConstraints.KeyValuePair("IceRestart", "true"));
        localPeer.createOffer(sdpObserverImpl, sdpConstraints);
    }


    public void hangup() {
        streamSourceCallback = null;

        if (dataChannel != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (dataChannel.state() == DataChannel.State.CONNECTING || dataChannel.state() == DataChannel.State.OPEN) {
                        dataChannel.close();
                    }
                    dataChannel.dispose();
                    dataChannel = null;
                }
            }).start();
        }
        if (localPeer != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if(PeerConnection.PeerConnectionState.CONNECTED == localPeer.connectionState() ||
                            PeerConnection.PeerConnectionState.CONNECTING == localPeer.connectionState() ||
                            PeerConnection.PeerConnectionState.NEW == localPeer.connectionState()) {
                        localPeer.close();
                        localPeer.dispose();
                        localPeer = null;
                    }
                }
            }).start();
        }
        Log.d(TAG, "Stopping capture.");
    }


    private void initPeerConnectionFactory(Context context) {
        PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions.builder(context)
                        .setEnableInternalTracer(true)
                        .createInitializationOptions());
        /* .............. create and initialize PeerConnectionFactory .........*/
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        EglBase rootEglBase = EglBase.create(); //需要java8

        final VideoEncoderFactory encoderFactory;
        final VideoDecoderFactory decoderFactory;
        encoderFactory = new DefaultVideoEncoderFactory(
                rootEglBase.getEglBaseContext(), true /* enableIntelVp8Encoder */, true);
        decoderFactory = new DefaultVideoDecoderFactory(rootEglBase.getEglBaseContext());

        peerConnectionFactory = PeerConnectionFactory.builder()
                .setOptions(options)
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory)
                .createPeerConnectionFactory();

        /*create localPeer*/
        configuration = new PeerConnection.RTCConfiguration(Constant.getICEServers());
        configuration.iceTransportsType = PeerConnection.IceTransportsType.ALL;
        configuration.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED;
        configuration.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE;
        configuration.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE;
        configuration.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;
        sdpConstraints = new MediaConstraints();

        Log.d(TAG, "Peer connection factory created.");
    }

    private void initPeerConnect() {
        if (localPeer == null) {
            localPeer = peerConnectionFactory.createPeerConnection(configuration, peerConnObserver);
        }
    }


    /**
     * 线程初始化
     */
    private void initHandler() {
        if (mProcessHandler == null) {
            HandlerThread handlerThread = new HandlerThread("handler looper Thread");
            handlerThread.start();
            mProcessHandler = new ProcessHandler(handlerThread.getLooper());
        }
    }

    /**
     * 子线程handler,looper
     *
     * @author Administrator
     */
    private class ProcessHandler extends Handler {

        public ProcessHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                default:
                    break;
            }

        }

    }

}
