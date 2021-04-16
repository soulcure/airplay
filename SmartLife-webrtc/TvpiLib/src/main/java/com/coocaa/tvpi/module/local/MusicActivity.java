package com.coocaa.tvpi.module.local;

import android.Manifest;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.coocaa.publib.base.BaseAppletActivity;
import com.coocaa.publib.data.local.AudioData;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.data.channel.events.ProgressEvent;
import com.coocaa.smartsdk.SmartApi;
import com.coocaa.smartsdk.object.ISmartDeviceInfo;
import com.coocaa.tvpi.module.connection.ConnectDialogActivity;
import com.coocaa.tvpi.module.connection.WifiConnectActivity;
import com.coocaa.tvpi.module.local.adapter.MusicAdapter;
import com.coocaa.tvpi.module.local.utils.MusicBrowseAsyncTask;
import com.coocaa.tvpi.module.local.view.LocalResStatesView;
import com.coocaa.tvpi.module.log.LogParams;
import com.coocaa.tvpi.module.log.LogSubmit;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpi.util.TimeStringUtils;
import com.coocaa.tvpi.util.permission.PermissionListener;
import com.coocaa.tvpi.util.permission.PermissionsUtil;
import com.coocaa.tvpi.view.CommonTitleBar;
import com.coocaa.tvpi.view.PushProgressDialogFragment;
import com.coocaa.tvpi.view.decoration.CommonVerticalItemDecoration;
import com.coocaa.tvpilib.R;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.Executors;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.ss.channel.im.IMMessageCallback;

import static com.coocaa.smartscreen.connect.SSConnectManager.CONNECT_BOTH;
import static com.coocaa.smartscreen.connect.SSConnectManager.CONNECT_LOCAL;
import static com.coocaa.smartscreen.connect.SSConnectManager.CONNECT_NOTHING;
import static com.coocaa.smartscreen.connect.SSConnectManager.TARGET_CLIENT_APP_STORE;

/**
 * @ClassName DLNAMusicActivity
 * @Description
 * @User WHY
 * @Date 2018/7/26
 */
public class MusicActivity extends BaseAppletActivity implements MusicAdapter.OnMusicItemClickListener {

    private static final String TAG = MusicActivity.class.getSimpleName();

    private Context mContext;
    private MusicBrowseAsyncTask mMusicBrowseAsyncTask;
    private RecyclerView mRecyclerView;
    private MusicAdapter mAdapter;
    private PushProgressDialogFragment pushProgressDialogFragment;
    private LocalResStatesView localResStatesView;
    private CommonTitleBar titleBar;

