package swaiotos.runtime.h5.common.event;

public class OnQrCodeCBData {

    public String event;
    public String qrCode;
    public String bindCode;

    public OnQrCodeCBData(String event,String qrCode,String bindCode){
        this.event = event;
        this.qrCode = qrCode;
        this.bindCode = bindCode;
    }
}
