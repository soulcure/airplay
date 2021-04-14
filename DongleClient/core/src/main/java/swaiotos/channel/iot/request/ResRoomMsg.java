package swaiotos.channel.iot.request;


import swaiotos.channel.iot.entity.MessageData;

public class ResRoomMsg {
    private String id;
    private String data;

    public ResRoomMsg(String id, MessageData data) {
        this.id = id;
        this.data = data.toString();
    }


    public void setId(String id) {
        this.id = id;
    }


    public void setData(MessageData data) {
        this.data = data.toString();
    }


}
