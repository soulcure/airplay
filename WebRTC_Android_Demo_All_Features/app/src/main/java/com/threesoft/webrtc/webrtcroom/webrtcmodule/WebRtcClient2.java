package com.threesoft.webrtc.webrtcroom.webrtcmodule;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;

import com.threesoft.webrtc.webrtcroom.activity.ChatActivity2;

import org.apache.commons.codec.binary.Hex;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
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
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.ScreenCapturerAndroid;
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
import org.webrtc.voiceengine.WebRtcAudioManager;
import org.webrtc.voiceengine.WebRtcAudioRecord;
import org.webrtc.voiceengine.WebRtcAudioTrack;
import org.webrtc.voiceengine.WebRtcAudioUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import okhttp3.OkHttpClient;



/**
 * WebRtcClient类 封装PeerConnectionFactory工厂类及Socket.IO信令服务器
 * Created by zengjinlong 2021-01-20
 */

public class WebRtcClient2 {
    //Log Tag
    private final static String TAG = "WebRtcClient2";//WebRtcClient.class.getCanonicalName()

    //***************   WebRTC相关   *********************//
    ////webRtc定义常量////
    private static final String VIDEO_TRACK_ID = "ARDAMSv0";
    private static final String AUDIO_TRACK_ID = "ARDAMSa0";
    private static final String VIDEO_TRACK_ID1 = "ARDAMSv1";
    private static final String AUDIO_ECHO_CANCELLATION_CONSTRAINT = "googEchoCancellation";
    private static final String AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT = "googAutoGainControl";
    private static final String AUDIO_HIGH_PASS_FILTER_CONSTRAINT = "googHighpassFilter";
    private static final String AUDIO_NOISE_SUPPRESSION_CONSTRAINT = "googNoiseSuppression";
    private static final String VIDEO_FLEXFEC_FIELDTRIAL =
            "WebRTC-FlexFEC-03-Advertised/Enabled/WebRTC-FlexFEC-03/Enabled/";
    private static final String VIDEO_VP8_INTEL_HW_ENCODER_FIELDTRIAL = "WebRTC-IntelVP8/Enabled/";
    private static final String DISABLE_WEBRTC_AGC_FIELDTRIAL =
            "WebRTC-Audio-MinimizeResamplingOnMobile/Enabled/";

    public static final int FONT_FACTING = 0 ;
    public static final int BACK_FACING = 1 ;

    //PeerConnectionFactory工厂类
    private PeerConnectionFactory factory;
    //Peer集合
    private HashMap<String, Peer> peers = null;
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
    //本地音频资源
    private AudioSource localAudioSource;
    // enableAudio is set to true if audio should be sent.
    private boolean enableAudio = true;
    //音频Track
    private AudioTrack localAudioTrack;
    //本地摄像头视频捕获
    private CameraVideoCapturer cameraVideoCapturer;
    //屏幕录制
    private ScreenCapturerAndroid screenCapturer;
    //视频文件
    private MP4Capturer fileVideoCapturer;
    private VideoSource fileVideoSource;
    private VideoTrack fileVideoTrack;

    //页面context
    private Context appContext;
    //WebRtc EglContext环境
    private EglBase eglBase;
    //Activity回调接口
    private RtcListener rtcListener;
    private int currVolume;

    //录屏
    private boolean screenCaptureEnabled;
    private int screenWidth;
    private int screenHeight;
    private  Intent permisionData;

    //视频文件
    private boolean fileVideoCaptureEnabled;
    private String inputVideoFile;
    private int videoWidth;
    private int videoHeight;
//    private  Intent videoData;


    // 数据通道
    private boolean dataChannelEnabled = true;
    @Nullable
    private DataChannel dtChanel;





    //***************   SocketIO 信令服务相关  *********************//
    //socket.io信令交互
    private Socket client;
    //信令服务器地址
    private String host = "http://39.108.224.231:8888";//信令服务器
    //本地socket id
//    private String socketId;
    //room id
    private String  roomId;
    private String toId;
    private Double mLat = 0.0;
    private Double mLng = 0.0;




    //创建 SingleObject 的一个对象
    private static WebRtcClient2 instance = new WebRtcClient2();

    //让构造函数为 private，这样该类就不会被实例化
    private WebRtcClient2(){}

    //获取唯一可用的对象
    public static WebRtcClient2 getInstance(){
        return instance;
    }

    public void init(Context appContext) {
        Log.d(TAG,"init");
        if(client != null && client.connected()){
            Log.e(TAG,"socket io is ok,not need reconnect");
            return;
        }
        this.appContext = appContext;
        createSocket();
    }
    public void reConnect() {
        Log.d(TAG,"init");
        if(client != null && client.connected()){
            Log.e(TAG,"socket io is ok,not need reconnect");
            return;
        }
        createSocket();
    }

    //初始化RTC
    public void initRTC(EglBase eglBase,
                        PeerConnectionParameters peerConnectionParameters,
                        RtcListener listener) {
        Log.d(TAG,"initRTC");
        this.peers = new HashMap<>();
        this.eglBase = eglBase;
        this.pcParams = peerConnectionParameters;
        this.rtcListener = listener;
        //PeerConnectionFactory工厂类构建
        createPeerConnectionFactoryInternal();
        //创建iceservers
        createIceServers();
        //创建RTCConfiguration参数
        createRtcConfig();
        //创建Pc及Sdp约束
        createMediaConstraintsInternal();

    }

    public void setRtcListener(RtcListener rtcListener) {
        this.rtcListener = rtcListener;
    }

    public RtcListener getRtcListener() {
        return rtcListener;
    }



    //创建IceServers参数
    private void createIceServers() {
        //iceServers.add(PeerConnection.IceServer.builder("stun:stun.xten.com").createIceServer());
        iceServers.add(PeerConnection.IceServer.builder("stun:39.108.224.231").createIceServer());
        Log.d(TAG,"createIceServers end");
    }

    //创建RTCConfiguration参数
    private void createRtcConfig() {
        rtcConfig =
                new PeerConnection.RTCConfiguration(iceServers);
        // TCP candidates are only useful when connecting to a server that supports
        // ICE-TCP.
        rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED;
        rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE;
        rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE;
        rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;
        // Use ECDSA encryption.
        rtcConfig.keyType = PeerConnection.KeyType.ECDSA;
        // Enable DTLS for normal calls and disable for loopback calls.
        rtcConfig.enableDtlsSrtp = !pcParams.loopback;
        rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN;
    }


