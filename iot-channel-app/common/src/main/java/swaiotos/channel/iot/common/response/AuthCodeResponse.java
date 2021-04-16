package swaiotos.channel.iot.common.response;

import swaiotos.channel.iot.common.entity.ValidCode;

/**
 * @ProjectName: iot-channel-tv
 * @Package: swaiotos.channel.iot.tv.response
 * @ClassName: AuthCodeResponse
 * @Description: java类作用描述
 * @Author: wangyuehui
 * @CreateDate: 2020/4/8 22:11
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/4/8 22:11
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class AuthCodeResponse extends CooCaaResponse {
    private ValidCode data;

    public ValidCode getData() {
        return data;
    }

    public void setData(ValidCode data) {
        this.data = data;
    }
}
