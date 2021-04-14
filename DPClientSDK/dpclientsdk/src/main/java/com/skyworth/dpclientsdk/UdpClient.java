package com.skyworth.dpclientsdk;

import android.media.MediaCodec;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.LinkedBlockingQueue;

public class UdpClient extends PduUtil implements Runnable {

    private static final String TAG = UdpClient.class.getSimpleName();

    private static final int SOCKET_RECEIVE_BUFFER_SIZE = 1024; //1KB

    private String mAddress;
    private int port;
    private StreamSourceCallback mCallback;
    private Handler mHandler;

    private DatagramSocket udpSocket;

    /**
     * socket网络发送线程对象
     **/
    private UdpSendThread mSender;


    /**
     * 发送队列
     */
    private final LinkedBlockingQueue<ByteBuffer> mSendQueue;

    public UdpClient(String address, int port, StreamSourceCallback callback) {
        Log.d(TAG, "Create udpClient Task---");
        this.mAddress = address;
        this.port = port;
        mCallback = callback;
        mHandler = new Handler(Looper.getMainLooper());
        mSendQueue = new LinkedBlockingQueue<>();
    }

    public void open() {
        Log.d(TAG, "udpClient Connect to---" + mAddress + ":" + port);
        new Thread(this, "udpClient-thread").start();
    }

