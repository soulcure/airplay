package com.swaiot.webrtc.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.google.gson.Gson;
import com.swaiot.webrtc.R;
import com.swaiot.webrtc.StackAct;
import com.swaiot.webrtc.config.Constant;
import com.swaiot.webrtc.entity.FileDescription;
import com.swaiot.webrtc.entity.Model;
import com.swaiot.webrtc.entity.SSEEvent;
import com.swaiot.webrtc.observer.DateChannelObserverImpl;
import com.swaiot.webrtc.observer.PeerConnObserverImpl;
import com.swaiot.webrtc.observer.SdpObserverImpl;
import com.swaiot.webrtc.util.AppUtils;
import com.swaiot.webrtc.util.Constants;
import com.swaiot.webrtc.util.IToast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioTrack;
import org.webrtc.DataChannel;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoFrame;
import org.webrtc.VideoSink;
import org.webrtc.VideoTrack;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.ss.session.Session;
import swaiotos.channel.iot.utils.ThreadManager;

public class WebRTCActivity extends Activity {
    private static final String TAG = "webrtc";

    private final static int MY_PERMISSIONS_REQUEST_CAMERA = 100;
    private final static int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 101;
    private final static int MY_PERMISSIONS_REQUEST = 102;

    private static final String SOURCE_CLIENT = "com.coocaa.webrtc.airplay";
    private static final String TARGET_CLIENT = "ss-clientKey-runtime-h5-channel";

    private Context mContext;
    private View tv_info;

    private PeerConnectionFactory peerConnectionFactory;
    private SurfaceViewRenderer remoteVideoView;
    private ProxyVideoSink remoteVideoSink;
    private RelativeLayout loadingLayout;
    private LottieAnimationView lottieLikeanim;

    private DataChannel dataChannel;
    private PeerConnection localPeer;
    private EglBase rootEglBase;

    private SSChannel mSSChannel;
    private Session targetSession;
    private String targetClient = TARGET_CLIENT;
    private boolean isWeb = true;
    private boolean isUserExitAppFlag = false;
    private boolean isWebExitAppFlag = false;
    private boolean isWebRtcConnectFailure = false;
    private boolean isStop = false;

    private long outfileCurPos;
    private FileChannel outfileChannel;
    private boolean isFirstSink;

