package com.coocaa.tvpi.module.local;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.publib.base.BaseAppletActivity;
import com.coocaa.publib.data.local.VideoData;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.data.channel.events.ProgressEvent;
import com.coocaa.smartsdk.SmartApi;
import com.coocaa.smartsdk.object.ISmartDeviceInfo;
import com.coocaa.tvpi.module.connection.ConnectDialogActivity;
import com.coocaa.tvpi.module.connection.WifiConnectActivity;
import com.coocaa.tvpi.module.io.HomeIOThread;
import com.coocaa.tvpi.module.local.adapter.VideoPreviewListAdapter;
import com.coocaa.tvpi.module.local.album.AlbumFragment;
import com.coocaa.tvpi.module.local.album.AlbumPreviewLayoutManager;
import com.coocaa.tvpi.module.local.utils.LocalMediaHelp;
import com.coocaa.tvpi.module.local.utils.VideoBrowseAsyncTask;
import com.coocaa.tvpi.module.log.LogParams;
import com.coocaa.tvpi.module.log.LogSubmit;
import com.coocaa.tvpi.util.FileCalculatorUtil;
import com.coocaa.tvpi.view.CommonTitleBar;
import com.coocaa.tvpi.view.PushProgressDialogFragment;
import com.coocaa.tvpilib.R;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.ss.channel.im.IMMessageCallback;

import static android.widget.AbsListView.OnScrollListener.SCROLL_STATE_IDLE;
import static com.coocaa.smartscreen.connect.SSConnectManager.PUSH_INVALID_NOT_PING;
import static com.coocaa.smartscreen.connect.SSConnectManager.TARGET_CLIENT_APP_STORE;

/**
 * 新视频预览
 */
public class VideoPreviewActivity2 extends BaseAppletActivity {
    public static final String TAG = VideoPreviewActivity2.class.getSimpleName();
    private PushProgressDialogFragment pushProgressDialogFragment;
    private Context mContext;
    private VideoFragment videoFragment;
    private LinearLayout rlPush;
    private TextView mPushToTv;
    //    private ImageView mImgPushIcon;
    private ImageView mImgPushOk;
    private CommonTitleBar mCommonTitleBar;
    private RecyclerView recyclerView;

    private int mPosition;
    private List<VideoData> videoDataList;
    private Handler uiHandler;
    private VideoPreviewListAdapter albumPreviewListAdapter;
    private AlbumPreviewLayoutManager albumPreviewLayoutManager;
    private long startTime;
    private long endTime;

    private VideoBrowseAsyncTask mVideoBrowseAsyncTask;

    public static void start(Context context,int position) {
        Intent starter = new Intent(context, VideoPreviewActivity2.class);
        starter.putExtra("POSITION",position);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        EventBus.getDefault().register(this);
        if (null != getIntent()) {
            Bundle bundle = getIntent().getExtras();
            mPosition = bundle.getInt("POSITION");
        }
        setContentView(R.layout.local_activity_video_preview3);
        setupView();
        initData();
        initPushFragment();
        if (mHeaderHandler != null) {
            mHeaderHandler.setTitle("预览");
            mHeaderHandler.setBackgroundColor(Color.parseColor("#ffffffff"));
        }
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
        EventBus.getDefault().unregister(this);
        if (uiHandler != null) {
            uiHandler.removeCallbacksAndMessages(null);
            uiHandler = null;
        }
    }

    @Override
    public void finish() {
        super.finish();

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ProgressEvent progressEvent) {
        Log.d(TAG, "onEvent: " + progressEvent.getInfo());

        if (progressEvent.getProgress() == 0) {
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
            submitPushDuration();
        }
    }

