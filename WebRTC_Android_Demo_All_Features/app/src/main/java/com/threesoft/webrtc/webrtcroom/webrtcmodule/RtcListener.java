package com.threesoft.webrtc.webrtcroom.webrtcmodule;

import org.json.JSONObject;
import org.webrtc.VideoTrack;

/**
 * UI页面事件监听
 * Created by zengjinlong on 2021/01/20.
 */

public interface RtcListener {

    //远程音视频流加入 Peer通道
    void onAddRemoteStream(String peerId,VideoTrack videoTrack);

    //远程音视频流移除 Peer通道销毁
    void onRemoveRemoteStream(String peerId);

    //收到datachannel消息
    void onPeerTalkMsg(JSONObject msg);

}
