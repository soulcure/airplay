package com.coocaa.tvpi.module.local.album2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.coocaa.publib.base.BaseAppletActivity;
import com.coocaa.publib.data.local.ImageData;
import com.coocaa.publib.data.local.MediaData;
import com.coocaa.publib.data.local.VideoData;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.data.channel.events.ProgressEvent;
import com.coocaa.smartsdk.SmartApi;
import com.coocaa.smartsdk.object.ISmartDeviceInfo;
import com.coocaa.swaiotos.virtualinput.VirtualInputStarter;
import com.coocaa.tvpi.event.StartPushEvent;
import com.coocaa.tvpi.module.connection.ConnectDialogActivity;
import com.coocaa.tvpi.module.connection.WifiConnectActivity;
import com.coocaa.tvpi.module.io.HomeIOThread;
import com.coocaa.tvpi.module.local.adapter.PreviewListAdapter;
import com.coocaa.tvpi.module.local.album.AlbumFragment;
import com.coocaa.tvpi.module.local.album.AlbumPreviewActivity2;
import com.coocaa.tvpi.module.local.album.AlbumPreviewLayoutManager;
import com.coocaa.tvpi.module.local.utils.LocalMediaHelper;
import com.coocaa.tvpi.module.log.LogParams;
import com.coocaa.tvpi.module.log.LogSubmit;
import com.coocaa.tvpi.util.FileCalculatorUtil;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpi.view.CommonTitleBar;
import com.coocaa.tvpi.view.PushProgressDialogFragment;
import com.coocaa.tvpilib.R;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.text.DecimalFormat;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import swaiotos.channel.iot.ss.analysis.data.SSConnect;
import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.ss.channel.im.IMMessageCallback;

import static android.widget.AbsListView.OnScrollListener.SCROLL_STATE_IDLE;
import static com.coocaa.smartscreen.connect.SSConnectManager.CONNECT_BOTH;
import static com.coocaa.smartscreen.connect.SSConnectManager.CONNECT_LOCAL;
import static com.coocaa.smartscreen.connect.SSConnectManager.CONNECT_NOTHING;
import static com.coocaa.smartscreen.connect.SSConnectManager.CONNECT_SSE;
import static com.coocaa.smartscreen.connect.SSConnectManager.PUSH_INVALID_NOT_PING;
import static com.coocaa.smartscreen.connect.SSConnectManager.TARGET_CLIENT_APP_STORE;

/**
 * 相册预览
 * @author chenaojun
 */
public class PreviewActivity extends BaseAppletActivity {
    public static final String TAG = PreviewActivity.class.getSimpleName();
    public static final String KEY_SHOW_TYPE = "KEY_SHOW_TYPE";
    public static final int SHOW_ALL = 0;
    public static final int SHOW_COLLECT = 1;

    private static String KEY_ALBUM_NAME = "ALBUMNAME";
    private static String KEY_POSITION = "POSITION";

    private Context mContext;
    private PreviewFragment mAlbumView;
    private LinearLayout rlPush;
    private CommonTitleBar mCommonTitleBar;
    private RecyclerView recyclerView;

    private int mPosition;
    private String mAlbumName;
    private MediaData mMediaData;
    private Handler uiHandler;
    private PreviewListAdapter mPreviewListAdapter;
    private AlbumPreviewLayoutManager albumPreviewLayoutManager;
    private long startTime;
    private int showType;
    private boolean isHided = false;