    public static void start(Context context) {
        Intent intent = new Intent(context, WebRTCActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void start(Context context,boolean isWeb) {
        Intent intent = new Intent(context, WebRTCActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle bundle = new Bundle();
        bundle.putBoolean(Constants.COOCAA_IS_WEB,isWeb);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    private final SdpObserverImpl sdpObserverImpl = new SdpObserverImpl() {
        @Override
        public void onCreateSuccess(SessionDescription sessionDescription) {
            //??????????????????????????????
            localPeer.setLocalDescription(this, sessionDescription);
            SessionDescription localDescription = localPeer.getLocalDescription();
            SessionDescription.Type type = localDescription.type;
            Log.d(TAG, "onCreateSuccess == " + " type == " + type);

            //????????????????????????WebSocket?????????offer??????????????????
            if (type == SessionDescription.Type.OFFER) {
                //??????
                sendOffer(sessionDescription);
            } else if (type == SessionDescription.Type.ANSWER) {
                //??????
                sendAnswer(sessionDescription);
            }
        }
    };


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
            gotRemoteStream(mediaStream);
        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState state) {
            super.onIceConnectionChange(state);
            if (state == PeerConnection.IceConnectionState.CLOSED
                    || state == PeerConnection.IceConnectionState.DISCONNECTED) {
                runOnUiThread(() -> {
                    if (!isStop) {
                        isWebExitAppFlag = true;
                        IToast.showLong(mContext, "??????????????????????????????");
                        finish();
                    }

                });
            } else if (state == PeerConnection.IceConnectionState.CONNECTED) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("code", 0);
                    json.put("type", "SIGNALING_NOTIFY");
                    json.put("message", "??????????????????");
                    String text = json.toString();
                    sendData(text);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        lottieLikeanim.cancelAnimation();
//                        loadingLayout.setVisibility(View.INVISIBLE);
                    }
                });

            } else if (state == PeerConnection.IceConnectionState.FAILED) {
                try {
//                    if (!isWebExitAppFlag) {
//                        JSONObject json = new JSONObject();
//                        json.put("code", -1);
//                        json.put("type", "SIGNALING_NOTIFY");
//                        json.put("message", "????????????????????????");
//                        String text = json.toString();
//                        sendData(text);
//                        isWebRtcConnectFailure = true;
//                    }

                    runOnUiThread(() -> {
                        boolean isStart = AppUtils.isTopActivity(mContext, WebRTCActivity.class.getName());
                        if (isStart) {
                            isWebRtcConnectFailure = true;
//                            Toast.makeText(mContext, "???????????????????????????????????????????????????????????????", Toast.LENGTH_LONG).show();
                            IToast.showLong(mContext, "???????????????????????????????????????????????????????????????");
                            finish();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_rtc);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mContext = this;
        askForPermissions();
        initViewAndVideos();
        startPeerConnection();
        configPeerConnection();

        initDataChannel();
        isWebExitAppFlag = false;
        isStop = false;
        isFirstSink = true;
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            isWeb = bundle.getBoolean(Constants.COOCAA_IS_WEB);
        }
        Log.d("wang1", "-------------onCreate--------isWeb:"+isWeb);
        startReport();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("wang1", "-------------onNewIntent--------");
    }

    private void initDataChannel() {
        DataChannel.Init dcInit = new DataChannel.Init();
        dcInit.id = 1;
        dataChannel = localPeer.createDataChannel(Constant.CHANNEL, dcInit);
    }


    public void sendChannelData(final String data) {
        ByteBuffer buffer = ByteBuffer.wrap(data.getBytes());
        dataChannel.send(new DataChannel.Buffer(buffer, false));
    }

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
                    if (fileDescription.isCheckFile()) { //?????????????????????
                        answerCheckSend(fileDescription);
                    } else if (fileDescription.isStart()) { //?????????????????????
                        try {
                            File file = fileDescription.getDownLoadFile();
                            outfileChannel = new FileOutputStream(file).getChannel();
                            outfileCurPos = 0;
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    } else if (fileDescription.isEnd()) { //?????????????????????
                        if (outfileCurPos == fileDescription.getFileSize()) {
                            try {
                                outfileChannel.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            outfileChannel = null;
                            outfileCurPos = 0;

                            File file = fileDescription.getDownLoadFile();
                            if (AppUtils.checkFileMd5(file, fileDescription.getMd5())) {
                                //done
                                Log.d(TAG, "DataChannel receive file finish");
                            }
                        }
                    }

                }

            }

        }
    }


