package com.coocaa.tvpi.module.local;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.coocaa.publib.base.BaseActionBarActivity;
import com.coocaa.publib.data.local.VideoData;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.tvpi.module.connection.ConnectDialogActivity;
import com.coocaa.tvpi.module.local.adapter.VideoPageAdapter;
import com.coocaa.tvpi.module.local.album.AlbumViewPager;
import com.coocaa.tvpi.module.local.album.OnPullProgressListener;
import com.coocaa.tvpilib.R;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.ss.channel.im.IMMessageCallback;

import static com.coocaa.smartscreen.connect.SSConnectManager.TARGET_CLIENT_APP_STORE;
import static com.coocaa.tvpi.common.UMengEventId.CAST_LOCAL_RESOURCE;

/**
 * @ClassName LocalVideoPlayerActivity
 * @Description 点击视频块放大全屏预览的页面
 * @User WHY
 * @Date 2018/7/27
 */
public class LocalVideoPlayerActivity extends BaseActionBarActivity implements
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnVideoSizeChangedListener {

    public String TAG = "LocalVideoPlayerActivity";

    public static String KEY_VIDEO_DATAS = "KEY_VIDEO_DATAS";

    private Context mContext;
    private RelativeLayout mainLayout;
    private AlbumViewPager mViewPager;
    private VideoPageAdapter mPagerAdapter;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private MediaPlayer mMediaPlayer;
    private LinearLayout mTopLayout;
    private ImageView mBackImg;
    private RelativeLayout mPushLayout;
    private ImageView mPushIcon;
    private TextView mPushText;
    private ImageView mPushIconOk;

    private List<VideoData> mVideoDatas = null;
    private int mPosition;
    private Handler uiHandler;
    private boolean isStartClick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
//        setShowTvToolBar(false);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.local_video_player_activity);
        mainLayout = findViewById(R.id.local_video_player_rl);
        mainLayout.setBackgroundColor(0xFF000000);

        Intent intent = getIntent();
        if (intent != null) {
            mVideoDatas = (List<VideoData>) intent.getSerializableExtra(KEY_VIDEO_DATAS);
            mPosition = intent.getIntExtra("POSITION", 0);
            if (mVideoDatas != null) {
                mPosition = (mPosition >= 0 && mPosition < mVideoDatas.size()) ? mPosition : 0;
            }
        }

        isStartClick = false;
        uiHandler = new Handler();
        initViewPager();
        initOtherView();
        initSurfaceView();
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
        if (uiHandler != null) {
            uiHandler.removeCallbacksAndMessages(null);
            uiHandler = null;
        }
        super.onDestroy();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public void initViewPager() {
        mViewPager = findViewById(R.id.local_video_player_view_page);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.addOnPageChangeListener(onPageChangeListener);
        mViewPager.setOnPullProgressListener(mOnPullProgressListener);
        mViewPager.setOnClickListener(mOnClickListener);
        mPagerAdapter = new VideoPageAdapter(mContext, mVideoDatas);
        mViewPager.setAdapter(mPagerAdapter);

        if (mPosition < mVideoDatas.size()) {
            mViewPager.setCurrentItem(mPosition, false);
        }
    }

    private void initOtherView() {
        mTopLayout = findViewById(R.id.local_video_player_top_ll);
        mBackImg = findViewById(R.id.local_video_player_back);
        mPushLayout = findViewById(R.id.local_video_player_push_rl);
        mPushIcon = findViewById(R.id.local_video_player_push_icon);
        mPushText = findViewById(R.id.local_video_player_push_tv);
        mPushIconOk = findViewById(R.id.local_video_player_push_icon_ok);

        mBackImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mPushLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPushIcon.getVisibility() == View.VISIBLE) {
                    pushVideo(mVideoDatas.get(mPosition));
                    showOkIcon();
                }
            }
        });
    }

    private void initSurfaceView() {
        mSurfaceView = findViewById(R.id.local_video_player_surface_view);
        mSurfaceView.setZOrderOnTop(false);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.addCallback(surfaceHolderCallbak);
        mSurfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: pauseLocalVideo...  mSurfaceView INVISIBLE");
                pauseLocalVideo();
            }
        });
        Log.d(TAG, "initSurfaceView....mSurfaceView INVISIBLE");
        mSurfaceView.setVisibility(View.INVISIBLE);
    }

    AlbumViewPager.OnPageChangeListener onPageChangeListener = new AlbumViewPager
            .OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            mPosition = position;
            Log.d(TAG, "onPageSelected: position, title: " + mPosition + mVideoDatas.get
                    (mPosition).tittle);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    OnPullProgressListener mOnPullProgressListener = new OnPullProgressListener() {
        @Override
        public void startPull() {
            //后续添加隐藏动画
            mTopLayout.setVisibility(View.GONE);
            mPushLayout.setVisibility(View.GONE);
        }

        @Override
        public void onProgress(float progress) {
            mainLayout.setBackgroundColor(0xFF000000);
            mainLayout.getBackground().setAlpha((int) (progress * 255));
        }

        @Override
        public void stopPull(boolean isFinish) {
            if (isFinish) {
                finish();
            } else {
                mainLayout.setBackgroundColor(0xFF000000);
                mainLayout.getBackground().setAlpha(255);
                mTopLayout.setVisibility(View.VISIBLE);
                mPushLayout.setVisibility(View.VISIBLE);
            }
        }
    };

    public void changCoverUI(boolean showCover) {
        if (showCover) {
            mTopLayout.setVisibility(View.VISIBLE);
            mPushLayout.setVisibility(View.VISIBLE);
            mViewPager.setVisibility(View.VISIBLE);
            mSurfaceView.setVisibility(View.INVISIBLE);
        } else {
            mTopLayout.setVisibility(View.GONE);
            mPushLayout.setVisibility(View.GONE);
            mViewPager.setVisibility(View.GONE);
        }
    }

    private void pushVideo(VideoData videoData) {
        if (!SSConnectManager.getInstance().isConnected()) {
            ConnectDialogActivity.start(mContext);
            return;
        }
        ToastUtils.getInstance().showGlobalLong(getResources().getString(R.string.push_screen_success_tips));
        SSConnectManager.getInstance().sendVideoMessage(videoData.tittle, new File(videoData.url), TARGET_CLIENT_APP_STORE, new IMMessageCallback() {
            @Override
            public void onStart(IMMessage message) {

            }

            @Override
            public void onProgress(IMMessage message, int progress) {

            }

            @Override
            public void onEnd(IMMessage message, int code, String info) {

            }
        });
        submitLocalPushUMData();
    }

    private void submitLocalPushUMData() {
        Map<String, String> map = new HashMap<>();
        map.put("type", "video");
        MobclickAgent.onEvent(mContext, CAST_LOCAL_RESOURCE, map);
    }

    private void showOkIcon() {
        mPushIcon.setVisibility(View.GONE);
        mPushText.setVisibility(View.GONE);
        mPushIconOk.setVisibility(View.VISIBLE);

        Animation showOkAnim = AnimationUtils.loadAnimation(mContext, R.anim.icon_show);
        showOkAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                uiHandler.postDelayed(runnable, 1000);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mPushIconOk.startAnimation(showOkAnim);
    }

    private void hideOkIcon() {
        mPushIconOk.setVisibility(View.GONE);
        mPushIcon.setVisibility(View.VISIBLE);
        mPushText.setVisibility(View.VISIBLE);

        Animation showOkAnim = AnimationUtils.loadAnimation(mContext, R.anim.icon_hide);
        mPushIconOk.startAnimation(showOkAnim);
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            hideOkIcon();
        }
    };

    //开始按钮按下
    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(TAG, "onClick: start btn click....mSurfaceView VISIBLE");
            isStartClick = true;
            mSurfaceView.setVisibility(View.VISIBLE);
        }
    };

    SurfaceHolder.Callback surfaceHolderCallbak = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            Log.d(TAG, "surfaceCreated: .....");
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDisplay(surfaceHolder);
            try {
                mMediaPlayer.setDataSource(mVideoDatas.get(mPosition).url);
                mMediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mMediaPlayer.setOnCompletionListener(LocalVideoPlayerActivity.this);
            mMediaPlayer.setOnErrorListener(LocalVideoPlayerActivity.this);
            mMediaPlayer.setOnVideoSizeChangedListener(LocalVideoPlayerActivity.this);
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    Log.d(TAG, "MediaPlayer onPrepared: ");
                    if(isStartClick) {
                        isStartClick = false;
                        mediaPlayer.start();
                        changCoverUI(false);
                    }
                }
            });
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
        }
    };

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        changCoverUI(true);
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        changeVideoSize();
    }

    public void changeVideoSize() {
        int videoWidth = mMediaPlayer.getVideoWidth();
        int videoHeight = mMediaPlayer.getVideoHeight();

        int surfaceWidth = DimensUtils.getDeviceWidth(mContext);
        int surfaceHeight = DimensUtils.getDeviceHeight(mContext);

        //根据视频尺寸去计算->视频可以在sufaceView中放大的最大倍数。
        float max = 1;
        if (getResources().getConfiguration().orientation == ActivityInfo
                .SCREEN_ORIENTATION_PORTRAIT) {
            //竖屏模式下按视频宽度计算放大倍数值
            max = Math.max((float) videoWidth / (float) surfaceWidth, (float) videoHeight /
                    (float) surfaceHeight);
        }

        //视频宽高分别/最大倍数值 计算出放大后的视频尺寸
        videoWidth = (int) Math.ceil((float) videoWidth / max);
        videoHeight = (int) Math.ceil((float) videoHeight / max);

        //无法直接设置视频尺寸，将计算出的视频尺寸设置到surfaceView 让视频自动填充。
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(videoWidth,
                videoHeight);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mSurfaceView.setLayoutParams(params);
        mainLayout.setBackgroundColor(Color.BLACK);
    }

    private void pauseLocalVideo() {
        if (mMediaPlayer == null)
            return;
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            changCoverUI(true);
        }
    }
}
