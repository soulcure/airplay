package swaiotos.runtime.h5.common.bean;

/**
 * @ClassName: SsePushBean
 * @Author: AwenZeng
 * @CreateDate: 2020/4/2 20:08
 * @Description:
 */
public class SsePushBean {
    public enum EVENT_TYPE{
        DONGLEANDTV,
        ALL_DEVICES
    }
    private String event;
    private String data;
    private Integer clientVersion;
    public String getEvent() {
        return event;
    }

    public Integer getClientVersion() {
        return clientVersion;
    }

    public void setClientVersion(Integer clientVersion) {
        this.clientVersion = clientVersion;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getData() {
        return data;
    }

    public void setData(String data){
        this.data = data;
    }
}
