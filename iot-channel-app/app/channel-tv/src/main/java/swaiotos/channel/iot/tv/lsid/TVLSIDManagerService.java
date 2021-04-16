package swaiotos.channel.iot.tv.lsid;

import swaiotos.channel.iot.common.lsid.LSIDManagerService;
import swaiotos.channel.iot.common.utils.TYPE;

/**
 * @ProjectName: iot-channel-app
 * @Package: swaiotos.channel.iot.tv.lsid
 * @ClassName: TVLSIDManagerService
 * @Description: java类作用描述
 * @Author: wangyuehui
 * @CreateDate: 2020/4/28 18:46
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/4/28 18:46
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class TVLSIDManagerService extends LSIDManagerService {

    @Override
    public TYPE getLSIDType() {
        return TYPE.TV;
    }
}
