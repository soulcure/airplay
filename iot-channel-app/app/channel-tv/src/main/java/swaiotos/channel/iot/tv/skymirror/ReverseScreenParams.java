package swaiotos.channel.iot.tv.skymirror;

import com.google.gson.Gson;

/**
 * @ClassName DeviceParams
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 2020/4/8
 * @Version TODO (write something)
 */
public class ReverseScreenParams {
    /**
     * 用于给CmdData cmd字段赋值
     */
    public enum CMD{
        START_REVERSE,
        STOP_REVERSE,
    }

    public String ip;

    public String toJson() {
        return new Gson().toJson(this);
    }
}
