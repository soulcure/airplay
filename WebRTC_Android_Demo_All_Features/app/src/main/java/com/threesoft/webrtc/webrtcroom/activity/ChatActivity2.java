package com.threesoft.webrtc.webrtcroom.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.threesoft.webrtc.webrtcroom.R;
import com.threesoft.webrtc.webrtcroom.webrtcmodule.AudioUtil;
import com.threesoft.webrtc.webrtcroom.webrtcmodule.JoinMsg;
import com.threesoft.webrtc.webrtcroom.webrtcmodule.PeerConnectionParameters;
import com.threesoft.webrtc.webrtcroom.webrtcmodule.RtcListener;
import com.threesoft.webrtc.webrtcroom.webrtcmodule.WebRtcClient;
import com.threesoft.webrtc.webrtcroom.webrtcmodule.WebRtcClient2;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.EglBase;
import org.webrtc.Logging;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoFrame;
import org.webrtc.VideoSink;
import org.webrtc.VideoTrack;

import java.util.List;

public class ChatActivity2 extends Activity implements  RtcListener{
    //控件
    private static final String TAG = "ChatActivity2";
    private TextView roomName;
    private TextView waiting;
    private ImageButton switchCamera;
    private ImageButton createRoom;
    private ImageButton disableMic;
    private ImageButton switchLaba;
    private ImageButton lupingBtn;
    private LinearLayout mengceng;
    private LinearLayout llBottomCall;
    private ImageButton videoPlayBtn;
    private TextView tvMsgView;
    private EditText etSendMsg;
    private Button btnSend;
    private ImageButton btnFileSend;

    private SurfaceViewRenderer pipSurfaceViewRenderer;
    private SurfaceViewRenderer fullSurfaceViewRenderer;
    private SurfaceViewRenderer videoSurfaceViewRenderer;

    private final ProxyVideoSink remoteProxyRenderer = new ProxyVideoSink();
    private final ProxyVideoSink localProxyVideoSink = new ProxyVideoSink();
    private final ProxyVideoSink videoProxyVideoSink = new ProxyVideoSink();

    private EglBase rootEglBase;

    private PeerConnectionParameters peerConnectionParameters;

    private String socketHost = "http://39.108.224.231:8888";


    private boolean isCameraOpen = true;
    private boolean isCalling = false;
    private boolean isMicEnabled = true;
    private boolean isSwappedSurface;
    private boolean isSwappedSurface2;
    private boolean isWaifang = true;
    private boolean isLuping = false;
    private boolean isVideoPlaying = false;

    //录屏
    private static Intent mediaProjectionPermissionResultData;

    //视频文件
    private static Intent localVideoResultData;

    private static final int CAPTURE_PERMISSION_REQUEST_CODE = 1;
    private static final int LOCAL_VIDEO_REQUEST_CODE = 2;
    private static final int LOCAL_FILE_REQUEST_CODE = 3;
    private SelectMediaUtil selectMediaUtil= new SelectMediaUtil();

    //申请录音权限
    private static final int GET_RECODE_AUDIO = 1;
    private static String[] PERMISSION_AUDIO = {
            Manifest.permission.RECORD_AUDIO
    };


    private String fromId;
    private String toId;
    private String roomId;
    private int reason;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        if(android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1){
            setShowWhenLocked(true);
        }
        getWindow().getDecorView().setSystemUiVisibility(getSystemUiVisibility());


        setContentView(R.layout.activity_chat2);
        Log.d(TAG,"onCreate");
        Intent intent = getIntent();
        if(intent != null){
            fromId = intent.getStringExtra("from");
            toId = intent.getStringExtra("to");
            roomId = intent.getStringExtra("room");
            reason = intent.getIntExtra("reason",2);

            Log.d(TAG,"onCreate,intent"+intent+",from:"+fromId+",to:"+toId+",room:"+roomId);
        }

        rootEglBase = EglBase.create();

        roomName =  findViewById(R.id.contact_name_call);
        roomName.setText("from:"+fromId+",to:"+toId);
        waiting = findViewById(R.id.tv_waiting);

        switchLaba = findViewById(R.id.btn_laba);
        switchLaba.setOnClickListener(switchLabaListener);
        //默认开外放
        isWaifang = true;
        AudioUtil.getInstance(getApplicationContext()).setSpeakerStatus(true);

