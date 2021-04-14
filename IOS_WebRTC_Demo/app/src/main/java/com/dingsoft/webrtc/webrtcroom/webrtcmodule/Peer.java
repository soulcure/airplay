package com.dingsoft.webrtc.webrtcroom.webrtcmodule;

import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.MediaStreamTrack;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpReceiver;
import org.webrtc.RtpTransceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.VideoTrack;

/**
 * PeerConnection通道封装，包括PeerConnection创建及状态回调
 * Created by chengshaobo on 2018/10/23.
 */

public class Peer implements SdpObserver, PeerConnection.Observer {
    //日志Tag
    private final static String TAG = "yao";

    //PeerConnection对象
    private PeerConnection pc;
    //PeerConnection标识
    private String id;
    //webRtClient对象
    private WebRtcClient webRtcClient;



    //构造函数
    public Peer(String id,
                PeerConnectionFactory factory,
                PeerConnection.RTCConfiguration rtcConfig,
                WebRtcClient webRtcClient) {
        Log.d(TAG, "new Peer: " + id);
        this.pc = factory.createPeerConnection(rtcConfig, this);
        this.id = id;
        this.webRtcClient = webRtcClient;


        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    public PeerConnection getPc() {
        return pc;
    }

    public void setPc(PeerConnection pc) {
        this.pc = pc;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * SdpObserver是来回调sdp是否创建(offer,answer)成功，是否设置描述成功(local,remote）的接口
     **/

    //Create{Offer,Answer}成功回调
    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {
        //设置本地LocalDescription
        pc.setLocalDescription(Peer.this, sessionDescription);
        SessionDescription localDescription = pc.getLocalDescription();
        SessionDescription.Type type = localDescription.type;
        //构建信令数据
        //接下来使用之前的WebSocket实例将offer发送给服务器
        if (type == SessionDescription.Type.OFFER) {
            //呼叫
            webRtcClient.sendOffer(sessionDescription);
        } else if (type == SessionDescription.Type.ANSWER) {
            //应答
            webRtcClient.sendAnswer(sessionDescription);
        }


    }

    //Set{Local,Remote}Description()成功回调
    @Override
    public void onSetSuccess() {
        Log.d("yao", "onSetSuccess");
    }

    //Create{Offer,Answer}失败回调
    @Override
    public void onCreateFailure(String s) {
        Log.e("yao", "onCreateFailure info=" + s);
    }

    //Set{Local,Remote}Description()失败回调
    @Override
    public void onSetFailure(String s) {
        Log.e("yao", "onSetFailure info=" + s);
    }

    /**
     * SdpObserver是来回调sdp是否创建(offer,answer)成功，是否设置描述成功(local,remote）的接口
     **/
    //信令状态改变时候触发
    @Override
    public void onSignalingChange(PeerConnection.SignalingState signalingState) {

    }

    //IceConnectionState连接状态改变时候触发
    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
        Log.d(TAG, "onIceConnectionChange " + iceConnectionState);
        if (iceConnectionState == PeerConnection.IceConnectionState.DISCONNECTED) {
            /** ice连接中断处理 **/
        }
    }

    //IceConnectionState连接接收状态改变
    @Override
    public void onIceConnectionReceivingChange(boolean b) {

    }

    //IceConnectionState网络信息获取状态改变
    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {

    }

    //新ice地址被找到触发
    @Override
    public void onIceCandidate(IceCandidate iceCandidate) {
        Log.d(TAG, "onIceCandidate " + iceCandidate.sdpMid);
        webRtcClient.setIceCandidate(iceCandidate);

    }

    //ice地址被移除掉触发
    @Override
    public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {

    }

    //Peer连接远端音视频数据到达时触发 注：用onTrack回调代替
    @Override
    public void onAddStream(MediaStream mediaStream) {
        Log.d(TAG, "onAddStream " + mediaStream.getId());
    }

    //Peer连接远端音视频数据移除时触发
    @Override
    public void onRemoveStream(MediaStream mediaStream) {
        Log.d(TAG, "onRemoveStream " + mediaStream.getId());
        //移除Peer连接 & 通知监听远端音视频数据到达
    }

    //Peer连接远端开启数据传输通道时触发
    @Override
    public void onDataChannel(DataChannel dataChannel) {

    }

    //通道交互协议需要重新协商时触发
    @Override
    public void onRenegotiationNeeded() {

    }

    //Triggered when a new track is signaled by the remote peer, as a result of setRemoteDescription.
    @Override
    public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {

    }

    @Override
    public void onTrack(RtpTransceiver transceiver) {
        MediaStreamTrack track = transceiver.getReceiver().track();
        Log.d(TAG, "onTrack " + track.id());
        if (track instanceof VideoTrack) {
            webRtcClient.getRtcListener().onAddRemoteStream(id, (VideoTrack) track);
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

                pc.setRemoteDescription(this, sdp);
                pc.createAnswer(this, new MediaConstraints());
            }
            break;
            case Constant.ANSWER: {
                SessionDescription sdp = model.getPayload().getSdp();
                Log.d(TAG, "EventBus Received answer sdp:\n" + sdp.description);
                Log.d(TAG, "EventBus Received answer type:" + sdp.type.toString());
                pc.setRemoteDescription(this, sdp);
            }
            break;
            case Constant.CANDIDATE: {
                //服务端 发送 接收方sdpAnswer
                IceCandidate iceCandidate = model.getPayload().getIceCandidate();
                if (iceCandidate != null && pc != null) {
                    Log.d(TAG, "EventBus Received iceCandidate sdpMid=" + iceCandidate.sdpMid);
                    Log.d(TAG, "EventBus Received iceCandidate sdpMLineIndex=" + iceCandidate.sdpMLineIndex);
                    Log.d(TAG, "EventBus Received iceCandidate sdp=" + iceCandidate.sdp);
                    pc.addIceCandidate(iceCandidate);
                }
            }
            break;
        }
    }


}
