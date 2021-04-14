package com.skyworth.dpclientsdk;

import android.media.MediaCodec;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

public class TcpClient extends PduUtil implements Runnable {

    private static final String TAG = TcpClient.class.getSimpleName();

    private static final int SOCKET_SEND_BUFFER_SIZE = 5 * 1024 * 1024; //5MB
    private static final int SOCKET_RECEIVE_BUFFER_SIZE = 1024 * 1024; //1MB


    private String mAddress;
    private int port;
    private StreamSourceCallback mCallback;
    private Handler mHandler;

    private SocketChannel socketChannel;

    /**
     * socket网络发送线程对象
     **/
    private TcpSendThread mSender;

    /**
     * 发送缓冲队列（用于socket不通的情况）
     */
    private final LinkedBlockingQueue<ByteBuffer> mCacheQueue;

    /**
     * 接收buffer
     */
    private final ByteBuffer receiveBuffer;
    /**
     * 发送队列
     */
    private final LinkedBlockingQueue<ByteBuffer> mSendQueue;
    private int random;

    public TcpClient(String address, int port, StreamSourceCallback callback) {
        Log.d(TAG, "Create tcpClient Task ");
        this.mAddress = address;
        this.port = port;
        mCallback = callback;
        mHandler = new Handler(Looper.getMainLooper());
        mCacheQueue = new LinkedBlockingQueue<>();
        mSendQueue = new LinkedBlockingQueue<>();
        receiveBuffer = ByteBuffer.allocate(SOCKET_RECEIVE_BUFFER_SIZE);
        Random r = new Random();
        random = r.nextInt(10000);
    }

    public void open() {
        Log.d(TAG, "tcpClient Connect to---" + mAddress + ":" + port);
        new Thread(this, "tcpClient-" + random).start();
    }


    public void reOpen(String address, int port, StreamSourceCallback callback) {
        Log.d(TAG, "reOpen tcpClient Task ");
        this.mAddress = address;
        this.port = port;
        this.mCallback = callback;
        open();
    }

    public String getIp() {
        return mAddress;
    }

    public int getPort() {
        return port;
    }

    public void setCallBack(StreamSourceCallback callback) {
        this.mCallback = callback;
    }


