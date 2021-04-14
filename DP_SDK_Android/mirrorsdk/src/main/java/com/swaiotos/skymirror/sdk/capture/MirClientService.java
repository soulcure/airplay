/*
 * 创建VirtualDisplay进行抓取屏幕数据，将数据进行编码，再通过socket将编码后的数据发送至接收端
 * 发送数据完整流程：
 * 发送数据时通道有3种：
 * 1.iot-channel用来发送开启、关闭消息（替代设备发现）
 * 2.httpServer用来传输指令 （用于验证版本、传递分辨率、触控事件及心跳包）
 * 3.socket用来传输数据流 （传输编码后的数据）
 * if(iot-channel) ：
 * 1.接收端通过iot-channel（携带接收端IP,创建通道所需要port）发起传递数据请求，并创建接收端指令、数据服务端，
 * 发送端接收到消息后开启抓屏服务，并创建client连接至服务端（使用iot-channel携带的ip,port），至此指令、传输数据通道建立
 * 2.经过一系列的指令交互验证，验证通过后创建编码器及绘制所需画布（surface）
 * 3.获取到surface后创建VirtualDisplay抓取屏幕数据并绘制到surface，由于电视端系统缘故，抓屏方式分两种：
 * ① 电视端通过DisplayManager创建VirtualDisplay(需要system权限)
 * ② 手机端通过MediaProjection创建VirtualDisplay（需要手动获取显示最上层权限）
 * 4.将编码后的数据通过socket发送至接收端
 * 关于分辨率：理论上：取发送端编码器和接收端编解码器共同支持的最大分辨率进行编解码操作。
 * 目前优化分辨率为业务层开通接口将分辨率传入
 * <p>
 * if(本地设备发现)
 * //暂定
 */
package com.swaiotos.skymirror.sdk.capture;


import android.annotation.TargetApi;
import android.app.Instrumentation;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.RequiresApi;

import com.google.gson.Gson;
import com.skyworth.dpclientsdk.ConnectState;
import com.skyworth.dpclientsdk.PduBase;
import com.skyworth.dpclientsdk.ResponseCallback;
import com.skyworth.dpclientsdk.StreamSourceCallback;
import com.skyworth.dpclientsdk.TcpClient;
import com.skyworth.dpclientsdk.UdpClient;
import com.skyworth.dpclientsdk.WebSocketClient;
import com.swaiotos.skymirror.sdk.Command.Command;
import com.swaiotos.skymirror.sdk.Command.DecoderStatus;
import com.swaiotos.skymirror.sdk.Command.Dog;
import com.swaiotos.skymirror.sdk.Command.SendData;
import com.swaiotos.skymirror.sdk.Command.ServerVersionCodec;
import com.swaiotos.skymirror.sdk.data.PortKey;
import com.swaiotos.skymirror.sdk.data.TouchData;
import com.swaiotos.skymirror.sdk.reverse.IPlayerListener;
import com.swaiotos.skymirror.sdk.reverse.MotionEventUtil;
import com.swaiotos.skymirror.sdk.util.DeviceUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @ClassName: MirClientService
 * @Description: 抓屏服务类，提供抓屏、编码、传流功能（即发送数据端，亦为client端）
 * @Author: lfz
 * @Date: 2020/4/15 9:38
 */
public class MirClientService extends Service {
    //常量定义--start---
    private static final String TAG = MirClientService.class.getSimpleName();
    private static final String MIR_CLIENT_VERSION = "3.0";  //支持UDP协议传流
    private static final int HEART_BEAT_INTERVAL = 5; //心跳间隔30秒
    //常量定义--end---
    private Context mContext;
    private ScheduledExecutorService heartBeatScheduled;

    private Thread inputWorkerTouch;
    private Thread encoderWorker;

    private boolean isExit;
    private boolean isExitTouch;

    private int mWatchDog = 0;
    private long mWatchTs;
    private long consumedUs;