        lupingBtn = findViewById(R.id.btn_luping);
        lupingBtn.setOnClickListener(lupingListener);
        isLuping = false;
        if(reason == 2){
            lupingBtn.setVisibility(View.INVISIBLE);
        }

        videoPlayBtn = findViewById(R.id.btn_local_video);
        videoPlayBtn.setOnClickListener(videoPlayBtnListener);
        isVideoPlaying = false;

        tvMsgView = findViewById(R.id.tv_msg);
        etSendMsg = findViewById(R.id.et_send_msg);
        btnSend = findViewById(R.id.btn_data_send);
        btnSend.setOnClickListener(datasendBtnListener);

        btnFileSend = findViewById(R.id.btn_file_send);
        btnFileSend.setOnClickListener(filesendBtnListener);


        mengceng = findViewById(R.id.mengceng);

        llBottomCall = findViewById(R.id.buttons_call_container);


        switchCamera =  findViewById(R.id.button_call_switch_camera);
        switchCamera.setOnClickListener(switchCameraListener);

        createRoom = findViewById(R.id.btn_call);
        createRoom.setOnClickListener(createRoomListener);

        disableMic = findViewById(R.id.button_call_toggle_mic);
        disableMic.setOnClickListener(disableMicListener);

        pipSurfaceViewRenderer = findViewById(R.id.pip_surface);
        pipSurfaceViewRenderer.setVisibility(View.INVISIBLE);
        fullSurfaceViewRenderer = findViewById(R.id.full_surface);
        videoSurfaceViewRenderer = findViewById(R.id.video_surface);
//        videoSurfaceViewRenderer.setVisibility(View.INVISIBLE);
        initSurface();
        setSwappedSurface(true);
        isSwappedSurface2 = false;
        videoProxyVideoSink.setTarget(videoSurfaceViewRenderer);
        pipSurfaceViewRenderer.setOnClickListener(pipClickListener);
        videoSurfaceViewRenderer.setOnClickListener(videsurfaceListener);

        if(rootEglBase != null){

            //默认打开摄像头;
            if(isCameraOpen){
                openCamera();
            }
        }else{
            Log.e(TAG,"eglBase create faild ,finish now");
            finish();
        }

    }
    private  View.OnClickListener switchLabaListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
             isWaifang = !isWaifang;
            AudioUtil.getInstance(getApplicationContext()).setSpeakerStatus(isWaifang);
            if(isWaifang){
                switchLaba.setBackground(getDrawable(R.mipmap.waifang));
            }else{
                switchLaba.setBackground(getDrawable(R.mipmap.tingtong));
            }
        }
    };

    private View.OnClickListener lupingListener = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            isLuping = ! isLuping;
            if(isLuping){
                Log.d(TAG,"录屏开始");
                lupingBtn.setBackground(getDrawable(R.mipmap.luping3));
                videoPlayBtn.setVisibility(View.INVISIBLE);
                llBottomCall.setVisibility(View.INVISIBLE);
                startCall();
                startScreenCapture();


            }else{
                Log.d(TAG,"录屏结束");
//                lupingBtn.setBackground(getDrawable(R.mipmap.luping2));
//                WebRtcClient2.getInstance().closePeers();
//                //启动摄像头
//                startCamera();
                finish();
            }
        }
    };


    private View.OnClickListener videoPlayBtnListener = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            openLocalVideo();
