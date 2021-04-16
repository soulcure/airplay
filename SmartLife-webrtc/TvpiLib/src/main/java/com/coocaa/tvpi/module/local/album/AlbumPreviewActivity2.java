package com.coocaa.tvpi.module.local.album;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import com.coocaa.publib.data.local.ImageData;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.data.channel.events.ProgressEvent;
import com.coocaa.smartsdk.SmartApi;
import com.coocaa.smartsdk.object.ISmartDeviceInfo;
import com.coocaa.swaiotos.virtualinput.VirtualInputStarter;
import com.coocaa.tvpi.module.connection.ConnectDialogActivity;
import com.coocaa.tvpi.module.connection.WifiConnectActivity;
import com.coocaa.tvpi.module.io.HomeIOThread;
import com.coocaa.tvpi.module.local.VideoPlayerActivity;
import com.coocaa.tvpi.module.local.adapter.AlbumPreviewListAdapter;
import com.coocaa.tvpi.module.local.utils.LocalMediaHelp;
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
import java.util.HashMap;
import java.util.List;

import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.ss.channel.im.IMMessageCallback;

import static android.widget.AbsListView.OnScrollListener.SCROLL_STATE_IDLE;
import static com.coocaa.smartscreen.connect.SSConnectManager.CONNECT_BOTH;
import static com.coocaa.smartscreen.connect.SSConnectManager.CONNECT_LOCAL;
import static com.coocaa.smartscreen.connect.SSConnectManager.CONNECT_NOTHING;
import static com.coocaa.smartscreen.connect.SSConnectManager.PUSH_INVALID_NOT_PING;
import static com.coocaa.smartscreen.connect.SSConnectManager.TARGET_CLIENT_APP_STORE;

/**
 * 相册预览
 */
public class AlbumPreviewActivity2 extends BaseAppletActivity {
    public static final String TAG = AlbumPreviewActivity2.class.getSimpleName();
    private PushProgressDialogFragment pushProgressDialogFragment;
    private Context mContext;
    private AlbumFragment mAlbumView;
    private LinearLayout rlPush;
    private TextView mPushToTv;
    //    private ImageView mImgPushIcon;
    private ImageView mImgPushOk;
    private CommonTitleBar mCommonTitleBar;
    private RecyclerView recyclerView;

    private int mPosition;
    private String mAlbumName;
    private ImageData mImageData;
    List<String> mImageCovers;
    ArrayList<ImageData> mImageDatas;
    boolean fromShare = false;//从分享进来
    HashMap<String, ArrayList<ImageData>> mImageMap;
    private Handler uiHandler;
    private AlbumPreviewListAdapter albumPreviewListAdapter;
    private AlbumPreviewLayoutManager albumPreviewLayoutManager;
    private long startTime;
    private long endTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        EventBus.getDefault().register(this);
        if (null != getIntent()) {
            Bundle bundle = getIntent().getExtras();
            mPosition = bundle.getInt("POSITION");
            mAlbumName = bundle.getString("ALBUMNAME");
            mImageData = (ImageData) bundle.getParcelable("IMAGEDATA");
            fromShare = bundle.getBoolean("fromShare", false);
            if (fromShare && mImageData != null) {
                mImageDatas = new ArrayList<>(1);
                mImageDatas.add(mImageData);
            }
        }
        setContentView(R.layout.local_activity_album_preview3);
        initData();
        setupView();
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

        if (progressEvent.getType() == IMMessage.TYPE.PROGRESS) {
            uiHandler.removeCallbacksAndMessages(null);
            Log.d(TAG, "onEvent: 0");
            uiHandler.post(runnableSetProgress);
            uiHandler.postDelayed(runnablePushError, 15000);
            uiHandler.postDelayed(runnableDismiss, 16000);
            return;
        }

