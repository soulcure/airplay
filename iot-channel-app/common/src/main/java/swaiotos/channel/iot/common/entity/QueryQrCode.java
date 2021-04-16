package swaiotos.channel.iot.common.entity;

/**
 * @ProjectName: iot-channel-tv
 * @Package: swaiotos.channel.iot.tv.entity
 * @ClassName: QueryQrCode
 * @Description: java类作用描述
 * @Author: wangyuehui
 * @CreateDate: 2020/4/23 16:08
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/4/23 16:08
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class QueryQrCode {
    private String bindCodeType; //授权绑定二维码绑定状态（1：未绑定  2：已绑定）
    private String attributeJson;   //网络变更信息
    private String deviceInfo;      //设备的硬件属性信息
    private String status;          //在线状态:1为在线,0为下线

    public String getBindCodeType() {
        return bindCodeType;
    }

    public void setBindCodeType(String bindCodeType) {
        this.bindCodeType = bindCodeType;
    }

    public String getAttributeJson() {
        return attributeJson;
    }

    public void setAttributeJson(String attributeJson) {
        this.attributeJson = attributeJson;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
