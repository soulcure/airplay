package swaiotos.channel.iot.webrtc;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.SSContext;
import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.ss.channel.stream.IStreamChannel;
import swaiotos.channel.iot.utils.AppUtils;
import swaiotos.channel.iot.utils.DeviceUtil;
import swaiotos.channel.iot.utils.SpeedTest;
import swaiotos.channel.iot.utils.WifiAccount;
import swaiotos.channel.iot.webrtc.config.Constant;
import swaiotos.channel.iot.webrtc.entity.Model;
import swaiotos.channel.iot.webrtc.entity.SSEEvent;

public class DataChannelServer {
    private static final String TAG = "DataChannelServer";

    private PeerConnectionFactory peerConnectionFactory;
    private final SSContext mSSContext;
    private final ConcurrentHashMap<String, Peer> peers;
    private PeerConnection.RTCConfiguration rtcConfig;

    private IStreamChannel.Receiver mReceiver;

    public interface ConnectCallBack {
        void onConnect(int code, String sid);

        void onError(int code, String sid);
    }


    public DataChannelServer(SSContext ssContext, IStreamChannel.Receiver receiver) {
        mSSContext = ssContext;
        mReceiver = receiver;
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        peers = new ConcurrentHashMap<>();
        initPeerConnectionFactory(ssContext.getContext());
        initRtcConfig();
        Log.d(TAG, "DataChannelServer created...");
    }

