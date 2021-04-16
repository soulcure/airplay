package swaiotos.channel.iot.tv.iothandle.data;

/**
 * @ClassName: DeviceParams
 * @Author: AwenZeng
 * @CreateDate: 2020/4/8 18:50
 * @Description: 设备信息
 */
public class DeviceParams{
    /**
     * 用于给CmdData cmd字段赋值
     */
    public enum CMD{
        CONNECT,
        DISCONNECT,
        DEVICE_INFO,
    }
    public String name;//名字
    public String room;//房间
    public String model;//型号
    public String activeId;//激活id
    public String ip;
    public int isAIStandby;//ai待机 1是 2否
}
