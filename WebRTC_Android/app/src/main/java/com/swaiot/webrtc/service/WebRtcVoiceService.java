package com.swaiot.webrtc.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.swaiot.webrtc.config.Constant;
import com.swaiot.webrtc.entity.Model;
import com.swaiot.webrtc.entity.SSEEvent;
import com.swaiot.webrtc.observer.PeerConnObserverImpl;
import com.swaiot.webrtc.observer.SdpObserverImpl;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.webrtc.AudioTrack;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoTrack;
import org.webrtc.audio.AudioDeviceModule;
import org.webrtc.audio.JavaAudioDeviceModule;
import org.webrtc.voiceengine.WebRtcAudioManager;
import org.webrtc.voiceengine.WebRtcAudioRecord;
import org.webrtc.voiceengine.WebRtcAudioTrack;
import org.webrtc.voiceengine.WebRtcAudioUtils;

import java.util.List;

import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.ss.session.Session;

public class WebRtcVoiceService extends Service {

    private static final String TAG = WebRtcVoiceService.class.getSimpleName();

    private PeerConnection localPeer;
    private EglBase rootEglBase;
    private PeerConnectionFactory peerConnectionFactory;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onIceConnectionChange onCreate");

        rootEglBase = EglBase.create(); //需要java8
        startPeerConnection();
        configPeerConnection();

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onIceConnectionChange onStartCommand");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onIceConnectionChange onDestroy");
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
        public void onAddStream(MediaStream mediaStream) {
            super.onAddStream(mediaStream);
            Log.d(TAG, "Received Remote stream");

            final List<VideoTrack> videoTracks = mediaStream.videoTracks;
            final List<AudioTrack> audioTracks = mediaStream.audioTracks;

            final int videoSize = videoTracks.size();
            final int audioSize = audioTracks.size();
            Log.d(TAG, "gotRemoteStream videoTracks size=" + videoSize);
            Log.d(TAG, "gotRemoteStream audioTracks size=" + audioSize);

            if (audioSize > 0) {
                audioTracks.get(0).setEnabled(true);
            }

        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState state) {
            super.onIceConnectionChange(state);
            if (state == PeerConnection.IceConnectionState.CLOSED
                    || state == PeerConnection.IceConnectionState.DISCONNECTED) {
                Log.e(TAG, "onIceConnectionChange 对方已经退出屏幕镜像");
            } else if (state == PeerConnection.IceConnectionState.CONNECTED) {
                Log.e(TAG, "onIceConnectionChange 屏幕镜像CONNECTED");
            } else if (state == PeerConnection.IceConnectionState.FAILED) {
                Log.e(TAG, "onIceConnectionChange 本地连接建立失败，请检查是否在同一局域网");
            }
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


    private SSChannel mSSChannel;
    private Session target;
    private String sourceClient;
    private String targetClient;

    //黏性事件 发送了该事件之后再订阅者依然能够接收到的事件
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEvent(final SSEEvent event) {
        String type = event.getMsgType();
        Model model = event.getModel();
        Log.e(TAG, "onIceConnectionChange onEvent type=" + type);
        switch (type) {
            case Constant.OFFER: {
                mSSChannel = event.getSsChannel();
                target = event.getImMessage().getSource();
                sourceClient = event.getImMessage().getClientTarget();
                targetClient = event.getImMessage().getClientSource();

                final SessionDescription sdp = model.getPayload().getSdp();
                createOfferImpl(sdp);

                Log.d(TAG, "EventBus Received offer sdp:\n" + sdp.description);
                Log.d(TAG, "EventBus Received offer type:" + sdp.type.toString());
            }
            break;
            case Constant.ANSWER: {
                SessionDescription sdp = model.getPayload().getSdp();
                Log.d(TAG, "EventBus Received answer sdp:\n" + sdp.description);
                Log.d(TAG, "EventBus Received answer type:" + sdp.type.toString());
            }
            break;
            case Constant.CANDIDATE: {
                //服务端 发送 接收方sdpAnswer
                IceCandidate iceCandidate = model.getPayload().getIceCandidate();
                if (iceCandidate != null) {
                    Log.d(TAG, "EventBus Received iceCandidate sdpMid=" + iceCandidate.sdpMid);
                    Log.d(TAG, "EventBus Received iceCandidate sdpMLineIndex=" + iceCandidate.sdpMLineIndex);
                    Log.d(TAG, "EventBus Received iceCandidate sdp=" + iceCandidate.sdp);
                    localPeer.addIceCandidate(iceCandidate);
                }
            }
            break;
        }
    }


    public void createOfferImpl(SessionDescription sdp) {
        localPeer.setRemoteDescription(sdpObserverImpl, sdp);
        localPeer.createAnswer(sdpObserverImpl, new MediaConstraints());
    }

    /**
     * 呼叫
     *
     * @param sdpDescription 会话状态描述
     */
    private void sendOffer(SessionDescription sdpDescription) {
        Model.PayLoad payLoad = new Model.PayLoad();
        payLoad.setSdp(sdpDescription);

        Model model = new Model();
        model.setType(Constant.OFFER);

        model.setPayload(payLoad);

        String text = model.toJson(false);

        Log.d(TAG, "sendOffer : " + text);
        sendData(text);
    }

    /**
     * 应答
     *
     * @param sdpDescription 会话状态描述
     */
    private void sendAnswer(SessionDescription sdpDescription) {
        Log.e(TAG, "sendAnswer sdp:\n" + sdpDescription.description);

        Model.PayLoad payLoad = new Model.PayLoad();
        payLoad.setSdp(sdpDescription);

        Model model = new Model();

        model.setType(Constant.ANSWER);
        model.setPayload(payLoad);

        String text = model.toJson(false);

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


    private void sendData(String content) {
        if (mSSChannel != null) {
            try {
                Session mySession = mSSChannel.getSessionManager().getMySession();
                if (mySession != null
                        && target != null
                        && !TextUtils.isEmpty(sourceClient)
                        && !TextUtils.isEmpty(targetClient)) {
                    IMMessage message = IMMessage.Builder.createTextMessage(mySession, target,
                            sourceClient, targetClient, content);
                    message.putExtra(SSChannel.FORCE_SSE, "true");//强制云端
                    mSSChannel.getIMChannel().send(message);
                    Log.d(TAG, "send Data by sse message=" + message.encode());
                    //Log.d(TAG, "send Data by sse content=" + content);
                } else {
                    Log.e(TAG, "send Data fail by targetSession is null");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "SSChannel Open Fail!!!");
        }
    }


    private void startPeerConnection() {
        PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions.builder(getApplicationContext())
                        .setFieldTrials("WebRTC-SupportVP9SVC/") //试用特性
                        .setEnableInternalTracer(true)
                        .createInitializationOptions());
        /* .............. create and initialize PeerConnectionFactory .........*/
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();

        VideoEncoderFactory encoderFactory = new DefaultVideoEncoderFactory(rootEglBase.getEglBaseContext(),
                false, false);
        VideoDecoderFactory decoderFactory = new DefaultVideoDecoderFactory(rootEglBase.getEglBaseContext());

        //AudioDeviceModule adm = createLegacyAudioDevice();
        AudioDeviceModule adm = createJavaAudioDevice();
        peerConnectionFactory = PeerConnectionFactory.builder()
                .setOptions(options)
                .setAudioDeviceModule(adm)
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory)
                .createPeerConnectionFactory();
        adm.release();
        Log.d(TAG, "Peer connection factory created.");
    }

    /**
     * create localPeer
     */
    private void configPeerConnection() {
        PeerConnection.RTCConfiguration configuration = new PeerConnection.RTCConfiguration(Constant.getICEServers(this));
        configuration.iceTransportsType = PeerConnection.IceTransportsType.ALL;
        configuration.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED;
        configuration.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE;
        configuration.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.NEGOTIATE;
        configuration.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;
        localPeer = peerConnectionFactory.createPeerConnection(configuration, peerConnObserver);
        Log.d(TAG, "configPeerConnection init success...");
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

        return JavaAudioDeviceModule.builder(this)
                //.setSamplesReadyCallback(saveRecordedAudioToFile)
                .setUseHardwareAcousticEchoCanceler(false)//使用硬件回声消除
                .setUseHardwareNoiseSuppressor(false) //使用硬件噪音抑制器
                .setAudioRecordErrorCallback(audioRecordErrorCallback)
                .setAudioTrackErrorCallback(audioTrackErrorCallback)
                .setAudioRecordStateCallback(audioRecordStateCallback)
                .setAudioTrackStateCallback(audioTrackStateCallback)
                .createAudioDeviceModule();
    }

}
