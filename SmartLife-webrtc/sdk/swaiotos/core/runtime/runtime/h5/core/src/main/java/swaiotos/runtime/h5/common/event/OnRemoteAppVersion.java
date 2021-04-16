package swaiotos.runtime.h5.common.event;

public class OnRemoteAppVersion {
    public String event;
    public int version;
    public String appId;

    public OnRemoteAppVersion() {
    }

    public OnRemoteAppVersion(String event, int version, String appId) {
        this.event = event;
        this.version = version;
        this.appId = appId;
    }
}
