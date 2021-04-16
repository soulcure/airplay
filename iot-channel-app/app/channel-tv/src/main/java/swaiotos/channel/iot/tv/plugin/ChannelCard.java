package swaiotos.channel.iot.tv.plugin;

import android.os.Bundle;

/**
 * @ProjectName: iot-channel-app
 * @Package: swaiotos.channel.iot.tv.plugin
 * @ClassName: ChannelCard
 * @Description: java类作用描述
 * @Author: wangyuehui
 * @CreateDate: 2020/8/20 20:35
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/8/20 20:35
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public interface ChannelCard {

    void onChannelCoreData();

    void onDeliverPluginMessage(Bundle bundle); //获取数据

    void onShortcutDataChanged(int state);

}

