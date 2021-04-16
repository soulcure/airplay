package swaiotos.runtime.h5.core.os.exts.device;

import java.io.Serializable;

/**
 * @Author: yuzhan
 */
public class ScanQrCodeEvent implements Serializable {
    private String result;

    public ScanQrCodeEvent setResult(String result) {
        this.result = result;
        return this;
    }

    public String getResult() {
        return result;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ScanQrCodeEvent{");
        sb.append("result='").append(result).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
