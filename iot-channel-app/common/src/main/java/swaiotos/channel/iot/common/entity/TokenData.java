package swaiotos.channel.iot.common.entity;

/**
 * @ProjectName: iot-channel-tv
 * @Package: swaiotos.channel.iot.tv.entity
 * @ClassName: CooCaaData
 * @Description: java类作用描述
 * @Author: wangyuehui
 * @CreateDate: 2020/4/8 22:14
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/4/8 22:14
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class TokenData {
    private String accessToken;
    private String expiresIn;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(String expiresIn) {
        this.expiresIn = expiresIn;
    }
}