    @Override
    public void OnRec(final PduBase pduBase) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (pduBase.pduType == PduBase.LOCAL_BYTES) {
                    byte[] bytes = pduBase.body;
                    Log.d(TAG, "tcpClient local OnRec bytes:" + bytes.length);
                    if (mCallback != null) {
                        if (mCallback != null)
                            mCallback.onData(bytes);
                    }
                } else if (pduBase.pduType == PduBase.LOCAL_STRING) {
                    String data = new String(pduBase.body);
                    Log.d(TAG, "tcpClient local OnRec string:" + data);
                    if (mCallback != null) {
                        if (mCallback != null)
                            mCallback.onData(data);
                    }
                } else if (pduBase.pduType == PduBase.VIDEO_FRAME) {
                    //do nothing
                } else if (pduBase.pduType == PduBase.AUDIO_FRAME) {
                    //do nothing
                } else if (pduBase.pduType == PduBase.PING_MSG) {
                    String msg = new String(pduBase.body);
                    Log.d(TAG, "tcpClient OnRec ping msg:" + msg);
                    if (mCallback != null) {
                        if (mCallback != null)
                            mCallback.ping(msg);
                    }
                } else if (pduBase.pduType == PduBase.PONG_MSG) {
                    String msg = new String(pduBase.body);
                    Log.d(TAG, "tcpClient OnRec pong msg:" + msg);
                    if (mCallback != null) {
                        if (mCallback != null)
                            mCallback.pong(msg);
                    }
                }
            }
        });
    }

    @Override
    public void OnRec(PduBase pduBase, SocketChannel channel) {
        //for socket server
    }


    private boolean isDump = false;

    /**
     * 发送视频或音频帧
     *
     * @param type   0x02 video frame ; 0x03 audio frame
     * @param buffer video frame ; audio frame
     * @return
     */
    public void sendData(byte type, MediaCodec.BufferInfo bufferInfo, ByteBuffer buffer) {
        int flags = bufferInfo.flags;
        int size = mSendQueue.size();
        if (size > 60) { //缓存区大于60，执行丢帧
            isDump = true;
            Log.e("frame", "dump frame and mSendQueue---" + size);
            return;
        } else {
            if (isDump && flags == 0) {
                Log.e("frame", "dump B frame and mSendQueue---" + size);
                return;
            }
        }

        int length = buffer.remaining();
        ByteBuffer byteBuffer = ByteBuffer.allocate(PduBase.PDU_HEADER_LENGTH + length);
        byteBuffer.clear();

        byteBuffer.putInt(PduBase.pduStartFlag);
        byteBuffer.put(type);
        byteBuffer.putInt(bufferInfo.offset);
        byteBuffer.putInt(bufferInfo.size);
        byteBuffer.putLong(bufferInfo.presentationTimeUs);
        byteBuffer.putInt(flags);
        byteBuffer.putInt(0);  //reserved
        byteBuffer.putInt(length);
        byteBuffer.put(buffer);

        isDump = false;
        sendByteBuffer(byteBuffer, false);
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

        sendByteBuffer(byteBuffer, true);
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

        sendByteBuffer(byteBuffer, true);
    }


    public void ping(String data) {
        byte[] bytes = data.getBytes();
        int length = PduBase.PDU_HEADER_LENGTH + bytes.length;

        ByteBuffer byteBuffer = ByteBuffer.allocate(length);
        byteBuffer.clear();

        PduBase pduBase = new PduBase();
        pduBase.pduType = PduBase.PING_MSG;
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

        sendByteBuffer(byteBuffer, false);
    }


    private void sendByteBuffer(ByteBuffer byteBuffer, boolean isCache) {
        synchronized (this) {
            if (mSender != null && isOpen()) {
                mSender.send(byteBuffer);
            } else {
                if (mCallback != null) {
                    mCallback.onConnectState(ConnectState.DISCONNECT);
                }
                if (isCache) {
                    sendToCacheQueue(byteBuffer);
                }
            }
        }
    }


    @Override
    public void run() {
        Log.d(TAG, "run tcpClient Thread---");
        try {
            socketConnect();
            tcpReceive();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "tcpClient failed on  " + mAddress + ":" + port + "  " + e.getMessage());
            if (mCallback != null) {
                mCallback.onConnectState(ConnectState.DISCONNECT);
            }
        }
        Log.e(TAG, "StreamChannell--tcpClient-thread is exit:" + this + " random:" + random);
    }//#run


    /**
     * 连接socket
     *
     * @throws IOException
     */
    private void socketConnect() throws Exception {
        SocketAddress isa = new InetSocketAddress(InetAddress.getByName(mAddress), port);
        socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(true);
        socketChannel.socket().setSendBufferSize(SOCKET_SEND_BUFFER_SIZE);
        socketChannel.socket().setReceiveBufferSize(SOCKET_RECEIVE_BUFFER_SIZE);
        socketChannel.socket().setKeepAlive(true);
        //socketChannel.socket().setReuseAddress(false);
        socketChannel.socket().setSoLinger(false, 0);
        socketChannel.socket().setSoTimeout(5);  //超时5秒
        //socketChannel.socket().setTcpNoDelay(true);
        socketChannel.connect(isa);

        while (!socketChannel.finishConnect()) {  //非阻塞模式,必需设置
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Log.e(TAG, "socket connect" + e.toString());
            }
        }

        if (socketChannel.isConnected()) {
            Log.d(TAG, "connect socket success ");
            mSender = new TcpSendThread();
            mSender.start();    //开启发送线程
            if (mCallback != null) {
                mCallback.onConnectState(ConnectState.CONNECT);
            }


            while (!mCacheQueue.isEmpty()) {
                ByteBuffer buffer = mCacheQueue.poll();
                mSendQueue.offer(buffer);
            }

        } else {
            Log.e(TAG, "connect socket failed on port :" + port);
            if (mCallback != null) {
                mCallback.onConnectState(ConnectState.ERROR);
            }
        }

    }


    /**
     * 关闭socket
     */
    public void close() {
        if (mCallback != null) {
            mCallback = null;
        }

        if (mSender != null) {
            mSender.close();
        }

        try {
            if (socketChannel != null) {
                socketChannel.close();
                socketChannel = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "socket close error :" + e.toString());
        }

        mSendQueue.clear();
        mCacheQueue.clear();
    }


    /**
     * Socket连接是否是正常的
     *
     * @return 是否连接
     */
    public boolean isOpen() {
        return socketChannel != null && socketChannel.isConnected();
    }

    /**
     * socket receive
     *
     * @throws IOException
     */

    private void tcpReceive() throws IOException {
        Log.v(TAG, "tcp is Blocking model read buffer");
        receiveBuffer.clear();
        while (socketChannel != null && socketChannel.isConnected()
                && (socketChannel.read(receiveBuffer)) > 0) {
            receiveBuffer.flip();
            Log.v(TAG, "tcp read buffer");
            while (parsePdu(receiveBuffer) > 0) {
                Log.v(TAG, "read while loop");
            }
        }

    }

    private void sendToCacheQueue(ByteBuffer buffer) {
        mCacheQueue.offer(buffer);
    }

    /**
     * socket 发送线程类
     */
    private class TcpSendThread implements Runnable {
        boolean isExit = false;  //是否退出

        /**
         * 发送线程开启
         */
        public void start() {
            Thread thread = new Thread(this);
            thread.setName("tcpSend-" + random);
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
                Log.v(TAG, "tcpSend-thread is running");

                synchronized (mSendQueue) {
                    while (!mSendQueue.isEmpty()
                            && socketChannel != null
                            && socketChannel.isConnected()) {
                        ByteBuffer buffer = mSendQueue.poll();
                        if (buffer == null) {
                            continue;
                        }
                        buffer.flip();
                        Log.v(TAG, "tcp will send SendQueue size..." + mSendQueue.size());

                        if (buffer.remaining() > 0) {
                            int count;
                            try {
                                while (buffer.hasRemaining() && (count = socketChannel.write(buffer)) > 0) {
                                    Log.v(TAG, "tcp send buffer count:" + count);
                                    Log.e("colin", "colin start time06 --- tv Encoder data send finish by socket");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.e(TAG, "tcp send error " + e.toString());
                                if (mCallback != null) {
                                    mCallback.onConnectState(ConnectState.DISCONNECT);
                                }
                            } finally {
                                buffer.clear();
                            }
                        }
                    }//#while
                }

                synchronized (this) {
                    try {
                        wait();// 发送完消息后，线程进入等待状态
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Log.e(TAG, "tcp mSendQueue error---" + e.getMessage());
                    }
                }

            }

            Log.e(TAG, "StreamChannell--tcpSend-thread is exit:" + TcpClient.this + " random:" + random);

        }//#run


    }//# TcpSendThread
}