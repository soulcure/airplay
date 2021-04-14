package com.swaiot.webrtc.config;

import android.content.Context;
import android.util.Log;

import com.swaiot.webrtc.util.AppUtils;

import org.webrtc.PeerConnection;

import java.util.ArrayList;
import java.util.List;

public class Constant {
    public static final String URL = "wss://www.kuiper.fun:3000";//server服务器地址

    public static final String CHANNEL = "channel";

    public static final String OFFER = "SIGNALING_OFFER";//拨打
    public static final String ANSWER = "SIGNALING_ANSWER";//接听
    public static final String CANDIDATE = "SIGNALING_CANDIDATE";//ice互传
    public static final String SIGNALING_GET_NET_INFO = "SIGNALING_GET_NETINFO";

    private static final String STUN_KEY = "com.swaiot.webrtc.stun";
    private static final String TURN_KEY = "com.swaiot.webrtc.turn";

    public static List<PeerConnection.IceServer> getICEServers(Context context) {
        List<PeerConnection.IceServer> iceServers = new ArrayList<>();

        String stun = AppUtils.getMetaData(context, STUN_KEY);
        String turn = AppUtils.getMetaData(context, TURN_KEY);

        PeerConnection.IceServer stunServer = PeerConnection.IceServer.builder(stun)
                .createIceServer();

        PeerConnection.IceServer turnServer = PeerConnection.IceServer.builder(turn)
                .setUsername("skyiot")
                .setPassword("skyworth.adb.123")
                .createIceServer();

        if (stun.contains("beta")) {  //for beta test
            //iceServers.add(stunServer);
            iceServers.add(turnServer);
            Log.d("yao", "beta getICEServers=" + turnServer.toString());
        } else { // online
            iceServers.add(stunServer);
            //iceServers.add(turnServer);
            Log.d("yao", "online getICEServers=" + stunServer.toString());
        }
        return iceServers;
    }
}
