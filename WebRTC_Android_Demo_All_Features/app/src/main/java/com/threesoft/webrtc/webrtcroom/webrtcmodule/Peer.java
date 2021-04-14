package com.threesoft.webrtc.webrtcroom.webrtcmodule;

import android.app.Activity;
import android.os.Build;
import android.util.Log;

import com.threesoft.webrtc.webrtcroom.activity.ChatActivity2;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.MediaStreamTrack;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpReceiver;
import org.webrtc.RtpTransceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.VideoTrack;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * PeerConnection通道封装，包括PeerConnection创建及状态回调
 * Created by zengjinlong on 2021/01/20.
 */

public class Peer implements SdpObserver, PeerConnection.Observer {
    //PeerConnection对象
    private PeerConnection pc;
    //PeerConnection标识
    private String id;
    //webRtClient对象
    private WebRtcClient2 webRtcClient;

    //日志Tag
    private final static String TAG = Peer.class.getCanonicalName();

    //构造函数
    public Peer(String id,
                PeerConnectionFactory factory,
                PeerConnection.RTCConfiguration rtcConfig,
                WebRtcClient2 webRtcClient) {
        Log.d(TAG,"new Peer: " + id );
        this.pc = factory.createPeerConnection(rtcConfig,this);
        this.id = id;
        this.webRtcClient = webRtcClient;
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

    /**SdpObserver是来回调sdp是否创建(offer,answer)成功，是否设置描述成功(local,remote）的接口**/

    //Create{Offer,Answer}成功回调
    @Override
    public void onCreateSuccess(SessionDescription sdp) {
        String type = sdp.type.canonicalForm();
        Log.d(TAG,"onCreateSuccess " + type);
        if( pc == null){
            Log.d(TAG,"onCreateSuccess ,pc is null ");
            return;
        }
        //设置本地LocalDescription
        pc.setLocalDescription(Peer.this, sdp);
        //构建信令数据
        try {
            JSONObject message = new JSONObject();
            message.put("from",webRtcClient.getSocketId());
            message.put("to",id);
            message.put("room",webRtcClient.getRoomId());
            message.put("sdp",sdp.description);
            //向信令服务器发送信令
            Log.d(TAG,"onCreateSuccess,message:"+message);
            if(webRtcClient != null){
                webRtcClient.sendMessage(type,message);
            }

        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    //Set{Local,Remote}Description()成功回调
    @Override
    public void onSetSuccess() {
        Log.d(TAG,"onSetSuccess");
    }

    //Create{Offer,Answer}失败回调
    @Override
    public void onCreateFailure(String s) {
        Log.e(TAG,"onCreateFailure:"+s);
    }

    //Set{Local,Remote}Description()失败回调
    @Override
    public void onSetFailure(String s) {
        Log.e(TAG,"onSetFailure:"+s);
    }

    /**SdpObserver是来回调sdp是否创建(offer,answer)成功，是否设置描述成功(local,remote）的接口**/
    //信令状态改变时候触发
    @Override
    public void onSignalingChange(PeerConnection.SignalingState signalingState) {
        Log.d(TAG,"onSignalingChange,signalingState:"+signalingState);
    }

    //IceConnectionState连接状态改变时候触发
    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
        Log.d(TAG,"onIceConnectionChange " + iceConnectionState);
        if (iceConnectionState == PeerConnection.IceConnectionState.DISCONNECTED){
            /** ice连接中断处理 **/
        }
    }

    //IceConnectionState连接接收状态改变
    @Override
    public void onIceConnectionReceivingChange(boolean b) {
        Log.d(TAG,"onIceConnectionReceivingChange,b:"+b);
    }

    //IceConnectionState网络信息获取状态改变
    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
        Log.d(TAG,"onIceGatheringChange,iceGatheringState:"+iceGatheringState);
    }

    //新ice地址被找到触发
    @Override
    public void onIceCandidate(IceCandidate iceCandidate) {
        Log.d(TAG,"onIceCandidate "+iceCandidate.sdpMid);
        try {
            //构建信令数据
            JSONObject message = new JSONObject();
            message.put("from",webRtcClient.getSocketId());
            message.put("to",id);
            message.put("room",webRtcClient.getRoomId());
            //candidate参数
            JSONObject candidate = new JSONObject();
            candidate.put("sdpMid",iceCandidate.sdpMid);
            candidate.put("sdpMLineIndex",iceCandidate.sdpMLineIndex);
            candidate.put("sdp",iceCandidate.sdp);
            message.put("candidate",candidate);
            //向信令服务器发送信令
            if(webRtcClient != null){
                webRtcClient.sendMessage("candidate",message);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    //ice地址被移除掉触发
    @Override
    public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
        Log.d(TAG,"onIceCandidatesRemoved");
    }

    //Peer连接远端音视频数据到达时触发 注：用onTrack回调代替
    @Override
    public void onAddStream(MediaStream mediaStream) {
        Log.d(TAG,"onAddStream "+ mediaStream.getId());
    }

    //Peer连接远端音视频数据移除时触发
    @Override
    public void onRemoveStream(MediaStream mediaStream) {
        Log.d(TAG,"onRemoveStream "+ mediaStream.getId());
        //移除Peer连接 & 通知监听远端音视频数据到达
    }

    //Peer连接远端开启数据传输通道时触发
    @Override
    public void onDataChannel(DataChannel dataChannel) {
        Log.d(TAG,"onDataChannel,dataChannel:"+dataChannel);
        dataChannel.registerObserver(   new DataChannel.Observer() {
            @Override
            public void onBufferedAmountChange(long l) {
                Log.d(TAG,"onBufferedAmountChange,l"+l);
            }

            @Override
            public void onStateChange() {
                Log.d(TAG,"onStateChange");
            }

            @Override
            public void onMessage(DataChannel.Buffer buffer) {
                Log.d(TAG,"onMessage");
                if(buffer != null){
                    ByteBuffer bf = buffer.data;
                    if(webRtcClient.getRtcListener() != null){
                        if(buffer.binary == false){
                            webRtcClient.peerReceiveMsg(Charset.forName("utf-8").decode(bf).toString());
                        }else {
                            webRtcClient.peerReceiveFile(bf);
                        }

                    }
                }

            }
        });

    }

    //通道交互协议需要重新协商时触发
    @Override
    public void onRenegotiationNeeded() {
        Log.d(TAG,"onRenegotiationNeeded");
    }

    //Triggered when a new track is signaled by the remote peer, as a result of setRemoteDescription.
    @Override
    public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
        Log.d(TAG,"onAddTrack");
    }

    @Override
    public void onTrack(RtpTransceiver transceiver) {
        MediaStreamTrack track = transceiver.getReceiver().track();
        Log.d(TAG,"onTrack "+ track.id());
        if (track instanceof VideoTrack) {
            if(webRtcClient != null){
                if(!isActivityDestroy((ChatActivity2)(webRtcClient.getRtcListener()))){
                    webRtcClient.getRtcListener().onAddRemoteStream(id,(VideoTrack)track);
                }

            }

        }
    }


    public void dispose(){
        Log.d(TAG,"Peer dispose now");
        pc.dispose();
        pc = null;
        webRtcClient = null;
        this.id = null;
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
}
