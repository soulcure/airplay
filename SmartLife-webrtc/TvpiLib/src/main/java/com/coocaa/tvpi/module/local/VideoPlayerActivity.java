package com.coocaa.tvpi.module.local;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.coocaa.publib.base.BaseAppletActivity;
import com.coocaa.publib.data.local.VideoData;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.data.channel.events.ProgressEvent;
import com.coocaa.smartsdk.SmartApi;
import com.coocaa.smartsdk.object.ISmartDeviceInfo;
import com.coocaa.tvpi.module.base.VirtualInputable;
import com.coocaa.tvpi.module.connection.ConnectDialogActivity;
import com.coocaa.tvpi.module.connection.WifiConnectActivity;
import com.coocaa.tvpi.module.log.LogParams;
import com.coocaa.tvpi.module.log.LogSubmit;
import com.coocaa.tvpi.util.TimeStringUtils;
import com.coocaa.tvpi.view.PushProgressDialogFragment;
import com.coocaa.tvpilib.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.ss.channel.im.IMMessageCallback;

import static com.coocaa.smartscreen.connect.SSConnectManager.CONNECT_BOTH;
import static com.coocaa.smartscreen.connect.SSConnectManager.CONNECT_LOCAL;
import static com.coocaa.smartscreen.connect.SSConnectManager.CONNECT_NOTHING;
import static com.coocaa.smartscreen.connect.SSConnectManager.TARGET_CLIENT_APP_STORE;
/**
 * 旧视频预览
 * */
public class VideoPlayerActivity extends BaseAppletActivity implements VirtualInputable {
    public static String KEY_VIDEO_DATAS = "KEY_VIDEO_DATAS";

