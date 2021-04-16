package swaiotos.channel.iot.ss.server.data;

import java.util.List;

/**
 * @ProjectName: iot-channel-app
 * @Package: swaiotos.channel.iot.ss.server.data
 * @ClassName: DeviceStatusData
 * @Description: java类作用描述
 * @Author: wangyuehui
 * @CreateDate: 2020/7/20 14:51
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/7/20 14:51
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class DeviceStatusData {
    private String screen_id;
    private int online_status;

    public String getScreen_id() {
        return screen_id;
    }

    public void setScreen_id(String screen_id) {
        this.screen_id = screen_id;
    }

    public void setOnline_status(int online_status) {
        this.online_status = online_status;
    }

    public int getOnline_status() {
        return online_status;
    }
}
