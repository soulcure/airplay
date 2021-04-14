package com.threesoft.webrtc.webrtcroom.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.threesoft.webrtc.webrtcroom.R;
import com.threesoft.webrtc.webrtcroom.webrtcmodule.PeerConnectionParameters;
import com.threesoft.webrtc.webrtcroom.webrtcmodule.RtcListener;
import com.threesoft.webrtc.webrtcroom.webrtcmodule.WebRtcClient;
import com.threesoft.webrtc.webrtcroom.webrtcmodule.WebRtcClient2;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import org.json.JSONObject;
import org.webrtc.EglBase;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoTrack;

import java.util.HashMap;
import java.util.List;

public class ChatActivity extends Activity implements  RtcListener,View.OnClickListener{
    //控件
    private static final String TAG = "ChatActivity";
    private TextView roomName;
    private Button openCamera;
    private Button switchCamera;
    private Button createRoom;
    private Button exitRoom;
    private SurfaceViewRenderer localSurfaceViewRenderer;
    private LinearLayout remoteVideoLl;
    private HashMap<String,View> remoteViews;
    //EglBase
    private EglBase rootEglBase;
    //WebRtcClient
    //private WebRtcClient webRtcClient;
    //PeerConnectionParameters
    private PeerConnectionParameters peerConnectionParameters;
    //host地址
    //private String socketHost = "http://172.16.70.226:8081";
    //private String socketHost = "https://172.16.70.226:8443";
    //private String socketHost = "http://39.108.224.231:8443/";
    private String socketHost = "http://39.108.224.231:8888";

    //摄像头是否开启
    private boolean isCameraOpen = true;

    //申请录音权限
    private static final int GET_RECODE_AUDIO = 1;
    private static String[] PERMISSION_AUDIO = {
            Manifest.permission.RECORD_AUDIO
    };

    private String fromId;
    private String toId;
    private String roomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Log.d(TAG,"onCreate");
        Intent intent = getIntent();
        if(intent != null){
            fromId = intent.getStringExtra("from");
            toId = intent.getStringExtra("to");
            roomId = intent.getStringExtra("room");
        }


        roomName =  findViewById(R.id.room);
        openCamera = findViewById(R.id.openCamera);
        openCamera.setOnClickListener(this);
        switchCamera =  findViewById(R.id.switchCamera);
        switchCamera.setOnClickListener(this);
        createRoom = findViewById(R.id.create);
        createRoom.setOnClickListener(this);
        exitRoom = findViewById(R.id.exit);
        exitRoom.setOnClickListener(this);
        localSurfaceViewRenderer = findViewById(R.id.localVideo);
        remoteVideoLl = findViewById(R.id.remoteVideoLl);
        remoteViews = new HashMap<>();

        roomName.setText("from:"+fromId+",to:"+toId);