    private WebSocketClient mWebSocketClient;  //web socket client
    private TcpClient tcpClient; //data socket client
    private UdpClient udpClient;

    boolean isUdpSupport = false;  //是否支持UDP协议传流

    private LinkedBlockingQueue<MotionEvent> touchEventQueue;

    private Handler mHandler;
    private long timeoutUs = 1000 * 1000;  //单位微秒 1秒

    private PlayerEncoder playerEncoder;

    private StreamSourceCallback mTcpCallback = new StreamSourceCallback() {
        @Override
        public void onConnectState(ConnectState state) {
            if (state == ConnectState.CONNECT) {
                Log.i(TAG, "tcpClient onConnectState: --- CONNECT");
            } else if (state == ConnectState.DISCONNECT) {
                Log.e(TAG, "tcpClient onConnectState: --- DISCONNECT");
            } else if (state == ConnectState.ERROR) {
                Log.e(TAG, "tcpClient onConnectState: --- ERROR");
                if (playerEncoder != null)
                    playerEncoder.setReset(false);
                stopMirService(IPlayerListener.ERR_CODE_SOCKET_CLIENT, IPlayerListener.ERR_MSG_SOCKET_CLIENT);
            }
        }

        @Override
        public void onData(String data) {

        }

        @Override
        public void onData(byte[] data) {

        }

        @Override
        public void ping(String msg) {

        }

        @Override
        public void pong(String msg) {

        }
    };


    private StreamSourceCallback mUdpCallback = new StreamSourceCallback() {
        @Override
        public void onConnectState(ConnectState state) {
            if (state == ConnectState.CONNECT) {
                Log.i(TAG, "udpClient onConnectState: --- CONNECT");
            } else if (state == ConnectState.DISCONNECT) {
                Log.e(TAG, "udpClient onConnectState: --- DISCONNECT");
            } else if (state == ConnectState.ERROR) {
                Log.e(TAG, "udpClient onConnectState: --- ERROR");
                //stopMirService(IPlayerListener.ERR_CODE_SOCKET_CLIENT, IPlayerListener.ERR_MSG_SOCKET_CLIENT);
            }
        }

        @Override
        public void onData(String data) {

        }

        @Override
        public void onData(byte[] data) {

        }

        @Override
        public void ping(String msg) {

        }

        @Override
        public void pong(String msg) {

        }
    };


    private ResponseCallback mWebSocketCallback = new ResponseCallback() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onCommand(String s) {
            clientOnRead(s);
        }

