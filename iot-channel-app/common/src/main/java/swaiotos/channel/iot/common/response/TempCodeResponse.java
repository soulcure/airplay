package swaiotos.channel.iot.common.response;

/**
 * @ProjectName: iot-channel-app
 * @Package: swaiotos.channel.iot.common.response
 * @ClassName: TempCodeResponse
 * @Description: java类作用描述
 * @Author: wangyuehui
 * @CreateDate: 2020/10/28 14:01
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/10/28 14:01
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class TempCodeResponse extends CooCaaResponse {
    private String data;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
