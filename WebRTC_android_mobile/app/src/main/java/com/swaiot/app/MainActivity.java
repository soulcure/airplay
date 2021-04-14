package com.swaiot.app;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.skyworthiot.iotssemsg.IotSSEMsgLib;
import com.swaiot.webrtcc.sound.WebRTCSoundManager;
import com.swaiot.webrtcc.video.WebRTCVideoManager;
import com.swaiot.webrtcc.config.Constant;
import com.swaiot.webrtcc.entity.Model;
import com.swaiot.webrtcc.entity.SSEEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.Map;
import java.util.UUID;

import in.xiandan.countdowntimer.CountDownTimerSupport;
import in.xiandan.countdowntimer.OnCountDownTimerListener;
import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.ss.session.Session;


public class MainActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "yao";

    private final int MY_PERMISSIONS_REQUEST_CAMERA = 100;
    private final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 101;
    private final int MY_PERMISSIONS_REQUEST = 102;


    private static final int CAPTURE_PERMISSION_REQUEST_CODE = 1;
    private static final int HANDLER_THREAD_INIT_CONFIG_START = 2;

    private static final String DEVICE_ID = "5619397646c8b3f43e82f61486e22bde";
    private static final String TV_ID = "ebba2d7f2b964580aa7c3407fa72e309"; //me
    //private static final String TV_ID = "e7069766cce64a9495495edc0af16b48"; //对面
    //private static final String TV_ID = "547c08a5f84b411fa945ca8f73c426f6"; //对面

    private static final String SOURCE_CLIENT = "com.coocaa.webrtc.client.airplay";

    private static final String TARGET_CLIENT = "com.coocaa.webrtc.airplay";
    private static final String TARGET_CLIENT1 = "com.coocaa.webrtc.airplay.voice";

    private TextView tv_time;


    private IotSSEMsgLib iotSSE;
    private Context mContext;

    private Handler mHandler;
    private ProcessHandler mProcessHandler;

    private String targetClient;

    private WebRTCVideoManager.SenderImpl senderVideo = new WebRTCVideoManager.SenderImpl() {
        @Override
        public void onSend(String content) {

            Session mySession = new Session();
            mySession.setId(DEVICE_ID);

            Session targetSession = new Session();
            targetSession.setId(TV_ID);

            IMMessage message = IMMessage.Builder.createTextMessage(mySession, targetSession,
                    SOURCE_CLIENT, targetClient, content);
            message.putExtra(SSChannel.FORCE_SSE, "true");//强制云端
            message.putExtra("target-client", SOURCE_CLIENT);//回复消息target

            String text = message.encode();
            sendMessage(text);
        }
    };

    private WebRTCSoundManager.SenderImpl senderSound = new WebRTCSoundManager.SenderImpl() {
        @Override
        public void onSend(String content) {

            Session mySession = new Session();
            mySession.setId(DEVICE_ID);

            Session targetSession = new Session();
            targetSession.setId(TV_ID);

            IMMessage message = IMMessage.Builder.createTextMessage(mySession, targetSession,
                    SOURCE_CLIENT, targetClient, content);
            message.putExtra(SSChannel.FORCE_SSE, "true");//强制云端
            message.putExtra("target-client", SOURCE_CLIENT);//回复消息target

            String text = message.encode();
            sendMessage(text);
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        initHandler();
        startSignalChannel();

        tv_time = findViewById(R.id.tv_time);

        findViewById(R.id.btn_start_video).setOnClickListener(this);
        findViewById(R.id.btn_start_camera).setOnClickListener(this);
        findViewById(R.id.btn_stop_video).setOnClickListener(this);

        findViewById(R.id.btn_start_data_channel).setOnClickListener(this);
        findViewById(R.id.btn_send_text).setOnClickListener(this);
        findViewById(R.id.btn_send_file).setOnClickListener(this);

        findViewById(R.id.btn_start_voice).setOnClickListener(this);
        findViewById(R.id.btn_stop_voice).setOnClickListener(this);

        WebRTCVideoManager.instance().init(this, new WebRTCVideoManager.InitListener() {
            @Override
            public void success() {
                Log.d(TAG, "success...");
                WebRTCVideoManager.instance().setSender(senderVideo);
            }

            @Override
            public void fail() {
                Log.d(TAG, "fail...");
            }
        });


        WebRTCSoundManager.instance().init(this, new WebRTCSoundManager.InitListener() {
            @Override
            public void success() {
                Log.d(TAG, "success...");
                WebRTCSoundManager.instance().setSender(senderSound);
            }

            @Override
            public void fail() {
                Log.d(TAG, "fail...");
            }
        });

        askForPermissions();
    }

    private void startScreenCapture() {
        if (iotSSE.isSSEConnected()) {
            MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), CAPTURE_PERMISSION_REQUEST_CODE);
        }
    }

    private void sendMessage(String content) {
        if (iotSSE.isSSEConnected()) {
            Log.d(TAG, "sendMessage=" + content);
            iotSSE.sendMessage(TV_ID, UUID.randomUUID().toString(), "swaiot-os-iotchannel", content);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_PERMISSION_REQUEST_CODE) {
            WebRTCVideoManager.instance().start(data);
        }
    }

    int index = 0;

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_start_video) {
            targetClient = TARGET_CLIENT;
            startScreenCapture();
            test();
        } else if (id == R.id.btn_start_camera) {
            targetClient = TARGET_CLIENT;
            WebRTCVideoManager.instance().startCamera();
        } else if (id == R.id.btn_stop_video) {
            WebRTCVideoManager.instance().stop();
        } else if (id == R.id.btn_start_data_channel) {
            targetClient = TARGET_CLIENT;
            WebRTCVideoManager.instance().startDataChannel();
        } else if (id == R.id.btn_send_text) {

            String text = "hello data channel at index=" + index;
            WebRTCVideoManager.instance().sendData(text);
            index++;

        } else if (id == R.id.btn_send_file) {
            String path = "/storage/emulated/0/Documents/1.ppt";
            File file = new File(path);
            WebRTCVideoManager.instance().sendData(file);

        } else if (id == R.id.btn_start_voice) {
            targetClient = TARGET_CLIENT1;
            WebRTCSoundManager.instance().start();
        } else if (id == R.id.btn_stop_voice) {
            WebRTCSoundManager.instance().stop();
        }
    }


    private void startSignalChannel() {
        mProcessHandler.post(new Runnable() {
            @Override
            public void run() {
                if (iotSSE == null) {
                    iotSSE = new IotSSEMsgLib(mContext, new CustomerIOTMsgListener());
                    iotSSE.connectSSEAsSmartScreen(DEVICE_ID);
                }
            }
        });


    }

    private void stopSignalChannel() {
        if (null != iotSSE) {
            iotSSE.close();
        }
    }


    private class CustomerIOTMsgListener implements IotSSEMsgLib.IOTSSEMsgListener {

        @Override
        public String appSalt() {
            return "5619397646c8b3f43e82f61486e22bde";
        }

        @Override
        public void onSSELibError(IotSSEMsgLib.SSEErrorEnum sseErrorEnum, String s) {
            Log.d(TAG, "onSSELibError :error enum = " + sseErrorEnum + " error message = " + s);
        }

        @Override
        public void onSSEStarted() {
            Log.d(TAG, "onSSELibError onSSEStarted");
        }

        @Override
        public void onSendResult(IotSSEMsgLib.SSESendResultEnum sendResult, String destId, String msgId, String msgName, String message) {
            Log.d(TAG, " onSendResult: sendResult= " + sendResult + " destId = " + destId + " msgId = " + msgId + " msgName = " + msgName + " message=" + message);
        }

        @Override
        public void onReceivedSSEMessage(String msgId, String msgName, String message) {
            Log.d(TAG, "onReceivedSSEMessage message=" + message);
            try {
                IMMessage imMessage = IMMessage.Builder.decode(message);
                String content = imMessage.getContent();
                Session sourceSession = imMessage.getSource();
                String sendSid = sourceSession.getId();

                Log.d(TAG, "WebRtcClientService handleIMMessage: content =" + content);
                postData(sendSid, content, imMessage, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSendFileToCloud(IotSSEMsgLib.SendFileResultEnum sendFileResultEnum, String s, long l, long l1, int i, String s1, String s2) {
            // not usage
        }

        @Override
        public void onReceivedFileFromCloud(IotSSEMsgLib.ReceivedFileResultEnum receivedFileResultEnum, String s, long l, long l1, String s1) {
            // not usage
        }
    }


    private void postData(String sid, String content, IMMessage imMessage, SSChannel ssChannel) {
        if (content.contains(Constant.OFFER)) {
            Map<String, String> extras = imMessage.getExtra();
            Log.i(TAG, "Received Offer sid=" + sid + " extras=" + extras.size());

            SSEEvent event = new SSEEvent();
            Model model = new Model(content);
            event.setModel(model);
            event.setMsgType(Constant.OFFER);
            event.setTargetSid(sid);
            event.setSsChannel(ssChannel);
            event.setExtras(extras);

            EventBus.getDefault().postSticky(event);

        } else if (content.contains(Constant.ANSWER)) {
            Log.i(TAG, "Received Answer");

            SSEEvent event = new SSEEvent();
            Model model = new Model(content);
            event.setModel(model);
            event.setMsgType(Constant.ANSWER);
            //黏性事件 发送了该事件之后再订阅者依然能够接收到的事件
            EventBus.getDefault().postSticky(event);

        } else if (content.contains(Constant.CANDIDATE)) {
            Log.i(TAG, "Received candidate");
            SSEEvent event = new SSEEvent();
            Model model = new Model(content);
            event.setModel(model);
            event.setMsgType(Constant.CANDIDATE);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "EventBus post candidate");
                    EventBus.getDefault().post(event);
                }
            }, 1000);

        }

    }

    public void askForPermissions() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED)
                || (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED)
                || (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST);
        }
    }


    private void test() {
        //总时长 间隔时间
        CountDownTimerSupport mTimer = new CountDownTimerSupport(500000, 1000);
        mTimer.setOnCountDownTimerListener(new OnCountDownTimerListener() {
            @Override
            public void onTick(long millisUntilFinished) {
                // 倒计时间隔
                tv_time.setText(millisUntilFinished + "ms\n" + millisUntilFinished / 1000 + "s");
            }

            @Override
            public void onFinish() {
                // 倒计时结束
            }

            @Override
            public void onCancel() {
                // 倒计时手动停止
            }
        });

        mTimer.start();
    }


    /**
     * 线程初始化
     */
    private void initHandler() {
        mHandler = new Handler(Looper.getMainLooper());

        if (mProcessHandler == null) {
            HandlerThread handlerThread = new HandlerThread(
                    "handler looper Thread");
            handlerThread.start();
            mProcessHandler = new ProcessHandler(handlerThread.getLooper());
        }
    }

    /**
     * 子线程handler,looper
     *
     * @author Administrator
     */
    private class ProcessHandler extends Handler {

        public ProcessHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_THREAD_INIT_CONFIG_START:
                    break;
                default:
                    break;
            }

        }

    }
}