        //使用send byte[] 接口专门发送触控事件
        @Override
        public void onCommand(byte[] bytes) {
            String json = new String(bytes);
            Gson gson = new Gson();
            try {
                MotionEvent event = MotionEventUtil.formatMotionEvent(gson.fromJson(json, TouchData.class));
                touchEventQueue.add(event);
                Log.d(TAG, "addInputEvent2 onCommand:event --- " + event.toString());
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "addInputEvent2 onCommand event error--- " + e.toString());
            }
        }


        @Override
        public void ping(String cmd) {
            //Log.d(TAG, "WebSocket client receive ping--- " + cmd);
        }

        @Override
        public void pong(String cmd) {
            Log.d(TAG, "WebSocket client pong---" + cmd + " mWatchDog:" + mWatchDog);
            mWatchDog--;
            mWatchTs = System.currentTimeMillis();
        }


        @Override
        public void onConnectState(ConnectState connectState) {
            if (connectState == ConnectState.CONNECT) {
                Log.i(TAG, "WebSocket client onConnectState ----- CONNECT");
                startHeartBeat();    //开启心跳

                //first cmd by WebSocket Client connect success
                sendMsg(Command.setCheckVersion(MIR_CLIENT_VERSION)); //WebSocket Client 连接成功后，首次信令消息
                touchEventQueue.clear();

                if (inputWorkerTouch == null) {
                    inputWorkerTouch = new Thread(new InputWorkerTouch(), "Input Thread Touch");
                }
                if (!inputWorkerTouch.isAlive()) {
                    inputWorkerTouch.start();//bsp
                }
            } else if (connectState == ConnectState.DISCONNECT) { // ConnectState.ERROR ,ConnectState.DISCONNECT
                Log.e(TAG, "WebSocket client onConnectState ----- DISCONNECT");
            } else if (connectState == ConnectState.ERROR) { // ConnectState.ERROR ,ConnectState.DISCONNECT
                Log.e(TAG, "WebSocket client onConnectState ----- ERROR");
                stopMirService(IPlayerListener.ERR_CODE_WEB_SOCKET_CLIENT,
                        IPlayerListener.ERR_MSG_WEB_SOCKET_CLIENT);
            }
        }
    };


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotification() {
        String channelId = "CHANNEL_ONE_ID";
        String channelName = "MirClientService";

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
            startForeground(1024, new Notification.Builder(this, channelId).build());
        } else {
            startForeground(1024, new Notification());
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();

        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void run() {
                if (playerEncoder == null)
                    playerEncoder = new PlayerEncoder(getApplicationContext());
                playerEncoder.checkEncoderSupportCodec();
            }
        }).start();

        Log.d(TAG, "MirClientService onCreate");

        isExit = false;
        isExitTouch = false;
        touchEventQueue = new LinkedBlockingQueue<>();
        mContext = this;
        mHandler = new Handler(Looper.getMainLooper());

        mWatchDog = 0;
        createNotification();
        CustomToast.instance().clear();
    }

    /**
     * Main Entry Point of the server code. Create a WebSocket server and start
     * the encoder.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();

        Log.d(TAG, "MirClientService onStartCommand---" + action);

        if (action != null && action.equals("START")) {
            String ip = intent.getStringExtra("serverip");
            int resultCode = intent.getIntExtra("resultCode", -10);
            Intent data = intent.getParcelableExtra("intent");

            Log.d(TAG, "onStartCommand: client(PHONE) ip ----- " + ip);
            Log.d(TAG, "onStartCommand: server(TV) ip ----- " + DeviceUtil.getLocalIPAddress(this));

//            initMediaProjection(resultCode, data);
            if (playerEncoder == null)
                playerEncoder = new PlayerEncoder(getApplicationContext());
            playerEncoder.createMediaProjection(resultCode, data);

            mWebSocketClient = new WebSocketClient(ip, PortKey.PORT_WEB_SOCKET, mWebSocketCallback);
            mWebSocketClient.open();

            //udpClient = new UdpClient(ip, PortKey.PORT_UDP, mUdpCallback);
            //udpClient.open();

            tcpClient = new TcpClient(ip, PortKey.PORT_TCP, mTcpCallback);
            tcpClient.open();

        } else if (action != null && action.equals("STOP")) {
            Log.d(TAG, "onStartCommand: stop flag");
            stopMirService(IPlayerListener.ERR_CODE_MIR_CLOSE, IPlayerListener.ERR_MSG_MIR_CLOSE);
        }

        return START_NOT_STICKY;
    }

    public void ping(String msg) {
        if (mWebSocketClient != null) {
            Log.d(TAG, "WebSocket client ping---" + msg + " mWatchDog:" + mWatchDog);
            mWebSocketClient.ping(msg);
        }
    }


    public void sendMsg(String msg) {
        if (mWebSocketClient != null) {
            mWebSocketClient.send(msg);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //resetWH ->reset -> reconfigure -> start
                    if (playerEncoder != null) {
                        isExit = true;
                        playerEncoder.checkEncodeWH();
                        playerEncoder.reset();
                        playerEncoder.reConfigure();
                        playerEncoder.start();
                        String hw = Command.setFrameWH(true, playerEncoder.getWidth(), playerEncoder.getHeight());
                        sendMsg(hw);
                        isExit = false;
                        while (!isExit) {
                            doEncodeWork();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "encoder stop---" + e.getMessage());
                }
            }
        }).start();
    }

    //clint onRead
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void clientOnRead(String s) {
        if (s.startsWith(Command.ServerVersion)) {   // first cmd
            ServerVersionCodec versionCodec = Command.getServerVersionCodec(s);

            if (versionCodec.serverVersion.compareTo(MIR_CLIENT_VERSION) >= 0) {
                Log.d(TAG, "ServerVersion fitted with " + MIR_CLIENT_VERSION);
                isUdpSupport = true;
            } else {
                isUdpSupport = false;
            }

            if (playerEncoder.getEncoderCodecSupportType() == 0) {  //自己不支持硬编码
                stopMirService(IPlayerListener.ERR_CODE_ENCODER_NOT_SUPPORTED,
                        IPlayerListener.ERR_MSG_ENCODER_NOT_SUPPORTED);
                return;
            }

            int codeSupport = versionCodec.codecSupport; //对方的编码类型
            int encoderCodecType = Command.CODEC_AVC_FLAG;//自己的编码类型
            String mimeType = MediaFormat.MIMETYPE_VIDEO_AVC;  //H264
            //高分辨率手机使用h265
            if (((codeSupport & Command.CODEC_HEVC_FLAG) == Command.CODEC_HEVC_FLAG)  //对方支持H265
                    && ((playerEncoder.getEncoderCodecSupportType() & Command.CODEC_HEVC_FLAG) == Command.CODEC_HEVC_FLAG)) { //自己支持H265
                mimeType = MediaFormat.MIMETYPE_VIDEO_HEVC;  //H265
                encoderCodecType = Command.CODEC_HEVC_FLAG;
            }

            Log.d(TAG, "CodecSupport is remote---" + codeSupport + "  ---self---" + encoderCodecType);

            playerEncoder.setContentMimeType(mimeType);

            String localIp = DeviceUtil.getLocalIPAddress(MirClientService.this);

            Log.d(TAG, "onCommand: getLocalIp ---- " + localIp);

            String ipCodec = Command.setClientIpCodec(true, localIp, encoderCodecType); //发送给对方

            sendMsg(ipCodec);

            Log.e("colin", "colin start time02 --- tv check version and codeSupport");
        } else if (s.startsWith(Command.DecoderStatus)) {  //second cmd
            DecoderStatus status = Command.getDecoderStatus(s);
            if (status.decoderStatus) {
                int waitTime = 10;
                while ((playerEncoder.getWidth() == 0 || playerEncoder.getHeight() == 0) && waitTime != 0) {
                    waitTime--;
                    try {
                        Log.e(TAG, "MirClientService mWidth or mHeight is 0----" + waitTime);
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (playerEncoder.getWidth() == 0 || playerEncoder.getHeight() == 0) {
                    Log.e(TAG, "MirClientService mWidth or mHeight is 0");
                    stopMirService(IPlayerListener.ERR_CODE_VIRTUAL_DISPLAY,
                            IPlayerListener.ERR_MSG_VIRTUAL_DISPLAY);
                    return;
                }
                // 处理屏幕旋转，通知解码端分辨率，动态设置，如果屏幕旋转，需要重新设置
                String hw = Command.setFrameWH(true, playerEncoder.getWidth(), playerEncoder.getHeight());
                sendMsg(hw);

                Log.e("colin", "colin start time03 --- tv check DecoderStatus and set hw");
            }
        } else if (s.startsWith(Command.SendData)) { //third cmd
            SendData data = Command.getSendData(s);
            if (data.sendData) {
                Log.d(TAG, "start Encode....");
                if (encoderWorker == null) {
                    encoderWorker = new Thread(new EncoderWorker(), "Encoder Thread");
                }
                if (!encoderWorker.isAlive()) {
                    encoderWorker.start();
                }

                MirManager.instance().setMirRunning(true);
                Log.e("colin", "colin start time04 --- tv start Encoder and SendData");
            }
        } else if (s.startsWith(Command.Dog)) { //播放时延信息
            Dog dog = Command.getDogData(s);
            if (dog != null) {
                consumedUs = dog.dog;
            }
        } else if (s.startsWith(Command.Bye)) { //exit cmd
            Log.e(TAG, "MirClientService receive msg bye...");
            stopMirService(IPlayerListener.ERR_CODE_BYE, IPlayerListener.ERR_MSG_BYE);
        }
    }

    private void doEncodeWork() {
        try {
            int index = playerEncoder.dequeueOutputBuffer(timeoutUs);
            if (index == MediaCodec.INFO_TRY_AGAIN_LATER) { //无推流数据
                Log.e(TAG, "MediaCodec INFO_TRY_AGAIN_LATER---");
                CustomToast.instance().popUp(mContext);
                timeoutUs = -1;  //第二次设为阻塞试，无限等待

            } else if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                Log.e(TAG, "MediaCodec INFO_OUTPUT_FORMAT_CHANGED---");
            } else if (index == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                Log.e(TAG, "MediaCodec INFO_OUTPUT_BUFFERS_CHANGED---");
            } else if (index >= 0) {
                //实时适配
                playerEncoder.adjustBitRate(consumedUs);
                //获取数据
                ByteBuffer encodeData = playerEncoder.getOutputBuffers(index);
                MediaCodec.BufferInfo mBufferInfo = playerEncoder.getBufferInfo();

                Log.e("colin", "colin start isUdpSupport---" + isUdpSupport);
                if (isUdpSupport
                        && encodeData != null
                        && udpClient != null
                        && udpClient.isOpen()
                        && mBufferInfo != null
                        && mBufferInfo.size != 0) {
                    udpClient.sendData(PduBase.VIDEO_FRAME, playerEncoder.getBufferInfo(), encodeData);
                    Log.e("colin", "colin start time05 --- tv start Encoder finish will send by udp socket");
                } else if (encodeData != null
                        && tcpClient != null
                        && tcpClient.isOpen()
                        && mBufferInfo != null
                        && mBufferInfo.size != 0) {
                    tcpClient.sendData(PduBase.VIDEO_FRAME, mBufferInfo, encodeData);
                    Log.e("colin", "colin start time05 --- tv start Encoder finish will send by tcp socket");
                }
                //释放
                playerEncoder.releaseOutputBuffer(index, false);
            }
        } catch (Exception e) {
            Log.e(TAG, "doEncodeWork error---" + playerEncoder.isReset());
            if (playerEncoder != null && playerEncoder.isReset() || isExit) {
                playerEncoder.setReset(false);
                return;
            }
            e.printStackTrace();
            Log.e(TAG, "doEncodeWork error---" + e.getMessage());
            stopMirService(IPlayerListener.ERR_CODE_VIRTUAL_DISPLAY,
                    IPlayerListener.ERR_MSG_VIRTUAL_DISPLAY);

        }
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
            Log.e(TAG, "WebSocket client watchdog timeout..." + mWatchDog + "&" + delay);
            stopMirService(IPlayerListener.ERR_CODE_WATCHDOG, IPlayerListener.ERR_MSG_WATCHDOG);
            return;
        }
        mWatchDog++;
        Log.d(TAG, "--heatBeat----:" + mWatchDog);
        ping(Command.setClientData(ts));
    }


    private class InputWorkerTouch implements Runnable {
        @Override
        public void run() {
            Log.d(TAG, "InputWorkerTouch enter----");
            while (!isExitTouch) {
                try {
                    MotionEvent motionEvent = touchEventQueue.take();

                    long downtime = SystemClock.uptimeMillis();
                    long eventTime = SystemClock.uptimeMillis();
                    int count = motionEvent.getPointerCount();
                    int action = motionEvent.getAction();
                    MotionEvent.PointerProperties[] pointerProperties = new MotionEvent.PointerProperties[count];
                    MotionEvent.PointerCoords[] pointerCoords = new MotionEvent.PointerCoords[count];
                    //int meta = motionEvent.getMetaState();
                    //int bstat = motionEvent.getButtonState();
                    //float xprec = motionEvent.getXPrecision();
                    //float yprec = motionEvent.getYPrecision();
                    //int edgef = motionEvent.getEdgeFlags();
                    //int flag = motionEvent.getFlags();

                    for (int i = 0; i < count; i++) {
                        pointerProperties[i] = new MotionEvent.PointerProperties();
                        motionEvent.getPointerProperties(i, pointerProperties[i]);
                    }
                    for (int i = 0; i < count; i++) {
                        pointerCoords[i] = new MotionEvent.PointerCoords();
                        motionEvent.getPointerCoords(i, pointerCoords[i]);
                    }

                    MotionEvent met = MotionEvent.obtain(downtime, eventTime, action, count,
                            pointerProperties, pointerCoords, 0, 0,
                            0, 0, 0, 0, 0, 0);

                    Log.d(TAG, "touch obtain:" + met.toString());

                    Instrumentation inst = new Instrumentation();
                    inst.sendPointerSync(met);

                } catch (InterruptedException e) {
                    Log.e(TAG, "touchEventQueue take error---" + e.getMessage());
                    e.printStackTrace();
                } catch (SecurityException e) {
                    Log.e(TAG, "Instrumentation  error---" + e.getMessage());
                    e.printStackTrace();
                }

            }
            Log.d(TAG, "InputWorkerTouch exit");
        }
    }

    @TargetApi(19)
    private class EncoderWorker implements Runnable {
        @Override
        public void run() {
            Log.d(TAG, "EncoderWorker:start WatchDogThread");
//            startDisplayManager();//it will be jammed for loop
            try {
                playerEncoder.createMediaCodec();
                playerEncoder.createDisplayManager();
                playerEncoder.start();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "startDisplayManager: create virtualDisplay error");
                stopMirService(IPlayerListener.ERR_CODE_VIRTUAL_DISPLAY,
                        IPlayerListener.ERR_MSG_VIRTUAL_DISPLAY);
            }
            // 创建BufferedOutputStream对象
            ToastUtils.instance().showToast(mContext);
            while (!isExit) {
                doEncodeWork();
            }
            Log.d(TAG, "EncoderWorker exit");
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "MirClientService onDestroy...");
        stopForeground(true);
        MirManager.instance().setMirRunning(false);

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (playerEncoder != null)
                    playerEncoder.release();  //MediaCodec stop 有大量耗时操作
                playerEncoder = null;
            }
        }).start();
    }

    private void stopMirService(final int errCode, String errMsg) {
        Log.e(TAG, "MirClientService stopMirService..." + errCode + "&" + errMsg);
        isExit = true;
        isExitTouch = true;

        MirManager.instance().setMirRunning(false);
        String bye = Command.setByeData(true, errCode, errMsg);
        sendMsg(bye);

        stopHeartBeat();//停止心跳

        final IPlayerListener playerListener = MirManager.instance().getMirServiceListener();

        if (playerListener != null) {
            final String info = errMsg;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    playerListener.onError(errCode, info);
                }
            });
        }
        stopClient();
        stopSelf();
    }

    private void stopClient() {
        Log.d(TAG, "stopClient...");

        if (mWebSocketClient != null) {
            Log.d(TAG, "=====> Close WebSocketClient");
            mWebSocketClient.close();
            mWebSocketClient = null;
        }


        if (tcpClient != null) {
            Log.d(TAG, "=====> Close tcpClient");
            tcpClient.close();
            tcpClient = null;
        }

        if (udpClient != null) {
            Log.d(TAG, "=====> Close udpClient");
            udpClient.close();
            udpClient = null;
        }
    }

}