    //创建PeerConnection工厂类
    private void createPeerConnectionFactoryInternal() {
        //创建webRtc连接工厂类
        final VideoEncoderFactory encoderFactory;
        final VideoDecoderFactory decoderFactory;
        final boolean enableH264HighProfile =
                "H264 High".equals(pcParams.videoCodec);
        //adjustVol();
        Log.d(TAG,"createPeerConnectionFactoryInternal, videoCodeC:"+pcParams.videoCodec);
        AudioDeviceModule adm = createLegacyAudioDevice();
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
                .setAudioDeviceModule(adm)
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory)
                .createPeerConnectionFactory();

    }

    /** WebRtc相关 */
    //构建webRtc连接并返回
    //特别注意：这个id应该是发起者的id
    private Peer getOrCreateRtcConnect(String socketId) {
        Log.d(TAG,"getOrCreateRtcConnect,socketId:"+socketId);
        if(peers != null && localVideoTrack != null && localAudioTrack != null){
            Peer pc = peers.get(socketId);
            if (pc == null) {
                Log.d(TAG,"getOrCreateRtcConnect new pc");
                //构建RTCPeerConnection PeerConnection相关回调进入Peer中
                pc = new Peer(socketId,factory,rtcConfig, WebRtcClient2.this);
                List<String> mediaStreamLabels = Collections.singletonList("ARDAMS");
                //设置本地视频流
                pc.getPc().addTrack(localVideoTrack,mediaStreamLabels);
                //设置本地音频流
                pc.getPc().addTrack(localAudioTrack,mediaStreamLabels);
                //视频文件流
                pc.getPc().addTrack(fileVideoTrack,mediaStreamLabels);
                //创建数据通道
                initDataChannel(pc.getPc(),"test");


                //保存peer连接
                peers.put(socketId,pc);
            }

            return pc;
        }
        return null;

    }