    public static void start(Context context,
                             String albumName,
                             int position,
                             int showType) {
        Intent starter = new Intent(context, PreviewActivity.class);
        starter.putExtra(KEY_ALBUM_NAME, albumName);
        starter.putExtra(KEY_POSITION, position);
        starter.putExtra(KEY_SHOW_TYPE, showType);
//        starter.putParcelable("MEDIADATA", mediaDataList.get(pos));
        if(!(context instanceof Activity)) {
            starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(starter);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.local_activity_album_preview3);
        mContext = this;
        EventBus.getDefault().register(this);
        parseIntent();
        setupView();
        initData();
        if (mHeaderHandler != null) {
            mHeaderHandler.setTitle("预览");
            mHeaderHandler.setBackgroundColor(Color.parseColor("#ffffffff"));
        }
        StatusBarHelper.translucent(this);
        StatusBarHelper.setStatusBarLightMode(this);
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

        if (progressEvent.getType() == IMMessage.TYPE.PROGRESS) {
            uiHandler.removeCallbacksAndMessages(null);
            return;
        }

        if (progressEvent.getType() != IMMessage.TYPE.RESULT) {
            submitPushDuration();
            submitCastResult(progressEvent.result, progressEvent.info);
        }
    }

    private void parseIntent() {
        if (null != getIntent()) {
            Intent intent = getIntent();
            mPosition = intent.getIntExtra(KEY_POSITION, 0);
            mAlbumName = intent.getStringExtra(KEY_ALBUM_NAME);
//            mMediaData = bundle.getParcelable("MEDIADATA");
            showType = intent.getIntExtra(KEY_SHOW_TYPE, SHOW_ALL);
        }
    }

    private void initData() {
        switch (showType) {
            case SHOW_ALL:
                mPreviewListAdapter.setMediaDataList(LocalMediaHelper.getInstance().getAllMediaDataMap().get(mAlbumName));
                break;
            case SHOW_COLLECT:
                mPreviewListAdapter.setMediaDataList(LocalMediaHelper.getInstance().getCollectedMediaData(this));
                break;
            default:
                break;
        }

    }

