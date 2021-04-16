package swaiotos.channel.iot.common.response;

import swaiotos.channel.iot.common.entity.TokenData;

/**
 * @ProjectName: iot-channel-tv
 * @Package: swaiotos.channel.iot.tv.response
 * @ClassName: TokenResponse
 * @Description: java类作用描述
 * @Author: wangyuehui
 * @CreateDate: 2020/4/8 22:12
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/4/8 22:12
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class TokenResponse extends CooCaaResponse {
    private TokenData data;

    public TokenData getData() {
        return data;
    }

    public void setData(TokenData data) {
        this.data = data;
    }
}
