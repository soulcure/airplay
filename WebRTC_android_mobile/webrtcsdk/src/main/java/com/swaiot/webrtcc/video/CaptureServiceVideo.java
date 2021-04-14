package com.swaiot.webrtcc.video;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.swaiot.webrtcc.Constant;
import com.swaiot.webrtcc.DateChannelObserverImpl;
import com.swaiot.webrtcc.PeerConnObserverImpl;
import com.swaiot.webrtcc.SdpObserverImpl;
import com.swaiot.webrtcc.entity.FileDescription;
import com.swaiot.webrtcc.entity.Model;
import com.swaiot.webrtcc.entity.SSEEvent;
import com.swaiot.webrtcc.util.AppUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.DataChannel;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.ScreenCapturerAndroid;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;
import org.webrtc.audio.AudioDeviceModule;
import org.webrtc.audio.JavaAudioDeviceModule;
import org.webrtc.voiceengine.WebRtcAudioManager;
import org.webrtc.voiceengine.WebRtcAudioRecord;
import org.webrtc.voiceengine.WebRtcAudioTrack;
import org.webrtc.voiceengine.WebRtcAudioUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class CaptureServiceVideo extends Service {
    private static final String TAG = CaptureServiceVideo.class.getSimpleName();

    private PeerConnection localPeer;
    private EglBase rootEglBase;
    private PeerConnectionFactory peerConnectionFactory;
    private DataChannel dataChannel;
    private WebRTCVideoManager.SenderImpl mSender;
    private WebRTCVideoManager.WebRtcResult mResult;
    private VideoCapturer videoCaptureAndroid;
    private CameraVideoCapturer cameraVideoCapturer;

    private Handler mHandler;
    private Context mContext;

    private static final int PEER_FILE_BLOCK_SIZE = 1472;    //UDP MTU max size

    private long outfileCurPos;
    private FileChannel outfileChannel;
    private ProcessHandler mProcessHandler;
    private ConcurrentHashMap<String, File> sendFileMap;

    private PeerConnection.IceConnectionState mState;

    /**
     * activity和service通信接口
     */
    public class ReverseServiceBinder extends Binder {

        public void setSender(WebRTCVideoManager.SenderImpl sender) {
            mSender = sender;
        }

        public void setResult(WebRTCVideoManager.WebRtcResult result) {
            mResult = result;
        }


        public void start(Intent permissionData) {
            startCapture(permissionData);
        }

        public void start() {
            startCapture();
        }

        public void startCamera() {
            startCameraSub();
        }


        public void startChannel() {
            startDataChannel();
        }

        public void sendData(String data) {
            sendChannelData(data);
        }

        public void sendData(byte[] data) {
            sendChannelData(data);
        }

        public void sendData(File file) {
            sendChannelData(file);
        }


        public boolean isStart() {
            return mState == PeerConnection.IceConnectionState.CONNECTED;
        }


        public void stop() {
            hangup();
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new ReverseServiceBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler(Looper.getMainLooper());
        mContext = this;

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        configPeerConnection();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
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
            if (state == PeerConnection.IceConnectionState.CLOSED
                    || state == PeerConnection.IceConnectionState.DISCONNECTED) {
                Log.e(TAG, "对方已经退出屏幕镜像");
                if (mResult != null) {
                    mResult.onResult(-1, "对方已经退出屏幕镜像");
                }

            } else if (state == PeerConnection.IceConnectionState.CONNECTED) {
                /*mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, " ", Toast.LENGTH_SHORT).show();
                    }
                });*/
                if (mResult != null) {
                    mResult.onResult(0, "屏幕镜像连接成功");
                }
                try {
                    JSONObject json = new JSONObject();
                    json.put("code", 0);
                    json.put("type", "SIGNALING_NOTIFY");
                    json.put("message", "屏幕镜像成功");
                    String text = json.toString();
                    sendData(text);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (state == PeerConnection.IceConnectionState.FAILED) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("code", -1);
                    json.put("type", "SIGNALING_NOTIFY");
                    json.put("message", "本地连接建立失败");
                    String text = json.toString();
                    sendData(text);
                    Log.e(TAG, "本地连接建立失败，请检查是否在同一局域网");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (mResult != null) {
                    mResult.onResult(-2, "屏幕镜像建立失败");
                }
            } else if (state == PeerConnection.IceConnectionState.CHECKING) {
                Log.e(TAG, "屏幕镜像正在连接中");
                if (mResult != null) {
                    mResult.onResult(1, "屏幕镜像正在连接中");
                }
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
            try {
                read(buffer.data, buffer.binary);
            } catch (Exception e) {
                e.printStackTrace();
            }
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
            sendFileMap = new ConcurrentHashMap<>();
            initHandler();

            DataChannel.Init dcInit = new DataChannel.Init();
            dcInit.id = 1;
            dataChannel = localPeer.createDataChannel(Constant.CHANNEL, dcInit);
        }
    }


    public void sendChannelData(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        dataChannel.send(new DataChannel.Buffer(buffer, false));
    }

    public void sendChannelData(String data) {
        ByteBuffer buffer = ByteBuffer.wrap(data.getBytes());
        dataChannel.send(new DataChannel.Buffer(buffer, false));
    }

    public void sendChannelData(File file) {
        FileDescription fileDescription = new FileDescription();
        fileDescription.setFile(file);
        fileDescription.setSendFilePath(file.getAbsolutePath());

        fileDescription.setCheckFile();

        Log.e(TAG, "put sendFileMap fileName=" + fileDescription.getFileName());
        sendFileMap.put(fileDescription.getMd5(), file);

        String checkFile = fileDescription.toJson();
        sendChannelData(checkFile);
    }


    private void progress(String md5, int progress) {
        Log.d("yao", "send file md5=" + md5 + " progress=" + progress);
    }


    private void result(String md5, boolean b, String info) {
        Log.d("yao", "send file md5=" + md5 + " result=" + b + " info=" + info);
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

            }

        }
    }


    //黏性事件 发送了该事件之后再订阅者依然能够接收到的事件
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEvent(final SSEEvent event) {
        String type = event.getMsgType();
        Model model = event.getModel();

        Log.d(TAG, "onEvent type= " + type);
        switch (type) {
            case Constant.OFFER: {
                SessionDescription sdp = model.getPayload().getSdp();
                String targetSid = event.getTargetSid();
                //收到对方offer sdp
                Log.d(TAG, "EventBus Received offer sdp:\n" + sdp.description);
                Log.d(TAG, "EventBus Received offer type:" + sdp.type.toString());
                Log.d(TAG, "EventBus Received offer from Sid:" + targetSid);
                if (!TextUtils.isEmpty(targetSid)) {
                    localPeer.setRemoteDescription(sdpObserverImpl, sdp);
                    localPeer.createAnswer(sdpObserverImpl, new MediaConstraints());
                } else {
                    Toast.makeText(this, "未知请求sid", Toast.LENGTH_SHORT).show();
                }
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
        if (mSender != null) {
            mSender.onSend(content);
        } else {
            Log.e(TAG, "sender fail!!!");
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

        String text = new Gson().toJson(model);

        String oldString = "96 97 98 99 100";
        //String newString = "100 98 96 97 99"; //H264
        String newString = "98 100 96 97 99"; //VP9

        text = text.replaceAll(oldString, newString);

        Log.d(TAG, "log sendOffer : " + text);
        sendData(text);
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

    private void startDataChannel() {
        // create dataChannel
        initDataChannel();
        offer();
    }

    private void sendDataChannel(String text) {

    }

    private void sendDataChannel(File file) {

    }


    private void startCapture() {
        // create videoSource
        MediaConstraints audioConstraints = new MediaConstraints();
        AudioSource audioSource = peerConnectionFactory.createAudioSource(audioConstraints);
        AudioTrack localAudioTrack = peerConnectionFactory.createAudioTrack("101", audioSource);
        localAudioTrack.setEnabled(true);
        MediaStream stream = peerConnectionFactory.createLocalMediaStream("102");
        stream.addTrack(localAudioTrack);
        localPeer.addStream(stream);
        offer();
    }


    private void startCameraSub() {
        String cameraName = "";
        Camera1Enumerator camera1Enumerator = new Camera1Enumerator();
        String[] deviceNames = camera1Enumerator.getDeviceNames();
        for (String deviceName : deviceNames) {
            if (camera1Enumerator.isBackFacing(deviceName)) {
                cameraName = deviceName;
            }
        }
        cameraVideoCapturer = camera1Enumerator.createCapturer(cameraName, null);

        SurfaceTextureHelper surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread",
                rootEglBase.getEglBaseContext());
        // create videoSource
        VideoSource videoSource = peerConnectionFactory.createVideoSource(false);
        cameraVideoCapturer.initialize(surfaceTextureHelper, this, videoSource.getCapturerObserver());
        cameraVideoCapturer.startCapture(1920, 1080, 30);

        VideoTrack localVideoTrack = peerConnectionFactory.createVideoTrack("100", videoSource);
        MediaStream stream = peerConnectionFactory.createLocalMediaStream("102");
        stream.addTrack(localVideoTrack);

        localPeer.addStream(stream);

        offer();
    }


    private void startCapture(Intent permissionData) {
        videoCaptureAndroid = new ScreenCapturerAndroid(permissionData, new MediaProjection.Callback() {
            @Override
            public void onStop() {
                super.onStop();
                Log.e(TAG, "user has revoked permissions");
            }
        });

        SurfaceTextureHelper surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread",
                rootEglBase.getEglBaseContext());
        // create videoSource
        VideoSource videoSource = peerConnectionFactory.createVideoSource(videoCaptureAndroid.isScreencast());
        videoCaptureAndroid.initialize(surfaceTextureHelper, this, videoSource.getCapturerObserver());
        videoCaptureAndroid.startCapture(1920, 1080, 30);

        VideoTrack localVideoTrack = peerConnectionFactory.createVideoTrack("100", videoSource);
        MediaStream stream = peerConnectionFactory.createLocalMediaStream("102");
        stream.addTrack(localVideoTrack);

        localPeer.addStream(stream);

        offer();
    }


    private void offer() {
        localPeer.createOffer(sdpObserverImpl, new MediaConstraints());
    }


    private void hangup() {
        if (videoCaptureAndroid != null) {
            try {
                videoCaptureAndroid.stopCapture();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            videoCaptureAndroid.dispose();
            videoCaptureAndroid = null;
        }

        if (cameraVideoCapturer != null) {
            try {
                cameraVideoCapturer.stopCapture();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            cameraVideoCapturer.dispose();
            cameraVideoCapturer = null;
        }

        if (localPeer != null) {
            localPeer.close();
            localPeer = null;
        }

        if (peerConnectionFactory != null) {
            peerConnectionFactory.dispose();
            peerConnectionFactory = null;
        }
        Log.d(TAG, "Stopping capture.");

        stopSelf();
    }


    private void configPeerConnection() {
        PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions.builder(this)
                        //.setFieldTrials(getFieldTrials())
                        .setFieldTrials("WebRTC-H264HighProfile/Enabled/")
                        .setEnableInternalTracer(true)
                        .createInitializationOptions());
        /* .............. create and initialize PeerConnectionFactory .........*/
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        rootEglBase = EglBase.create(); //需要java8

        final VideoEncoderFactory encoderFactory;
        final VideoDecoderFactory decoderFactory;
        encoderFactory = new DefaultVideoEncoderFactory(
                rootEglBase.getEglBaseContext(), false, true);
        decoderFactory = new DefaultVideoDecoderFactory(rootEglBase.getEglBaseContext());

        //AudioDeviceModule adm = createLegacyAudioDevice();
        AudioDeviceModule adm = createJavaAudioDevice();
        peerConnectionFactory = PeerConnectionFactory.builder()
                .setOptions(options)
                .setAudioDeviceModule(adm)
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory)
                .createPeerConnectionFactory();
        adm.release();
        /*create localPeer*/
        PeerConnection.RTCConfiguration configuration = new PeerConnection.RTCConfiguration(getICEServers());
        configuration.iceTransportsType = PeerConnection.IceTransportsType.ALL;
        configuration.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED;
        configuration.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE;
        configuration.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE;
        configuration.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;

        localPeer = peerConnectionFactory.createPeerConnection(configuration, peerConnObserver);

        Log.d(TAG, "Peer connection factory created.");
    }


    private List<PeerConnection.IceServer> getICEServers() {
        List<PeerConnection.IceServer> iceServers = new ArrayList<>();

        String stun = "stun:atum.skyworthiot.com";
        String turn = "turn:turn-dev.skyworthiot.com";

        PeerConnection.IceServer stunServer = PeerConnection.IceServer.builder(stun)
                .createIceServer();

        PeerConnection.IceServer turnServer = PeerConnection.IceServer.builder(turn)
                .setUsername("skyiot")
                .setPassword("skyworth.abc.123")
                .createIceServer();
        iceServers.add(stunServer);
        //iceServers.add(turnServer);
        return iceServers;
    }


    /**
     * 声音减噪算法
     *
     * @return
     */
    private AudioDeviceModule createLegacyAudioDevice() {
        // Enable/disable OpenSL ES playback.

        WebRtcAudioManager.setBlacklistDeviceForOpenSLESUsage(true /* enable */);
        WebRtcAudioUtils.setWebRtcBasedAcousticEchoCanceler(true);
        WebRtcAudioUtils.setWebRtcBasedNoiseSuppressor(true);

        // Set audio record error callbacks.
        WebRtcAudioRecord.setErrorCallback(new WebRtcAudioRecord.WebRtcAudioRecordErrorCallback() {
            @Override
            public void onWebRtcAudioRecordInitError(String errorMessage) {
                Log.e(TAG, "onWebRtcAudioRecordInitError: " + errorMessage);
            }

            @Override
            public void onWebRtcAudioRecordStartError(
                    WebRtcAudioRecord.AudioRecordStartErrorCode errorCode, String errorMessage) {
                Log.e(TAG, "onWebRtcAudioRecordStartError: " + errorCode + ". " + errorMessage);
            }

            @Override
            public void onWebRtcAudioRecordError(String errorMessage) {
                Log.e(TAG, "onWebRtcAudioRecordError: " + errorMessage);
            }
        });

        WebRtcAudioTrack.setErrorCallback(new WebRtcAudioTrack.ErrorCallback() {
            @Override
            public void onWebRtcAudioTrackInitError(String errorMessage) {
                Log.e(TAG, "onWebRtcAudioTrackInitError: " + errorMessage);
            }

            @Override
            public void onWebRtcAudioTrackStartError(
                    WebRtcAudioTrack.AudioTrackStartErrorCode errorCode, String errorMessage) {
                Log.e(TAG, "onWebRtcAudioTrackStartError: " + errorCode + ". " + errorMessage);
            }

            @Override
            public void onWebRtcAudioTrackError(String errorMessage) {
                Log.e(TAG, "onWebRtcAudioTrackError: " + errorMessage);
            }
        });

        return new AudioDeviceModule() {
            @Override
            public long getNativeAudioDeviceModulePointer() {
                return 0;
            }

            @Override
            public void release() {

            }

            @Override
            public void setSpeakerMute(boolean b) {

            }

            @Override
            public void setMicrophoneMute(boolean b) {

            }
        };
    }


    private AudioDeviceModule createJavaAudioDevice() {
        // Set audio record error callbacks.
        JavaAudioDeviceModule.AudioRecordErrorCallback audioRecordErrorCallback =
                new JavaAudioDeviceModule.AudioRecordErrorCallback() {
                    @Override
                    public void onWebRtcAudioRecordInitError(String errorMessage) {
                        Log.e(TAG, "onWebRtcAudioRecordInitError: " + errorMessage);
                    }

                    @Override
                    public void onWebRtcAudioRecordStartError(
                            JavaAudioDeviceModule.AudioRecordStartErrorCode errorCode, String errorMessage) {
                        Log.e(TAG, "onWebRtcAudioRecordStartError: " + errorCode + ". " + errorMessage);
                    }

                    @Override
                    public void onWebRtcAudioRecordError(String errorMessage) {
                        Log.e(TAG, "onWebRtcAudioRecordError: " + errorMessage);
                    }
                };

        JavaAudioDeviceModule.AudioTrackErrorCallback audioTrackErrorCallback =
                new JavaAudioDeviceModule.AudioTrackErrorCallback() {
                    @Override
                    public void onWebRtcAudioTrackInitError(String errorMessage) {
                        Log.e(TAG, "onWebRtcAudioTrackInitError: " + errorMessage);
                    }

                    @Override
                    public void onWebRtcAudioTrackStartError(
                            JavaAudioDeviceModule.AudioTrackStartErrorCode errorCode, String errorMessage) {
                        Log.e(TAG, "onWebRtcAudioTrackStartError: " + errorCode + ". " + errorMessage);
                    }

                    @Override
                    public void onWebRtcAudioTrackError(String errorMessage) {
                        Log.e(TAG, "onWebRtcAudioTrackError: " + errorMessage);
                    }
                };

        // Set audio record state callbacks.
        JavaAudioDeviceModule.AudioRecordStateCallback audioRecordStateCallback =
                new JavaAudioDeviceModule.AudioRecordStateCallback() {
                    @Override
                    public void onWebRtcAudioRecordStart() {
                        Log.i(TAG, "Audio recording starts");
                    }

                    @Override
                    public void onWebRtcAudioRecordStop() {
                        Log.i(TAG, "Audio recording stops");
                    }
                };

        // Set audio track state callbacks.
        JavaAudioDeviceModule.AudioTrackStateCallback audioTrackStateCallback =
                new JavaAudioDeviceModule.AudioTrackStateCallback() {
                    @Override
                    public void onWebRtcAudioTrackStart() {
                        Log.i(TAG, "Audio playout starts");
                    }

                    @Override
                    public void onWebRtcAudioTrackStop() {
                        Log.i(TAG, "Audio playout stops");
                    }
                };

        return JavaAudioDeviceModule.builder(getApplicationContext())
                //.setSamplesReadyCallback(saveRecordedAudioToFile)
                .setUseHardwareAcousticEchoCanceler(false)//使用硬件回声消除
                .setUseHardwareNoiseSuppressor(false) //使用硬件噪音抑制器
                .setAudioRecordErrorCallback(audioRecordErrorCallback)
                .setAudioTrackErrorCallback(audioTrackErrorCallback)
                .setAudioRecordStateCallback(audioRecordStateCallback)
                .setAudioTrackStateCallback(audioTrackStateCallback)
                .createAudioDeviceModule();
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