        if (pushProgressDialogFragment.isAdded()) {
            if (progressEvent.getType() != IMMessage.TYPE.RESULT) {
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
        Log.d(TAG, "initData: start");
        mImageCovers = LocalMediaHelp.getImageGroup();
        mImageMap = LocalMediaHelp.getImageCacheMap();
        if (mImageMap != null && !TextUtils.isEmpty(mAlbumName)) {
            mImageDatas = mImageMap.get(mAlbumName);
        }
        Log.d(TAG, "initData: end");
    }

    private void setupView() {
        uiHandler = new Handler();

        mCommonTitleBar = findViewById(R.id.album_preview_common_title_bar);
        mCommonTitleBar.setOnClickListener(new CommonTitleBar.OnClickListener() {
            @Override
            public void onClick(CommonTitleBar.ClickPosition position) {
                if (position == CommonTitleBar.ClickPosition.LEFT) {
                    finish();
                } else if (position == CommonTitleBar.ClickPosition.RIGHT) {

                }
            }
        });
        if (mNPAppletInfo != null) {
            mCommonTitleBar.setVisibility(View.GONE);
        }

        if (mAlbumView == null) {
            mAlbumView = AlbumFragment.newInstance(mImageDatas, mPosition, mContext, fromShare);
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.add(R.id.album_preview_content, mAlbumView, AlbumFragment.class.getName());
            transaction.commit();
        }

        mAlbumView.setOnAlbumEventListener(new AlbumFragment.OnAlbumEventListener() {
            @Override
            public void onClick() {

            }

            @Override
            public void onPageChanged(int page) {
                mPosition = page;
                if (mImageDatas != null) {
                    mImageData = mImageDatas.get(page);
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

        albumPreviewListAdapter = new AlbumPreviewListAdapter(AlbumPreviewActivity2.this, mImageDatas);
        recyclerView = findViewById(R.id.rv_album);
        albumPreviewLayoutManager = new AlbumPreviewLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(albumPreviewLayoutManager);
        recyclerView.setAdapter(albumPreviewListAdapter);
        albumPreviewListAdapter.setOnPictureItemClickListener(new AlbumPreviewListAdapter.OnPictureItemClickListener() {
            @Override
            public void onPictureItemClick(int position, ImageData imageData) {
                Log.d(TAG, "onPictureItemClick: ");
                mPosition = position;
                mImageData = imageData;
                if (mAlbumView.isAdded()) {
                    mAlbumView.showSelectPicture(position);
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
                if (recyclerView.getLayoutManager().findViewByPosition(mPosition) != null) {
                    recyclerView.getLayoutManager().findViewByPosition(mPosition).requestFocus();
                }
            }
        });
        albumPreviewLayoutManager.scrollToPositionWithOffset(mPosition, DimensUtils.getDeviceWidth(this) / 2 - DimensUtils.dp2Px(this, 20 + 38));
    }

    private void initPushFragment() {
        pushProgressDialogFragment = new PushProgressDialogFragment().with((AppCompatActivity) AlbumPreviewActivity2.this);
        pushProgressDialogFragment.setListener(new PushProgressDialogFragment.PushProgressDialogFragmentListener() {
            @Override
            public void onDialogDismiss() {
                uiHandler.removeCallbacksAndMessages(null);
            }
        });
    }

    private void pushToTv() {
        int connectState = SSConnectManager.getInstance().getConnectState();
        final ISmartDeviceInfo deviceInfo = SmartApi.getConnectDeviceInfo();
        Log.d(TAG, "pushToTv: connectState" + connectState);
        Log.d(TAG, "pushToTv: deviceInfo" + deviceInfo);
        //未连接
        if(connectState == CONNECT_NOTHING || deviceInfo == null){
            ConnectDialogActivity.start(AlbumPreviewActivity2.this);
            return;
        }
        //本地连接不通
        if(!(connectState == CONNECT_LOCAL || connectState == CONNECT_BOTH)){
            WifiConnectActivity.start(AlbumPreviewActivity2.this);
            return;
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
                SSConnectManager.getInstance().sendImageMessage(mImageData.tittle, new File(mImageData.url), TARGET_CLIENT_APP_STORE, new IMMessageCallback() {
                    @Override
                    public void onStart(IMMessage message) {
                        Log.d(TAG, "onStart: ");
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
        });
        //ToastUtils.getInstance().showGlobalShort(getResources().getString(R.string.push_screen_success_tips));
        submitLocalPushUMData();

    }

    private void submitLocalPushUMData() {
        if (mImageData == null) {
            return;
        }
        DecimalFormat df = new DecimalFormat("#0.0");
        String size = String.valueOf(df.format(Double.valueOf(mImageData.size) / 1024 / 1024));
        LogParams params = LogParams.newParams().append("applet_id", mApplet == null ? "" : mApplet.getId())
                .append("applet_name", mApplet == null ? "" : mApplet.getName())
                .append("file_size", size)
                .append("file_format", mImageData.url.substring(mImageData.url.lastIndexOf('.') + 1));
        LogSubmit.event("file_cast_btn_clicked", params.getParams());
    }

    private void submitPushDuration() {
        String duration = "0";
        endTime = System.currentTimeMillis();
        if (startTime < endTime) {
            duration = String.valueOf(endTime - startTime);
        }
        if (mImageData == null) {
            return;
        }
        LogParams params = LogParams.newParams().append("applet_id", mApplet == null ? "" : mApplet.getId())
                .append("applet_name", mApplet == null ? "" : mApplet.getName())
                .append("duration", duration)
                .append("file_size", FileCalculatorUtil.getFileSize(mImageData.size))
                .append("file_type", mImageData.url.substring(mImageData.url.lastIndexOf('.') + 1));
        LogSubmit.event("cast_load_duration", params.getParams());
    }

    private void showOkIcon() {
//        mImgPushIcon.setVisibility(View.GONE);
        mPushToTv.setVisibility(View.GONE);
        mImgPushOk.setVisibility(View.VISIBLE);

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
        mImgPushOk.startAnimation(showOkAnim);
    }

    private void hideOkIcon() {
        mImgPushOk.setVisibility(View.GONE);
//        mImgPushIcon.setVisibility(View.VISIBLE);
        mPushToTv.setVisibility(View.VISIBLE);

        Animation showOkAnim = AnimationUtils.loadAnimation(mContext, R.anim.icon_hide);
        mImgPushOk.startAnimation(showOkAnim);
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            hideOkIcon();
        }
    };

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

}
