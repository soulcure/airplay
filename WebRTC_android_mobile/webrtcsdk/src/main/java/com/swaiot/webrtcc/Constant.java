package com.swaiot.webrtcc;

/**
 * 地址配置 信令
 * Created by taxiao on 2019/8/14
 * CSDN:http://blog.csdn.net/yin13753884368/article
 * Github:https://github.com/taxiao213
 * 微信公众号:他晓
 */
public class Constant {
    public static final String URL = "wss://www.kuiper.fun:3000";//server服务器地址
    public static final String STUN = "stun:134.175.116.164";//穿透服务器
    public static final String CHANNEL = "channel";
    public static final String REGISTER_RESPONSE = "register_response";//注册回复
    public static final int RESPONSE_SUCCEED = 1;//1成功
    public static final int RESPONSE_FAILURE = 2;//2失败

    public static final String OPEN = "open";
    public static final String REGISTER = "register";//注册


    public static final String LOGIN = "CLIENT_USER_EVENT_LOGIN";//登录
    public static final String OFFER = "SIGNALING_OFFER";//拨打
    public static final String ANSWER = "SIGNALING_ANSWER";//接听
    public static final String UPDATE_USERS = "SERVER_USER_EVENT_UPDATE_USERS";
    public static final String CANDIDATE = "SIGNALING_CANDIDATE";//ice互传



    public static final String INCALL_RESPONSE = "incall_response";//接听回复
    //public static final String OFFER = "offer";//发送流


    public static final String CALL_RESPONSE = "call_response";//拨打回复
    public static final int VOLUME = 10;//声音调节

    public static final String VIDEO_TRACK_ID = "videtrack";
    public static final String AUDIO_TRACK_ID = "audiotrack";
    public static final String LOCAL_VIDEO_STREAM = "localVideoStream";
    public static final String LOCAL_AUDIO_STREAM = "localAudioStream";

    public static final int VIDEO_RESOLUTION_WIDTH = 320;
    public static final int VIDEO_RESOLUTION_HEIGHT = 240;
    public static final int VIDEO_FPS = 60;
}