    private MediaPlayer mediaPlayer;
    Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        EventBus.getDefault().register(this);
        setContentView(R.layout.local_activity_music);
        StatusBarHelper.translucent(this, 0xfff2f2f2);
        StatusBarHelper.setStatusBarLightMode(this);
        initView();
        initTitle();
        checkPermission();
    }

    private void checkPermission() {
        PermissionsUtil.getInstance().requestPermission(this, new PermissionListener() {
            @Override
            public void permissionGranted(String[] permission) {
                initData();
                initPushFragment();
            }

            @Override
            public void permissionDenied(String[] permission) {
                localResStatesView.setViewLoadState(LocalResStatesView.STATE_NO_PERMISSION);
            }
        }, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(TAG); // 统计页面
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(TAG); // 统计页面
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ProgressEvent progressEvent) {
        Log.d(TAG, "onEvent: " + progressEvent.getInfo());

        if(progressEvent.getType() == IMMessage.TYPE.PROGRESS){
            mHandler.removeCallbacksAndMessages(null);
            mHandler.post(runnableSetProgress);
            mHandler.postDelayed(runnablePushError, 15000);
            mHandler.postDelayed(runnableDismiss, 16000);
            return;
        }
        if (pushProgressDialogFragment.isAdded()) {
            if(progressEvent.getType() != IMMessage.TYPE.RESULT){
                return;
            }
            if (progressEvent.isResultSuccess()) {
                mHandler.removeCallbacksAndMessages(null);
                pushProgressDialogFragment.showPushSuccess();
                mHandler.postDelayed(runnableDismiss, 1000);
            } else {
                mHandler.removeCallbacksAndMessages(null);
                pushProgressDialogFragment.showPushError();
                mHandler.postDelayed(runnableDismiss, 1000);
            }
        }
    }

    private void initData() {
        mMusicBrowseAsyncTask = new MusicBrowseAsyncTask(this,
                new MusicBrowseAsyncTask.MusicBrowseCallback() {
                    @Override
                    public void onResult(List<AudioData> result) {
                        Log.d(TAG, "onResult: " + result);
                        if (null != result && result.size() > 0) {
                            mAdapter.addAll(result);
                            localResStatesView.setViewLoadState(LocalResStatesView.STATE_LOAD_FINISH);
                        } else {
                            localResStatesView.setViewLoadState(LocalResStatesView.STATE_NO_DATA, "暂未搜索到相关音乐");
                        }
                    }
                });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mMusicBrowseAsyncTask.executeOnExecutor(Executors.newCachedThreadPool());
        } else {
            mMusicBrowseAsyncTask.execute();
        }
    }

    private void initView() {
        if (mNPAppletInfo != null) {
            ViewGroup content = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.local_activity_music, null);
            mRecyclerView = content.findViewById(R.id.activity_music_recyclerview);
            localResStatesView = content.findViewById(R.id.local_res_state_view);
            titleBar = content.findViewById(R.id.titleBar);
            content.removeView(titleBar);
            setContentView(content);
        } else {
            mRecyclerView = findViewById(R.id.activity_music_recyclerview);
            localResStatesView = findViewById(R.id.local_res_state_view);
            titleBar = findViewById(R.id.titleBar);
        }
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.addItemDecoration(new CommonVerticalItemDecoration(0, 0, DimensUtils
                .dp2Px(this, 50f)));
        mAdapter = new MusicAdapter(this);
        mAdapter.setOnMusicItemClickLis(this);
        mRecyclerView.setAdapter(mAdapter);
        mediaPlayer = new MediaPlayer();
        mHandler = new Handler();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (mediaPlayer != null) {
                    mAdapter.refreshPlayTime(TimeStringUtils.secToTime(mediaPlayer.getDuration() / 1000));
                }
                mHandler.removeCallbacksAndMessages(null);
            }
        });
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
                mediaPlayer.reset();
                mHandler.removeCallbacksAndMessages(null);
                return false;
            }
        });
    }

    private void initTitle() {
        if (mNPAppletInfo != null && mHeaderHandler != null) {
            if (TextUtils.isEmpty(mApplet.getName())) {
                mHeaderHandler.setTitle("音乐共享");
            }
            mHeaderHandler.setHeaderVisible(true);
        } else {

            titleBar.setOnClickListener(new CommonTitleBar.OnClickListener() {
                @Override
                public void onClick(CommonTitleBar.ClickPosition position) {
                    if (position == CommonTitleBar.ClickPosition.LEFT) {
                        finish();
                    }
                }
            });
        }
    }

    private void initPushFragment() {
        pushProgressDialogFragment = new PushProgressDialogFragment().with((AppCompatActivity) MusicActivity.this);
        pushProgressDialogFragment.setListener(new PushProgressDialogFragment.PushProgressDialogFragmentListener() {
            @Override
            public void onDialogDismiss() {
                mHandler.removeCallbacksAndMessages(null);
            }
        });
    }

    private int selectedPosition = -1;

    @Override
    public void onMusicItemClick(int position, AudioData audioData) {
        if (mediaPlayer != null) {
            mHandler.removeCallbacksAndMessages(null);
            if (selectedPosition == position) {
                if (!mediaPlayer.isPlaying()) {
                    // 点击同一个item，如果当前没有在播放，接着播放
                    mediaPlayer.start();
                    mHandler.post(runnable);
                } else {
                    //点击同一个item，当前正在播放，做暂停动作
                    mediaPlayer.pause();
                }
            } else {
                //第一次进来或切换歌曲
                selectedPosition = position;
                initMediaplayer(audioData);
                mediaPlayer.start();
                mHandler.post(runnable);
            }
        }
        submitLocalClickUMData(position, audioData);
    }

    @Override
    public void onMusicPush(AudioData audioData) {
        int connectState = SSConnectManager.getInstance().getConnectState();
        final ISmartDeviceInfo deviceInfo = SmartApi.getConnectDeviceInfo();
        Log.d(TAG, "pushToTv: connectState" + connectState);
        Log.d(TAG, "pushToTv: deviceInfo" + deviceInfo);

        //未连接
        if(connectState == CONNECT_NOTHING || deviceInfo == null){
            ConnectDialogActivity.start(mContext);
            return;
        }

        //本地连接不通
        if(!(connectState == CONNECT_LOCAL || connectState == CONNECT_BOTH)){
            WifiConnectActivity.start(mContext);
            return;
        }

        if (!pushProgressDialogFragment.isAdded()) {
            if (!pushProgressDialogFragment.isAdded()) {
                pushProgressDialogFragment.showPushing();
                mHandler.postDelayed(runnablePushError, 10000);
                mHandler.postDelayed(runnableDismiss, 11000);
            }
        }

        SSConnectManager.getInstance().sendAudioMessage(audioData.tittle, new File(audioData.url), TARGET_CLIENT_APP_STORE, new IMMessageCallback() {
            @Override
            public void onStart(IMMessage message) {
//                ToastUtils.getInstance().showGlobalShort(R.string.push_screen_success_tips);
            }

            @Override
            public void onProgress(IMMessage message, int progress) {

            }

            @Override
            public void onEnd(IMMessage message, int code, String info) {
                Log.d(TAG, "onEnd: code=" + code + "\n info:" + info);
            }
        });
        submitLocalPushUMData(audioData);
    }

    /**
     * 初始化播放器
     */
    private void initMediaplayer(AudioData audioData) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(audioData.url);
            mediaPlayer.prepare();
            Log.d(TAG, "initMediaplayer: setdata source: " + audioData.tittle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer.getCurrentPosition() > mediaPlayer.getDuration()) {
                return;
            }
            mHandler.obtainMessage(0);
            mHandler.postDelayed(this, 1000);
            int currentTime = Math.round(mediaPlayer.getCurrentPosition() / 1000);
            String currentStr = TimeStringUtils.secToTime(currentTime);
            mAdapter.refreshPlayTime(currentStr);
//            Log.d(TAG, "run: currentStr: " + currentStr);
        }
    };

    private void submitLocalPushUMData(AudioData audioData) {
        DecimalFormat df = new DecimalFormat("#0.0");
        String size = String.valueOf(df.format(Double.valueOf(audioData.size) / 1024 / 1024));
        LogParams params = LogParams.newParams().append("applet_id", mApplet == null ? "" :mApplet.getId())
                .append("applet_name", mApplet == null ? "" :mApplet.getName())
                .append("file_size", size)
                .append("file_format", audioData.url.substring(audioData.url.lastIndexOf('.') + 1));
        LogSubmit.event("file_cast_btn_clicked", params.getParams());
    }

    private void submitLocalClickUMData(int pos, AudioData audioData) {
        LogParams params = LogParams.newParams().append("applet_id", mApplet == null ? "" :mApplet.getId())
                .append("applet_name", mApplet == null ? "" :mApplet.getName())
                .append("file_size", String.valueOf(audioData.size))
                .append("file_format", audioData.url.substring(audioData.url.lastIndexOf('.') + 1))
                .append("pos_id", String.valueOf(pos + 1));
        LogSubmit.event("local_file_clicked", params.getParams());
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