    private String TAG = VideoPlayerActivity.class.getSimpleName();
    private static final long UPDATE_TIME = 500;
    private PushProgressDialogFragment pushProgressDialogFragment;
    private ImageView ivBack;
    private TextView tvTitle;
    private ImageView ivPauseOrStart;
    private TextView tvPlayDuring;
    private TextView tvTotalDuring;
    private SeekBar seekBar;
    private ImageView ivPush;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Handler uiHandler;
    private MediaPlayer mediaPlayer;
    private VideoData videoData;
    private boolean isPause;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_video_player);
        Intent intent = getIntent();
        if (intent != null) {
            videoData = (VideoData) intent.getSerializableExtra(KEY_VIDEO_DATAS);
        }
        EventBus.getDefault().register(this);
        uiHandler = new Handler(Looper.getMainLooper());
        initView();
        setListener();
        initPushFragment();
        Log.d("CCCC", "getNetworkForceKey=" + getNetworkForceKey());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        EventBus.getDefault().unregister(this);
        if (uiHandler != null) {
            uiHandler.removeCallbacksAndMessages(null);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ProgressEvent progressEvent) {
        Log.d(TAG, "onEvent: " + progressEvent.getInfo());

        if(progressEvent.getType() == IMMessage.TYPE.PROGRESS){
            uiHandler.removeCallbacksAndMessages(null);
            uiHandler.post(runnableSetProgress);
            uiHandler.postDelayed(runnablePushError, 15000);
            uiHandler.postDelayed(runnableDismiss, 16000);
            return;
        }
        if (pushProgressDialogFragment.isAdded()) {
            if(progressEvent.getType() != IMMessage.TYPE.RESULT){
                return;
            }
            if (progressEvent.isResultSuccess()) {
                uiHandler.removeCallbacksAndMessages(null);
                pushProgressDialogFragment.showPushSuccess();
                uiHandler.postDelayed(runnableDismiss, 1000);
            } else {
                uiHandler.removeCallbacksAndMessages(null);
                pushProgressDialogFragment.showPushError();
                uiHandler.postDelayed(runnableDismiss, 1000);
            }
        }
    }

    private void initView() {
        ivBack = findViewById(R.id.iv_back);
        tvTitle = findViewById(R.id.tv_title);
        ivPauseOrStart = findViewById(R.id.iv_start_or_pause);
        tvPlayDuring = findViewById(R.id.tv_during_play);
        tvTotalDuring = findViewById(R.id.tv_during_total);
        seekBar = findViewById(R.id.seekBar);
        ivPush = findViewById(R.id.iv_push);
        surfaceView = findViewById(R.id.surface);
        surfaceView.setZOrderOnTop(false);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.addCallback(surfaceHolderCallback);
        tvTitle.setText(videoData.tittle);
        seekBar.setMax((int) videoData.duration);
        seekBar.setProgress(0);
        Log.d(TAG, "initView: " + videoData.duration + "seekBar" + seekBar.getMax());
        tvTotalDuring.setText(TimeStringUtils.secToTime(videoData.duration / 1000));

        if(mNPAppletInfo != null) {
            View ivTop = findViewById(R.id.ll_top_bar);
            ivTop.setVisibility(View.GONE);
            setTitle(videoData.tittle);
        }
    }


    private void setListener() {
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ivPauseOrStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPause = !isPause;
                if (isPause) {
                    pausePlay();
                    stopUpdateProgress();
                } else {
                    startPlay();
                    startUpdateProgress();
                }
                updatePlayButtonBackground();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());

            }
        });

        ivPush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int connectState = SSConnectManager.getInstance().getConnectState();
                final ISmartDeviceInfo deviceInfo = SmartApi.getConnectDeviceInfo();
                Log.d(TAG, "pushToTv: connectState" + connectState);
                Log.d(TAG, "pushToTv: deviceInfo" + deviceInfo);
                //未连接
                if(connectState == CONNECT_NOTHING || deviceInfo == null){
                    ConnectDialogActivity.start(VideoPlayerActivity.this);
                    return;
                }
                //本地连接不通
                if(!(connectState == CONNECT_LOCAL || connectState == CONNECT_BOTH)){
                    WifiConnectActivity.start(VideoPlayerActivity.this);
                    return;
                }


                if (!pushProgressDialogFragment.isAdded()) {
                    pushProgressDialogFragment.showPushing();
                    uiHandler.postDelayed(runnablePushError, 10000);
                    uiHandler.postDelayed(runnableDismiss, 11000);
                }
                //ToastUtils.getInstance().showGlobalLong(getResources().getString(R.string.push_screen_success_tips));
                SSConnectManager.getInstance().sendVideoMessage(videoData.tittle, new File(videoData.url), TARGET_CLIENT_APP_STORE, new IMMessageCallback() {
                    @Override
                    public void onStart(IMMessage message) {
                        Log.d(TAG, "onStart: " + message.toString());
                    }

                    @Override
                    public void onProgress(IMMessage message, int progress) {
                        Log.d(TAG, "onProgress: " + progress);
                    }

                    @Override
                    public void onEnd(IMMessage message, int code, String info) {
                        Log.d(TAG, "onEnd: code = " + code + "  info = " + info);
                    }
                });
                submitLocalPushUMData();
            }
        });
    }

    private void initPushFragment() {
        pushProgressDialogFragment = new PushProgressDialogFragment().with((AppCompatActivity) VideoPlayerActivity.this);
        pushProgressDialogFragment.setIsLocalPush(false).setListener(new PushProgressDialogFragment.PushProgressDialogFragmentListener() {
            @Override
            public void onDialogDismiss() {
                uiHandler.removeCallbacksAndMessages(null);
            }
        });
    }


    private void submitLocalPushUMData() {
        DecimalFormat df = new DecimalFormat("#0.0");
        String size = String.valueOf(df.format(Double.valueOf(videoData.size) / 1024 / 1024));
        LogParams params = LogParams.newParams().append("applet_id", mApplet == null ? "" :mApplet.getId())
                .append("applet_name", mApplet == null ? "" :mApplet.getName())
                .append("file_size", size)
                .append("file_format",videoData.url.substring(videoData.url.lastIndexOf('.')+1));
        LogSubmit.event("file_cast_btn_clicked", params.getParams());
    }

    private SurfaceHolder.Callback surfaceHolderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            Log.d(TAG, "surfaceCreated");
            initMediaPlay();
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            stopPlay();
            releasePlay();
            stopUpdateProgress();
        }
    };

    private void initMediaPlay() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setDisplay(surfaceHolder);
        try {
            mediaPlayer.setDataSource(videoData.url);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                changeVideoSize();
            }
        });

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                startPlay();
                startUpdateProgress();
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (uiHandler != null) {
                    uiHandler.removeCallbacks(updateProgress);
                }
                isPause = true;
                updatePlayButtonBackground();
                seekBar.setProgress(0);
                tvPlayDuring.setText(TimeStringUtils.secToTime(0));
            }
        });

    }


    private void startPlay() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            isPause = false;
        }
    }

    private void pausePlay() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPause = true;
        }
    }

    private void stopPlay() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            isPause = false;
        }
    }

    private void releasePlay() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }


    public void changeVideoSize() {
        int videoWidth = mediaPlayer.getVideoWidth();
        int videoHeight = mediaPlayer.getVideoHeight();

        int surfaceWidth = getResources().getDisplayMetrics().widthPixels;
        int surfaceHeight = DimensUtils.getDeviceHeight(this) ;
        if(mNPAppletInfo != null){
            surfaceHeight = (int) (surfaceHeight - getResources().getDimension(R.dimen.runtime_title_height));
        }

        //根据视频尺寸去计算->视频可以在sufaceView中放大的最大倍数。
        float max = 1;
        if (getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            //竖屏模式下按视频宽度计算放大倍数值
            max = Math.max((float) videoWidth / (float) surfaceWidth, (float) videoHeight / (float) surfaceHeight);
        }

        //视频宽高分别/最大倍数值 计算出放大后的视频尺寸
        videoWidth = (int) Math.ceil((float) videoWidth / max);
        videoHeight = (int) Math.ceil((float) videoHeight / max);

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) surfaceView.getLayoutParams();
        layoutParams.width = videoWidth;
        layoutParams.height = videoHeight;
        surfaceView.setLayoutParams(layoutParams);
    }

    private void startUpdateProgress() {
        uiHandler.post(updateProgress);
    }

    private void stopUpdateProgress() {
        if (uiHandler != null) {
            uiHandler.removeCallbacks(updateProgress);
        }
    }

    private Runnable updateProgress = new Runnable() {
        @Override
        public void run() {
            doUpdateProgress();
            uiHandler.postDelayed(this, UPDATE_TIME);
        }
    };

    private void updatePlayButtonBackground() {
        if (isPause) {
            ivPauseOrStart.setBackgroundResource(R.drawable.icon_play_state_pause);
        } else {
            ivPauseOrStart.setBackgroundResource(R.drawable.icon_play_state_playing);
        }
    }

    private void doUpdateProgress() {
        int currentPosition = mediaPlayer.getCurrentPosition();
        if (currentPosition >= videoData.duration) {
            currentPosition = (int) videoData.duration;
        }
        seekBar.setProgress(currentPosition);
        tvPlayDuring.setText(TimeStringUtils.secToTime(currentPosition / 1000));
    }

    Runnable runnableSetProgress = new Runnable() {
        @Override
        public void run() {
            if (pushProgressDialogFragment.isAdded()) {
                pushProgressDialogFragment.setProgress(80);
            }
        }
    };

    Runnable runnablePushError = new Runnable() {
        @Override
        public void run() {
            if (pushProgressDialogFragment.isAdded()) {
                pushProgressDialogFragment.showPushTimeout();
            }

        }
    };


    Runnable runnableDismiss = new Runnable() {
        @Override
        public void run() {
            if (pushProgressDialogFragment.isAdded()) {
                pushProgressDialogFragment.dismiss();
            }
        }
    };
}
