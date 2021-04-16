package swaiotos.channel.iot.tv.iothandle.data;

public class CmdEnum {
    public enum START_APP_CMD {
        LIVE_VIDEO,//直播
        ONE_KEY_CLEAR,//一键清理
        PREVIEW_SCREENSAVER,//预览屏保
        CUSTOM_SCREENSAVER//定制屏保
    }

    public enum ACCOUNT_CMD {
        GET_ACCESS_TOKEN //获取账户token
    }
}
