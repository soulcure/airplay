package com.swaiotos.skymirror.sdk.reverse;

import android.content.Context;
import android.content.res.Configuration;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Range;
import android.view.MotionEvent;
import android.view.Surface;

import androidx.annotation.RequiresApi;

import com.skyworth.dpclientsdk.ConnectState;
import com.skyworth.dpclientsdk.RequestCallback;
import com.skyworth.dpclientsdk.StreamSinkCallback;
import com.skyworth.dpclientsdk.TcpServer;
import com.skyworth.dpclientsdk.UdpServer;
import com.skyworth.dpclientsdk.WebSocketServer;
import com.swaiotos.skymirror.sdk.Command.Bye;
import com.swaiotos.skymirror.sdk.Command.CheckVer;
import com.swaiotos.skymirror.sdk.Command.ClientIpCodec;
import com.swaiotos.skymirror.sdk.Command.Command;
import com.swaiotos.skymirror.sdk.Command.FrameWH;
import com.swaiotos.skymirror.sdk.capture.MirManager;
import com.swaiotos.skymirror.sdk.data.FrameInfo;
import com.swaiotos.skymirror.sdk.data.MediaCodecConfig;
import com.swaiotos.skymirror.sdk.data.PortKey;
import com.swaiotos.skymirror.sdk.util.DLNACommonUtil;
import com.swaiotos.skymirror.sdk.util.MediaConfig;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * 同屏控制的控制方
 * 接收投屏端发送过来的视频流，解码播放
 */
public class PlayerDecoder {

    //常量定义--start---
    private static final String TAG = PlayerDecoder.class.getSimpleName();
    private static final String MIR_SERVER_VERSION = "3.0";  //支持UDP协议传流

    private static final int HEART_BEAT_INTERVAL = 5; //心跳间隔5秒
    //常量定义--end---

    private ScheduledExecutorService heartBeatScheduled;

    private IDrawListener drawListener;
    private IPlayerListener playerListener;


    private int mEncoderCodecType; //接受到对方的编码类型

    private Context mContext;
    private int mDecoderCodecSupportType;  //硬件编解码器信息

    private WebSocketServer mWebSocketServer;
    private Set<Integer> mWebSocketClients = new HashSet<>();

    //video socket 传输
    private TcpServer tcpServer;
    private UdpServer udpServer;
    private LinkedBlockingQueue<FrameInfo> videoList = null;

    private boolean videoDecoderConfigured = false;

    private MediaCodec mVideoDecoder = null;

    private int mWatchDog = 0;
    private long mWatchTs;


    private volatile boolean isExit = false;

    private int mFrameWidth = 1080;
    private int mFrameHeight = 1920;


    private Surface mSurface;
    private Handler mHandler;

