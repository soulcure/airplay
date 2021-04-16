package swaiotos.channel.iot.webrtc;

import android.util.Log;

import com.google.gson.Gson;

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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.SSContext;
import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.ss.channel.stream.IStreamChannel;
import swaiotos.channel.iot.ss.session.Session;
import swaiotos.channel.iot.utils.AppUtils;
import swaiotos.channel.iot.webrtc.config.Constant;
import swaiotos.channel.iot.webrtc.entity.FileDescription;
import swaiotos.channel.iot.webrtc.entity.Model;


public class Peer implements SdpObserver, PeerConnection.Observer {
    //日志Tag
    private final static String TAG = "peer";
    public static final String SOURCE_CLIENT = "com.coocaa.webrtc.datachannel.server";

    //PeerConnection对象
    private PeerConnection pc;
    //PeerConnection标识
    private String mSid;
    private DataChannel dataChannel;

    private final SSContext mSSContext;
    private final String targetClient;
    private final Session targetSession;
    private long outfileCurPos;
    private FileChannel outfileChannel;
    private IStreamChannel.Receiver mReceiver;
    private DataChannelServer.ConnectCallBack mConnectCallBack;
    private PeerConnection.IceConnectionState mState;

    //构造函数
    public Peer(String sid, PeerConnectionFactory factory, PeerConnection.RTCConfiguration rtcConfig,
                SSContext ssContext, IStreamChannel.Receiver receiver,
                IMMessage message, DataChannelServer.ConnectCallBack callBack) {
        Log.d(TAG, "new Peer: " + sid);
        mSid = sid;
        mSSContext = ssContext;
        mReceiver = receiver;
        mConnectCallBack = callBack;

        targetClient = message.getClientSource();
        targetSession = message.getSource();
        Log.d(TAG, "factory createPeerConnection start");
        pc = factory.createPeerConnection(rtcConfig, this);
        Log.d(TAG, "factory createPeerConnection end=" + pc);
        if (pc != null) {
            DataChannel.Init dcInit = new DataChannel.Init();
            //dcInit.id = 1;
            dataChannel = pc.createDataChannel(sid, dcInit);
            Log.d(TAG, "dataChannel createDataChannel");
        }

    }

    public PeerConnection getPc() {
        return pc;
    }

    public void setPc(PeerConnection pc) {
        this.pc = pc;
    }

    public String getId() {
        return mSid;
    }


    public boolean isOpen() {
        return mState == PeerConnection.IceConnectionState.CONNECTED;
    }

    /**
     * SdpObserver是来回调sdp是否创建(offer,answer)成功，是否设置描述成功(local,remote）的接口
     **/

    //Create{Offer,Answer}成功回调
    @Override
    public void onCreateSuccess(SessionDescription sdp) {
        //设置本地LocalDescription
        pc.setLocalDescription(this, sdp);
        SessionDescription localDescription = pc.getLocalDescription();
        SessionDescription.Type type = localDescription.type;
        Log.d(TAG, "onCreateSuccess == " + " type == " + type);

        //接下来使用之前的WebSocket实例将offer发送给服务器
        if (type == SessionDescription.Type.OFFER) {
            //呼叫
            sendOffer(sdp);
        } else if (type == SessionDescription.Type.ANSWER) {
            //应答
            sendAnswer(sdp);
        }
    }

    //Set{Local,Remote}Description()成功回调
    @Override
    public void onSetSuccess() {
        Log.d(TAG, "onSetSuccess");
    }

    //Create{Offer,Answer}失败回调
    @Override
    public void onCreateFailure(String s) {
        Log.e(TAG, "onCreateFailure:" + s);
    }

    //Set{Local,Remote}Description()失败回调
    @Override
    public void onSetFailure(String s) {
        Log.e(TAG, "onSetFailure:" + s);
    }

    /**
     * SdpObserver是来回调sdp是否创建(offer,answer)成功，是否设置描述成功(local,remote）的接口
     **/
    //信令状态改变时候触发
    @Override
    public void onSignalingChange(PeerConnection.SignalingState signalingState) {
        Log.d(TAG, "onSignalingChange,signalingState:" + signalingState);
    }

