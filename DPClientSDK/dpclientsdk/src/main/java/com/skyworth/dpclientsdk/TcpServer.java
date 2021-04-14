package com.skyworth.dpclientsdk;

import android.media.MediaCodec;
import android.util.Log;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class TcpServer extends PduUtil implements Runnable {

    private static final String TAG = TcpServer.class.getSimpleName();

    public static final int BUFFER_SIZE_HIGH = 5 * 1024 * 1024; //5MB
    public static final int BUFFER_SIZE_DEFAULT = 500 * 1024; //500KB
    public static final int BUFFER_SIZE_LOW = 200 * 1024; //200KB

    private volatile boolean isExit = false;
    private boolean isOpen;

    private Selector selector;
    private StreamSinkCallback mCallback;
    private int port;
    private ServerSocketChannel listenerChannel;

    private ProcessHandler processHandler;  //子线程Handler

    private int mBufferSize;

    private List<Socket> clientSockets;

    public TcpServer(int port, int bufferSize, StreamSinkCallback callback) {
        this.port = port;
        mBufferSize = bufferSize;
        clientSockets = new ArrayList<>();

        if (bufferSize == -1) {
            mBufferSize = BUFFER_SIZE_DEFAULT;
        }

        if (mBufferSize < BUFFER_SIZE_LOW) {
            mBufferSize = BUFFER_SIZE_LOW;
        }

        if (mBufferSize > BUFFER_SIZE_HIGH) {
            mBufferSize = BUFFER_SIZE_HIGH;
        }


        this.mCallback = callback;
        processHandler = new ProcessHandler("ProcessHandler", true);
    }

    public void open() {
        Log.d(TAG, "Socket Server Listener to port:" + port);
        new Thread(this, "tcpServer-thread").start();
    }

    public boolean isOpen() {
        return isOpen;
    }

    /**
     * 关闭tcp server
     */
    public void close() {
        try {
            isOpen = false;
            isExit = true;
            for (Socket item : clientSockets) {
                item.close();
            }
            clientSockets.clear();

            selector.close();
            listenerChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "close tcp server error:" + e.toString());
        }
    }


    @Override
    public void run() {
        try {
            tcpServerStart();
        } catch (BindException e) {
            Log.d(TAG, "TcpServer listen:" + e.toString());
            if (mCallback != null) {
                mCallback.onConnectState(ConnectState.CONNECT);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "TcpServer listen:" + e.toString());
            isOpen = false;
            if (mCallback != null) {
                mCallback.onConnectState(ConnectState.ERROR);
            }
        }
    }

    @Override
    public void OnRec(PduBase pduBase) {
        //for socket client
    }

    @Override
    public void OnRec(final PduBase pduBase, final SocketChannel channel) {
        if (mCallback != null) {
            processHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (pduBase.pduType == PduBase.LOCAL_BYTES) {
                        byte[] bytes = pduBase.body;
                        //Log.d(TAG, "TcpServer local OnRec bytes:" + HexUtil.bytes2HexString(bytes));
                        Log.d(TAG, "TcpServer local OnRec bytes length:" + bytes.length);
                        mCallback.onData(bytes, channel);
                    } else if (pduBase.pduType == PduBase.LOCAL_STRING) {
                        String data = new String(pduBase.body);
                        Log.d(TAG, "TcpServer local OnRec string:" + data);
                        mCallback.onData(data, channel);
                    } else if (pduBase.pduType == PduBase.VIDEO_FRAME) {
                        Log.d(TAG, "TcpServer OnRec videoFrame size:" + pduBase.size);
                        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                        bufferInfo.set(pduBase.offset, pduBase.size, pduBase.presentationTimeUs, pduBase.flags);

                        ByteBuffer byteBuffer = ByteBuffer.wrap(pduBase.body);
                        mCallback.onVideoFrame(bufferInfo, byteBuffer, channel);
                    } else if (pduBase.pduType == PduBase.AUDIO_FRAME) {
                        Log.d(TAG, "TcpServer OnRec audioFrame size:" + pduBase.size);
                        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                        bufferInfo.set(pduBase.offset, pduBase.size, pduBase.presentationTimeUs, pduBase.flags);

                        ByteBuffer byteBuffer = ByteBuffer.wrap(pduBase.body);
                        mCallback.onAudioFrame(bufferInfo, byteBuffer, channel);
                    } else if (pduBase.pduType == PduBase.PING_MSG) {
                        pongMsg(pduBase, channel);   //pong client

                        String msg = new String(pduBase.body);
                        Log.d(TAG, "TcpServer OnRec ping msg:" + msg);
                        mCallback.ping(msg, channel);
                    } else if (pduBase.pduType == PduBase.PONG_MSG) {
                        String msg = new String(pduBase.body);
                        Log.d(TAG, "TcpServer OnRec pong msg:" + msg);
                        mCallback.pong(msg, channel);
                    }
                }
            });
        }
    }

    private void pongMsg(PduBase pduBase, SocketChannel channel) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(PduBase.PDU_HEADER_LENGTH + pduBase.body.length);

        byteBuffer.putInt(PduBase.pduStartFlag);
        if (pduBase.pduType == PduBase.PING_MSG) {
            byteBuffer.put(PduBase.PONG_MSG);
        } else {
            byteBuffer.put(PduBase.PING_MSG);
        }
        byteBuffer.putInt(pduBase.offset);
        byteBuffer.putInt(pduBase.size);
        byteBuffer.putLong(pduBase.presentationTimeUs);
        byteBuffer.putInt(pduBase.flags);
        byteBuffer.putInt(pduBase.reserved);  //reserved
        byteBuffer.putInt(pduBase.length);
        byteBuffer.put(pduBase.body);
        byteBuffer.flip();

        try {
            channel.write(byteBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void tcpServerStart() throws Exception {
        // 创建选择器
        selector = Selector.open();
        // 打开监听信道
        listenerChannel = ServerSocketChannel.open();

        listenerChannel.socket().setReuseAddress(true);
        // 与本地端口绑定
        listenerChannel.socket().bind(new InetSocketAddress(port));

        listenerChannel.configureBlocking(false);
        // 注册到Selector中，ACCEPT操作
        listenerChannel.register(selector, SelectionKey.OP_ACCEPT);

        Log.d(TAG, "tcp server bind to port:" + port);

        isOpen = true;
        if (mCallback != null) {
            mCallback.onConnectState(ConnectState.CONNECT);
        }

        // 不断轮询Selector
        while (!isExit) {
            // 当准备好的通道大于0才有往下的操作
            if (selector.select() > 0) {
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    // 处理过的key要移除掉
                    iterator.remove();
                    // 接收状态
                    if (key.isAcceptable()) {
                        handleAccept(key);
                    }
                    // 可读状态
                    if (key.isReadable()) {
                        handleRead(key);
                    }

                }
            }
        }
    }


    /**
     * 客户端连接到来
     *
     * @param key
     * @throws Exception
     */
    private void handleAccept(SelectionKey key) throws Exception {
        Log.d(TAG, "tcp server handleAccept:" + key.channel().isOpen());
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        // 获取客户端链接，并注册到Selector中
        SocketChannel clientChannel = serverSocketChannel.accept();
        clientSockets.add(clientChannel.socket());

        clientChannel.configureBlocking(false);
        // 讲通道注册到Selector里头，然后设置为读操作
        clientChannel.register(key.selector(), SelectionKey.OP_READ, ByteBuffer.allocate(mBufferSize));
        //clientChannel.register(key.selector(), SelectionKey.OP_READ);

    }


    /**
     * 客户端发送到的数据可读
     *
     * @param key
     * @throws Exception
     */
    private void handleRead(SelectionKey key) throws Exception {
        try {
            Log.d(TAG, "tcp server handleRead read to ByteBuffer...");

            SocketChannel clientChannel = (SocketChannel) key.channel();
            ByteBuffer byteBuffer = (ByteBuffer) key.attachment();

            long byteRead;
            while ((byteRead = clientChannel.read(byteBuffer)) > 0) {
                // 将缓冲区准备为数据传出状态
                byteBuffer.flip();
                int readResult = 0;
                while ((readResult = parsePdu(byteBuffer, clientChannel)) > 0) {
                    //loop parse
                    Log.d(TAG, "socket read length:" + readResult);
                }
                //判断起始标记
                key.interestOps(SelectionKey.OP_READ);
            }

            if (byteRead == -1) { //客户端关闭了socket
                // 没有读取到内容的情况
                clientChannel.close();
                key.cancel();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}






