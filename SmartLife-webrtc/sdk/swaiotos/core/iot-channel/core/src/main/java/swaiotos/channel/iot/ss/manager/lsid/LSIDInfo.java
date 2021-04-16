package swaiotos.channel.iot.ss.manager.lsid;

/**
 * @ClassName: LSIDInfo
 * @Author: lu
 * @CreateDate: 2020/4/26 10:35 AM
 * @Description:
 */
public class LSIDInfo {
    public String lsid;
    public String accessToken;
    public String tempCode; //临时绑定码
    public String roomId;   //房间号
    public String userId;   //用户唯一标识  手机使用手机号码  tv、dongle、panel使用激活id

    public LSIDInfo() {
    }

    public LSIDInfo(String lsid, String accessToken) {
        this.lsid = lsid;
        this.accessToken = accessToken;
    }

    public LSIDInfo(String lsid, String accessToken, String userId) {
        this.lsid = lsid;
        this.accessToken = accessToken;
        this.userId = userId;
    }

    public LSIDInfo(String lsid, String accessToken, String tempCode, String roomId) {
        this.lsid = lsid;
        this.accessToken = accessToken;
        this.tempCode = tempCode;
        this.roomId = roomId;
    }

    public void setLsid(String lsid) {
        this.lsid = lsid;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setTempCode(String tempCode) {
        this.tempCode = tempCode;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}