    //IceConnectionState连接状态改变时候触发
    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState state) {
        mState = state;
        Log.d(TAG, mSid + " onIceConnectionChange: " + state + " & peer hash=" + Peer.this.hashCode());
        if (state == PeerConnection.IceConnectionState.CLOSED
                || state == PeerConnection.IceConnectionState.DISCONNECTED
                || state == PeerConnection.IceConnectionState.FAILED) {
            //Log.e(TAG, "onIceConnectionChange disconnected");
            if (mConnectCallBack != null) {
                mConnectCallBack.onError(-2, mSid);
            }
        } else if (state == PeerConnection.IceConnectionState.CONNECTED) {
            if (mConnectCallBack != null) {
                mConnectCallBack.onConnect(0, mSid);
            }
            //Log.d(TAG, "onIceConnectionChange connected");
        }
    }

    //IceConnectionState连接接收状态改变
    @Override
    public void onIceConnectionReceivingChange(boolean b) {
        Log.d(TAG, "onIceConnectionReceivingChange,b:" + b);
    }

    //IceConnectionState网络信息获取状态改变
    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
        Log.d(TAG, "onIceGatheringChange,iceGatheringState:" + iceGatheringState);
    }

    //新ice地址被找到触发
    @Override
    public void onIceCandidate(IceCandidate iceCandidate) {
        setIceCandidate(iceCandidate);
    }

    //ice地址被移除掉触发
    @Override
    public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
        Log.d(TAG, "onIceCandidatesRemoved");
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
        Log.d(TAG, "onDataChannel,dataChannel:" + dataChannel);
        dataChannel.registerObserver(new DataChannel.Observer() {
            @Override
            public void onBufferedAmountChange(long l) {
                Log.d(TAG, "onBufferedAmountChange,l" + l);
            }

            @Override
            public void onStateChange() {
                Log.d(TAG, "onStateChange");
            }

            @Override
            public void onMessage(DataChannel.Buffer buffer) {
                read(buffer.data, buffer.binary);
            }
        });

    }

    //通道交互协议需要重新协商时触发
    @Override
    public void onRenegotiationNeeded() {
        Log.d(TAG, "onRenegotiationNeeded");
    }

    //Triggered when a new track is signaled by the remote peer, as a result of setRemoteDescription.
    @Override
    public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
        Log.d(TAG, "onAddTrack");
    }

    @Override
    public void onTrack(RtpTransceiver transceiver) {
        MediaStreamTrack track = transceiver.getReceiver().track();
        Log.d(TAG, "onTrack " + track.id());
    }


    public void close() {
        Log.d(TAG, "Peer dispose now");
        if (dataChannel != null) {
            dataChannel.close();
            dataChannel.dispose();
            dataChannel = null;
        }

        if (pc != null) {
            pc.close();
            pc.dispose();
            pc = null;
        }

    }


    public boolean sendChannelData(String data) {
        ByteBuffer buffer = ByteBuffer.wrap(data.getBytes());
        return dataChannel.send(new DataChannel.Buffer(buffer, false));
    }

    public boolean sendChannelData(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        return dataChannel.send(new DataChannel.Buffer(buffer, false));
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
            Log.d(TAG, mSid + " DataChannel onMessage=" + command);

            if (command.contains("dataType")) {
                FileDescription fileDescription = new Gson().fromJson(command, FileDescription.class);
                if (fileDescription != null) {
                    if (fileDescription.isCheckFile()) { //文件接收方消息
                        answerCheckSend(fileDescription);
                    } else if (fileDescription.isStart()) { //文件接收方消息
                        try {
                            File file = fileDescription.getDownLoadFile();
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

                            File file = fileDescription.getDownLoadFile();
                            if (AppUtils.checkFileMd5(file, fileDescription.getMd5())) {
                                //done
                                Log.d(TAG, "DataChannel receive file finish");
                            }
                        }
                    }

                }

            } else {//通道消息
                mReceiver.onReceive(bytes);
            }

        }
    }

    private void sendData(String content) {
        try {
            Session mySession = mSSContext.getSessionManager().getMySession();
            if (targetSession != null) {
                IMMessage message = IMMessage.Builder.createTextMessage(mySession, targetSession,
                        SOURCE_CLIENT, targetClient, content);
                message.putExtra(SSChannel.FORCE_SSE, "true");//强制云端

                mSSContext.getIMChannel().send(message);
                Log.d(TAG, "send Data by sse content=" + content);
            } else {
                Log.e(TAG, "send Data fail by targetSession is null");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

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

}
