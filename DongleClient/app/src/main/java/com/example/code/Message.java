package com.example.code;

public class Message {

    public enum MSG_TYPE {
        SEND, RECEIVE,
    }

    private MSG_TYPE messageType;
    private String content;
    private String msgType;


    public Message(MSG_TYPE type, String content, String msgType) {
        this.messageType = type;
        this.content = content;
        this.msgType = msgType;
    }


    public MSG_TYPE getMessageType() {
        return messageType;
    }

    public void setMessageType(MSG_TYPE messageType) {
        this.messageType = messageType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public String getText() {
        return "TYPE=" + msgType + " & content=" + content;
    }
}