    private void initData() {
//        Log.d(TAG, "initData: start");
//        mVideoBrowseAsyncTask = new VideoBrowseAsyncTask(this, new VideoBrowseAsyncTask.VideoBrowseCallback() {
//            @Override
//            public void onResult(List<VideoData> result) {
//                if(result != null){
//                    videoDataList = result;
//                    albumPreviewListAdapter.setVideoDataList(videoDataList);
//                    albumPreviewLayoutManager.scrollToPositionWithOffset(mPosition,DimensUtils.getDeviceWidth(VideoPreviewActivity2.this)/2-DimensUtils.dp2Px(VideoPreviewActivity2.this,20+38));
//                }
//            }
//        });
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//            mVideoBrowseAsyncTask.executeOnExecutor(Executors.newCachedThreadPool());
//        } else {
//            mVideoBrowseAsyncTask.execute();
//        }
//        Log.d(TAG, "initData: end");

        boolean useShare = false;
        if (null != getIntent()) {
            Bundle bundle = getIntent().getExtras();
            mPosition = bundle.getInt("POSITION");
            VideoData videoData = (VideoData) bundle.getParcelable("VIDEODATA");
            boolean fromShare = bundle.getBoolean("fromShare", false);
            Log.d(TAG, "fromShare=" + fromShare + ", pos=" + mPosition + ", videoData=" + videoData);
            if (fromShare && videoData != null) {
                useShare = true;
                videoDataList = new ArrayList<>(1);
                videoDataList.add(videoData);
                videoFragment.setShareVideoList(videoDataList);
            }
        }
        if(!useShare) {
            videoDataList = LocalMediaHelp.getVideoList();
        }
        if(videoDataList != null){
            List<VideoData> vList = LocalMediaHelp.getVideoList();
            Log.d(TAG, "local vList : " + vList);
            for(VideoData v : vList) {
                Log.d(TAG, "local video : " + v);
            }

            albumPreviewListAdapter.setVideoDataList(videoDataList);
            albumPreviewLayoutManager.scrollToPositionWithOffset(mPosition, DimensUtils.getDeviceWidth(VideoPreviewActivity2.this)/2- DimensUtils.dp2Px(VideoPreviewActivity2.this,20+38));
        }
    }

