package com.dingsoft.webrtc.webrtcroom.webrtcmodule;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.dingsoft.webrtc.webrtcroom.activity.MainActivity;
import com.google.gson.Gson;
import com.skyworthiot.iotssemsg.IotSSEMsgLib;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.SoftwareVideoDecoderFactory;
import org.webrtc.SoftwareVideoEncoderFactory;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoSink;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;
import org.webrtc.audio.AudioDeviceModule;
import org.webrtc.audio.JavaAudioDeviceModule;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * WebRtcClient类 封装PeerConnectionFactory工厂类及Socket.IO信令服务器
 * Created by chengshaobo on 2018/10/24.
 */

public class WebRtcClient {
    //Log Tag
    private final static String TAG = "yao";


    private static final int HANDLER_THREAD_INIT_CONFIG_START = 2;
    private Handler mHandler;
    private ProcessHandler mProcessHandler;
    private IotSSEMsgLib iotSSE;
    private static final String DEVICE_ID = "547c08a5f84b411fa945ca8f73c426f6";   //pixel 3xl


    //private static final String TV_ID = "47377d7249a042889545d8421249f308"; //IOS
    private static final String TV_ID = "547c08a5f84b411fa945ca8f73c426f7"; //pixel


    //PeerConnectionFactory工厂类
    private PeerConnectionFactory factory;
    //Peer集合
    private HashMap<String, Peer> peers = new HashMap<>();
    //IceServer集合 用于构建PeerConnection
    private LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<>();
    //PeerConnectFactory构建参数
    private PeerConnectionParameters pcParams;
    //PeerConnect构建参数
    PeerConnection.RTCConfiguration rtcConfig;
    //PeerConnect 音频约束
    private MediaConstraints audioConstraints;
    //PeerConnect sdp约束
    private MediaConstraints sdpMediaConstraints;
    //本地Video视频资源
    private VideoSource localVideoSource;
    //视频Track
    private VideoTrack localVideoTrack;
    //音频Track
    private AudioTrack localAudioTrack;
    //本地摄像头视频捕获
    private CameraVideoCapturer cameraVideoCapturer;
    //页面context
    private Context appContext;
    //WebRtc EglContext环境
    private EglBase eglBase;
    //Activity回调接口
    private RtcListener rtcListener;
    //socket.io信令交互
    private Socket client;
    //信令服务器地址
    private String host;
    //本地socket id
    private String socketId;
    //room id
    private String roomId;

    ////webRtc定义常量////
    private static final String AUDIO_ECHO_CANCELLATION_CONSTRAINT = "googEchoCancellation";
    private static final String AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT = "googAutoGainControl";
    private static final String AUDIO_HIGH_PASS_FILTER_CONSTRAINT = "googHighpassFilter";
    private static final String AUDIO_NOISE_SUPPRESSION_CONSTRAINT = "googNoiseSuppression";
    private static final String VIDEO_FLEXFEC_FIELDTRIAL =
            "WebRTC-FlexFEC-03-Advertised/Enabled/WebRTC-FlexFEC-03/Enabled/";
    private static final String VIDEO_VP8_INTEL_HW_ENCODER_FIELDTRIAL = "WebRTC-IntelVP8/Enabled/";
    private static final String DISABLE_WEBRTC_AGC_FIELDTRIAL =
            "WebRTC-Audio-MinimizeResamplingOnMobile/Enabled/";
    public static final int FONT_FACTING = 0;
    public static final int BACK_FACING = 1;

    //构造函数
    public WebRtcClient(Context appContext,
                        EglBase eglBase,
                        PeerConnectionParameters peerConnectionParameters,
                        RtcListener listener,
                        String host) {
        //参数设置
        this.appContext = appContext;
        this.eglBase = eglBase;
        this.pcParams = peerConnectionParameters;
        this.rtcListener = listener;
        this.host = host;
        //PeerConnectionFactory工厂类构建
        createPeerConnectionFactoryInternal();
        //创建iceservers
        createIceServers();
        //创建RTCConfiguration参数
        createRtcConfig();
        //创建Pc及Sdp约束
        createMediaConstraintsInternal();
        //Socket.IO信令服务器构建
        //createSocket();


        initHandler();
        startSignalChannel();
    }

    public RtcListener getRtcListener() {
        return rtcListener;
    }

    public String getSocketId() {
        return socketId;
    }

