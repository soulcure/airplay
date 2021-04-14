package com.skyworth.bleclient;

import android.util.Log;

import java.nio.ByteBuffer;


public abstract class BlePduUtil {

    private static final String TAG = BlePduUtil.class.getSimpleName();


    /**
     * socket client接收数据回调
     *
     * @param blePdu
     */
    public abstract void OnRec(BlePdu blePdu);


    /**
     * socket server端解析包
     *
     * @param buffer
     * @return
     */
    public int parsePdu(ByteBuffer buffer) {
        if (buffer.limit() > BlePdu.PDU_BASIC_LENGTH) {
            byte begin = buffer.get(0);
            Log.v(TAG, "pdu begin is " + begin);
            if (begin != BlePdu.pduStartFlag) {
                Log.e(TAG, "pdu header error buffer limit:" + buffer.limit());
                buffer.clear();
                return -1;
            }
        } else {    //did not contain a start flag yet.continue read.
            Log.v(TAG, "did not has full start flag");
            buffer.position(buffer.limit());
            buffer.limit(buffer.capacity());
            return 0;
        }

        if (buffer.limit() >= BlePdu.PDU_HEADER_LENGTH) {
            //has full header
            int bodyLength = buffer.getShort(BlePdu.PDU_BODY_LENGTH_INDEX);
            int totalLength = bodyLength + BlePdu.PDU_HEADER_LENGTH;

            if (totalLength < buffer.limit()) {
                //has a full pack.
                byte[] packByte = new byte[totalLength];
                buffer.get(packByte);
                BlePdu blePdu = buildPdu(packByte);
                OnRec(blePdu);
                buffer.compact();
                //read to read.
                buffer.flip();

                Log.v(TAG, "pdu read is totalLength:" + totalLength);
                return totalLength;

            } else if (totalLength == buffer.limit()) {
                //has a full pack.
                byte[] packByte = new byte[totalLength];
                buffer.get(packByte);
                BlePdu blePdu = buildPdu(packByte);
                OnRec(blePdu);
                buffer.compact();
                //read to write.
                buffer.clear();

                Log.v(TAG, "pdu read is totalLength:" + totalLength);

                return 0;
            } else {
                buffer.position(buffer.limit());
                buffer.limit(buffer.capacity());
                return 0;
            }

        } else {
            Log.v(TAG, " not a full header");
            buffer.position(buffer.limit());
            buffer.limit(buffer.capacity());
            return -2;
        }
    }


    public static BlePdu buildPdu(byte[] bytes) {
        BlePdu units = new BlePdu();
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
        buffer.put(bytes);
        buffer.flip();

        buffer.get();  //units.flag
        units.pduType = buffer.get();
        short length = buffer.getShort();
        units.length = length;
        units.body = new byte[length];
        buffer.get(units.body);
        return units;
    }


}
