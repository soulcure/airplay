package com.swaiot.webrtcc.entity;

import java.util.Map;

import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.channel.im.IMMessage;

public class SSEEvent {
    public String msgType;
    public Model model;
    public String targetSid;
    public SSChannel ssChannel;
    public Map<String, String> extras;
    public IMMessage imMessage;


    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public String getTargetSid() {
        return targetSid;
    }

    public void setTargetSid(String sid) {
        this.targetSid = sid;
    }

    public SSChannel getSsChannel() {
        return ssChannel;
    }

    public void setSsChannel(SSChannel ssChannel) {
        this.ssChannel = ssChannel;
    }


    public Map<String, String> getExtras() {
        return extras;
    }

    public void setExtras(Map<String, String> extras) {
        this.extras = extras;
    }


    public IMMessage getImMessage() {
        return imMessage;
    }

    public void setImMessage(IMMessage imMessage) {
        this.imMessage = imMessage;
    }
}