    public String getRoomId() {
        return roomId;
    }

    //创建IceServers参数
    private void createIceServers() {
        iceServers.add(PeerConnection.IceServer.builder("stun:stun.xten.com").createIceServer());
        //iceServers.add(PeerConnection.IceServer.builder("stun:atum.skyworthiot.com").createIceServer());
    }

    //创建RTCConfiguration参数
    private void createRtcConfig() {
        rtcConfig =
                new PeerConnection.RTCConfiguration(iceServers);
        // TCP candidates are only useful when connecting to a server that supports
        // ICE-TCP.
        rtcConfig.iceTransportsType = PeerConnection.IceTransportsType.ALL;
        rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED;
        rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE;
        rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.NEGOTIATE;
        rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;
        // Use ECDSA encryption.
//        rtcConfig.keyType = PeerConnection.KeyType.ECDSA;
//        // Enable DTLS for normal calls and disable for loopback calls.
 //       rtcConfig.enableDtlsSrtp = !pcParams.loopback;
        rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN;
    }


    //创建PeerConnection工厂类
    private void createPeerConnectionFactoryInternal() {
        //创建webRtc连接工厂类
        final VideoEncoderFactory encoderFactory;
        final VideoDecoderFactory decoderFactory;
        final boolean enableH264HighProfile =
                "H264 High".equals(pcParams.videoCodec);
        //音频模式
        final AudioDeviceModule adm = pcParams.useLegacyAudioDevice
                ? createJavaAudioDevice()
                : createJavaAudioDevice();
        //编解码模式【硬件加速，软编码】
        if (pcParams.videoCodecHwAcceleration) {
            encoderFactory = new DefaultVideoEncoderFactory(
                    eglBase.getEglBaseContext(), true /* enableIntelVp8Encoder */, enableH264HighProfile);
            decoderFactory = new DefaultVideoDecoderFactory(eglBase.getEglBaseContext());
        } else {
            encoderFactory = new SoftwareVideoEncoderFactory();
            decoderFactory = new SoftwareVideoDecoderFactory();
        }
        //PeerConnectionFactory.initialize
        String fieldTrials = "";
        if (pcParams.videoFlexfecEnabled) {
            fieldTrials += VIDEO_FLEXFEC_FIELDTRIAL;
        }
        fieldTrials += VIDEO_VP8_INTEL_HW_ENCODER_FIELDTRIAL;
        if (pcParams.disableWebRtcAGCAndHPF) {
            fieldTrials += DISABLE_WEBRTC_AGC_FIELDTRIAL;
        }
        //PeerConnectionFactory.initialize
        PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions.builder(appContext)
                        .setFieldTrials(fieldTrials)
                        .setEnableInternalTracer(true)
                        .createInitializationOptions());
        //构建PeerConnectionFactory
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        factory = PeerConnectionFactory.builder()
                .setOptions(options)
                //.setAudioDeviceModule(adm)
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory)
                .createPeerConnectionFactory();
    }

    //创建信令服务器及监听

    public void startCapture() {
        // create videoSource
        Peer pc = getOrCreateRtcConnect("1");
        //设置offer
        pc.getPc().createOffer(pc, sdpMediaConstraints);
    }


    /**
     * UI操作相关
     */
    //创建并加入
    public void createAndJoinRoom(String roomId) {
        //构建信令数据并发送
//        try {
//            JSONObject message = new JSONObject();
//            message.put("room", roomId);
//            //向信令服务器发送信令
//            sendMessage("createAndJoinRoom", message);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
    }

    //退出room
    public void exitRoom() {
        //信令服务器发送 exit [from room]
//        try {
//            JSONObject message = new JSONObject();
//            message.put("from", socketId);
//            message.put("room", roomId);
//            //向信令服务器发送信令
//            sendMessage("exit", message);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
        //循环遍历 peer关闭
        for (Peer pc : peers.values()) {
            pc.getPc().close();
        }
        //数据重置
        socketId = "";
        roomId = "";
        peers.clear();
        //通知UI清空远端摄像头
        ((MainActivity) rtcListener).clearRemoteCamera();
    }

    /**
     * WebRtc相关
     */
    //构建webRtc连接并返回
    private Peer getOrCreateRtcConnect(String socketId) {
        Peer pc = peers.get(socketId);
        if (pc == null) {
            //构建RTCPeerConnection PeerConnection相关回调进入Peer中
            pc = new Peer(socketId, factory, rtcConfig, WebRtcClient.this);
            //设置本地数据流
            pc.getPc().addTrack(localVideoTrack);
            //保存peer连接
            peers.put(socketId, pc);
        }
        return pc;
    }

    //启动设备视频并关联本地video
    public void startCamera(VideoSink localRender, int type) {
        if (pcParams.videoCallEnabled) {
            //创建VideoCapturer
            if (cameraVideoCapturer == null) {
                String cameraname = "";
                Camera1Enumerator camera1Enumerator = new Camera1Enumerator();
                String[] deviceNames = camera1Enumerator.getDeviceNames();
                if (type == FONT_FACTING) {
                    //前置摄像头
                    for (String deviceName : deviceNames) {
                        if (camera1Enumerator.isFrontFacing(deviceName)) {
                            cameraname = deviceName;
                        }
                    }
                } else {
                    //后置摄像头
                    for (String deviceName : deviceNames) {
                        if (camera1Enumerator.isBackFacing(deviceName)) {
                            cameraname = deviceName;
                        }
                    }
                }
                cameraVideoCapturer = camera1Enumerator.createCapturer(cameraname, null);
                SurfaceTextureHelper surfaceTextureHelper =
                        SurfaceTextureHelper.create("CaptureThread", eglBase.getEglBaseContext());
                localVideoSource = factory.createVideoSource(false);
                cameraVideoCapturer.initialize(surfaceTextureHelper, appContext, localVideoSource.getCapturerObserver());
                cameraVideoCapturer.startCapture(pcParams.videoWidth, pcParams.videoHeight, pcParams.videoFps);
                localVideoTrack = factory.createVideoTrack("ARDAMSv0", localVideoSource);
                localVideoTrack.setEnabled(true);
                localVideoTrack.addSink(localRender);
            } else {
                cameraVideoCapturer.startCapture(pcParams.videoWidth, pcParams.videoHeight, pcParams.videoFps);
            }
        }
    }

    //切换摄像头
    public void switchCamera() {
        if (cameraVideoCapturer != null) {
            cameraVideoCapturer.switchCamera(null);
        }
    }

    //关闭摄像头
    public void closeCamera() {
        if (cameraVideoCapturer != null) {
            try {
                cameraVideoCapturer.stopCapture();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 信令服务器处理相关
     **/
    //created [id,room,peers]

    //joined [id,room]
    private Emitter.Listener joinedListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d(TAG, "joined:" + data);
            try {
                //获取新加入socketId
                String fromId = data.getString("id");
                //构建pcconnection
                getOrCreateRtcConnect(fromId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    //offer [from,to,room,sdp]
    private Emitter.Listener offerListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d(TAG, "offer:" + data);
            try {
                //获取id
                String fromId = data.getString("from");
                //获取peer
                Peer pc = getOrCreateRtcConnect(fromId);
                //构建RTCSessionDescription参数
                SessionDescription sdp = new SessionDescription(
                        SessionDescription.Type.fromCanonicalForm("offer"),
                        data.getString("sdp")
                );
                //设置远端setRemoteDescription
                pc.getPc().setRemoteDescription(pc, sdp);
                //设置answer
                pc.getPc().createAnswer(pc, sdpMediaConstraints);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    //answer [from,to,room,sdp]
    private Emitter.Listener answerListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d(TAG, "answer:" + data);
            try {
                //获取id
                String fromId = data.getString("from");
                //获取peer
                Peer pc = getOrCreateRtcConnect(fromId);
                //构建RTCSessionDescription参数
                SessionDescription sdp = new SessionDescription(
                        SessionDescription.Type.fromCanonicalForm("answer"),
                        data.getString("sdp")
                );
                //设置远端setRemoteDescription
                pc.getPc().setRemoteDescription(pc, sdp);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    //candidate [from,to,room,candidate[sdpMid,sdpMLineIndex,sdp]]
    private Emitter.Listener candidateListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d(TAG, "candidate:" + data);
            try {
                //获取id
                String fromId = data.getString("from");
                //获取peer
                Peer pc = getOrCreateRtcConnect(fromId);
                //获取candidate
                JSONObject candidate = data.getJSONObject("candidate");
                IceCandidate iceCandidate = new IceCandidate(
                        candidate.getString("sdpMid"), //描述协议id
                        candidate.getInt("sdpMLineIndex"),//描述协议的行索引
                        candidate.getString("sdp")//描述协议
                );

                //添加远端设备路由描述
                pc.getPc().addIceCandidate(iceCandidate);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    //exit [from,room]
    private Emitter.Listener exitListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d(TAG, "exit:" + data);
            try {
                //获取id
                String fromId = data.getString("from");
                //判断是否为当前连接
                Peer pc = peers.get(fromId);
                if (pc != null) {
                    //peer关闭
                    getOrCreateRtcConnect(fromId).getPc().close();
                    //删除peer对象
                    peers.remove(fromId);
                    //通知UI界面移除video
                    rtcListener.onRemoveRemoteStream(fromId);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * 信令服务器发送消息
     **/
    public void sendMessage(String content) {
        if (iotSSE.isSSEConnected()) {
            Log.d(TAG, "sendMessage=" + content);
            iotSSE.sendMessage(TV_ID, UUID.randomUUID().toString(), "swaiot-os-iotchannel", content);
        }
    }

    /**
     * WebRtc 音视频相关辅助函数
     **/
    //创建Media及Sdp约束
    private void createMediaConstraintsInternal() {
        // 音频约束
        audioConstraints = new MediaConstraints();
        // added for audio performance measurements
//        if (pcParams.noAudioProcessing) {
//            Log.d(TAG, "Disabling audio processing");
//            audioConstraints.mandatory.add(
//                    new MediaConstraints.KeyValuePair(AUDIO_ECHO_CANCELLATION_CONSTRAINT, "false"));
//            audioConstraints.mandatory.add(
//                    new MediaConstraints.KeyValuePair(AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT, "false"));
//            audioConstraints.mandatory.add(
//                    new MediaConstraints.KeyValuePair(AUDIO_HIGH_PASS_FILTER_CONSTRAINT, "false"));
//            audioConstraints.mandatory.add(
//                    new MediaConstraints.KeyValuePair(AUDIO_NOISE_SUPPRESSION_CONSTRAINT, "false"));
//        }

        //SDP约束 createOffer  createAnswer
        sdpMediaConstraints = new MediaConstraints();
//        sdpMediaConstraints.mandatory.add(
//                new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
//        sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
//                "OfferToReceiveVideo", "true" ));
        //sdpMediaConstraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));
    }

    //创建PeerConnection
    private void createPeerConnectionInternal() {

    }


    //创建音频模式JavaAudioDevice
    private AudioDeviceModule createJavaAudioDevice() {
        // Enable/disable OpenSL ES playback.
        if (!pcParams.useOpenSLES) {
            Log.w(TAG, "External OpenSLES ADM not implemented yet.");
            // TODO(magjed): Add support for external OpenSLES ADM.
        }

        // Set audio record error callbacks.
        JavaAudioDeviceModule.AudioRecordErrorCallback audioRecordErrorCallback = new JavaAudioDeviceModule.AudioRecordErrorCallback() {
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

        JavaAudioDeviceModule.AudioTrackErrorCallback audioTrackErrorCallback = new JavaAudioDeviceModule.AudioTrackErrorCallback() {
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

        return JavaAudioDeviceModule.builder(appContext)
                //.setSamplesReadyCallback(saveRecordedAudioToFile)
                .setUseHardwareAcousticEchoCanceler(!pcParams.disableBuiltInAEC)
                .setUseHardwareNoiseSuppressor(!pcParams.disableBuiltInNS)
                .setAudioRecordErrorCallback(audioRecordErrorCallback)
                .setAudioTrackErrorCallback(audioTrackErrorCallback)
                .createAudioDeviceModule();
    }

    //返回SSLSocketFactory 用于ssl连接
    private SSLSocketFactory getSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;

        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new TrustAllCerts()}, new SecureRandom());

            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ssfFactory;
    }


    public void sendData(String content) {
        sendMessage(content);
    }


    /**
     * 呼叫
     *
     * @param sdpDescription
     */
    public void sendOffer(SessionDescription sdpDescription) {
        Log.d(TAG, "log sendOffer sdp:\n" + sdpDescription.description);

        Model.PayLoad payLoad = new Model.PayLoad();
        payLoad.setSdp(sdpDescription);

        Model model = new Model();
        model.setType(Constant.OFFER);

        model.setPayload(payLoad);

        String text = new Gson().toJson(model);

        //String oldString = "96 97 98 99 100";
        //String newString = "100 98 96 97 99";
        //text = text.replaceAll(oldString, newString);

        Log.d(TAG, "log sendOffer : " + text);
        sendData(text);
    }

    /**
     * 呼叫
     *
     * @param sdpDescription
     */
    public void sendAnswer(SessionDescription sdpDescription) {
        Log.d(TAG, "sendAnswer sdp:\n" + sdpDescription.description);

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
    public void setIceCandidate(IceCandidate iceCandidate) {
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


    private class CustomerIOTMsgListener implements IotSSEMsgLib.IOTSSEMsgListener {

        @Override
        public String appSalt() {
            return "5619397646c8b3f43e82f61486e22bde";
        }

        @Override
        public void onSSELibError(IotSSEMsgLib.SSEErrorEnum sseErrorEnum, String s) {
            Log.d(TAG, "onSSELibError :error enum = " + sseErrorEnum + " error message = " + s);
        }

        @Override
        public void onSSEStarted() {
            Log.d(TAG, "onSSELibError onSSEStarted");
        }

        @Override
        public void onSendResult(IotSSEMsgLib.SSESendResultEnum sendResult, String destId, String msgId, String msgName, String message) {
            Log.d(TAG, " onSendResult: sendResult= " + sendResult + " destId = " + destId + " msgId = " + msgId + " msgName = " + msgName + " message=" + message);
        }

        @Override
        public void onReceivedSSEMessage(String msgId, String msgName, String message) {
            Log.d(TAG, "onReceivedSSEMessage message=" + message);
            try {
                Log.d(TAG, "WebRtcClientService handleIMMessage: content =" + message);
                postData(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSendFileToCloud(IotSSEMsgLib.SendFileResultEnum sendFileResultEnum, String s, long l, long l1, int i, String s1, String s2) {
            // not usage
        }

        @Override
        public void onReceivedFileFromCloud(IotSSEMsgLib.ReceivedFileResultEnum receivedFileResultEnum, String s, long l, long l1, String s1) {
            // not usage
        }
    }


    private void startSignalChannel() {
        mProcessHandler.post(new Runnable() {
            @Override
            public void run() {
                if (iotSSE == null) {
                    iotSSE = new IotSSEMsgLib(appContext, new CustomerIOTMsgListener());
                    iotSSE.connectSSEAsSmartScreen(DEVICE_ID);
                }
            }
        });


    }


    /**
     * 线程初始化
     */
    private void initHandler() {
        mHandler = new Handler(Looper.getMainLooper());

        if (mProcessHandler == null) {
            HandlerThread handlerThread = new HandlerThread(
                    "handler looper Thread");
            handlerThread.start();
            mProcessHandler = new ProcessHandler(handlerThread.getLooper());
        }
    }


    private void postData(String content) {
        if (content.contains(Constant.OFFER)) {
            Log.i(TAG, "postData Received OFFER");

            SSEEvent event = new SSEEvent();
            Model model = new Model(content);
            event.setModel(model);
            event.setMsgType(Constant.OFFER);
            event.setTargetSid(TV_ID);

            //获取peer
            Peer pc = getOrCreateRtcConnect("1");
            //构建RTCSessionDescription参数
            SessionDescription sdp = model.getPayload().getSdp();

            //设置远端setRemoteDescription
            pc.getPc().setRemoteDescription(pc, sdp);
            Log.d(TAG, "offer sdp:\n" + sdp.description);
            //设置answer
            pc.getPc().createAnswer(pc, sdpMediaConstraints);

        } else if (content.contains(Constant.ANSWER)) {
            Log.i(TAG, "postData Received Answer");

            SSEEvent event = new SSEEvent();
            Model model = new Model(content);
            event.setModel(model);
            event.setMsgType(Constant.ANSWER);
            //黏性事件 发送了该事件之后再订阅者依然能够接收到的事件
            EventBus.getDefault().postSticky(event);

        } else if (content.contains(Constant.CANDIDATE)) {
            Log.i(TAG, "postData Received candidate");
            SSEEvent event = new SSEEvent();
            Model model = new Model(content);
            event.setModel(model);
            event.setMsgType(Constant.CANDIDATE);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "EventBus post candidate");
                    EventBus.getDefault().post(event);
                }
            }, 1000);

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
                case HANDLER_THREAD_INIT_CONFIG_START:
                    break;
                default:
                    break;
            }

        }

    }


}
