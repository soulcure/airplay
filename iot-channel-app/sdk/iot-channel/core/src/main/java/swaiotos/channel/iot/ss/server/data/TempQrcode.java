package swaiotos.channel.iot.ss.server.data;

/**
 * @ProjectName: iot-channel-app
 * @Package: swaiotos.channel.iot.ss.server.data
 * @ClassName: TempQrcode
 * @Description: java类作用描述
 * @Author: wangyuehui
 * @CreateDate: 2020/10/26 20:01
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/10/26 20:01
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class TempQrcode {
    private String bindCode;
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
}