    //???????????? ??????????????????????????????????????????????????????????????????
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEvent(final SSEEvent event) {
        String type = event.getMsgType();
        Model model = event.getModel();
        Map<String, String> extras = event.getExtras();
        if (extras != null) {
            Log.d(TAG, "extras size=" + extras.size());
            String target_client = extras.get("target-client");
            if (!TextUtils.isEmpty(target_client)) {
                targetClient = target_client;
                Log.d(TAG, "targetClient= " + target_client);
            }
        }

        Log.d(TAG, "onEvent type= " + type);
        switch (type) {
            case Constant.OFFER: {
                mSSChannel = event.getSsChannel();
                SessionDescription sdp = model.getPayload().getSdp();
                String targetSid = event.getTargetSid();
                StackAct.instance().addActivity(targetSid, this);
                //????????????offer sdp
                Log.d(TAG, "EventBus Received offer sdp:\n" + sdp.description);
                Log.d(TAG, "EventBus Received offer type:" + sdp.type.toString());
                Log.d(TAG, "EventBus Received offer from Sid:" + targetSid);
                if (!TextUtils.isEmpty(targetSid)) {
                    targetSession = new Session();
                    targetSession.setId(targetSid);
                    localPeer.setRemoteDescription(sdpObserverImpl, sdp);
                    localPeer.createAnswer(sdpObserverImpl, new MediaConstraints());
                } else {
                    IToast.showLong(this, "????????????sid");
                    finish();
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
                //????????? ?????? ?????????sdpAnswer
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
        if (mSSChannel != null) {
            try {
                Session mySession = mSSChannel.getSessionManager().getMySession();
                if (targetSession != null) {
                    IMMessage message = IMMessage.Builder.createTextMessage(mySession, targetSession,
                            SOURCE_CLIENT, targetClient, content);
                    message.putExtra(SSChannel.FORCE_SSE, "true");//????????????

                    mSSChannel.getIMChannel().send(message);
                    Log.d(TAG, "send Data by sse content=" + content);
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


    /**
     * ??????
     *
     * @param sdpDescription ??????????????????
     */
    private void sendOffer(SessionDescription sdpDescription) {
        Model.PayLoad payLoad = new Model.PayLoad();
        payLoad.setSdp(sdpDescription);

        Model model = new Model();
        model.setType(Constant.OFFER);

        model.setPayload(payLoad);

        String text = model.toJson(isWeb);

        Log.d(TAG, "sendOffer : " + text);
        sendData(text);
    }

    /**
     * ??????
     *
     * @param sdpDescription ??????????????????
     */
    private void sendAnswer(SessionDescription sdpDescription) {
        Log.e(TAG, "sendAnswer sdp:\n" + sdpDescription.description);

        Model.PayLoad payLoad = new Model.PayLoad();
        payLoad.setSdp(sdpDescription);

        Model model = new Model();

        model.setType(Constant.ANSWER);
        model.setPayload(payLoad);

        String text = model.toJson(isWeb);

        Log.d(TAG, "sendAnswer : " + text);
        sendData(text);
    }


    /**
     * ?????? IceCandidate
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


    private void askForPermissions() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_REQUEST);
        } else if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_CAMERA);
        }
    }

    private void initViewAndVideos() {
        tv_info = findViewById(R.id.tv_info);
        loadingLayout = findViewById(R.id.web_loading_layout);
        lottieLikeanim = findViewById(R.id.lottie_likeanim);
        remoteVideoView = findViewById(R.id.remote_gl_surface_view);
        rootEglBase = EglBase.create(); //??????java8
        remoteVideoView.init(rootEglBase.getEglBaseContext(), null);
        remoteVideoView.setZOrderMediaOverlay(true);

        remoteVideoView.setEnableHardwareScaler(true);
        remoteVideoView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
    }

    private void startPeerConnection() {
        PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions.builder(this)
                        .setFieldTrials("WebRTC-SupportVP9SVC/") //????????????
                        .setEnableInternalTracer(true)
                        .createInitializationOptions());
        /* .............. create and initialize PeerConnectionFactory .........*/
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();

        VideoEncoderFactory encoderFactory = new DefaultVideoEncoderFactory(rootEglBase.getEglBaseContext(),
                false, false);
        VideoDecoderFactory decoderFactory = new DefaultVideoDecoderFactory(rootEglBase.getEglBaseContext());

        peerConnectionFactory = PeerConnectionFactory.builder()
                .setOptions(options)
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory)
                .createPeerConnectionFactory();
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


    private void gotRemoteStream(MediaStream stream) {
        final List<VideoTrack> videoTracks = stream.videoTracks;
        final List<AudioTrack> audioTracks = stream.audioTracks;

        final int videoSize = videoTracks.size();
        final int audioSize = audioTracks.size();
        Log.d(TAG, "gotRemoteStream videoTracks size=" + videoSize);
        Log.d(TAG, "gotRemoteStream audioTracks size=" + audioSize);
        runOnUiThread(() -> {
            if (videoSize > 0) {
                remoteVideoSink = new ProxyVideoSink();
                remoteVideoSink.setTarget(remoteVideoView);
                videoTracks.get(0).addSink(remoteVideoSink);
            }
            if (audioSize > 0) {
                audioTracks.get(0).setEnabled(true);
            }
        });

    }

    private void startReport() {
        String id;
        if (isWeb) {
            id = getPackageName() + "$" + "airplay";
        } else {
            id = getPackageName() + "$" + "airplay_mobile";
        }
        Log.d(TAG,"startReport--ID:"+id);
        businessStateReport(id, "{}");
    }


    private void businessStateReport(String id, String values) {
        Intent intent = new Intent();
        intent.setAction("coocaa.intent.action.BusinessStateReportService");
        intent.setPackage("swaiotos.channel.iot");
        intent.putExtra("id", id);
        intent.putExtra("values", values);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }


    private void exitProtocol() {

        try {
            JSONObject json = new JSONObject();
            json.put("code", isUserExitAppFlag ? 1 : 2);
            json.put("type", "SIGNALING_NOTIFY");
            json.put("message", "????????????????????????");
            String text = json.toString();
            sendData(text);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    /**
     * ??????????????????????????????
     */
    private long lastBackPressTime = -1L;


    @Override
    public void onBackPressed() {
        long currentTIme = System.currentTimeMillis();
        Log.d("wang1", "----------onBackPressed----------------");
        if (lastBackPressTime == -1L || currentTIme - lastBackPressTime >= 2000) {
            // ??????????????????
            showBackPressTip();
            // ????????????
            lastBackPressTime = currentTIme;
        } else {
            isUserExitAppFlag = true;
            //????????????
            finish();
        }

    }


    private void showBackPressTip() {
        IToast.showLong(this, "??????????????????????????????");
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("wang1", "----------onPause----------------");
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            Log.d("wang1", "----------onStop----------------");
            //??????eventbus
            unRegisterEventBus();
            //????????????
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("cmd", "exit");
            businessStateReport("", new Gson().toJson(hashMap));

            isStop = true;
            if (localPeer != null) {
                localPeer.close();
                localPeer.dispose();
                localPeer = null;
            }

            if (dataChannel != null) {
                dataChannel.dispose();
                dataChannel = null;
            }

            if (peerConnectionFactory != null) {
                peerConnectionFactory.dispose();
                peerConnectionFactory = null;
            }

            // disconnect ??? close ????????????
            if (isWebExitAppFlag) {
                isWebExitAppFlag = false;
                return;
            }

            //failure ??????  ?????????
            if (isWebRtcConnectFailure) {
                isWebRtcConnectFailure = false;
                return;
            }

            exitProtocol();
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void unRegisterEventBus() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //????????????????????????
        Log.d(TAG, "startReport exitBusiness");
        Log.d("wang1", "----------onDestroy----------------");
    }

    private int frameWidth;
    private int frameHeight;

    private class ProxyVideoSink implements VideoSink {
        private VideoSink target;

        @Override
        synchronized public void onFrame(VideoFrame frame) {
            if (target == null) {
                Log.d(TAG, "Dropping frame in proxy because target is null.");
                return;
            }
            int width = frame.getRotatedWidth();
            int height = frame.getRotatedHeight();
            Log.d(TAG, "onFrame width=" + width);
            Log.d(TAG, "onFrame height=" + height);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isFirstSink) {
                        isFirstSink = false;
                        remoteVideoView.setVisibility(View.VISIBLE);
                        loadingLayout.setVisibility(View.INVISIBLE);
                    }
                }
            });

            if (width <= 2 || height <= 2) {
                if (frameWidth != width && frameHeight != height) {
                    runOnUiThread(() -> {
                        Log.d(TAG, "onFrame ?????????????????????");

                        if (remoteVideoView.getVisibility() == View.VISIBLE) {
                            remoteVideoView.setVisibility(View.GONE);
                        }

                        if (tv_info.getVisibility() == View.GONE) {
                            tv_info.setVisibility(View.VISIBLE);
                        }

                        frameWidth = width;
                        frameHeight = height;
                    });
                }
            } else {
                if (frameWidth != width && frameHeight != height) {
                    runOnUiThread(() -> {
                        Log.d(TAG, "onFrame ??????????????????");

                        if (remoteVideoView.getVisibility() == View.GONE) {
                            remoteVideoView.setVisibility(View.VISIBLE);
                        }

                        if (tv_info.getVisibility() == View.VISIBLE) {
                            tv_info.setVisibility(View.GONE);
                        }

                        frameWidth = width;
                        frameHeight = height;
                    });
                }
            }

            target.onFrame(frame);
        }

        synchronized public void setTarget(VideoSink target) {
            this.target = target;
        }
    }
}