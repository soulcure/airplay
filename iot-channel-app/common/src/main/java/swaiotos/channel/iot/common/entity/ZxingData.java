package swaiotos.channel.iot.common.entity;

/**
 * @ProjectName: iot-channel-tv
 * @Package: swaiotos.channel.iot.tv.entity
 * @ClassName: ZxingData
 * @Description: java类作用描述
 * @Author: wangyuehui
 * @CreateDate: 2020/4/8 22:23
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/4/8 22:23
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class ZxingData {
    private String bindCode;
    private String url;
    private String expires_in;
    private String type_loop_time;

    public String getBindCode() {
        return bindCode;
    }

    public void setBindCode(String bindCode) {
        this.bindCode = bindCode;
    }

    public String getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(String expires_in) {
        this.expires_in = expires_in;
    }

    public String getType_loop_time() {
        return type_loop_time;
    }

    public void setType_loop_time(String type_loop_time) {
        this.type_loop_time = type_loop_time;
    }

    public String getUrl() {
        return url;
    }


}
