package com.skyworth.dpclientsdk;

import android.media.MediaCodec;
import android.text.TextUtils;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;


public class UdpServer extends PduUtil implements Runnable {

    private static final String TAG = UdpServer.class.getSimpleName();

    private static final int BUFFER_SIZE = 2 * 1024 * 1024; //2MB

    private StreamSinkCallback mCallback;

    private DatagramSocket udpSocket;
    private int port;
    private ByteBuffer receiveBuffer;

    private ProcessHandler processHandler;  //子线程Handler

    public UdpServer(int port, StreamSinkCallback callback) {
        this.port = port;
        this.mCallback = callback;
        receiveBuffer = ByteBuffer.allocate(BUFFER_SIZE);

        processHandler = new ProcessHandler("udp-server", true);
    }

    /**
     * 打开 udp server
     */
    public void open() {
        new Thread(this, "udpServer-thread").start();
    }


    /**
     * 关闭 udp server
     */
    public void close() {
        if (udpSocket != null) {
            udpSocket.close();
            udpSocket = null;
        }
    }

    /**
     * 是否开启 udp server 绑定
     */
    public boolean isOpen() {
        return udpSocket != null && udpSocket.isBound();
    }

    @Override
    public void run() {
        try {
            udpServerStart();
        } catch (SocketException e) {
            e.printStackTrace();
            String err = e.getMessage();
            Log.e(TAG, "udpServer error----" + err);

            if (!TextUtils.isEmpty(err) && err.contains("Socket closed")) {
                if (mCallback != null) {
                    //客户端正常关闭连接
                    mCallback.onConnectState(ConnectState.DISCONNECT);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "udpServer error----" + e.getMessage());
            if (mCallback != null) {
                mCallback.onConnectState(ConnectState.ERROR);
            }
        }

        Log.e(TAG, "udpServer exit---");
    }


    @Override
    public void OnRec(PduBase pduBase, SocketChannel channel) {

    }


    @Override
    public void OnRec(final PduBase pduBase) {
        if (mCallback != null) {
            processHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (pduBase.pduType == PduBase.LOCAL_BYTES) {
                        byte[] cmd = pduBase.body;
                        Log.d(TAG, "udpServer local OnRec byte length:" + cmd.length);
                        mCallback.onData(cmd, null);
                    } else if (pduBase.pduType == PduBase.LOCAL_STRING) {
                        byte[] cmd = pduBase.body;
                        String msg = new String(cmd);
                        Log.d(TAG, "udpServer local OnRec String:" + msg);
                        mCallback.onData(msg, null);
                    } else if (pduBase.pduType == PduBase.VIDEO_FRAME) {
                        Log.d(TAG, "udpServer OnRec videoFrame size:" + pduBase.size);
                        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                        bufferInfo.set(pduBase.offset, pduBase.size, pduBase.presentationTimeUs, pduBase.flags);

                        ByteBuffer byteBuffer = ByteBuffer.wrap(pduBase.body);
                        mCallback.onVideoFrame(bufferInfo, byteBuffer, null);

                    } else if (pduBase.pduType == PduBase.AUDIO_FRAME) {
                        Log.d(TAG, "udpServer OnRec audioFrame size:" + pduBase.size);
                        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                        bufferInfo.set(pduBase.offset, pduBase.size, pduBase.presentationTimeUs, pduBase.flags);

                        ByteBuffer byteBuffer = ByteBuffer.wrap(pduBase.body);
                        mCallback.onVideoFrame(bufferInfo, byteBuffer, null);
                    }
                }

            });
        }
    }


    private void udpServerStart() throws Exception {
        udpSocket = new DatagramSocket(port);
        Log.d(TAG, "udpServer bind to port:" + port);

        if (udpSocket.isBound()) {
            if (mCallback != null) {
                mCallback.onConnectState(ConnectState.CONNECT);
            }
        }

        receiveBuffer.clear();

        while (udpSocket.isBound()) {
            byte[] container = new byte[BUFFER_SIZE];
            DatagramPacket packet = new DatagramPacket(container, container.length);

            udpSocket.receive(packet);  // blocks until a packet is received
            byte[] buffer = packet.getData();  //read buffer

            int length = packet.getLength();
            receiveBuffer.put(buffer, 0, length);

            receiveBuffer.flip();
            while (parsePdu(receiveBuffer) > 0) {
                Log.v(TAG, "read while loop---");
            }
        }
    }


}






