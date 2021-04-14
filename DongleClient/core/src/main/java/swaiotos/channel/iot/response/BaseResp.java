package swaiotos.channel.iot.response;


public class BaseResp {
    public int code;
    public String msg;

    public boolean isSuccess() {
        return code == 0;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