    @Override
    public void OnRec(final PduBase pduBase) {
        if (mCallback != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (pduBase.pduType == PduBase.LOCAL_BYTES) {
                        byte[] bytes = pduBase.body;
                        Log.d(TAG, "udpClient local OnRec bytes:" + HexUtil.bytes2HexString(bytes));
                        mCallback.onData(bytes);
                    } else if (pduBase.pduType == PduBase.LOCAL_STRING) {
                        String data = new String(pduBase.body);
                        Log.d(TAG, "udpClient local OnRec string:" + data);
                        mCallback.onData(data);
                    } else if (pduBase.pduType == PduBase.VIDEO_FRAME) {
                        //do nothing
                    } else if (pduBase.pduType == PduBase.AUDIO_FRAME) {
                        //do nothing
                    } else if (pduBase.pduType == PduBase.PING_MSG) {
                        String msg = new String(pduBase.body);
                        Log.d(TAG, "udpClient OnRec ping msg:" + msg);
                        mCallback.ping(msg);
                    } else if (pduBase.pduType == PduBase.PONG_MSG) {
                        String msg = new String(pduBase.body);
                        Log.d(TAG, "udpClient OnRec pong msg:" + msg);
                        mCallback.pong(msg);
                    }
                }
            });
        }
    }

    @Override
    public void OnRec(PduBase pduBase, SocketChannel channel) {
        //for socket server
    }

    /**
     * 发送视频或音频帧
     *
     * @param type   0x02 video frame ; 0x03 audio frame
     * @param buffer video frame ; audio frame
     * @return
     */
    public void sendData(byte type, MediaCodec.BufferInfo bufferInfo, ByteBuffer buffer) {
        int length = buffer.remaining();

        ByteBuffer byteBuffer = ByteBuffer.allocate(PduBase.PDU_HEADER_LENGTH + length);
        byteBuffer.clear();

        byteBuffer.putInt(PduBase.pduStartFlag);
        byteBuffer.put(type);
        byteBuffer.putInt(bufferInfo.offset);
        byteBuffer.putInt(bufferInfo.size);
        byteBuffer.putLong(bufferInfo.presentationTimeUs);
        byteBuffer.putInt(bufferInfo.flags);
        byteBuffer.putInt(0);  //reserved
        byteBuffer.putInt(length);
        byteBuffer.put(buffer);

        sendByteBuffer(byteBuffer);
    }


    /**
     * 发送local channel data
     *
     * @return
     */
    public void sendData(byte[] data) {
        int length = PduBase.PDU_HEADER_LENGTH + data.length;

        ByteBuffer byteBuffer = ByteBuffer.allocate(length);
        byteBuffer.clear();

        PduBase pduBase = new PduBase();
        pduBase.pduType = PduBase.LOCAL_BYTES;
        pduBase.length = data.length;
        pduBase.body = data;

        byteBuffer.putInt(PduBase.pduStartFlag);
        byteBuffer.put(pduBase.pduType);
        byteBuffer.putInt(pduBase.offset);
        byteBuffer.putInt(pduBase.size);
        byteBuffer.putLong(pduBase.presentationTimeUs);
        byteBuffer.putInt(pduBase.flags);
        byteBuffer.putInt(pduBase.reserved);  //reserved
        byteBuffer.putInt(pduBase.length);
        byteBuffer.put(pduBase.body);

        sendByteBuffer(byteBuffer);
    }

    /**
     * 发送local channel data
     *
     * @return
     */
    public void sendData(String data) {
        byte[] bytes = data.getBytes();
        int length = PduBase.PDU_HEADER_LENGTH + bytes.length;

        ByteBuffer byteBuffer = ByteBuffer.allocate(length);
        byteBuffer.clear();

        PduBase pduBase = new PduBase();
        pduBase.pduType = PduBase.LOCAL_STRING;
        pduBase.length = bytes.length;
        pduBase.body = bytes;

        byteBuffer.putInt(PduBase.pduStartFlag);
        byteBuffer.put(pduBase.pduType);
        byteBuffer.putInt(pduBase.offset);
        byteBuffer.putInt(pduBase.size);
        byteBuffer.putLong(pduBase.presentationTimeUs);
        byteBuffer.putInt(pduBase.flags);
        byteBuffer.putInt(pduBase.reserved);  //reserved
        byteBuffer.putInt(pduBase.length);
        byteBuffer.put(pduBase.body);

        sendByteBuffer(byteBuffer);

    }


    private void sendByteBuffer(ByteBuffer byteBuffer) {
        synchronized (this) {
            if (mSender != null && isOpen()) {
                mSender.send(byteBuffer);
            } else {
                mCallback.onConnectState(ConnectState.ERROR);
            }
        }
    }


    @Override
    public void run() {
        Log.d(TAG, "run udpClient Thread---");
        try {
            socketConnect();
            udpReceive();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "udpClient failed on  " + mAddress + ":" + port + "  " + e.getMessage());
            if (mCallback != null) {
                mCallback.onConnectState(ConnectState.ERROR);
            }
        }

    }//#run


    /**
     * 连接socket
     *
     * @throws IOException
     */
    private void socketConnect() throws IOException {
        InetAddress ipAddress = InetAddress.getByName(mAddress);

        udpSocket = new DatagramSocket();
        udpSocket.connect(ipAddress, port); //连接

        if (udpSocket.isConnected()) {
            Log.d(TAG, "connect udpClient success---");

            mSender = new UdpSendThread();
            mSender.start();

            if (mCallback != null) {
                mCallback.onConnectState(ConnectState.CONNECT);
            }

        } else {
            Log.e(TAG, "connect udp socket failed on port :" + port);
            if (mCallback != null) {
                mCallback.onConnectState(ConnectState.ERROR);
            }
        }

    }


    /**
     * 关闭socket
     */
    public void close() {
        if (udpSocket != null) {
            udpSocket.close();
            udpSocket = null;
        }
        if (mSender != null) {
            mSender.close();
        }

        mSendQueue.clear();
    }


    /**
     * Socket连接是否是正常的
     *
     * @return 是否连接
     */
    public boolean isOpen() {
        return udpSocket != null && udpSocket.isConnected();
    }

    /**
     * socket receive
     *
     * @throws IOException
     */

    private void udpReceive() throws IOException {
        while (udpSocket != null && udpSocket.isConnected()) {
            byte[] container = new byte[SOCKET_RECEIVE_BUFFER_SIZE];
            DatagramPacket recPacket = new DatagramPacket(container, container.length);
            udpSocket.receive(recPacket); // blocks until a packet is received

            byte[] buffer = recPacket.getData();  //read buffer
            parsePdu(buffer); //read buffer
        }

    }


    /**
     * socket 发送线程类
     */
    private class UdpSendThread implements Runnable {
        boolean isExit = false;  //是否退出

        /**
         * 发送线程开启
         */
        public void start() {
            Thread thread = new Thread(this);
            thread.setName("udpSend-thread");
            thread.start();
        }

        public void send(ByteBuffer buffer) {
            synchronized (this) {
                if (buffer != null) {
                    mSendQueue.offer(buffer);
                    notify();
                }
            }

        }


        /**
         * 发送线程关闭
         */
        public void close() {
            synchronized (this) { // 激活线程
                isExit = true;
                notify();
            }
        }

        @Override
        public void run() {
            while (!isExit) {

                Log.v(TAG, "udpSend-thread send loop is running");

                synchronized (mSendQueue) {
                    while (!mSendQueue.isEmpty()
                            && udpSocket != null
                            && udpSocket.isConnected()) {

                        ByteBuffer buffer = mSendQueue.poll();
                        if (buffer == null) {
                            continue;
                        }
                        buffer.flip();

                        Log.v(TAG, "udp will send buffer to:" + mAddress + ":" + port +
                                "&header:" + buffer.getInt(0) +
                                "&length:" + buffer.getInt(PduBase.PDU_BODY_LENGTH_INDEX) +
                                "&mSendQueue size:" + mSendQueue.size());

                        int limit = 65507;//The limit on a UDP datagram payload in IPv4 is 65535-28=65507 bytes
                        int totalLen = buffer.remaining(); //帧数据总长度
                        try {
                            if (totalLen > limit) {
                                byte[] src = buffer.array();
                                int count = totalLen / limit + 1;
                                for (int i = 0; i < count; i++) {
                                    int offset = i * limit;
                                    int length;
                                    if (i < count - 1) {
                                        length = limit;
                                    } else {
                                        length = totalLen - (i * limit);
                                    }
                                    byte[] dst = new byte[length];
                                    System.arraycopy(src, offset, dst, 0, length);

                                    InetAddress ipAddress = InetAddress.getByName(mAddress);
                                    DatagramPacket sendPacket = new DatagramPacket(dst, dst.length, ipAddress, port);
                                    udpSocket.send(sendPacket);
                                    Log.e("colin", "colin start time06 --- tv Encoder data peer send finish by udp socket");
                                }

                            } else {
                                InetAddress ipAddress = InetAddress.getByName(mAddress);
                                DatagramPacket sendPacket = new DatagramPacket(buffer.array(), buffer.remaining(), ipAddress, port);
                                udpSocket.send(sendPacket);
                                Log.e("colin", "colin start time06 --- tv Encoder data send finish by udp socket");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e(TAG, "udp send error---" + e.getMessage());
                            if (mCallback != null) {
                                mCallback.onConnectState(ConnectState.ERROR);
                            }
                        }
                    }//#while
                }

                synchronized (this) {
                    try {
                        wait();// 发送完消息后，线程进入等待状态
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Log.e(TAG, "udp mSendQueue error---" + e.getMessage());
                    }
                }
            }

            Log.e(TAG, "udpSend-thread is exit---");

        }//#run

    }//# UdpSendThread
}