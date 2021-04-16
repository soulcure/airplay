package swaiotos.runtime.h5.common.event;

public class OnSetEnableIMReceive {
    public boolean enable;

    public OnSetEnableIMReceive() {
    }

    public OnSetEnableIMReceive(boolean enable) {
        this.enable = enable;
    }

    private String id;
    public OnSetEnableIMReceive setId(String id) {
        this.id = id;
        return this;
    }

    public String getId() {
        return id;
    }
}
