package swaiotos.channel.iot.ss.server.data;

import java.util.List;

import swaiotos.channel.iot.ss.session.RoomDevice;

/**
 * @ProjectName: iot-channel-app
 * @Package: swaiotos.channel.iot.ss.server.data
 * @ClassName: RoomDevices
 * @Description: java类作用描述
 * @Author: wangyuehui
 * @CreateDate: 2020/10/26 16:16
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/10/26 16:16
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class RoomDevices {
    private List<RoomDevice> devices;
    private String roomId;
    private int deviceCount;

    public List<RoomDevice> getDevices() {
        return devices;
    }

    public void setDevices(List<RoomDevice> devices) {
        this.devices = devices;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public int getDeviceCount() {
        return deviceCount;
    }

    public void setDeviceCount(int deviceCount) {
        this.deviceCount = deviceCount;
    }
}