//    public void reloadLocalVideoTrack(){
//        Log.d(TAG,"reloadLocalVideoTrack");
//        if(peers != null && localVideoTrack != null){
//
//            for (Peer pc:peers.values()){
//                List<String> mediaStreamLabels = Collections.singletonList("ARDAMS");
//                 List<RtpSender> sends = pc.getPc().getSenders();
//                 if(sends != null && sends.size() > 0){
//                     for(int i = 0; i < sends.size();i++){
//                         RtpSender sd = sends.get(i);
//                         pc.getPc().removeTrack(sd);
//                         Log.d(TAG,"reloadLocalVideoTrack ,remove senders");
//                     }
//                 }
//                //设置本地视频流
//                Log.d(TAG,"reloadLocalVideoTrack, and new track");
//                pc.getPc().addTrack(localVideoTrack,mediaStreamLabels);
//                pc.getPc().addTrack(localAudioTrack,mediaStreamLabels);
//
//
//            }
//
//        }
//    }

    //启动设备视频并关联本地video
    public void startCamera(VideoSink localRender,int type){
        Log.d(TAG,"startCamera ,localRender:"+localRender+",type:"+type);

        if( pcParams != null && pcParams.videoCallEnabled){
            if(cameraVideoCapturer == null){
                Log.d(TAG,"startCamera cameraVideoCapturer is null");
                String cameraname = "";
                Camera1Enumerator camera1Enumerator = new Camera1Enumerator();
                String[] deviceNames = camera1Enumerator.getDeviceNames();
                if (type == FONT_FACTING){
                    //前置摄像头
                    for (String deviceName : deviceNames){
                        if (camera1Enumerator.isFrontFacing(deviceName)){
                            cameraname = deviceName;
                        }
                    }
                }else {
                    //后置摄像头
                    for (String deviceName : deviceNames){
                        if (camera1Enumerator.isBackFacing(deviceName)){
                            cameraname = deviceName;
                        }
                    }
                }
                Log.d(TAG,"startCamera,cameraname:"+cameraname);
                if(cameraname == null || cameraname.length() == 0){
                    cameraname = deviceNames[0];
                }
                Log.d(TAG,"startCamera,cameraname:"+cameraname);

                cameraVideoCapturer = camera1Enumerator.createCapturer(cameraname,null);
                SurfaceTextureHelper surfaceTextureHelper =
                        SurfaceTextureHelper.create("CaptureThread", eglBase.getEglBaseContext());
                localVideoSource = factory.createVideoSource(false);
                cameraVideoCapturer.initialize(surfaceTextureHelper, appContext, localVideoSource.getCapturerObserver());
                cameraVideoCapturer.startCapture(pcParams.videoWidth,pcParams.videoHeight,pcParams.videoFps);
                localVideoTrack = factory.createVideoTrack(VIDEO_TRACK_ID, localVideoSource);
                localVideoTrack.setEnabled(true);
                localVideoTrack.addSink(localRender);
            }else {
                Log.d(TAG,"startCamera cameraVideoCapturer is not null");
                //onResume走这里是合理的，但onDestory后走这里则不合理
                cameraVideoCapturer.startCapture(pcParams.videoWidth,pcParams.videoHeight,pcParams.videoFps);

            }
        }

    }

    public void setScreenCaptureParameter(int width,int height,Intent pData){
        Log.d(TAG,"setScreenCaptureParameter,width:"+width+",height:"+height);
        screenCaptureEnabled = true;
        screenWidth = width;
        screenHeight = height;
        permisionData = pData;

    }

    public void startScreenCapture(VideoSink localRender){
        Log.d(TAG,"startScreenCapture");
        if(screenCaptureEnabled){
            if(screenCapturer == null){
                Log.d(TAG,"startScreenCapture screenCapturer is null");

                screenCapturer =  new ScreenCapturerAndroid(
                        permisionData, new MediaProjection.Callback() {
                    @Override
                    public void onStop() {
                        Log.e(TAG,"User revoked permission to capture the screen.");
                    }
                });
                SurfaceTextureHelper surfaceTextureHelper =
                        SurfaceTextureHelper.create("CaptureThread", eglBase.getEglBaseContext());
                localVideoSource = factory.createVideoSource(true);
                screenCapturer.initialize(surfaceTextureHelper, appContext, localVideoSource.getCapturerObserver());
                screenCapturer.startCapture(screenWidth,screenHeight,pcParams.videoFps);
                localVideoTrack = factory.createVideoTrack(VIDEO_TRACK_ID, localVideoSource);
                localVideoTrack.setEnabled(true);
                localVideoTrack.addSink(localRender);
            }else {
                Log.d(TAG,"startScreenCapture screenCapturer is not null");
                //onResume走这里是合理的，但onDestory后走这里则不合理
                screenCapturer.startCapture(screenWidth,screenHeight,pcParams.videoFps);

            }
        }

    }

    public void setFileVideoCaptureParameter(String path){
        Log.d(TAG,"setFileVideoCaptureParameter");
        fileVideoCaptureEnabled = true;
        inputVideoFile = path;


        Log.d(TAG,"filePath:"+inputVideoFile);
    }


    public void startFileVideoCapture(VideoSink localRender){
        Log.d(TAG,"startFileVideoCapture");
        if(fileVideoCaptureEnabled){
            if(fileVideoCapturer == null){
                Log.d(TAG,"startFileVideoCapture fileVideoCapture is null");

                Log.d(TAG,"inputVideoFile:"+inputVideoFile);
                fileVideoCapturer =  new MP4Capturer(inputVideoFile);

                SurfaceTextureHelper surfaceTextureHelper =
                        SurfaceTextureHelper.create("CaptureThread", eglBase.getEglBaseContext());
                localVideoSource = factory.createVideoSource(true);
                fileVideoCapturer.initialize(surfaceTextureHelper, appContext, localVideoSource.getCapturerObserver());
                fileVideoCapturer.startCapture(videoWidth,videoHeight,pcParams.videoFps);
                localVideoTrack = factory.createVideoTrack(VIDEO_TRACK_ID, localVideoSource);
                localVideoTrack.setEnabled(true);
                localVideoTrack.addSink(localRender);
            }else {
                Log.d(TAG,"startFileVideoCapture fileVideoCapture is not null");
                //onResume走这里是合理的，但onDestory后走这里则不合理
                fileVideoCapturer.startCapture(pcParams.videoWidth,pcParams.videoHeight,pcParams.videoFps);

            }
        }

    }


    public void startFileVideoCapture2(VideoSink localRender){
        Log.d(TAG,"startFileVideoCapture2");
        if(fileVideoCaptureEnabled){
            if(fileVideoCapturer == null){
                Log.d(TAG,"startFileVideoCapture2 fileVideoCapture is null");

                Log.d(TAG,"inputVideoFile:"+inputVideoFile);
                fileVideoCapturer =  new MP4Capturer(inputVideoFile);

                SurfaceTextureHelper surfaceTextureHelper =
                        SurfaceTextureHelper.create("CaptureThread2", eglBase.getEglBaseContext());
                fileVideoSource = factory.createVideoSource(true);
                fileVideoCapturer.initialize(surfaceTextureHelper, appContext, fileVideoSource.getCapturerObserver());
                fileVideoCapturer.startCapture(videoWidth,videoHeight,pcParams.videoFps);
                fileVideoTrack = factory.createVideoTrack(VIDEO_TRACK_ID1, fileVideoSource);
                fileVideoTrack.setEnabled(true);
                fileVideoTrack.addSink(localRender);
            }else {
                Log.d(TAG,"startFileVideoCapture2 fileVideoCapture is not null");
                //onResume走这里是合理的，但onDestory后走这里则不合理
                fileVideoCapturer.startCapture(pcParams.videoWidth,pcParams.videoHeight,pcParams.videoFps);

            }
            List<String> mediaStreamLabels = Collections.singletonList("ARDAMS");
            if(peers != null){
                for(Peer peer:peers.values()){
                    Log.d(TAG,"startFileVideoCapture2, add track now");
                    peer.getPc().addTrack(fileVideoTrack,mediaStreamLabels);

                }
            }{
                Log.e(TAG,"startFileVideoCapture2 peers is null");
            }

        }

    }


    public void initFileVideoCapture(VideoSink localRender){
        Log.d(TAG,"startFileVideoCapture3");

        if(fileVideoCapturer == null){
            Log.d(TAG,"startFileVideoCapture3 fileVideoCapture is null");

            fileVideoCapturer =  new MP4Capturer(null);

            SurfaceTextureHelper surfaceTextureHelper =
                    SurfaceTextureHelper.create("CaptureThread2", eglBase.getEglBaseContext());
            fileVideoSource = factory.createVideoSource(true);
            fileVideoCapturer.initialize(surfaceTextureHelper, appContext, fileVideoSource.getCapturerObserver());
//                fileVideoCapturer.startCapture(videoWidth,videoHeight,pcParams.videoFps);
            fileVideoTrack = factory.createVideoTrack(VIDEO_TRACK_ID1, fileVideoSource);
            fileVideoTrack.setEnabled(true);
            fileVideoTrack.addSink(localRender);
        }


    }

    public void startFileVideoCapture(String path){
        Log.d(TAG,"startFileVideoCapture,path:"+path);
        fileVideoCaptureEnabled = true;
        fileVideoCapturer.setVideoPath(path);
        fileVideoCapturer.startCapture(videoWidth,videoHeight,pcParams.videoFps);

    }
    public void stopFileVideoCapture() {
        if (fileVideoCapturer != null ) {
            Log.d(TAG, " stopFileVideoCapture.");
            try {
                fileVideoCapturer.stopCapture();
            } catch (InterruptedException e) {
            }
        }

    }

    //切换摄像头
    public void switchCamera(){
        if(cameraVideoCapturer != null){
            cameraVideoCapturer.switchCamera(null);
        }
    }

    //音频处理
    public   AudioTrack createAudioTrack() {
        localAudioSource = factory.createAudioSource(audioConstraints);
        localAudioTrack = factory.createAudioTrack(AUDIO_TRACK_ID, localAudioSource);
        enableAudio = true;
        localAudioTrack.setEnabled(enableAudio);

        Log.d(TAG,"createAudioTrack:localAudioTrack:"+localAudioTrack);
        return localAudioTrack;
    }


    //创建Media及Sdp约束
    private void createMediaConstraintsInternal() {
        // 音频约束
        audioConstraints = new MediaConstraints();
        // added for audio performance measurements
//        if (pcParams.noAudioProcessing) {
        Log.d(TAG, "Disabling audio processing");
            audioConstraints.mandatory.add(
                    new MediaConstraints.KeyValuePair(AUDIO_ECHO_CANCELLATION_CONSTRAINT, "true"));
        audioConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair(AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT, "true"));
        audioConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair(AUDIO_HIGH_PASS_FILTER_CONSTRAINT, "true"));
            audioConstraints.mandatory.add(
                    new MediaConstraints.KeyValuePair(AUDIO_NOISE_SUPPRESSION_CONSTRAINT, "true"));
