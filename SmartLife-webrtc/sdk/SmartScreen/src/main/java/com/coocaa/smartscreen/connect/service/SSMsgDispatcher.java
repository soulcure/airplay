package com.coocaa.smartscreen.connect.service;

import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import swaiotos.channel.iot.ss.channel.im.IMMessage;

/**
 * 派发消息给到其他进程，h5影视页面有此需求
 * @Author: yuzhan
 */
public class SSMsgDispatcher {

    public interface IMsgReceiver {
        void onReceive(String clientId, IMMessage message);
    }

    private static Map<String, Set<IMsgReceiver>> map = new HashMap<>();

    public static void register(String clientID, IMsgReceiver receiver) {
        if(TextUtils.isEmpty(clientID) || receiver == null) return ;
        Log.d("SSMsgDispatcher", "++ register clientId=" + clientID);

        Set<IMsgReceiver> receiverList = map.get(clientID);
        if(receiverList == null) {
            receiverList = new HashSet<>();
            map.put(clientID, receiverList);
        }
        receiverList.add(receiver);
    }

    public static void unRegister(String clientID) {
        Log.d("SSMsgDispatcher", "-- unRegister clientId=" + clientID);
        if(TextUtils.isEmpty(clientID)) return ;
        Set<IMsgReceiver> receiverList = map.get(clientID);
        if(receiverList != null) {
            if(receiverList.contains(clientID)) {
                receiverList.remove(clientID);
            }
            if(receiverList.isEmpty()) {
                map.remove(clientID);
            }
        }
    }

    public static void dispatch(IMMessage message) {
        if(message == null)
            return ;
        String clientSource = message.getClientSource();
        if(TextUtils.isEmpty(clientSource))
            return ;
        Set<IMsgReceiver> receiverList = map.get(clientSource);
        if(receiverList != null) {
            for(IMsgReceiver receiver : receiverList) {
                receiver.onReceive(clientSource, message);
            }
        }
    }
}
