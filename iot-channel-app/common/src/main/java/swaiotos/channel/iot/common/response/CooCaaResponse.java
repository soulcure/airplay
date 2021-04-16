package swaiotos.channel.iot.common.response;

/**
 * @ProjectName: iot-channel-tv
 * @Package: swaiotos.channel.iot.tv.response
 * @ClassName: CooCaaResponse
 * @Description: 返回数据的基类
 * @Author: wangyuehui
 * @CreateDate: 2020/4/8 22:06
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/4/8 22:06
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class CooCaaResponse {
    private String message;
    private String code;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