//        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googAutoGainControlLevel", "3"));
        //SDP约束 createOffer  createAnswer
        sdpMediaConstraints = new MediaConstraints();
        sdpMediaConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                "OfferToReceiveVideo", "true" ));
        sdpMediaConstraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));
    }

    private void adjustVol(){
        int mediaMax = AudioUtil.getInstance(appContext).getMediaMaxVolume();
        int callMax = AudioUtil.getInstance(appContext).getCallMaxVolume();
        int mediaCur = AudioUtil.getInstance(appContext).getMediaVolume();
        int callCur = AudioUtil.getInstance(appContext).getCallVolume();
        if(mediaCur != mediaMax*3/4){
            AudioUtil.getInstance(appContext).setMediaVolume(mediaMax*3/4);
        }
        if(callCur != callMax/2){
            AudioUtil.getInstance(appContext).setCallVolume(callMax/2);
        }
    }



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

        return  new AudioDeviceModule() {
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
                .setUseHardwareAcousticEchoCanceler(!pcParams.disableBuiltInAEC)
                .setUseHardwareNoiseSuppressor(!pcParams.disableBuiltInNS)
                .setAudioRecordErrorCallback(audioRecordErrorCallback)
                .setAudioTrackErrorCallback(audioTrackErrorCallback)
                .createAudioDeviceModule();
    }

    //返回SSLSocketFactory 用于ssl连接
    private  SSLSocketFactory getSSLSocketFactory() {
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

    public CameraVideoCapturer getCameraVideoCapturer(){
        return cameraVideoCapturer;
    }

    public void stopCapture() {
        if (cameraVideoCapturer != null ) {
            Log.d(TAG, " stopCapture.");
            try {
                cameraVideoCapturer.stopCapture();
            } catch (InterruptedException e) {
            }
        }

    }

    public void startCapture() {
        Log.d(TAG,"startVideoSource ");
        if (cameraVideoCapturer != null) {
            Log.d(TAG, "Restart video source.");
            cameraVideoCapturer.startCapture(pcParams.videoWidth,pcParams.videoHeight,pcParams.videoFps);

        }

    }

//    public void releaseCameraCapture(){
//        Log.d(TAG,"releaseCameraCapture");
//        stopCapture();
//        cameraVideoCapturer.dispose();
//        cameraVideoCapturer = null;
//        localVideoTrack = null;
//    }

//    public void stopScreenCapture() {
//        if (screenCapturer != null ) {
//            Log.d(TAG, " stopScreenCapture.");
//            screenCapturer.stopCapture();
//        }
//
//    }

//    public void startScreenCapture() {
//        Log.d(TAG,"startScreenCapture ");
//        if (screenCapturer != null) {
//            Log.d(TAG, "Restart screencapture source.");
//            screenCapturer.startCapture(screenWidth,screenHeight,pcParams.videoFps);
//
//        }
//
//    }
//    public void releaseScreenCapture(){
//        Log.d(TAG,"releaseScreenCapture");
//        stopScreenCapture();
//        screenCapturer.dispose();
//        screenCapturer = null;
//        screenCaptureEnabled = false;
//        localVideoTrack = null;
//
//    }



    public void closePeers(){
        Log.d(TAG,"closePeers");
        closeInternal();
    }

    private void closeInternal() {

        Log.d(TAG, "closeInternal");
        //循环遍历 peer关闭
        if(peers != null){
            peers.clear();
            peers = null;
        }
        if(localAudioSource != null){
            localAudioSource.dispose();
            localAudioSource = null;

        }
        if(localVideoSource != null){
            localVideoSource.dispose();
            localVideoSource = null;
        }
        if(localVideoTrack != null){

            //localVideoTrack.dispose();
            localVideoTrack = null;
        }

        if(cameraVideoCapturer != null){
            try {
                cameraVideoCapturer.stopCapture();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //cameraVideoCapturer.dispose();
            cameraVideoCapturer = null;

        }
        if(screenCapturer != null){
            screenCapturer.stopCapture();
            screenCapturer = null;
        }

        if(fileVideoCapturer != null){
            try {
                fileVideoCapturer.stopCapture();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            fileVideoCapturer = null;
        }

        if(dtChanel != null){
            dtChanel.unregisterObserver();
            dtChanel.close();
            dtChanel.dispose();
            dtChanel = null;

        }
//        if(outfileChannel != null){
//            try {
//                outfileChannel.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            outfileChannel = null;
//        }
        if(outrandomAccessFile != null){
            try {
                outrandomAccessFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            outrandomAccessFile = null;
        }


        Log.d(TAG, "Closing peer connection factory.");
//        if (factory != null) {
//            factory.dispose();
//            factory = null;
//        }
        eglBase = null;

        rtcListener = null;
//        PeerConnectionFactory.stopInternalTracingCapture();
//        PeerConnectionFactory.shutdownInternalTracer();
        Log.d(TAG,"close finish");
    }

    public void setAudioEnabled(final boolean enable) {
        enableAudio = enable;
        if (localAudioTrack != null) {
            localAudioTrack.setEnabled(enableAudio);
        }

    }


    private   boolean isActivityDestroy(Activity mActivity) {
        if (mActivity == null ||
                mActivity.isFinishing() ||
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && mActivity.isDestroyed())) {
            return true;
        } else {
            return false;
        }
    }


    ////////////////////////DataChannel///////////////////

    //消息结构
//    {
//        from:
//        msgType: talk:1,file:2,icon:3
//        data:
//             {
//                 xx:xx
//             }
//    }

    //消息类型
    public static final int PEER_MSG_TYPE_TALK = 1;
    public static final int PEER_MSG_TYPE_FILE = 2;
    public static final int PEER_MSG_TYPE_ICON = 3;
    public    boolean isOrder;

    public static final int PEER_FILE_ACTION_OPEN = 1;
    public static final int PEER_FILE_ACTION_CLOSE = 2;
    public static final int PEER_FILE_ACTION_SPEED = 3;
    public  int PEER_FILE_BLOCK_SIZE = 1024*8;
    FileChannel outfileChannel;
    RandomAccessFile outrandomAccessFile;
    String outfilepathName;
    String outfileMd5;
    long outfileSize;
    long outfileCurPos;
    long outfileStartTime ;
    long lastTimeStamp;
    long useTimePerMB;

    private void initDataChannel(PeerConnection pc,String label){
        if(dataChannelEnabled ==true){
            Log.d(TAG,"initDataChannel");
            isOrder = true;
            if(!isOrder){
                PEER_FILE_BLOCK_SIZE = 1024*16;
            }

            DataChannel.Init init = new DataChannel.Init();
            init.ordered = false;
//            init.negotiated = false;
//            init.ordered = true;
//            init.negotiated = false;
//            init.maxRetransmits = 3;
//            init.maxRetransmitTimeMs = 3*1000;
//            init.id = 10086;
//            init.protocol = "maopao";
            dtChanel = pc.createDataChannel(label,init);
            Log.d(TAG,"initDataChannel,dtChannel:"+dtChanel);


        }

    }



    public void peersendTalk(String content){
        JSONObject msg = new JSONObject();
        try {
            msg.put("from",getSocketId());
            msg.put("msgType",PEER_MSG_TYPE_TALK);
            JSONObject data = new JSONObject();
            data.put("content",content);
            msg.put("data",data);
            peerSendData(msg.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    private void peerSendData( String message){
        Log.d(TAG,"peerSendData,msg:"+message);
        if(dtChanel != null){

            try{
                Log.d(TAG,"dtchannel is ok,send now");
                boolean isOk = dtChanel.send(new DataChannel.Buffer(ByteBuffer.wrap(message.getBytes("utf-8")),false));
                if(isOk){
                    Log.d(TAG,"peerSendData success");
                }else {
                    Log.e(TAG,"peerSendData failed");
                }
            }catch (UnsupportedEncodingException e){
                e.printStackTrace();
            }

        }else {
            Log.e(TAG,"dtChannel is not ok,init now");
            for(Peer peer:peers.values()){
                initDataChannel(peer.getPc(),"test");
            }
        }
    }
    private void peerSendBinaryData(ByteBuffer buffer ){
        if(dtChanel != null){
            dtChanel.send(new DataChannel.Buffer(buffer,true));
        }
    }

    public void peerSendFile(String filepath,String filetype,int action){
        Log.d(TAG,"peerSendFile:"+filepath);

        peerSendFileMsg(filepath,filetype,action);
        if(isOrder){
            peerSendFileOrderImp(filepath);
        }else{
            peerSendFileRandomImp(filepath);
        }

    }


//    {
//        action: 1,2
//        fileType:
//        fileName:
//    }

    private void peerSendFileMsg(String filepath,String fileType,int action){
        Log.d(TAG,"peerSendFileMsg");
        long fileSize = 0;
        String fileMd5="";
        File infile = new File(filepath);
        if(infile != null &&  infile.exists() &&infile.isFile()){
            fileSize = infile.length();
//               fileMd5 = DigestUtils.md5Hex(new FileInputStream(infile));
                fileMd5 = getMD5(infile);
        }
        JSONObject msg = new JSONObject();
        try {
            msg.put("from",getSocketId());
            msg.put("msgType",PEER_MSG_TYPE_FILE);
            JSONObject data = new JSONObject();
            data.put("action",action);
            data.put("fileType",fileType);
            data.put("fileName",filepath);
            data.put("fileSize",fileSize);
            data.put("fileMd5",fileMd5);
            long startTime = System.currentTimeMillis();
            data.put("startTime",startTime);
            msg.put("data",data);
            peerSendData(msg.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    private void peerSendFileSpeedMsg(long useTime){
        Log.d(TAG,"peerSendFileSpeedMsg");

        JSONObject msg = new JSONObject();
        try {
            msg.put("from",getSocketId());
            msg.put("msgType",PEER_MSG_TYPE_FILE);
            JSONObject data = new JSONObject();
            data.put("action",PEER_FILE_ACTION_SPEED);
            data.put("useTimePerMB",useTime);
            msg.put("data",data);
            peerSendData(msg.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }




    private void peerSendFileOrderImp(String filepath){
        if(dtChanel == null || filepath == null || filepath.length()==0){
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG,"peerSendFileOrderImp:"+filepath);
                try {
                    FileChannel fileChannel = new FileInputStream(filepath).getChannel();
                    long fileSize = fileChannel.size();
                    Log.d(TAG,"peerSendFileOrderImp,file size:"+fileSize);
                    int block_size = PEER_FILE_BLOCK_SIZE;
                    ByteBuffer byteBuffer = ByteBuffer.allocate(8+block_size);
                    long totalBlock = fileSize/block_size;
                    Log.d(TAG,"peerSendFileOrderImp,block_size:"+block_size+",total block:"+totalBlock);

                    int cur_block = 0;
                    long startTime = System.currentTimeMillis();
                    long sleepTime = 1;
                    if(totalBlock <= 2*1000){
                        sleepTime = 1;
                    }else if(totalBlock <= 4*1000){
                        sleepTime = 2;
                    }else if(totalBlock <= 8*1000){
                        sleepTime = 3;
                    }else {
                        sleepTime = 4;
                    }


                    while(fileChannel.read(byteBuffer) != -1){
                        long t0 = System.currentTimeMillis();
                        Thread.sleep(sleepTime);//控制发送速度
                        long t1 = System.currentTimeMillis();
                        byteBuffer.flip();
                        cur_block ++;
                        if(cur_block % 100 == 0){
                            int tmp = cur_block/100;
                            if(tmp %2 ==0){
                                sleepTime = sleepTime*2;
                            }else {
                                sleepTime = sleepTime/2;
                            }
                        }
                        long t2 = System.currentTimeMillis();
                        dtChanel.send(new DataChannel.Buffer(byteBuffer,true));
                        byteBuffer.clear();
                        long t3 = System.currentTimeMillis();
                        Log.d(TAG,"peerSendFileOrderImp,block:"+cur_block+"/"+totalBlock+"s0:"+(t1-t0)+",s1:"+(t2-t1)+",S2:"+(t3-t2));
                    }

                    fileChannel.close();
                    double useTime = ((System.currentTimeMillis() - startTime)*1.0)/1000;
                    double fileMB = (fileSize*1.0)/(1024*1024);
                    double bitrate = fileMB*8/useTime;
                    Log.d(TAG,"peerSendFileOrderImp send finished,use time:"+useTime+"s,total_block:"+cur_block+",filesize:"+fileMB+"MB,bitrate:"+bitrate+" Mbps");


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();


    }

    private void peerSendFileRandomImp(String filepath){

        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG,"peerSendFileRandomImp:"+filepath);
                if(dtChanel == null || filepath == null || filepath.length()==0){
                    return;
                }
                try {
                    RandomAccessFile infile = new RandomAccessFile(filepath,"r");
                    long fileSize = infile.length();
                    Log.d(TAG,"peerSendFileRandomImp,file size:"+fileSize);
                    int block_size = PEER_FILE_BLOCK_SIZE;
                    //ByteBuffer byteBuffer = ByteBuffer.allocate(8+block_size);
                    long totalBlock = fileSize/block_size;
                    Log.d(TAG,"peerSendFileRandomImp,block_size:"+block_size+",total block:"+totalBlock);

                    int cur_block = 0;
                    long startTime = System.currentTimeMillis();

                    long sleepTime = 1;
                    byte[] bytes = new byte[8+block_size];
                    long cur_pos = 0;
                    ByteBuffer tmpB = ByteBuffer.allocate(8);
                    while(true){
                        long t0 = System.currentTimeMillis();
                        Thread.sleep(sleepTime);//控制发送速度
                        long t1 = System.currentTimeMillis();
                       //先把位置写到头部8个字节
                        tmpB.putLong(cur_pos);
                        tmpB.flip();
                        tmpB.get(bytes,0,8);
                        tmpB.clear();

                        //然后把真实数据从文件中读出，然后写入；
                        infile.seek(cur_pos);
                        int readbytenumber = infile.read(bytes,8,block_size);
                        if(readbytenumber == -1){
                            break;
                        }
                        long t2 = System.currentTimeMillis();
                        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
                        byteBuffer.position(0);
                        byteBuffer.limit(8+readbytenumber);

                        cur_pos += readbytenumber;
                        cur_block ++;
                        long t3 = System.currentTimeMillis();
                        if(dtChanel != null){
                            dtChanel.send(new DataChannel.Buffer(byteBuffer,true));
                        }else{
                            Log.e(TAG,"peerSendFileRandomImp ,dtChannel is null");
                            break;
                        }
                        long t4 = System.currentTimeMillis();
                        Log.d(TAG,"peerSendFileRandomImp,block:"+cur_block+"/"+totalBlock+",s0:"+(t1-t0)+",s1:"+(t2-t1)+",S2:"+(t3-t2)+",S3:"+(t4-t3));

                    }

                    infile.close();
                    double useTime = ((System.currentTimeMillis() - startTime)*1.0)/1000;
                    double fileMB = (fileSize*1.0)/(1024*1024);
                    double bitrate = fileMB*8/useTime;
                    Log.d(TAG,"peerSendFileRandomImp send finished,use time:"+useTime+"s,total_block:"+cur_block+",filesize:"+fileMB+"MB,bitrate:"+bitrate+" Mbps");


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();


    }



    public void peerReceiveMsg(String msg){
        Log.d(TAG,"peerReceiveMsg,msg:"+msg);
        try {

            JSONObject msgJson = new JSONObject(msg);
            int  msgType = msgJson.getInt("msgType");
            JSONObject dataJson = msgJson.getJSONObject("data");
            switch(msgType){
                case PEER_MSG_TYPE_TALK:
                    break;
                case PEER_MSG_TYPE_FILE:
                    processFileAction(dataJson);
                    break;
                case PEER_MSG_TYPE_ICON:

                    break;
            }
            if(rtcListener != null){
                rtcListener.onPeerTalkMsg(msgJson);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

//    {
//        action: 1,2
//        fileType:
//        fileName:
//    }

    private void processFileAction(JSONObject data){
        Log.d(TAG,"processFileAction,data:"+data);
        try {

            int action = data.getInt("action");
            switch(action){
                case PEER_FILE_ACTION_OPEN:
                    String fileType = data.getString("fileType");
                    String fileName = data.getString("fileName");
                    outfileStartTime = System.currentTimeMillis();
                    outfileSize = data.getLong("fileSize");
                    outfileMd5 = data.getString("fileMd5");
                    outfileCurPos = 0;
                    String outfile = "/storage/emulated/0/DCIM/"+fileName.substring(fileName.lastIndexOf("/")+1);
                    //打开文件
                    Log.d(TAG,"processFileAction,create file:"+outfile);
                    outfilepathName = outfile;
                    File file = new File(outfile);
                    if(isOrder){
                        outfileChannel = new FileOutputStream(file).getChannel();
                    }else {
                        outrandomAccessFile = new RandomAccessFile(file,"rw");
                        outrandomAccessFile.setLength(outfileSize);
                    }

                    break;
                case PEER_FILE_ACTION_CLOSE:
                    //关闭文件
//                    if(outfileChannel != null){
//                        outfileChannel.close();
//                        outfileChannel = null;
//                    }
                    break;
                case PEER_FILE_ACTION_SPEED:
                    useTimePerMB = data.getLong("useTimePerMB");
                    Log.d(TAG,"processFileAction,useTimePerMB:"+useTimePerMB);
                    break;
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void peerReceiveFile(ByteBuffer buffer){
        if(isOrder){
            peerReceiveOrderFile(buffer);
        }else{
            peerReceiveRandomFile(buffer);
        }
    }

    public void peerReceiveOrderFile(ByteBuffer buffer) {
        if (outfileChannel != null) {
            try {
                long t0 = System.currentTimeMillis();
                outfileChannel.write(buffer);
                outfileCurPos += buffer.limit();
                long t1 = System.currentTimeMillis();
                Log.d(TAG,"peerReceiveOrderFile,block:"+(outfileCurPos/PEER_FILE_BLOCK_SIZE)+"/"+(outfileSize/PEER_FILE_BLOCK_SIZE)+",S0:"+(t1-t0));
                if (outfileCurPos >= outfileSize) {
                    Log.d(TAG, "peerReceiveOrderFile receive file finished,received:" + outfileCurPos + ",fileSize:" + outfileSize);
                    double useTime = ((System.currentTimeMillis() - outfileStartTime) * 1.0) / 1000;
                    double fileMB = (outfileSize * 1.0) / (1024 * 1024);
                    double bitrate = fileMB * 8 / useTime;
                    Log.d(TAG, "peerReceiveFile,use time:" + useTime + "s,fileSize:" + fileMB + "MB,bitrate:" + bitrate + "Mbps");
                    outfileChannel.close();
                    outfileChannel = null;
                    outfileSize = 0;
                    outfileCurPos = 0;
                    String outMd5 = getMD5(new File(outfilepathName));
                    Log.d(TAG,"peerReceiveOrderFile , src md5:"+outfileMd5);
                    Log.d(TAG,"peerReceiveOrderFile , dst md5:"+outMd5);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void peerReceiveRandomFile(ByteBuffer buffer){
        if(outrandomAccessFile != null){
            try {
                long t0 = System.currentTimeMillis();
                long write_pos = buffer.getLong();
                int  remaining = buffer.remaining();
                byte [] data = new byte[remaining];
                buffer.get(data);
                outrandomAccessFile.seek(write_pos);
                outrandomAccessFile.write(data);
                outfileCurPos += remaining;
                long t1 = System.currentTimeMillis();
                Log.d(TAG,"peerReceiveRandomFile,block:"+(write_pos/PEER_FILE_BLOCK_SIZE)+"/"+(outfileSize/PEER_FILE_BLOCK_SIZE)+",S0:"+(t1-t0));

                if(outfileCurPos >= outfileSize){
                    Log.d(TAG,"peerReceiveRandomFile receive file finished,received:"+outfileCurPos+",fileSize:"+outfileSize);
                    double useTime = ((System.currentTimeMillis() - outfileStartTime)*1.0)/1000;
                    double fileMB = (outfileSize*1.0)/(1024*1024);
                    double bitrate = fileMB*8/useTime;
                    Log.d(TAG,"peerReceiveRandomFile,use time:"+useTime+"s,fileSize:"+fileMB+"MB,bitrate:"+bitrate+"Mbps");
                    outrandomAccessFile.close();
                    outrandomAccessFile = null;
                    outfileSize = 0;
                    outfileCurPos = 0;
                    String outMd5 = getMD5(new File(outfilepathName));
                    Log.d(TAG,"peerReceiveRandomFile , src md5:"+outfileMd5);
                    Log.d(TAG,"peerReceiveRandomFile , dst md5:"+outMd5);

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }








    /** 信令服务器处理相关 **/

    public String getSocketId() {
        if(client != null ){
            return client.id();
        }else{
            return null;
        }

    }

    public String getRoomId() {
        return roomId;
    }

    //创建信令服务器及监听
    private void createSocket() {
        Log.d(TAG,"createSocket");
        //socket模式连接信令服务器
        try {
            //普通连接
            //client = IO.socket(host);

            //SSL加密连接
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    })
                    .sslSocketFactory(getSSLSocketFactory(), new TrustAllCerts())
                    .build();
            IO.setDefaultOkHttpWebSocketFactory(okHttpClient);
            IO.setDefaultOkHttpCallFactory(okHttpClient);
            IO.Options opts = new IO.Options();
            opts.callFactory = okHttpClient;
            opts.webSocketFactory = okHttpClient;
            client = IO.socket(host, opts);

            //设置消息回调接口
            setMsgListener();

            //开始连接
            client.connect();
            heartBeating();
            Log.d(TAG,"createSocket connect :"+host);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }


    private void setMsgListener(){
        ////设置消息监听
        //连上服务器
        //connect
        client.on("connect",connectedListener);
        //上线
        //online [id,lat,lng]
        client.on("online",onlineListener);
        //created [id,room,peers]
        client.on("created", createdListener);
        //notifyJoinRoom [from,to,roomId]
        client.on("notifyJoinRoom",notifyJoinRoomListener);
        //joined [id,room]
        client.on("joined", joinedListener);
        //offer [from,to,room,sdp]
        client.on("offer", offerListener);
        //answer [from,to,room,sdp]
        client.on("answer", answerListener);
        //candidate [from,to,room,candidate[sdpMid,sdpMLineIndex,sdp]]
        client.on("candidate", candidateListener);
        //exit [from,room]
        client.on("exit", exitListener);
        //notifyExitRoom [from,to,room]
        client.on("notifyExitRoom",exitNotifyListener);
    }

    private void reOnline(){
        online(mLat,mLng);
    }


    //创建并加入
    public void online(double lat,double lng){
        mLat = lat;
        mLng = lng;
        try {
            JSONObject message = new JSONObject();
            message.put("id",getSocketId());
            message.put("lat",lat);
            message.put("lng",lng);
            //向信令服务器发送信令
            sendMessage("online",message);
            Log.d(TAG,"online,message:"+message);
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    //通知对方开始进入房间聊天
    public void notifyJoinRoom(String from,String to,String roomId){
        toId = to;//记录目标，方便对方没接的时候我们也可以让它退出；
        try {
            JSONObject message = new JSONObject();
            message.put("from",from);
            message.put("to",to);
            message.put("room",roomId);
            //向信令服务器发送信令
            sendMessage("notifyJoinRoom",message);
            Log.d(TAG,"notifyJoinRoom,message:"+message);
        }catch (JSONException e){
            e.printStackTrace();
        }
    }


    //创建并加入
    public void createAndJoinRoom(String roomId){
        //构建信令数据并发送
        try {
            JSONObject message = new JSONObject();
            message.put("room",roomId);
            //向信令服务器发送信令
            sendMessage("createAndJoinRoom",message);
            Log.d(TAG,"createAndJoinRoom,message:"+message);
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    public void exitNotify(String socketId,String toId,String roomId){
        try{
            JSONObject message = new JSONObject();
            message.put("from",socketId);
            message.put("to",toId);
            message.put("room",roomId);
            //向信令服务器发送信令
            sendMessage("notifyExitRoom",message);
            Log.d(TAG,"exitNotify,message:"+message);
        }catch (JSONException e){

        }

    }


    //退出room
    public void exitRoom(){
        Log.d(TAG,"exitRoom");
        //信令服务器发送 exit [from room]
        try {
            JSONObject message = new JSONObject();
            message.put("from",getSocketId());
            message.put("room",roomId);
            message.put("disconnect",0);
            //向信令服务器发送信令
            sendMessage("exit",message);
            Log.d(TAG,"exitRoom,message:"+message);

        }catch (JSONException e){
            e.printStackTrace();
        }


        closeInternal();

        roomId = "";



    }

    private Emitter.Listener connectedListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "connectedListener:"+getSocketId() );
            reOnline();
        }
    };

    //online [id,lat,lng]
    private Emitter.Listener onlineListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d(TAG, "onlineListener:" + data);
            Users users = new Users();
            try {
                JSONArray array = (JSONArray)data.getJSONArray("peers");
                for(int i=0;i<array.length();i++){
                    User user = new User();
                    user.socketId = array.getJSONObject(i).getString("id");
                    boolean hasPos = false;
                    try{
                        user.lat = array.getJSONObject(i).getDouble("lat");
                        user.lng = array.getJSONObject(i).getDouble("lng");
                        user.isGirl = i%2==1?true:false;
                        hasPos = true;
                    }catch (JSONException e){
                        Log.d(TAG,"onlinelistener, "+user.socketId+" lat lng is empty");
                        hasPos = false;
                    }
                    if(hasPos){
//                        users.peers.add(user);
                        users.peers.put(user.socketId,user);
                    }
                }
                EventBus.getDefault().post(users);

            } catch (JSONException e) {
                e.printStackTrace();
            }



        }
    };



    //created [id,room,peers]
    private Emitter.Listener createdListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d(TAG, "createdListener:" + data);
            if(peers == null){
                Log.d(TAG,"createdListener ,peers is null ,return");
                return;
            }
            try {
                //设置socket id
//                socketId = data.getString("id");
                //设置room id
                roomId = data.getString("room");
                //获取peer数据
                JSONArray peers = data.getJSONArray("peers");
                //根据回应peers 循环创建WebRtcPeerConnection，创建成功后发送offer消息 [from,to,room,sdp]
                for (int i = 0; i < peers.length(); i++) {
                    JSONObject otherPeer = peers.getJSONObject(i);
                    String otherSocketId = otherPeer.getString("id");
                    //创建WebRtcPeerConnection
                    Peer pc = getOrCreateRtcConnect(otherSocketId);
                    if(pc != null){
                        //设置offer
                        pc.getPc().createOffer(pc,sdpMediaConstraints);
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    //notifyJoinRoom [from,to,roomId]
    private Emitter.Listener notifyJoinRoomListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d(TAG, "notifyJoinRoomListener:" + data);
            try {

                NotifyJoinRoomMsg notifyJoinRoomMsg = new NotifyJoinRoomMsg();
                notifyJoinRoomMsg.from = data.getString("from");
                notifyJoinRoomMsg.to = data.getString("to");
                notifyJoinRoomMsg.room = data.getString("room");
                roomId = notifyJoinRoomMsg.room;

                //createAndJoinRoom(roomId);
                EventBus.getDefault().post(notifyJoinRoomMsg);


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    //joined [id,room]
    private Emitter.Listener joinedListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d(TAG, "joinedListener:" + data);
            try {
                //获取新加入socketId
                String fromId = data.getString("id");

                JoinMsg joinMsg = new JoinMsg();
                joinMsg.from = fromId;
                EventBus.getDefault().post(joinMsg);

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
            Log.d(TAG, "offerListener:" + data);
            if(peers == null){
                Log.d(TAG,"offerListener ,peers is null ,return");
                return;
            }
            try {
                //获取id
                String fromId = data.getString("from");
                //获取peer
                Peer pc = getOrCreateRtcConnect(fromId);
                if( pc != null){
                    //构建RTCSessionDescription参数
                    SessionDescription sdp = new SessionDescription(
                            SessionDescription.Type.fromCanonicalForm("offer"),
                            data.getString("sdp")
                    );
                    //设置远端setRemoteDescription
                    pc.getPc().setRemoteDescription(pc,sdp);
                    //设置answer
                    pc.getPc().createAnswer(pc,sdpMediaConstraints);
                }

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
            Log.d(TAG, "answerListener:" + data);
            if(peers == null){
                Log.d(TAG,"answerListener, peers is null ,return ");
                return ;
            }
            try {
                //获取id
                String fromId = data.getString("from");
                //获取peer
                Peer pc = getOrCreateRtcConnect(fromId);
                if(pc != null){
                    //构建RTCSessionDescription参数
                    SessionDescription sdp = new SessionDescription(
                            SessionDescription.Type.fromCanonicalForm("answer"),
                            data.getString("sdp")
                    );
                    //设置远端setRemoteDescription
                    pc.getPc().setRemoteDescription(pc,sdp);
                }

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
            Log.d(TAG, "candidateListener:" + data);
            if(peers == null){
                Log.d(TAG,"candidateListener, peers is null ,return ");
                return;
            }
            try {
                //获取id
                String fromId = data.getString("from");
                //获取peer
                Peer pc = getOrCreateRtcConnect(fromId);
                if(pc != null){
                    //获取candidate
                    JSONObject candidate = data.getJSONObject("candidate");
                    IceCandidate iceCandidate = new IceCandidate(
                            candidate.getString("sdpMid"), //描述协议id
                            candidate.getInt("sdpMLineIndex"),//描述协议的行索引
                            candidate.getString("sdp")//描述协议
                    );

                    //添加远端设备路由描述
                    pc.getPc().addIceCandidate(iceCandidate);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener exitNotifyListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d(TAG, "exitNotifyListener:" + data);
            try {
                //获取id
                String fromId = data.getString("from");
                if(peers != null){
                    //判断是否为当前连接
                    Peer pc = peers.get(fromId);
                    if (pc != null){
                        //peer关闭
                        pc.getPc().close();
                        //删除peer对象
                        if(peers != null){
                            peers.remove(fromId);
                        }


                    }
                }
                //通知UI界面移除video
                if(!isActivityDestroy((ChatActivity2)rtcListener)){
                    rtcListener.onRemoveRemoteStream(fromId);
                }
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
            Log.d(TAG, "exitListener:" + data);
            int disconnect = 0;
            try {
                //获取id
                String fromId = data.getString("from");
                disconnect = data.getInt("disconnect");
                if(peers != null){
                    //判断是否为当前连接
                    Peer pc = peers.get(fromId);
                    if (pc != null){
                        //peer关闭
                        pc.getPc().close();
                        //删除peer对象
                        peers.remove(fromId);

                    }
                }
                //通知UI界面移除video
                if(!isActivityDestroy((ChatActivity2)rtcListener)){
                    rtcListener.onRemoveRemoteStream(fromId);
                }
                if(disconnect == 1){
                    OfflineMsg offLineMsg = new OfflineMsg();
                    offLineMsg.from = fromId;
                    EventBus.getDefault().post(offLineMsg);
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    };

    /** 信令服务器发送消息 **/
    public void sendMessage(String event,JSONObject message){
        if(client != null){
            client.emit(event, message);
        }

    }

    public boolean isConnected(){
        if(client != null ){
            return client.connected();
        }
        return false;
    }




    private   void heartBeating() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        Thread.sleep(1000*60);
                        sendMessage("heart",null);
                        Log.d(TAG,getSocketId()+" heart");
                    } catch (InterruptedException e) {
                        //
                    }
                }
            }
        };
        Thread t = new Thread(r,"heartbeat");
        t.start();
    }


    public  String getMD5(File file) {
        FileInputStream fileInputStream = null;
        try {
            MessageDigest MD5 = MessageDigest.getInstance("MD5");
            fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[8192];
            int length;
            while ((length = fileInputStream.read(buffer)) != -1) {
                MD5.update(buffer, 0, length);
            }
            return new String(Hex.encodeHex(MD5.digest()));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (fileInputStream != null){
                    fileInputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
