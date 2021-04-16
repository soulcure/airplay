package swaiotos.channel.iot.ss.server.data;

/**
 * @ProjectName: iot-channel-swaiotos
 * @Package: swaiotos.channel.iot.okgo.entity
 * @ClassName: DeviceData
 * @Description: 设备的状态、属性等信息
 * @Author: wangyuehui
 * @CreateDate: 2020/4/26 14:24
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/4/26 14:24
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class JoinToLeaveData {

    private int code;     //返回码
    private String msg;   //错误信息

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
