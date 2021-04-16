package swaiotos.channel.iot.common.response;

import swaiotos.channel.iot.common.entity.ZxingData;

/**
 * @ProjectName: iot-channel-tv
 * @Package: swaiotos.channel.iot.tv.response
 * @ClassName: ZxingCodeResponse
 * @Description: java类作用描述
 * @Author: wangyuehui
 * @CreateDate: 2020/4/8 22:23
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/4/8 22:23
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class ZxingCodeResponse extends CooCaaResponse {
    private ZxingData data;

    public ZxingData getData() {
        return data;
    }

    public void setData(ZxingData data) {
        this.data = data;
    }
}