    private void setupView() {
        uiHandler = new Handler();

        mCommonTitleBar = findViewById(R.id.album_preview_common_title_bar);
        mCommonTitleBar.setOnClickListener(new CommonTitleBar.OnClickListener() {
            @Override
            public void onClick(CommonTitleBar.ClickPosition position) {
                if (position == CommonTitleBar.ClickPosition.LEFT) {
                    finish();
                }
            }
        });
        if (mNPAppletInfo != null) {
            mCommonTitleBar.setVisibility(View.GONE);
        }

        if (videoFragment == null) {
            videoFragment = VideoFragment.newInstance(mPosition, mContext);
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.add(R.id.album_preview_content, videoFragment, AlbumFragment.class.getName());
            transaction.commit();
        }

        videoFragment.setOnAlbumEventListener(new VideoFragment.OnAlbumEventListener() {
            @Override
            public void onClick() {

            }

            @Override
            public void onPageChanged(int page) {
                mPosition = page;
                if (videoDataList != null) {
                    //recyclerView.smoothScrollToPosition(page);
                    albumPreviewLayoutManager.smoothScrollToPosition(recyclerView, new RecyclerView.State(), mPosition);
                }
                if (recyclerView.getLayoutManager().findViewByPosition(page) != null) {
                    recyclerView.getLayoutManager().findViewByPosition(page).requestFocus();
                }
                uiHandler.removeCallbacksAndMessages(null);
                mImgPushOk.setVisibility(View.GONE);
//                mImgPushIcon.setVisibility(View.VISIBLE);
                mPushToTv.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStartPull() {
                mCommonTitleBar.setVisibility(View.GONE);
                rlPush.setVisibility(View.GONE);
            }

            @Override
            public void onPullProgress(float progress) {

            }

            @Override
            public void stopPull(boolean isFinish) {
                if (isFinish) {
                    finish();
                } else {
                    if (mNPAppletInfo == null) {
                        mCommonTitleBar.setVisibility(View.VISIBLE);
                    }
                    rlPush.setVisibility(View.VISIBLE);
                }
            }
        });

        rlPush = findViewById(R.id.rl_push_to_tv);
        mPushToTv = findViewById(R.id.album_preview_pushtotv);
        mImgPushOk = findViewById(R.id.album_preview_pushtotv_ok);
//        mImgPushIcon = findViewById(R.id.album_preview_push_icon);

        rlPush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPushToTv.getVisibility() == View.VISIBLE) {
                    pushToTv();
//                    showOkIcon();
                }
            }
        });

        albumPreviewListAdapter = new VideoPreviewListAdapter(VideoPreviewActivity2.this);
        recyclerView = findViewById(R.id.rv_album);
        albumPreviewLayoutManager = new AlbumPreviewLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(albumPreviewLayoutManager);
        recyclerView.setAdapter(albumPreviewListAdapter);
        albumPreviewListAdapter.setOnPictureItemClickListener(new VideoPreviewListAdapter.OnPictureItemClickListener() {
            @Override
            public void onPictureItemClick(int position, VideoData videoData) {
                Log.d(TAG, "onPictureItemClick: ");
                mPosition = position;
                if (videoFragment.isAdded()) {
                    videoFragment.showSelectPicture(position);
                }
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                Log.d(TAG, "onScrollStateChanged: " + newState);
                if (recyclerView.getLayoutManager().findViewByPosition(mPosition) != null && newState == SCROLL_STATE_IDLE) {
                    recyclerView.getLayoutManager().findViewByPosition(mPosition).requestFocus();
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Log.d(TAG, "onScrolled: " + dx);
            }
        });

        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.d(TAG, "onGlobalLayout: ");
                //albumPreviewLayoutManager.scrollToPositionWithOffset(mPosition, DimensUtils.getDeviceWidth(VideoPreviewActivity2.this) / 2 - DimensUtils.dp2Px(VideoPreviewActivity2.this, 20 + 38));
                //recyclerView.scrollToPosition(mPosition);
                if (recyclerView.getLayoutManager().findViewByPosition(mPosition) != null) {
                    recyclerView.getLayoutManager().findViewByPosition(mPosition).requestFocus();
                }
            }
        });
        //albumPreviewLayoutManager.scrollToPositionWithOffset(mPosition, DimensUtils.getDeviceWidth(this) / 2 - DimensUtils.dp2Px(this, 20 + 38));
        albumPreviewLayoutManager.scrollToPositionWithOffset(mPosition,0);
    }

    private void initPushFragment() {
        pushProgressDialogFragment = new PushProgressDialogFragment().with((AppCompatActivity) VideoPreviewActivity2.this);
        pushProgressDialogFragment.setListener(new PushProgressDialogFragment.PushProgressDialogFragmentListener() {
            @Override
            public void onDialogDismiss() {
                uiHandler.removeCallbacksAndMessages(null);
            }
        });
    }

    private void pushToTv() {
        final int available = SSConnectManager.getInstance().checkPushValid(getNetworkForceKey());
        final ISmartDeviceInfo deviceInfo = SmartApi.getConnectDeviceInfo();
        if (available == SSConnectManager.PUSH_INVALID_NOT_CONNECT || deviceInfo == null) {
            ConnectDialogActivity.start(VideoPreviewActivity2.this);
            return;
        }
        if (available == SSConnectManager.PUSH_INVALID_NOT_SAME_WIFI || available == PUSH_INVALID_NOT_PING) {
            WifiConnectActivity.start(VideoPreviewActivity2.this);
            return ;
        }

        HomeIOThread.execute(new Runnable() {
            @Override
            public void run() {
                startTime = System.currentTimeMillis();
                if (!pushProgressDialogFragment.isAdded()) {
                    pushProgressDialogFragment.showPushing();
                    uiHandler.postDelayed(runnablePushError, 10000);
                    uiHandler.postDelayed(runnableDismiss, 11000);
                }

                if(videoDataList != null && videoDataList.get(mPosition) != null) {
                    VideoData videoData = videoDataList.get(mPosition);

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
                }

            }
        });
        //ToastUtils.getInstance().showGlobalShort(getResources().getString(R.string.push_screen_success_tips));
        submitLocalPushUMData();
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

    @Override
    protected boolean isFloatHeader() {
        return true;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
//        super.onSaveInstanceState(outState);
    }

    private void submitLocalPushUMData() {
        if(videoDataList != null && videoDataList.get(mPosition) != null) {
            VideoData videoData = videoDataList.get(mPosition);
            DecimalFormat df = new DecimalFormat("#0.0");
            String size = String.valueOf(df.format(Double.valueOf(videoData.size) / 1024 / 1024));
            LogParams params = LogParams.newParams().append("applet_id", mApplet == null ? "" : mApplet.getId())
                    .append("applet_name", mApplet == null ? "" : mApplet.getName())
                    .append("file_size", size)
                    .append("file_format", videoData.url.substring(videoData.url.lastIndexOf('.') + 1));
            LogSubmit.event("file_cast_btn_clicked", params.getParams());
        }
    }

    private void submitPushDuration() {
        if(videoDataList != null && videoDataList.get(mPosition) != null) {
            VideoData videoData = videoDataList.get(mPosition);
            endTime = System.currentTimeMillis();
            String duration = "0";
            if(startTime < endTime){
                duration = String.valueOf(endTime - startTime);
            }
            LogParams params = LogParams.newParams().append("applet_id", mApplet == null ? "" : mApplet.getId())
                    .append("applet_name", mApplet == null ? "" : mApplet.getName())
                    .append("duration", duration)
                    .append("file_size", FileCalculatorUtil.getFileSize(videoData.size))
                    .append("file_type", videoData.url.substring(videoData.url.lastIndexOf('.') + 1));
            LogSubmit.event("cast_load_duration", params.getParams());
        }
    }
}
