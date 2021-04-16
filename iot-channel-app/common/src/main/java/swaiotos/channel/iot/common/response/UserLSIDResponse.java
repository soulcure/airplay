package swaiotos.channel.iot.common.response;

import swaiotos.channel.iot.common.entity.UserLSIDData;

/**
 * @ProjectName: iot-channel-tv
 * @Package: swaiotos.channel.iot.tv.response
 * @ClassName: UserResponse
 * @Description: java类作用描述
 * @Author: wangyuehui
 * @CreateDate: 2020/4/9 16:11
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/4/9 16:11
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class UserLSIDResponse extends CooCaaResponse {
    private UserLSIDData data;

    public UserLSIDData getData() {
        return data;
    }

    public void setData(UserLSIDData data) {
        this.data = data;
    }
}
