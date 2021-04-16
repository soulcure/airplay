package swaiotos.channel.iot.ss.server.data;

import java.util.List;

public class OnlineData {
    private String code;
    private String msg;
    private List<DeviceStatusData> data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<DeviceStatusData> getData() {
        return data;
    }

    public void setData(List<DeviceStatusData> data) {
        this.data = data;
    }
}