        createWebRtcClient();
        //默认打开摄像头;
        if(isCameraOpen){
            openCamera();
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG,"onNewIntent");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
        //某些机型锁屏点亮后需要重新开启摄像头
        if (isCameraOpen){
            WebRtcClient2.getInstance().startCamera(localSurfaceViewRenderer,WebRtcClient.FONT_FACTING);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy");
        //数据销毁
        localSurfaceViewRenderer.release();
        localSurfaceViewRenderer = null;


    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.openCamera:
                //开启_关闭摄像头
                if(isCameraOpen){
                    //关闭
                    WebRtcClient2.getInstance().stopCapture();
                    //数据
                    localSurfaceViewRenderer.clearImage();
                    localSurfaceViewRenderer.setBackground(new ColorDrawable(getResources().getColor(R.color.colorBlack)));
                    //localSurfaceViewRenderer.setForeground(new ColorDrawable(R.color.colorBlack));
                    localSurfaceViewRenderer.release();
                    isCameraOpen = false;
                    openCamera.setText("开启摄像头");
                }else{
                    //开启
                    openCamera();
                }
                break;
            case R.id.switchCamera:
                //切换摄像头
                switchCamera();
                break;
            case R.id.create:
                //创建并加入聊天室

                if(isCameraOpen){
                    WebRtcClient2.getInstance().createAndJoinRoom(roomId);
                    WebRtcClient2.getInstance().notifyJoinRoom(fromId,toId,roomId);
                    createRoom.setEnabled(false);
                }else{
                    Toast.makeText(this,"请先开启摄像头",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.exit:
                //退出聊天室
                WebRtcClient2.getInstance().exitRoom();
                createRoom.setEnabled(true);
                break;
            default:
                break;
        }
    }


    //创建配置参数
    private void createPeerConnectionParameters(){
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

    //创建webRtcClient
    private void createWebRtcClient(){
        Log.d(TAG,"createWebRtcClient");
            //配置参数
            createPeerConnectionParameters();
            //创建视频渲染器
            rootEglBase = EglBase.create();
            WebRtcClient2.getInstance().initRTC(rootEglBase,peerConnectionParameters,this);
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
                            Toast.makeText(ChatActivity.this, "没有获取照相机权限，该功能无法使用", Toast.LENGTH_SHORT).show();
                        }
                    }).start();
        }
    }



    //开启摄像头
    private void startCamera(){
        Log.d(TAG,"startCamera");
        //初始化渲染源
        localSurfaceViewRenderer.init(rootEglBase.getEglBaseContext(), null);
        //填充模式
        localSurfaceViewRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        localSurfaceViewRenderer.setZOrderMediaOverlay(true);
        localSurfaceViewRenderer.setEnableHardwareScaler(false);
        localSurfaceViewRenderer.setMirror(true);
        localSurfaceViewRenderer.setBackground(null);


        //启动摄像头
        WebRtcClient2.getInstance().startCamera(localSurfaceViewRenderer,WebRtcClient.FONT_FACTING);
        //状态设置
        isCameraOpen = true;
        //打开录音
        openAudioRecord();

        openCamera.setText("关闭摄像头");


    }


    //切换摄像头
    private void switchCamera(){

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
                            Toast.makeText(ChatActivity.this, "没有获取麦克风权限，该功能无法使用", Toast.LENGTH_SHORT).show();
                        }
                    }).start();
        }
    }





    //清空远端摄像头
    public void clearRemoteCamera(){
        remoteVideoLl.removeAllViews();
    }

    /* RtcListener 数据回调 */

    @Override
    public void onAddRemoteStream(String peerId, VideoTrack videoTrack) {
        ChatActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ////UI线程执行
                //构建远端view
                SurfaceViewRenderer remoteView = new SurfaceViewRenderer(ChatActivity.this);
                //初始化渲染源
                remoteView.init(rootEglBase.getEglBaseContext(), null);
                //填充模式
                remoteView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
                remoteView.setZOrderMediaOverlay(true);
                remoteView.setEnableHardwareScaler(false);
                remoteView.setMirror(true);
                //控件布局
                //LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(360,360);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(720,1280);
                layoutParams.topMargin = 20;
                remoteVideoLl.addView(remoteView,layoutParams);
                //添加至hashmap中
                remoteViews.put(peerId,remoteView);
                //添加数据
                //VideoTrack videoTrack = mediaStream.videoTracks.get(0);
                videoTrack.addSink(remoteView);
            }
        });
    }

    @Override
    public void onRemoveRemoteStream(String peerId) {
        ChatActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ////UI线程执行
                //移除远端view
                SurfaceViewRenderer remoteView = (SurfaceViewRenderer)remoteViews.get(peerId);
                if (remoteView != null){
                    remoteVideoLl.removeView(remoteView);
                    remoteViews.remove(peerId);
                    //数据销毁
                    remoteView.release();
                    remoteView = null;
                }
            }
        });
    }

    @Override
    public void onPeerTalkMsg(JSONObject msg) {

    }
}
