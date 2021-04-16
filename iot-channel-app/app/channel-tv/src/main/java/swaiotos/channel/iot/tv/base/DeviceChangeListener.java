package swaiotos.channel.iot.tv.base;

/**
 * @ProjectName: iot-channel-app
 * @Package: swaiotos.channel.iot.tv.base
 * @ClassName: DeviceChangeListener
 * @Description: java类作用描述
 * @Author: wangyuehui
 * @CreateDate: 2020/5/22 17:15
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/5/22 17:15
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public interface DeviceChangeListener {

    void OnUnBindCallBack(String sid);
    void onBindCallBack();
}
