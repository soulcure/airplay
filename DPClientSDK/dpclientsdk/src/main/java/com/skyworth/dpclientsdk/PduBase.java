package com.skyworth.dpclientsdk;

public class PduBase {

    public static final byte LOCAL_BYTES = 0x00;
    public static final byte LOCAL_STRING = 0x01;
    public static final byte VIDEO_FRAME = 0x02;
    public static final byte AUDIO_FRAME = 0x03;
    public static final byte PING_MSG = 0x0E;
    public static final byte PONG_MSG = 0x0F;


    /****************************************************
     * basic unit of data type length
     */
    public static final int PDU_BASIC_LENGTH = 4;

    /****************************************************
     * pdu header length
     */
    public static final int PDU_HEADER_LENGTH = 33;


    public static final int PDU_BODY_LENGTH_INDEX = 29;

    /****************************************************
     * index 0. pos:[0-4)
     * the begin flag of a pdu.
     */
    public static final int pduStartFlag = 0X12345678;

    /****************************************************
     * index 1. pos:[4-5)
     * 0 local channel data; 1 video frame ; 2 audio frame ;3 ping message ;4 pong message
     */
    public byte pduType;

    /****************************************************
     * index 2. pos:[5-9)  MediaCodec.BufferInfo.offset
     */
    public int offset;

    /****************************************************
     * index 3. pos:[9-13)  MediaCodec.BufferInfo.size
     */
    public int size;

    /****************************************************
     * index 4. pos:[13-21)  MediaCodec.BufferInfo.presentationTimeUs
     */
    public long presentationTimeUs;

    /****************************************************
     * index 5. pos:[21-25)  MediaCodec.BufferInfo.flags
     */
    public int flags;


    /****************************************************
     * index 6. pos:[25-29)  预留字段
     */
    public int reserved;

    /****************************************************
     *
     * index 7. pos:[29-33)
     */
    public int length;

    /***************************************************
     * index 8. pos:[33-infinity)
     */
    public byte[] body;


}


