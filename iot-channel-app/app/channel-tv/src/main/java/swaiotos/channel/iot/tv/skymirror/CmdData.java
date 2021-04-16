package swaiotos.channel.iot.tv.skymirror;

import com.google.gson.Gson;

/**
 * @ClassName CmdData
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 2019-12-27
 * @Version TODO (write something)
 */
public class CmdData {
    public String cmd;//具体指令
    public String param;//指令内部参数,用指定的bean转成json
    public String type;//指令的种类

    public enum CMD_TYPE{
        /**
         * 设备相关
         */
        DEVICE,
        /**
         * 遥控相关键值
         */
        KEY_EVENT,
        /**
         * 应用圈
         */
        APP_STORE,
        /**
         * 启动应用
         */
        START_APP,
        /**
         * 多媒体推送，在线，直播，本地（video，music，picture）
         */
        MEDIA,
        /**
         * 语音
         */
        VOICE,
        /**
         * 截屏
         */
        SCREEN_SHOT,
        /**
        * 同屏控制
        * */
        REVERSE_SCREEN,
        /**
         * 屏幕镜像
         * */
        MIRROR_SCREEN,

        /**
         *信发
         * */
        LETTER_SEND;
    }

    //ss-channel用到的
    public CmdData(String cmd, String type, String param) {
        this.cmd = cmd;
        this.type = type;
        this.param = param;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }
}