    public PlayerDecoder(Context context) {
        mContext = context;

        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void run() {
                checkDecoderSupportCodec();
            }
        }).start();

        mHandler = new Handler(Looper.getMainLooper());
        mWatchDog = 0;

        //控制端建立 webSocketServer 端，发送控制事件
        initWebSocketServer();
        //视频流接收方（接收方：socket server 端接收解码播放 ，socket client 端为录制编码发送方）
        initTcpServer();

        //initUdpServer();


        Log.d("playerDecoder", "onStartCommand: PlayerDecoder init success");
    }


    private void closeSocketServer() {
        Log.d(TAG, " --- Socket Server is close --- ");
        if (tcpServer != null) {
            tcpServer.close();
            tcpServer = null;
        }

        if (udpServer != null) {
            udpServer.close();
            udpServer = null;
        }


        if (mWebSocketServer != null) {
            mWebSocketServer.close();
            mWebSocketServer = null;
        }
    }


    /**
     * server send data to client
     *
     * @param socketId
     * @param data
     */
    private void sendData(int socketId, String data) {
        if (mWebSocketServer != null) {
            mWebSocketServer.sendData(socketId, data);
        }
    }


    private void ping(int socketId, String data) {
        if (mWebSocketServer != null) {
            Log.d(TAG, "WebSocket server ping---" + data);
            mWebSocketServer.ping(socketId, data);
        }
    }


    public void setSurface(Surface surface) {
        this.mSurface = surface;
    }

    public void setDrawListener(IDrawListener listener) {
        drawListener = listener;
    }

    public void setPlayerListener(IPlayerListener listener) {
        playerListener = listener;
    }

    public void startDecoder() {
        Log.d(TAG, "player decoder start...");
        videoList = new LinkedBlockingQueue<>();
        isExit = false;
        videoDecoderConfigured = false;

        new Thread(new Runnable() {
            @Override
            public void run() {
                videoDecoderInput();
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                videoDecoderOutput();
            }
        }).start();


        //开始发送数据
        for (int clientPort : mWebSocketClients) {
            sendData(clientPort, Command.setDecoderStatus(true));
            sendData(clientPort, Command.setSendData(true));
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setVideoData(MediaCodec.BufferInfo info, ByteBuffer encodedFrame) {
        if (mSurface == null) {
            Log.e(TAG, "setVideoData: surface is null");
            return;
        }
        String mimeType;  //解码器类型
        if (mEncoderCodecType == Command.CODEC_HEVC_FLAG) {
            mimeType = MediaFormat.MIMETYPE_VIDEO_HEVC; //H265
        } else {
            mimeType = MediaFormat.MIMETYPE_VIDEO_AVC; //H264
        }

        if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {//配置数据
            try {
                Log.d(TAG, "mimeType is " + mimeType + ",mFrameWidth:" + mFrameWidth + ",Height:" + mFrameHeight);
                MediaFormat format = MediaFormat.createVideoFormat(mimeType, mFrameWidth, mFrameHeight);
                format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, mFrameWidth * mFrameHeight);

                if (mimeType.equals(MediaFormat.MIMETYPE_VIDEO_AVC)) { //H264
                    MediaConfig.searchSPSandPPSFromH264(encodedFrame, format);
                } else {
                    MediaConfig.searchVpsSpsPpsFromH265(encodedFrame, format);
                }

                if (mVideoDecoder == null) {
                    mVideoDecoder = MediaCodec.createDecoderByType(mimeType);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mVideoDecoder.reset();
                }
                mVideoDecoder.configure(format, mSurface, null, 0);
                mVideoDecoder.setVideoScalingMode(MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT);//VIDEO_SCALING_MODE_SCALE_TO_FIT
                mVideoDecoder.start();
                videoDecoderConfigured = true;
                Log.e("colin", "colin start time05 --- pad start VideoDecoder configure finish");
            } catch (Exception e) {
                videoDecoderConfigured = false;
                e.printStackTrace();
                Log.e(TAG, "VideoDecoder init error" + e.toString());
                mirServerStop(IPlayerListener.ERR_CODE_DECODER_CONFIGURE, IPlayerListener.ERR_MSG_DECODER_CONFIGURE, true);
            }

        }

        FrameInfo videoFrame = new FrameInfo(info, encodedFrame);
        if (videoList != null) {
            videoList.add(videoFrame);
        }
    }


    /**
     * 解码器 input
     */
    private void videoDecoderInput() {
        while (!videoDecoderConfigured) {
            waitTimes(10);
        }

        int inputBufIndex = 0;
        while (!isExit) {
            FrameInfo videoFrame = videoList.poll();
            if (videoFrame == null) {
                waitTimes(1);
                continue;
            }

            ByteBuffer encodedFrames = videoFrame.encodedFrame;
            MediaCodec.BufferInfo info = videoFrame.bufferInfo;

            try {
                //解码 请求一个输入缓存
                inputBufIndex = mVideoDecoder.dequeueInputBuffer(-1);
                if (inputBufIndex < 0) {
                    Log.e(TAG, "dequeueInputBuffer result error---" + inputBufIndex);
                    waitTimes(1);
                    continue;
                }

                ByteBuffer[] inputBuf = mVideoDecoder.getInputBuffers();
                inputBuf[inputBufIndex].clear();
                inputBuf[inputBufIndex].put(encodedFrames);

                //解码数据添加到输入缓存中
                mVideoDecoder.queueInputBuffer(inputBufIndex, info.offset, info.size, info.presentationTimeUs, info.flags);

                Log.d(TAG, "end queue input buffer with ts " + info.presentationTimeUs + ",info.size :" + info.size);
                sendDogMsg(info.presentationTimeUs);

                Log.e("colin", "colin start time06 --- pad start VideoDecoder queueInputBuffer");
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "videoDecoderInput error---" + e.getMessage());
            }
        }

        closeDecoder();

    }


    /**
     * 解码器 output
     */
    private void videoDecoderOutput() {
        while (!videoDecoderConfigured) {
            waitTimes(10);
        }
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        while (!isExit) {
            try {
                int decoderIndex = mVideoDecoder.dequeueOutputBuffer(info, -1);
                if (decoderIndex > 0) {
                    mVideoDecoder.releaseOutputBuffer(decoderIndex, true);
                    Log.e("colin", "colin start time07 --- pad start VideoDecoder dequeueOutputBuffer finish");
                } else {
                    Log.e(TAG, "videoDecoderOutput dequeueOutputBuffer error=" + decoderIndex);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "videoDecoderOutput error---" + e.getMessage());
            }
        }

        closeDecoder();
    }


    /**
     * 开始心跳
     */
    private void startHeartBeat() {
        if (heartBeatScheduled == null) {
            heartBeatScheduled = Executors.newScheduledThreadPool(5);
            heartBeatScheduled.scheduleAtFixedRate(new Runnable() {
                public void run() {
                    heatBeat();
                }
            }, HEART_BEAT_INTERVAL, HEART_BEAT_INTERVAL, TimeUnit.SECONDS);
        }
    }


    /**
     * 停止心跳
     */
    private void stopHeartBeat() {
        if (heartBeatScheduled != null
                && !heartBeatScheduled.isShutdown()) {
            heartBeatScheduled.shutdown();
        }
    }


    /**
     * 心跳协议请求
     */
    private void heatBeat() {
        long ts = System.currentTimeMillis();

        long delay = (ts - mWatchTs) / 1000; //最后一次心跳延时

        if (mWatchDog > 3 && delay > 15) {
            Log.e(TAG, "WebSocket server watchdog timeout..." + mWatchDog + "&" + delay);
            mirServerStop(IPlayerListener.ERR_CODE_DOG_OUT,
                    IPlayerListener.ERR_MSG_DOG_OUT, true);//stop
            return;
        }

        mWatchDog++;
        for (int clientPort : mWebSocketClients) {
            ping(clientPort, Command.setServerHeatBeat(ts));
        }
    }


    /**
     * 发送解码延时信息
     */
    private void sendDogMsg(long consumeUs) {
        for (int clientPort : mWebSocketClients) {
            sendData(clientPort, Command.setDogData(consumeUs));
        }
    }


    private void waitTimes(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * 停止镜像服务
     *
     * @param errCode 错误类型
     */
    void mirServerStop(final int errCode, String errStr, boolean isCallBack) {

        Log.e(TAG, "mirServerStop---" + errStr);

        isExit = true;  //关闭解码线程

        MirManager.instance().setReverseRunning(false);

        for (int clientPort : mWebSocketClients) {
            sendData(clientPort, Command.setByeData(true, errCode, errStr));
        }

        stopHeartBeat();

        closeSocketServer();  //关闭 socket server


        if (playerListener != null && isCallBack) {
            final String info = errStr;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    playerListener.onError(errCode, info);
                }
            });
        }
    }


    private synchronized void closeDecoder() {
        try {
            if (mVideoDecoder != null) {
                Log.d(TAG, "unhappy decoder release");
                mVideoDecoder.stop();
                mVideoDecoder.release();
                mVideoDecoder = null;
            }
            MirManager.instance().setReverseRunning(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void sendMotionEvent(MotionEvent motionEvent) {
        String json = MotionEventUtil.formatTouchEvent(motionEvent, 1);
        sendMotionEvent(json);
    }

    private void sendMotionEvent(String json) {
        Log.d(TAG, "sendMotionEvent json---:" + json);
        for (int client : mWebSocketClients) {
            //使用send byte[] 接口专门发送触控事件
            mWebSocketServer.sendData(client, json.getBytes());
        }
    }


    private void serverOnRead(int socketId, String data) {
        Log.d(TAG, "WebSocket server Receive client String:" + data);

        if (data.startsWith(Command.CheckVersion)) {  // first cmd
            CheckVer clientVersion = Command.getCheckVersion(data);

            Log.d(TAG, "Client Version:" + clientVersion); //need be back

            String verCodec = Command.setServerVersionCodec(MIR_SERVER_VERSION, mDecoderCodecSupportType);

            sendData(socketId, verCodec);
            Log.e("colin", "colin start time02 --- pad start PlayerDecoder check version");
        } else if (data.startsWith(Command.Start)) {
            ClientIpCodec ipCodec = Command.getClientIpCodec(data);
            if (ipCodec.start) {
                String remoteIp = ipCodec.remoteIp;
                Log.d(TAG, "onCommand: connect touch remote ip:" + remoteIp);
                mEncoderCodecType = ipCodec.encoderCodecType;
                Log.d(TAG, "Encoder Codec Type:" + mEncoderCodecType);
                //开启播放页面，创建播放surface
                Log.e("colin", "colin start time03 --- pad start PlayerDecoder prepare:" + remoteIp);

                startDecoder();
            }

        } else if (data.startsWith(Command.SetWH)) {
            FrameWH frameWH = Command.getFrameWH(data);
            if (frameWH.setWH) {
                //设置分辨率
                mFrameWidth = frameWH.frameWidth;
                mFrameHeight = frameWH.frameHeight;

                if (drawListener != null) {
                    Log.d(TAG, "PlayerDecoder setUiHw:" + mFrameWidth + " X " + mFrameHeight);
                    drawListener.setHW(mFrameWidth, mFrameHeight, 0, this);
                }

                Log.e("colin", "colin start time04 --- pad start PlayerDecoder setUiHw:"
                        + mFrameWidth + " X " + mFrameHeight);
            }

        } else if (data.startsWith(Command.Bye)) {
            Log.e(TAG, "PlayerDecoder receive msg bye...");
            Bye bye = Command.getByeData(data);
            if (bye != null) {
                mirServerStop(bye.errCode, bye.errMsg, true);
            } else {
                mirServerStop(IPlayerListener.ERR_CODE_BYE, IPlayerListener.ERR_MSG_BYE, true);
            }
        }
    }


    /**
     * 初始化并创建web socket server
     * 用于控制，和传输触控事件
     */
    private void initWebSocketServer() {
        mWebSocketServer = new WebSocketServer(PortKey.PORT_WEB_SOCKET, new RequestCallback() {
            @Override
            public void onRead(int socketId, String s) {
                serverOnRead(socketId, s);
            }

            @Override
            public void onRead(int socketId, byte[] bytes) {

            }

            @Override
            public void ping(int socketId, String cmd) {
                //Log.d(TAG, "WebSocket server receive ping--- " + cmd);
            }

            @Override
            public void pong(int socketId, String cmd) {
                Log.d(TAG, "WebSocket server pong---" + cmd);
                mWatchDog--;
                mWatchTs = System.currentTimeMillis();

                /*ServerHeartBeat data = Command.getServerHeatBeat(cmd);
                if (data != null) {

                }*/
            }

            @Override
            public void onConnectState(int socketId, ConnectState connectState) {

                if (connectState == ConnectState.CONNECT) {
                    Log.d(TAG, "create WebSocketServer onConnectState --- ConnectState.CONNECT");
                    mWebSocketClients.add(socketId);
                    startHeartBeat();
                } else if (connectState == ConnectState.ERROR) {
                    Log.d(TAG, "create WebSocketServer onConnectState --- ConnectState.ERROR");
                    mWebSocketClients.remove(socketId);
                } else if (connectState == ConnectState.DISCONNECT) {
                    Log.d(TAG, "create WebSocketServer onConnectState --- ConnectState.DISCONNECT");
                    mWebSocketClients.remove(socketId);
                }
            }
        });

        mWebSocketServer.open();
    }


    /**
     * 初始化视频数据 socketServer 端
     * socketServer 建立在同屏控制的控制方
     */
    private void initTcpServer() {
        //just client send pdu to server
        tcpServer = new TcpServer(PortKey.PORT_TCP, TcpServer.BUFFER_SIZE_HIGH,
                new StreamSinkCallback() {
                    @Override
                    public void onData(String data, SocketChannel channel) {

                    }

                    @Override
                    public void onData(byte[] data, SocketChannel channel) {

                    }


                    @Override
                    public void onAudioFrame(MediaCodec.BufferInfo bufferInfo,
                                             ByteBuffer byteBuffer, SocketChannel channel) {

                    }

                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onVideoFrame(MediaCodec.BufferInfo bufferInfo,
                                             ByteBuffer byteBuffer, SocketChannel channel) {
                        Log.d(TAG, "onVideoFrame bufferInfo.size=" + bufferInfo.size +
                                "   byteBuffer size=" + byteBuffer.remaining());
                        setVideoData(bufferInfo, byteBuffer);
                    }

                    @Override
                    public void ping(String msg, SocketChannel channel) {

                    }

                    @Override
                    public void pong(String msg, SocketChannel channel) {

                    }

                    @Override
                    public void onConnectState(ConnectState connectState) {
                        Log.d(TAG, "create  tcpServer onConnectState --- " + connectState);
                        if (connectState == ConnectState.ERROR) {
                            mirServerStop(IPlayerListener.ERR_CODE_SOCKET_SERVER,
                                    IPlayerListener.ERR_MSG_SOCKET_SERVER, true);
                        }
                    }
                });

        tcpServer.open();
    }

    /**
     * 初始化视频数据 socketServer 端
     * socketServer 建立在同屏控制的控制方
     */
    private void initUdpServer() {
        //just client send pdu to server
        udpServer = new UdpServer(PortKey.PORT_UDP, new StreamSinkCallback() {
            @Override
            public void onData(byte[] data, SocketChannel channel) {

            }

            @Override
            public void onData(String data, SocketChannel channel) {

            }

            @Override
            public void onAudioFrame(MediaCodec.BufferInfo bufferInfo,
                                     ByteBuffer byteBuffer, SocketChannel channel) {

            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onVideoFrame(MediaCodec.BufferInfo bufferInfo,
                                     ByteBuffer byteBuffer, SocketChannel channel) {
                Log.d(TAG, "onData: onVideoFrame bufferInfo:" + bufferInfo.size);
                Log.d(TAG, "onData: onVideoFrame byteBuffer size:" + byteBuffer.remaining());
                setVideoData(bufferInfo, byteBuffer);
            }

            @Override
            public void ping(String msg, SocketChannel channel) {

            }

            @Override
            public void pong(String msg, SocketChannel channel) {

            }

            @Override
            public void onConnectState(ConnectState connectState) {
                Log.d(TAG, "create  udpSocket onConnectState --- " + connectState);
                if (connectState == ConnectState.ERROR) {
                    mirServerStop(IPlayerListener.ERR_CODE_SOCKET_SERVER,
                            IPlayerListener.ERR_MSG_SOCKET_SERVER, true);
                }
            }
        });
        udpServer.open();
    }

    /**
     * 检测是否支持H264 和 H265 硬解码
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void checkDecoderSupportCodec() {
        MediaCodecConfig h264Config = new MediaCodecConfig();
        MediaCodecConfig h265Config = new MediaCodecConfig();

        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (!codecInfo.isEncoder()) {  //解码器
                // 如果是解码器，判断是否支持Mime类型
                String[] types = codecInfo.getSupportedTypes();
                for (String type : types) {
                    if (type.equalsIgnoreCase(MediaFormat.MIMETYPE_VIDEO_AVC)) {
                        Log.d(TAG, codecInfo.getName() + "H264 硬解码 Supported");
                        h264Config.setSupport(true);

                        MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(type);
                        MediaCodecInfo.VideoCapabilities videoCapabilities = capabilities.getVideoCapabilities();
                        Range<Integer> bitrateRange = videoCapabilities.getBitrateRange();
                        int maxBitrate = bitrateRange.getUpper();
                        if (h264Config.getMaxBitrate() < maxBitrate) {
                            h264Config.setMaxBitrate(maxBitrate);
                        }
                        Log.d(TAG, codecInfo.getName() + "H264 硬解码 maxBitrate---" + maxBitrate);

                        Range<Integer> widthRange = videoCapabilities.getSupportedWidths();
                        int maxWidth = widthRange.getUpper();
                        if (h264Config.getMaxWidth() < maxWidth) {
                            h264Config.setMaxWidth(maxWidth);
                        }

                        Log.d(TAG, codecInfo.getName() + "H264 硬解码 maxWidth---" + maxWidth);

                        Range<Integer> heightRange = videoCapabilities.getSupportedHeights();
                        int maxHeight = heightRange.getUpper();
                        if (h264Config.getMaxHeight() < maxHeight) {
                            h264Config.setMaxHeight(maxHeight);
                        }

                        Log.d(TAG, codecInfo.getName() + "H264 硬解码 maxHeight---" + maxHeight);

                    } else if (type.equalsIgnoreCase(MediaFormat.MIMETYPE_VIDEO_HEVC)) {
                        Log.d(TAG, codecInfo.getName() + "H265 硬解码 Supported");
                        h265Config.setSupport(true);

                        MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(type);
                        MediaCodecInfo.VideoCapabilities videoCapabilities = capabilities.getVideoCapabilities();
                        Range<Integer> bitrateRange = videoCapabilities.getBitrateRange();
                        int maxBitrate = bitrateRange.getUpper();
                        if (h265Config.getMaxBitrate() < maxBitrate) {
                            h265Config.setMaxBitrate(maxBitrate);
                        }
                        Log.d(TAG, codecInfo.getName() + "H265 硬解码 maxBitrate---" + maxBitrate);

                        Range<Integer> widthRange = videoCapabilities.getSupportedWidths();
                        int maxWidth = widthRange.getUpper();
                        if (h265Config.getMaxWidth() < maxWidth) {
                            h265Config.setMaxWidth(maxWidth);
                        }
                        Log.d(TAG, codecInfo.getName() + "H265 硬解码 maxWidth---" + maxWidth);

                        Range<Integer> heightRange = videoCapabilities.getSupportedHeights();
                        int maxHeight = heightRange.getUpper();
                        if (h265Config.getMaxHeight() < maxHeight) {
                            h265Config.setMaxHeight(maxHeight);
                        }
                        Log.d(TAG, codecInfo.getName() + "H265 硬解码 maxHeight---" + maxHeight);
                    }
                }

            }
        }
        Log.d(TAG, "h264Config 硬解码---" + h264Config.toString());
        Log.d(TAG, "h265Config 硬解码---" + h265Config.toString());
        Configuration configuration = mContext.getResources().getConfiguration(); //获取设置的配置信息
        int ori = configuration.orientation; //获取屏幕方向
        int width, height;
        if (DLNACommonUtil.checkPermission(mContext)) {//for tv
            width = 1920;
            height = 1080;
        } else if (ori == Configuration.ORIENTATION_LANDSCAPE) {  ///横屏 for pad
            width = 1920;
            height = 1080;
        } else {  //竖屏 for mobile
            width = 1080;
            height = 1920;
        }
        if (width <= h264Config.getMaxWidth()
                && height <= h264Config.getMaxHeight()) {
            mDecoderCodecSupportType |= Command.CODEC_AVC_FLAG;  //支持h264
        }
        if (width <= h265Config.getMaxWidth()
                && height <= h265Config.getMaxHeight()) {
            mDecoderCodecSupportType |= Command.CODEC_HEVC_FLAG; //支持h265
        }
        Log.d(TAG, "h264 || h265 decoder---" + "mDecoderCodecSupportType:" + mDecoderCodecSupportType);
    }

}