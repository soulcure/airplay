package swaiotos.runtime.h5.common.event;

public class OnShakeRegisterCBData {
    public String event;
    public Object callbackId;
    public int callbackCode;

    public OnShakeRegisterCBData(String event, Object callbackId) {
        this.event = event;
        this.callbackCode = 1;
        this.callbackId = callbackId;
    }
}
