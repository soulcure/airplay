package com.skyworth.dpclientsdk.ble;

import java.nio.ByteBuffer;

public class BlePdu {

    public static final byte TEMP_CMD = 0x00;   //原通道控制指令
    public static final byte TEMP_PROTO = 0x01; //新增临时连接的协议命名

    /****************************************************
     * basic unit of data type length
     */
    public static final int PDU_BASIC_LENGTH = 1;

    /****************************************************
     * pdu header length
     */
    public static final int PDU_HEADER_LENGTH = 4;


    public static final int PDU_BODY_LENGTH_INDEX = 2;

    /****************************************************
     * index 0. pos:[0-1)
     * the begin flag of a pdu.
     */
    public static final byte pduStartFlag = (byte) 0XFF;

    /****************************************************
     * index 1. pos:[1-2)
     * 0 local channel data; 1 video frame ; 2 audio frame ;3 ping message ;4 pong message
     */
    public byte pduType;

    /****************************************************
     *
     * index 1. pos:[2-4)
     */
    public short length;

    /***************************************************
     * index 2. pos:[4-infinity)
     */
    public byte[] body;


    public byte[] build() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(PDU_HEADER_LENGTH + body.length);
        byteBuffer.put(pduStartFlag);
        byteBuffer.put(pduType);
        byteBuffer.putShort(length);
        byteBuffer.put(body);
        return byteBuffer.array();
    }

}