    private void setupView() {
        uiHandler = new Handler(Looper.myLooper());

        mCommonTitleBar = findViewById(R.id.album_preview_common_title_bar);
        mCommonTitleBar.setOnClickListener((CommonTitleBar.OnClickListener) position -> {
            if (position == CommonTitleBar.ClickPosition.LEFT) {
                finish();
            }
        });

        if (mNPAppletInfo != null) {
            mCommonTitleBar.setVisibility(View.GONE);
        }

        if (mAlbumView == null) {
            mAlbumView = PreviewFragment.newInstance(mPosition, mContext, showType, mAlbumName);
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.add(R.id.album_preview_content, mAlbumView, AlbumFragment.class.getName());
            transaction.commit();
        }

        mAlbumView.setOnAlbumEventListener(new PreviewFragment.OnAlbumEventListener() {
            @Override
            public void onClick() {
                //隐藏View
                if(isHided) {
                    showOutView();
                    isHided = false;
                } else {
                    hideOutView();
                    isHided = true;
                }
            }

            @Override
            public void onPageChanged(int page) {
                mPosition = page;
                if (mPreviewListAdapter.getMediaDataList() != null && mPreviewListAdapter.getMediaDataList().size() != 0) {
                    mMediaData = mPreviewListAdapter.getMediaDataList().get(page);
                    albumPreviewLayoutManager.smoothScrollToPosition(recyclerView, new RecyclerView.State(), mPosition);
                }
                if (recyclerView.getLayoutManager()!=null && recyclerView.getLayoutManager().findViewByPosition(page) != null) {
                    recyclerView.getLayoutManager().findViewByPosition(page).requestFocus();
                }
                uiHandler.removeCallbacksAndMessages(null);
                rlPush.setVisibility(View.VISIBLE);
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
                Log.d("chen", "stopPull: " + isFinish);
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
        rlPush.setOnClickListener(view -> {
            if (rlPush.getVisibility() == View.VISIBLE) {
                pushToTv();
            }
        });
        mPreviewListAdapter = new PreviewListAdapter(PreviewActivity.this);
        recyclerView = findViewById(R.id.rv_album);
        albumPreviewLayoutManager = new AlbumPreviewLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(albumPreviewLayoutManager);
        recyclerView.setAdapter(mPreviewListAdapter);
        mPreviewListAdapter.setOnPictureItemClickListener((position, mediaData) -> {
            Log.d(TAG, "onPictureItemClick: ");
            mPosition = position;
            mMediaData = mediaData;
            if (mAlbumView.isAdded()) {
                mAlbumView.showSelectPicture(position);
            }
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                Log.d(TAG, "onScrollStateChanged: " + newState);
                if (recyclerView.getLayoutManager()!=null && recyclerView.getLayoutManager().findViewByPosition(mPosition) != null && newState == SCROLL_STATE_IDLE) {
                    recyclerView.getLayoutManager().findViewByPosition(mPosition).requestFocus();
                }
            }
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Log.d(TAG, "onScrolled: " + dx);
            }
        });
        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Log.d(TAG, "onGlobalLayout: ");
            if (recyclerView.getLayoutManager()!=null && recyclerView.getLayoutManager().findViewByPosition(mPosition) != null) {
                recyclerView.getLayoutManager().findViewByPosition(mPosition).requestFocus();
            }
        });
        albumPreviewLayoutManager.scrollToPositionWithOffset(mPosition, DimensUtils.getDeviceWidth(this) / 2 - DimensUtils.dp2Px(this, 20 + 38));
    }

    private void showOutView() {
        mHeaderHandler.setHeaderVisible(true);
        rlPush.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void hideOutView() {
        mHeaderHandler.setHeaderVisible(false);
        rlPush.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);
    }

    private void pushToTv() {
        int connectState = SSConnectManager.getInstance().getConnectState();
        final ISmartDeviceInfo deviceInfo = SmartApi.getConnectDeviceInfo();
        Log.d(TAG, "pushToTv: connectState" + connectState);
        Log.d(TAG, "pushToTv: deviceInfo" + deviceInfo);

        //未连接
        if(connectState == CONNECT_NOTHING || deviceInfo == null){
            ConnectDialogActivity.start(PreviewActivity.this);
            return;
        }

        //本地连接不通
        if(!(connectState == CONNECT_LOCAL || connectState == CONNECT_BOTH)){
            WifiConnectActivity.start(PreviewActivity.this);
            return;
        }

        HomeIOThread.execute(() -> {
            startTime = System.currentTimeMillis();

            EventBus.getDefault().post(new StartPushEvent("album"));

            if (mMediaData.type == MediaData.TYPE.IMAGE) {
                pushImageData(deviceInfo);
            }

            if (mMediaData.type == MediaData.TYPE.VIDEO) {
                pushVideoData(deviceInfo);
            }
        });
        submitLocalPushUMData();
    }

    private void pushVideoData(ISmartDeviceInfo deviceInfo) {
        if (mMediaData == null || mMediaData.type != MediaData.TYPE.VIDEO) {
            return;
        }
        VideoData videoData = (VideoData) mMediaData;
        SSConnectManager.getInstance().sendVideoMessage(videoData.tittle, new File(videoData.url), TARGET_CLIENT_APP_STORE, new IMMessageCallback() {
            @Override
            public void onStart(IMMessage message) {
                Log.d(TAG, "onStart: " + message.toString());
                /*if (!deviceInfo.isTempDevice) {
                    ToastUtils.getInstance().showGlobalShort(R.string.push_screen_success_tips);
                }*/
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

    private void pushImageData(ISmartDeviceInfo deviceInfo) {
        if (mMediaData.type != MediaData.TYPE.IMAGE) {
            return;
        }
        ImageData imageData = (ImageData) mMediaData;
        SSConnectManager.getInstance().sendImageMessage(imageData.tittle, new File(imageData.url), TARGET_CLIENT_APP_STORE, new IMMessageCallback() {
            @Override
            public void onStart(IMMessage message) {
                Log.d(TAG, "onStart: ");
                /*if (!deviceInfo.isTempDevice) {
                    ToastUtils.getInstance().showGlobalShort(R.string.push_screen_success_tips);
                    VirtualInputStarter.show(PreviewActivity.this, false);
                }*/
            }

            @Override
            public void onProgress(IMMessage message, int progress) {

            }


            @Override
            public void onEnd(IMMessage message, int code, String info) {
                Log.d(TAG, "onEnd: code=" + code + "\n info:" + info);
            }
        });
    }

    private void submitLocalPushUMData() {
        if (mMediaData == null) {
            return;
        }
        if (mMediaData.type == MediaData.TYPE.IMAGE) {
            ImageData imageData = (ImageData) mMediaData;
            DecimalFormat df = new DecimalFormat("#0.0");
            String size = df.format((double) imageData.size / 1024 / 1024);
            LogParams params = LogParams.newParams().append("applet_id", mApplet == null ? "" : mApplet.getId())
                    .append("applet_name", mApplet == null ? "" : mApplet.getName())
                    .append("file_size", size)
                    .append("file_format", imageData.url.substring(imageData.url.lastIndexOf('.') + 1));
            LogSubmit.event("file_cast_btn_clicked", params.getParams());
        }
        if (mMediaData.type == MediaData.TYPE.VIDEO) {
            VideoData videoData = (VideoData) mMediaData;
            DecimalFormat df = new DecimalFormat("#0.0");
            String size = df.format((double) videoData.size / 1024 / 1024);
            LogParams params = LogParams.newParams().append("applet_id", mApplet == null ? "" : mApplet.getId())
                    .append("applet_name", mApplet == null ? "" : mApplet.getName())
                    .append("file_size", size)
                    .append("file_format", videoData.url.substring(videoData.url.lastIndexOf('.') + 1));
            LogSubmit.event("file_cast_btn_clicked", params.getParams());
        }
    }

    private void submitPushDuration() {
        String duration = "0";
        long endTime = System.currentTimeMillis();
        if (startTime < endTime) {
            duration = String.valueOf(endTime - startTime);
        }
        if (mMediaData == null) {
            return;
        }

        if (mMediaData.type == MediaData.TYPE.IMAGE) {
            ImageData imageData = (ImageData) mMediaData;
            LogParams params = LogParams.newParams().append("applet_id", mApplet == null ? "" : mApplet.getId())
                    .append("applet_name", mApplet == null ? "" : mApplet.getName())
                    .append("duration", duration)
                    .append("file_size", FileCalculatorUtil.getFileSize(imageData.size))
                    .append("file_type", imageData.url.substring(imageData.url.lastIndexOf('.') + 1));
            LogSubmit.event("cast_load_duration", params.getParams());
        }

        if (mMediaData.type == MediaData.TYPE.VIDEO) {
            VideoData videoData = (VideoData) mMediaData;
            LogParams params = LogParams.newParams().append("applet_id", mApplet == null ? "" : mApplet.getId())
                    .append("applet_name", mApplet == null ? "" : mApplet.getName())
                    .append("duration", duration)
                    .append("file_size", FileCalculatorUtil.getFileSize(videoData.size))
                    .append("file_type", videoData.url.substring(videoData.url.lastIndexOf('.') + 1));
            LogSubmit.event("cast_load_duration", params.getParams());
        }

    }

    private void submitCastResult(boolean result, String info) {
        LogParams params = LogParams.newParams().append("applet_id", mApplet == null ? "" : mApplet.getId())
                .append("applet_id", mApplet == null ? "" : mApplet.getId())
                .append("applet_name", mApplet == null ? "" : mApplet.getName())
                .append("cast_result", result ? "success" : "fail")
                .append("info", info);
        LogSubmit.event("cast_result", params.getParams());
    }

    @Override
    protected boolean isFloatHeader() {
        return true;
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
//        super.onSaveInstanceState(outState);
    }

}
