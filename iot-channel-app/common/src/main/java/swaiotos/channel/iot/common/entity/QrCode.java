package swaiotos.channel.iot.common.entity;

/**
 * @ProjectName: iot-channel-app
 * @Package: swaiotos.channel.iot.common.entity
 * @ClassName: QrCode
 * @Description: java类作用描述
 * @Author: wangyuehui
 * @CreateDate: 2020/9/30 16:49
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/9/30 16:49
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class QrCode {
    private String showCode;//传屏码
    private String qrCode;//二维码url
    private int type;

    public String getShowCode() {
        return showCode;
    }

    public void setShowCode(String showCode) {
        this.showCode = showCode;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
