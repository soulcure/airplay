package swaiotos.channel.iot.webrtc.config;

import org.webrtc.PeerConnection;

import java.util.ArrayList;
import java.util.List;

public class Constant {
    public static final String STUN = "stun:atum.skyworthiot.com";//stun server服务器地址
    public static final String TURN = "turn:turn-test.skyworthiot.com";//turn server服务器地址

    public static final String CHANNEL = "channel";

    public static final String OFFER = "SIGNALING_OFFER";//拨打
    public static final String ANSWER = "SIGNALING_ANSWER";//接听
    public static final String CANDIDATE = "SIGNALING_CANDIDATE";//ice互传


    public static List<PeerConnection.IceServer> getICEServers() {
        List<PeerConnection.IceServer> iceServers = new ArrayList<>();
        PeerConnection.IceServer stunServer = PeerConnection.IceServer.builder(STUN)
                .createIceServer();

        PeerConnection.IceServer turnServer = PeerConnection.IceServer.builder(TURN)
                .setUsername("user")
                .setPassword("passpass")
                .createIceServer();

        iceServers.add(stunServer);
        //iceServers.add(turnServer);
        return iceServers;
    }

}