    private void initPeerConnectionFactory(Context context) {
        EglBase rootEglBase = EglBase.create(); //需要java8
        PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions.builder(context)
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

    //创建RTCConfiguration参数
    private void initRtcConfig() {
        rtcConfig = new PeerConnection.RTCConfiguration(Constant.getICEServers());
        // TCP candidates are only useful when connecting to a server that supports
        // ICE-TCP.
        rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED;
        rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE;
        rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE;
        rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;
    }


    public void close() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    //黏性事件 发送了该事件之后再订阅者依然能够接收到的事件
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(final SSEEvent event) {
        String type = event.getMsgType();
        Model model = event.getModel();

        Log.d(TAG, "DataChannelServer onEvent type= " + type);
        switch (type) {
            case Constant.OFFER: {
                SessionDescription sdp = model.getPayload().getSdp();
                IMMessage message = event.getImMessage();
                String ip = message.getSource().getExtra(SSChannel.STREAM_LOCAL);
                String ssid = model.getSSid();

                //收到对方offer sdp
                Log.d(TAG, "EventBus Received offer sdp:\n" + sdp.description);
                Log.d(TAG, "EventBus Received offer type:" + sdp.type.toString());

                String netType = DeviceUtil.getNetworkType(mSSContext.getContext());
                if (netType.equals("WIFI")) {
                    String wifiSSID = AppUtils.getWifiInfoSSID(mSSContext.getContext());
                    Log.d(TAG, "my wifiSSID OFFER is =" + wifiSSID);

                    Peer connectPC = getPeerConnect(ssid);
                    if (connectPC != null) {
                        if (connectPC.isOpen()) {
                            return;
                        } else {
                            removePeer(ssid);
                            connectPC.close();
                        }
                    }
                    if (wifiSSID.equals(ssid)) {
                        Log.d(TAG, "WIFI OFFER is same wifi");
                        Peer pc = getOrCreateRtcConnect(message);
                        pc.getPc().setRemoteDescription(pc, sdp);
                        //设置answer
                        pc.getPc().createAnswer(pc, new MediaConstraints());
                    } else {
                        Log.e(TAG, "WIFI OFFER is not same wifi");
                    }
                } else if (netType.equals("ETHERNET")) {
                    List<String> list = WifiAccount.getSSIDConnectHistoryList(mSSContext.getContext());
                    if (!TextUtils.isEmpty(ssid) && list.contains(ssid)) {
                        Log.d(TAG, "ETHERNET OFFER is same wifi");
                        Peer pc = getOrCreateRtcConnect(message);
                        pc.getPc().setRemoteDescription(pc, sdp);
                        //设置answer
                        pc.getPc().createAnswer(pc, new MediaConstraints());
                    } else {
                        Log.e(TAG, "ETHERNET OFFER is not same wifi");
                    }

                    //pingTarget(ip, sdp, message);
                }
            }
            break;
            case Constant.CANDIDATE: {
                IMMessage message = event.getImMessage();
                //服务端 发送 接收方sdpAnswer
                IceCandidate iceCandidate = model.getPayload().getIceCandidate();
                if (iceCandidate != null) {
                    Log.d(TAG, "EventBus Received iceCandidate sdpMid=" + iceCandidate.sdpMid);
                    Log.d(TAG, "EventBus Received iceCandidate sdpMLineIndex=" + iceCandidate.sdpMLineIndex);
                    Log.d(TAG, "EventBus Received iceCandidate sdp=" + iceCandidate.sdp);
                    Peer pc = getOrCreateRtcConnect(message);
                    //添加远端设备路由描述
                    pc.getPc().addIceCandidate(iceCandidate);
                }
            }
            break;
        }
    }


    public List<String> getPeerSidList() {
        synchronized (DataChannelServer.this) {
            List<String> list = new ArrayList<>();
            for (Map.Entry<String, Peer> entry : peers.entrySet()) {
                String key = entry.getKey();
                list.add(key);
            }
            return list;
        }
    }


    public List<Peer> getPeerList() {
        List<Peer> list = new ArrayList<>();
        for (Map.Entry<String, Peer> entry : peers.entrySet()) {
            Peer value = entry.getValue();
            list.add(value);
        }
        return list;
    }

    public void removePeer(String sid) {
        synchronized (DataChannelServer.this) {
            peers.remove(sid);
        }
    }


    public Peer getPeerConnect(String sid) {
        synchronized (DataChannelServer.this) {
            return peers.get(sid);
        }
    }


    private Peer getOrCreateRtcConnect(IMMessage message) {
        synchronized (DataChannelServer.this) {
            String sid = message.getSource().getId();
            Log.d(TAG, "getOrCreateRtcConnect by sid:" + sid);
            Peer pc = peers.get(sid);
            if (pc == null) {
                Log.d(TAG, "getOrCreateRtcConnect new pc");
                //构建RTCPeerConnection PeerConnection相关回调进入Peer中
                ConnectCallBack connectCallBack = new ConnectCallBack() {
                    @Override
                    public void onConnect(int code, String sid) {
                        if (code == 0) {
                            Log.d(TAG, "onConnect...");
                        }
                    }

                    @Override
                    public void onError(int code, String sid) {
                        Peer peer = peers.get(sid);
                        if (peer != null) {
                            peer.close();
                        }
                        peers.remove(sid);
                    }
                };
                pc = new Peer(sid, peerConnectionFactory, rtcConfig, mSSContext,
                        mReceiver, message, connectCallBack);
                peers.put(sid, pc);
            }
            return pc;
        }

    }


    private void pingTarget(String ip, final SessionDescription sdp, final IMMessage imMessage) {
        SpeedTest.ConnectCallback callback = new SpeedTest.ConnectCallback() {
            @Override
            public void onFinished(float speed, String unit) {

            }

            @Override
            public void onProgress(float rate, String unit) {

            }

            @Override
            public void onResult(int code, String message) {
                Log.e(TAG, "SpeedTest result onFail code : " + code + "  info=" + message);
                if (code >= 0) {
                    Peer pc = getOrCreateRtcConnect(imMessage);
                    pc.getPc().setRemoteDescription(pc, sdp);
                    //设置answer
                    pc.getPc().createAnswer(pc, new MediaConstraints());
                }
            }

            @Override
            public void lossRate(String rate) {

            }
        };
        SpeedTest speedTest = new SpeedTest(ip, 2, 1, callback);
        speedTest.open();
    }

}