//            if(!isVideoPlaying){
//                videoPlayBtn.setBackground(getDrawable(R.mipmap.local_video2));
//                lupingBtn.setVisibility(View.INVISIBLE);
//                //llBottomCall.setVisibility(View.INVISIBLE);
//
//                openLocalVideo();
//
//
//            }else {
//                finish();
//            }
        }
    };

    private View.OnClickListener datasendBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            String msg = etSendMsg.getText().toString();
            Log.d(TAG,"datasendBtnListener,msg:"+msg);
            WebRtcClient2.getInstance().peersendTalk(msg);
        }
    };

    private View.OnClickListener filesendBtnListener = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            Log.d(TAG,"filesendBtnListener ");
            openLocalFile();
        }
    };


    private  View.OnClickListener switchCameraListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switchCamera();
        }
    };
    private View.OnClickListener createRoomListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(isCalling == false){
                //非通话中，点击开始通话
                lupingBtn.setVisibility(View.INVISIBLE);
                //videoPlayBtn.setVisibility(View.INVISIBLE);
                startCall();
            }else{
                //通话中，点击断开通话;
                Log.d(TAG,"结束通话");
                finish();
            }
        }
    };

    private View.OnClickListener disableMicListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            isMicEnabled = !isMicEnabled;
            if(isMicEnabled){
                disableMic.setBackground(getDrawable(R.mipmap.mic));
            }else{
                disableMic.setBackground(getDrawable(R.mipmap.mic_disable));
            }

            WebRtcClient2.getInstance().setAudioEnabled(isMicEnabled);

        }
    };

    private View.OnClickListener pipClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(TAG,"pip clicked");
            setSwappedSurface(!isSwappedSurface);
        }
    };
    private View.OnClickListener videsurfaceListener =new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(TAG,"video surface clicked");
            setSwappedSurface2(!isSwappedSurface2);
        }
    };

    private void startCall(){
        Log.d(TAG,"开始通话");
        WebRtcClient2.getInstance().createAndJoinRoom(roomId);
        if(reason == 1){
            WebRtcClient2.getInstance().notifyJoinRoom(fromId,toId,roomId);
            waiting.setVisibility(View.VISIBLE);
        }

        createRoom.setBackground(getDrawable(R.mipmap.disconnect));
        isCalling = true;
        switchCamera.setVisibility(View.VISIBLE);
        disableMic.setVisibility(View.VISIBLE);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG,"onNewIntent,intent:"+intent);
        if(intent != null){
            fromId = intent.getStringExtra("from");
            toId = intent.getStringExtra("to");
            roomId = intent.getStringExtra("room");
            Log.d(TAG,"onNewIntent,intent,from:"+fromId+",to:"+toId+",room:"+roomId);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
        //某些机型锁屏点亮后需要重新开启摄像头
        if (isCameraOpen){
            WebRtcClient2.getInstance().startCamera(localProxyVideoSink,WebRtcClient.FONT_FACTING);
        }
    }



    //创建配置参数
    private void createPeerConnectionParameters(){
        Log.d(TAG,"createPeerConnectionParameters");
        //获取webRtc 音视频配置参数
        Point displaySize = new Point();
        this.getWindowManager().getDefaultDisplay().getSize(displaySize);
        displaySize.set(480,320);
        peerConnectionParameters =  new PeerConnectionParameters(true, false,
                    false, displaySize.x, displaySize.y, 30,
                    0, "VP8",
                    true,false,0,"OPUS",
                    false,false,false,false,false,false,
                    false,false,false,false);
    }

    private void createFileVideoPeerParameters(int width,int height){
        Log.d(TAG,"createFileVideoPeerParameters");

        peerConnectionParameters =  new PeerConnectionParameters(true, false,
                false, width, height, 30,
                0, "H264 High",
                true,false,0,"OPUS",
                false,false,false,false,false,false,
                false,false,false,false);
    }



    //本地摄像头创建
    private void openCamera(){
        Log.d(TAG,"openCamera");
        if(AndPermission.hasPermissions(this,Permission.Group.CAMERA)){
            startCamera();
        }else{
            AndPermission.with(this)
                    .runtime()
                    .permission(Permission.Group.CAMERA)
                    .onGranted(new Action<List<String>>() {
                        @Override
                        public void onAction(List<String> data) {
                            //申请权限成功
                            startCamera();
                        }
                    })
                    .onDenied(new Action<List<String>>() {
                        @Override
                        public void onAction(List<String> data) {
                            //当用户没有允许该权限时，回调该方法
                            Toast.makeText(ChatActivity2.this, "没有获取照相机权限，该功能无法使用", Toast.LENGTH_SHORT).show();
                        }
                    }).start();
        }
    }


    //开启摄像头
    private void startCamera(){
        Log.d(TAG,"startCamera");
        createPeerConnectionParameters();
        WebRtcClient2.getInstance().initRTC(rootEglBase,peerConnectionParameters,this);
        //启动摄像头
        WebRtcClient2.getInstance().startCamera(localProxyVideoSink,WebRtcClient.FONT_FACTING);

        WebRtcClient2.getInstance().initFileVideoCapture(videoProxyVideoSink);
        //状态设置
        isCameraOpen = true;
        isLuping = false;
        isVideoPlaying = false;
        //打开录音
        openAudioRecord();

    }


    private void initSurface(){

        pipSurfaceViewRenderer.init(rootEglBase.getEglBaseContext(), null);
        pipSurfaceViewRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        pipSurfaceViewRenderer.setZOrderMediaOverlay(true);
        pipSurfaceViewRenderer.setEnableHardwareScaler(true);
        pipSurfaceViewRenderer.setMirror(true);
        pipSurfaceViewRenderer.setBackground(null);

        fullSurfaceViewRenderer.init(rootEglBase.getEglBaseContext(), null);
        fullSurfaceViewRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        fullSurfaceViewRenderer.setEnableHardwareScaler(false);
        fullSurfaceViewRenderer.setMirror(true);


        videoSurfaceViewRenderer.init(rootEglBase.getEglBaseContext(), null);
        videoSurfaceViewRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        videoSurfaceViewRenderer.setZOrderMediaOverlay(true);
        videoSurfaceViewRenderer.setEnableHardwareScaler(true);
        videoSurfaceViewRenderer.setMirror(true);
        videoSurfaceViewRenderer.setBackground(null);

    }

    private void setSwappedSurface(boolean isSwappedFeeds) {
        Log.d(TAG, "setSwappedFeeds: " + isSwappedFeeds);
        this.isSwappedSurface = isSwappedFeeds;
        localProxyVideoSink.setTarget(isSwappedFeeds ? fullSurfaceViewRenderer : pipSurfaceViewRenderer);
        remoteProxyRenderer.setTarget(isSwappedFeeds ? pipSurfaceViewRenderer : fullSurfaceViewRenderer);
        fullSurfaceViewRenderer.setMirror(isSwappedFeeds);
        pipSurfaceViewRenderer.setMirror(!isSwappedFeeds);
    }
    private void setSwappedSurface2(boolean isSwappedFeeds) {
        Log.d(TAG, "setSwappedFeeds2: " + isSwappedFeeds);
        this.isSwappedSurface2 = isSwappedFeeds;
        videoProxyVideoSink.setTarget(isSwappedFeeds ? fullSurfaceViewRenderer : videoSurfaceViewRenderer);
        remoteProxyRenderer.setTarget(isSwappedFeeds ? videoSurfaceViewRenderer : fullSurfaceViewRenderer);
        fullSurfaceViewRenderer.setMirror(isSwappedFeeds);
        videoSurfaceViewRenderer.setMirror(!isSwappedFeeds);
    }


    //切换摄像头
    private void switchCamera(){
        Log.d(TAG,"switchCamera");
        WebRtcClient2.getInstance().switchCamera();
    }
    //本地摄像头创建
    private void openAudioRecord(){
        Log.d(TAG,"openAudioRecord");
        if(AndPermission.hasPermissions(this,Permission.Group.MICROPHONE)){
            WebRtcClient2.getInstance().createAudioTrack();
        }else{
            AndPermission.with(this)
                    .runtime()
                    .permission(Permission.Group.MICROPHONE)
                    .onGranted(new Action<List<String>>() {
                        @Override
                        public void onAction(List<String> data) {
                            //申请权限成功
                            WebRtcClient2.getInstance().createAudioTrack();
                        }
                    })
                    .onDenied(new Action<List<String>>() {
                        @Override
                        public void onAction(List<String> data) {
                            //当用户没有允许该权限时，回调该方法
                            Toast.makeText(ChatActivity2.this, "没有获取麦克风权限，该功能无法使用", Toast.LENGTH_SHORT).show();
                        }
                    }).start();
        }
    }


    /* RtcListener 数据回调 */
    private int  remoteTrackNumber = 0;

    @Override
    public void onAddRemoteStream(String peerId, VideoTrack videoTrack) {
        Log.d(TAG,"onAddRemoteStream ,peeerId:"+peerId);
        if(ChatActivity2.this == null){
            Log.d(TAG,"onRemoveRemoteStream ,activity is destoryed");
        }
        ChatActivity2.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String trackId = videoTrack.id();
                Log.d(TAG,"onAddRemoteStream, trackId:"+trackId);
                if(remoteTrackNumber == 0){
                    mengceng.setVisibility(View.GONE);
                    if(reason == 1){
                        waiting.setVisibility(View.INVISIBLE);
                    }
                    Log.d(TAG,"onAddRemoteStream :"+remoteTrackNumber);
                    pipSurfaceViewRenderer.setVisibility(View.VISIBLE);
                    videoTrack.addSink(remoteProxyRenderer);
                    setSwappedSurface(!isSwappedSurface);
                    remoteTrackNumber ++ ;
//                lupingBtn.setVisibility(View.VISIBLE);
                }else {
                    Log.d(TAG,"onAddRemoteStream :"+remoteTrackNumber);
                    videoSurfaceViewRenderer.setVisibility(View.VISIBLE);
                    videoTrack.addSink(videoProxyVideoSink);
                    remoteTrackNumber ++ ;
                }


            }
        });
    }

    @Override
    public void onRemoveRemoteStream(String peerId) {
        Log.d(TAG,"onRemoveRemoteStream,peerId"+peerId);
        finish();
    }

    @Override
    public void onPeerTalkMsg(JSONObject msg){
        Log.d(TAG,"onPeerDataChannelMsg,msg:"+msg);
        ChatActivity2.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    String from = msg.getString("from");
                    int msgType = msg.getInt("msgType");
                    JSONObject data = msg.getJSONObject("data");
                    String showString = "";
                    switch(msgType){
                        case 1:
                            String content = data.getString("content");
                            showString =from+":"+content;
                            break;
                        case 2:
                            int action = data.getInt("action");
                            String fileType = data.getString("fileType");
                            String fileName = data.getString("fileName");
                            showString = from +":"+fileType+":"+fileName;
                            break;
                        case 3:

                            break;
                    }
                    String finalShowString = showString;
                    ChatActivity2.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvMsgView.setText(finalShowString);
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG,"onStop");
        WebRtcClient2.getInstance().stopCapture();

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG,"onStart");
        WebRtcClient2.getInstance().startCapture();
    }
    @Override
    protected void onDestroy() {
        Log.d(TAG,"onDestroy");
         if(reason == 1){
             WebRtcClient2.getInstance().exitNotify(fromId,toId,roomId);
         }
        exitCall();
        Thread.setDefaultUncaughtExceptionHandler(null);
        super.onDestroy();
    }

    private void exitCall(){
        Log.d(TAG,"exitCall start");
        pipSurfaceViewRenderer.release();
        fullSurfaceViewRenderer.release();
        pipSurfaceViewRenderer = null;
        fullSurfaceViewRenderer = null;

        remoteProxyRenderer.setTarget(null);
        localProxyVideoSink.setTarget(null);
        videoProxyVideoSink.setTarget(null);

        WebRtcClient2.getInstance().exitRoom();
        rootEglBase = null;
        isCalling = false;
        finish();

        Log.d(TAG,"exitCall end");
    }


    private static class ProxyVideoSink implements VideoSink {
        private VideoSink target;

        @Override
        synchronized public void onFrame(VideoFrame frame) {
            if (target == null) {
                Logging.d(TAG, "Dropping frame in proxy because target is null.");
                return;
            }

            target.onFrame(frame);
        }

        synchronized public void setTarget(VideoSink target) {
            this.target = target;
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUserJoinRoom(JoinMsg msg) {
        Log.d(TAG,"onUserJoinRoom,from:"+msg.from);
        if(reason == 1){
            waiting.setText("对方已接收，视频准备中...");
        }

    }

    @TargetApi(19)
    private static int getSystemUiVisibility() {
        int flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            flags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        return flags;
    }


    @TargetApi(17)
    private DisplayMetrics getDisplayMetrics() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager =
                (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getRealMetrics(displayMetrics);
        return displayMetrics;
    }
    @TargetApi(21)
    private void startScreenCapture() {
        MediaProjectionManager mediaProjectionManager =
                (MediaProjectionManager) getApplication().getSystemService(
                        Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(
                mediaProjectionManager.createScreenCaptureIntent(), CAPTURE_PERMISSION_REQUEST_CODE);
    }

    private void openLocalVideo(){
        Log.d(TAG,"openLocalVideo");

//        Intent intent = new Intent(ChatActivity2.this,LocalVideoListActivity.class);
//        startActivityForResult(intent,LOCAL_VIDEO_REQUEST_CODE);

//        Intent intent = new Intent();
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        intent.setDataAndType(android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI,"video/*");
//        startActivityForResult(intent,LOCAL_VIDEO_REQUEST_CODE);
        selectMediaUtil.select(this, SelectMediaUtil.SelectType.video);

    }

    private void openLocalFile(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent,LOCAL_FILE_REQUEST_CODE);
    }




    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(TAG,"onActivityResult requestCode:"+requestCode+",resultCode:"+resultCode);
        if(resultCode != RESULT_OK){
            Log.e(TAG,"onActivityResult not ok");
            return;
        }
        switch(requestCode){
            case CAPTURE_PERMISSION_REQUEST_CODE:
                Log.d(TAG,"onActivityResult,录屏授权成功，开始录屏");
                mediaProjectionPermissionResultData = data;
                if(isCameraOpen){
                    WebRtcClient2.getInstance().closePeers();
                    isCameraOpen = false;
                }

                startScreenCaptureImpl();
                break;
            case LOCAL_VIDEO_REQUEST_CODE:
//                if(isCameraOpen){
//                    WebRtcClient2.getInstance().closePeers();
//                    isCameraOpen = false;
//                }
                String videoPath = selectMediaUtil.onActivityResult(requestCode, resultCode, data);

                Log.d(TAG,"onActivityResult,文件选择成功，开始视频分享,path:"+videoPath);
                startFileVideoCapture2(videoPath);
                break;
            case LOCAL_FILE_REQUEST_CODE:
                Uri uri = data.getData();
                if(uri != null){
                    String filePath = getFilePath(this,uri);
                    Log.d(TAG,"onActivityResult,开始传输文件,file:"+filePath);
                    startFileSend(filePath);
                }

                break;

        }


    }


    private void startScreenCaptureImpl(){
        Log.d(TAG,"startScreenCaptureImpl");

        DisplayMetrics displayMetrics = getDisplayMetrics();
        createPeerConnectionParameters();
        WebRtcClient2.getInstance().initRTC(rootEglBase,peerConnectionParameters,this);
        WebRtcClient2.getInstance().setScreenCaptureParameter(displayMetrics.widthPixels,displayMetrics.heightPixels,mediaProjectionPermissionResultData);
        WebRtcClient2.getInstance().startScreenCapture(localProxyVideoSink);
        //状态设置
        isCameraOpen = false;
        isVideoPlaying = false;
        //打开录音
        openAudioRecord();

    }

    private void startFileVideoCapture(String path){

        Log.d(TAG,"startFileVideoCapture,path:"+path);

        createPeerConnectionParameters();
        WebRtcClient2.getInstance().initRTC(rootEglBase,peerConnectionParameters,ChatActivity2.this);

        WebRtcClient2.getInstance().setFileVideoCaptureParameter(path);
        WebRtcClient2.getInstance().startFileVideoCapture(localProxyVideoSink);
        //状态设置
        isCameraOpen = false;
        isLuping = false;
        isVideoPlaying = true;

        //打开录音
        openAudioRecord();
        startCall();
    }

    private void startFileVideoCapture2(String path){

        Log.d(TAG,"startFileVideoCapture2,path:"+path);
//        createPeerConnectionParameters();
//        WebRtcClient2.getInstance().initRTC(rootEglBase,peerConnectionParameters,ChatActivity2.this);

//        WebRtcClient2.getInstance().setFileVideoCaptureParameter(path);
//        WebRtcClient2.getInstance().startFileVideoCapture2(videoSurfaceViewRenderer);
        if(isVideoPlaying){
            WebRtcClient2.getInstance().stopFileVideoCapture();
        }

        WebRtcClient2.getInstance().startFileVideoCapture(path);
        //状态设置
//        isCameraOpen = false;
        isLuping = false;
        isVideoPlaying = true;

        //打开录音
//        openAudioRecord();
//        startCall();
    }

    private void startFileSend(String filepath){
        Log.d(TAG,"startFileSend,filePath:"+filepath);
        WebRtcClient2.getInstance().peerSendFile(filepath,"other",1);
    }

    public String getFilePath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
//                Log.i(TAG,"isExternalStorageDocument***"+uri.toString());
//                Log.i(TAG,"docId***"+docId);
//                以下是打印示例：
//                isExternalStorageDocument***content://com.android.externalstorage.documents/document/primary%3ATset%2FROC2018421103253.wav
//                docId***primary:Test/ROC2018421103253.wav
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
//                Log.i(TAG,"isDownloadsDocument***"+uri.toString());
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
//                Log.i(TAG,"isMediaDocument***"+uri.toString());
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
//            Log.i(TAG,"content***"+uri.toString());
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
//            Log.i(TAG,"file***"+uri.toString());
            return uri.getPath();
        }
        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public String getDataColumn(Context context, Uri uri, String selection,
                                String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    public boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


}
