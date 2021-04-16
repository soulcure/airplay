package swaiotos.channel.iot.common.response;

import swaiotos.channel.iot.common.entity.QueryQrCode;

/**
 * @ProjectName: iot-channel-tv
 * @Package: swaiotos.channel.iot.tv.response
 * @ClassName: QueryQrCodeResponse
 * @Description: java类作用描述
 * @Author: wangyuehui
 * @CreateDate: 2020/4/23 16:10
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/4/23 16:10
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class QueryQrCodeResponse extends CooCaaResponse {
    private QueryQrCode data;

    public QueryQrCode getData() {
        return data;
    }

    public void setData(QueryQrCode data) {
        this.data = data;
    }
}
