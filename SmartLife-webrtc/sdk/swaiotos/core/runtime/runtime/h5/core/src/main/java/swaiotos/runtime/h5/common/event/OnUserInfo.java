package swaiotos.runtime.h5.common.event;

public class OnUserInfo {
    public String event;
    public int code;
    public String nickName;
    public String mobile;
    public String avatar;
    public String open_id;
    public Object callbackId;
    public String accessToken;
    public int callbackCode;

    public OnUserInfo() {
    }

    public OnUserInfo(String event, int code,String nickName,String mobile,String avatar,String open_id,String accessToken, Object callbackId) {
        this.event = event;
        this.code = code;
        this.callbackId = callbackId;
        this.callbackCode = 1;
        this.nickName = nickName;
        this.mobile = mobile;
        this.avatar = avatar;
        this.open_id = open_id;
        this.accessToken = accessToken;
    }

    @Override
    public String toString() {
        return "OnUserInfo{" +
                "event='" + event + '\'' +
                ", code=" + code +
                ", nickName='" + nickName + '\'' +
                ", mobile='" + mobile + '\'' +
                ", avatar='" + avatar + '\'' +
                ", open_id='" + open_id + '\'' +
                ", accessToken='" + accessToken + '\'' +
                ", callbackId='" + callbackId + '\'' +
                ", callbackCode='" + callbackCode + '\'' +
                '}';
    }
}
