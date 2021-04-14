package com.swaiotos.skymirror.sdk.Command;

import org.yaml.snakeyaml.Yaml;


public class Command {

    public static int CODEC_AVC_FLAG = 1;  //H264
    public static int CODEC_HEVC_FLAG = 1 << 1;  //H265


    //client to server
    public static final String CheckVersion = "checkVersion";
    public static final String Start = "start";
    public static final String RemoteIp = "remoteIp";
    public static final String EncoderCodecType = "encoderCodecType";


    public static final String SetWH = "setWH";
    public static final String FrameWidth = "frameWidth";
    public static final String FrameHeight = "frameHeight";


    public static final String Client = "client"; //client 心跳


    //server to client
    public static final String ServerVersion = "serverVersion";
    public static final String CodecSupport = "codecSupport";
    public static final String DecoderStatus = "decoderStatus";
    public static final String SendData = "sendData";
    public static final String Dog = "dog";


    public static final String Server = "server"; //server 心跳


    //both
    public static final String Bye = "bye";
    public static final String ErrCode = "errCode";
    public static final String ErrMsg = "errMsg";

    public static final String COLON = ": ";


    /**
     * client send to server checkVersion
     *
     * @param ver 3.0
     * @return CheckVersion:3.0
     */
    public static String setCheckVersion(String ver) {
        return CheckVersion + COLON + ver;
    }


    /**
     * server onRead String
     *
     * @param yamlStr CheckVersion:3.0
     * @return 3.0
     */
    public static CheckVer getCheckVersion(String yamlStr) {
        Yaml yaml = new Yaml();
        return yaml.loadAs(yamlStr, CheckVer.class);
    }


    /**
     * server send to client ServerVersion
     *
     * @param ver 3.0
     * @return ServerVersion:3.0
     */
    public static String setServerVersionCodec(String ver, int codec) {
        return ServerVersion + COLON + ver +
                "\n" +
                CodecSupport + COLON + codec;
    }


    /**
     * client onRead String
     *
     * @param yamlStr ServerVersion:3.0
     * @return 3.0
     */
    public static ServerVersionCodec getServerVersionCodec(String yamlStr) {
        Yaml yaml = new Yaml();
        return yaml.loadAs(yamlStr, ServerVersionCodec.class);
    }


    /**
     * @param start            true
     * @param clientIp         192.168.50.111
     * @param encoderCodecType 1
     * @return Start:true \n RemoteIp:192.168.50.111 \n EncoderCodecType:1
     */
    public static String setClientIpCodec(boolean start, String clientIp, int encoderCodecType) {
        return Start + COLON + start +
                "\n" +
                RemoteIp + COLON + clientIp +
                "\n" +
                EncoderCodecType + COLON + encoderCodecType;
    }


    public static ClientIpCodec getClientIpCodec(String yamlStr) {
        Yaml yaml = new Yaml();
        return yaml.loadAs(yamlStr, ClientIpCodec.class);
    }

    public static String setDecoderStatus(boolean status) {
        return DecoderStatus + COLON + status;
    }


    public static DecoderStatus getDecoderStatus(String yamlStr) {
        Yaml yaml = new Yaml();
        return yaml.loadAs(yamlStr, DecoderStatus.class);
    }


    /**
     *
     */
    public static String setFrameWH(boolean setWH, int width, int height) {
        return SetWH + COLON + setWH +
                "\n" +
                FrameWidth + COLON + width +
                "\n" +
                FrameHeight + COLON + height;
    }


    /**
     * @param yamlStr FrameWidth:1980 \n FrameHeight:1080
     * @return FrameWH
     */
    public static FrameWH getFrameWH(String yamlStr) {
        Yaml yaml = new Yaml();
        return yaml.loadAs(yamlStr, FrameWH.class);
    }


    public static String setSendData(boolean status) {
        return SendData + COLON + status;
    }


    public static SendData getSendData(String yamlStr) {
        Yaml yaml = new Yaml();
        return yaml.loadAs(yamlStr, SendData.class);
    }


    public static String setByeData(boolean status, int errCode, String errMsg) {
        return Bye + COLON + status +
                "\n" +
                ErrCode + COLON + errCode +
                "\n" +
                ErrMsg + COLON + errMsg;
    }


    public static Bye getByeData(String yamlStr) {
        Yaml yaml = new Yaml();
        return yaml.loadAs(yamlStr, Bye.class);
    }


    public static String setDogData(long ts) {
        return Dog + COLON + ts;
    }


    public static Dog getDogData(String yamlStr) {
        Yaml yaml = new Yaml();
        return yaml.loadAs(yamlStr, Dog.class);
    }


    public static String setClientData(long ts) {
        return Client + COLON + ts;
    }


    public static String setServerHeatBeat(long ts) {
        return Server + COLON + ts;
    }


    public static ServerHeartBeat getServerHeatBeat(String yamlStr) {
        Yaml yaml = new Yaml();
        return yaml.loadAs(yamlStr, ServerHeartBeat.class);
    }


    // Server

}