package com.swaiotos.skymirror.sdk.reverse;

/**
 * @ClassName: IPlayerListener
 * @Description: java类作用描述
 * @Author: lfz
 * @Date: 2020/4/15 11:27
 */
public interface IPlayerListener {

    public static final int ERR_CODE_DOG_OUT = 1;
    public static final int ERR_CODE_SOCKET_SERVER = 2;
    public static final int ERR_CODE_ACTIVITY_DESTROY = 3;
    public static final int ERR_CODE_SERVICE_DESTROY = 4;
    public static final int ERR_CODE_BYE = 5;
    public static final int ERR_CODE_REVERSE_ERROR = 6;
    public static final int ERR_CODE_SOCKET_CLIENT = 7;
    public static final int ERR_CODE_WEB_SOCKET_CLIENT = 8;
    public static final int ERR_CODE_MIR_CLOSE = 9;
    public static final int ERR_CODE_VIRTUAL_DISPLAY = 10;
    public static final int ERR_CODE_WATCHDOG = 11;
    public static final int ERR_CODE_ENCODER_NOT_SUPPORTED = 12;
    public static final int ERR_CODE_DECODER_CONFIGURE = 13;


    public static final String ERR_MSG_DOG_OUT = "网络连接不稳定!";//"server 心跳连接检查异常";
    public static final String ERR_MSG_SOCKET_SERVER = "接收端网络异常";
    public static final String ERR_MSG_ACTIVITY_DESTROY = "页面退出";
    public static final String ERR_MSG_SERVICE_DESTROY = "服务被回收";
    public static final String ERR_MSG_BYE = "对方已经关闭了连接";
    public static final String ERR_MSG_REVERSE_ERROR = "PlayerDecoder error...";
    public static final String ERR_MSG_SOCKET_CLIENT = "发送端网络异常";
    public static final String ERR_MSG_WEB_SOCKET_CLIENT = "web socket client网络异常";
    public static final String ERR_MSG_MIR_CLOSE = "退出屏幕镜像,电视端屏幕镜像已结束";//"推流端退出";
    public static final String ERR_MSG_VIRTUAL_DISPLAY = "录屏服务创建异常";
    public static final String ERR_MSG_WATCHDOG = "网络连接不稳定";//"client 心跳连接检查异常";
    public static final String ERR_MSG_ENCODER_NOT_SUPPORTED = "不支持硬编码";
    public static final String ERR_MSG_DECODER_CONFIGURE = "解码器配置失败";

    void onError(int code, String errorMessage);
}
