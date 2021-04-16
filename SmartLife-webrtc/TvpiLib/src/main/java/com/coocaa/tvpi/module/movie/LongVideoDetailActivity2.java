package com.coocaa.tvpi.module.movie;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.coocaa.publib.PublibHelper;
import com.coocaa.publib.base.BaseAppletActivity;
import com.coocaa.publib.data.BaseData;
import com.coocaa.publib.data.channel.PlayParams;
import com.coocaa.publib.utils.IRLog;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.data.account.CoocaaUserInfo;
import com.coocaa.smartscreen.data.movie.Episode;
import com.coocaa.smartscreen.data.movie.LongVideoDetailModel;
import com.coocaa.smartscreen.data.movie.LongVideoDetailResp;
import com.coocaa.smartscreen.data.movie.LongVideoListResp;
import com.coocaa.smartscreen.network.NetWorkManager;
import com.coocaa.smartscreen.network.util.ParamsUtil;
import com.coocaa.smartscreen.utils.CmdUtil;
import com.coocaa.smartsdk.SmartApi;
import com.coocaa.smartsdk.object.ISmartDeviceInfo;
import com.coocaa.swaiotos.virtualinput.VirtualInputStarter;
import com.coocaa.tvpi.common.UMEventId;
import com.coocaa.tvpi.module.connection.ConnectDialogActivity;
import com.coocaa.tvpi.module.connection.WifiConnectActivity;
import com.coocaa.tvpi.module.log.LogParams;
import com.coocaa.tvpi.module.log.LogSubmit;
import com.coocaa.tvpi.module.login.LoginActivity;
import com.coocaa.tvpi.module.login.UserInfoCenter;
import com.coocaa.tvpi.module.movie.adapter.LongVideoRecyclerAdapter;
import com.coocaa.tvpi.module.movie.decoration.LongVideoItemDecoration;
import com.coocaa.tvpi.util.IntentUtils;
import com.coocaa.tvpi.util.ReportUtil;
import com.coocaa.tvpi.view.LoadTipsView;
import com.coocaa.tvpilib.R;
import com.google.gson.Gson;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DefaultObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import swaiotos.channel.iot.ss.device.Device;

import static com.coocaa.smartscreen.connect.SSConnectManager.CONNECT_BOTH;
import static com.coocaa.smartscreen.connect.SSConnectManager.CONNECT_LOCAL;
import static com.coocaa.smartscreen.connect.SSConnectManager.CONNECT_NOTHING;
import static com.coocaa.tvpi.common.UMEventId.CLICK_LONG_DETAIL_COLLECT;

/**
 * Created by wuhaiyuan on 2017/10/17.
 */
public class LongVideoDetailActivity2 extends BaseAppletActivity implements View.OnClickListener {

    private static final String TAG = LongVideoDetailActivity2.class.getSimpleName();

    public static final String KEY_THIRD_ALBUM_ID = "third_album_id";


    private LoadTipsView mLoadTipsView;
    private RecyclerView mRecyclerView;
    private LongVideoRecyclerAdapter mAdapter;

    private String mThirdAlbumId;
    private LongVideoDetailModel mLongVideoDetail;
    private int mCurY;

    private ImageView mVipIV, mCollectIV, mToolbarCollectIV, mBackIV, mToolbarBackIV;
    ;
    private TextView mTitleTV;
    private View mToolbarLayout;


    public static void start(Context context, String thirdAlbumId) {
        Intent starter = new Intent(context, LongVideoDetailActivity2.class);
        starter.putExtra(KEY_THIRD_ALBUM_ID, thirdAlbumId);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_long_video_detail2);
//        StatusBarHelper.translucent(this);
//        StatusBarHelper.setStatusBarDarkMode(this);
        if (getActionBar() != null) {
            getActionBar().hide();
        }

        Intent intent = getIntent();
        if (intent != null) {
            mThirdAlbumId = intent.getStringExtra(KEY_THIRD_ALBUM_ID);
            if (TextUtils.isEmpty(mThirdAlbumId)) {
                mThirdAlbumId = IntentUtils.INSTANCE.getStringExtra(intent, "videoId");
            }
        }

        initView();

        getVideoDetail();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent: ");
        super.onNewIntent(intent);

        if (intent != null) {
            mThirdAlbumId = intent.getStringExtra(KEY_THIRD_ALBUM_ID);
        }
        mAdapter.clear();
        mCurY = 0;
        getVideoDetail();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();

        MobclickAgent.onPageStart(TAG); // 统计页面
        if (mLongVideoDetail != null) {
            submitMovieDetailShow(mLongVideoDetail.third_album_id, mLongVideoDetail.album_title);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();

        MobclickAgent.onPageEnd(TAG); // 统计页面
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    ;

    private void initView() {
        mVipIV = findViewById(R.id.long_video_vip);
        mCollectIV = findViewById(R.id.long_video_collect_iv);
        mToolbarCollectIV = findViewById(R.id.long_video_toolbar_collect_iv);
        mToolbarLayout = findViewById(R.id.long_video_toolbar_layout);
        mCollectIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!UserInfoCenter.getInstance().isLogin()) {
                    LoginActivity.start(LongVideoDetailActivity2.this);
//                    UIHelper.toLogin(LongVideoDetailActivity2.this);
                    return;
                }
                mCollectIV.setClickable(false);
                if (null != mLongVideoDetail) {
                    showLoveAnim(mLongVideoDetail.is_collect == 1 ? false : true);
                    collect(mLongVideoDetail.is_collect == 1 ? 2 : 1);
                }
                submitLikeUMData();
                MobclickAgent.onEvent(LongVideoDetailActivity2.this, CLICK_LONG_DETAIL_COLLECT);
            }
        });

        mToolbarCollectIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!UserInfoCenter.getInstance().isLogin()) {
//                    UIHelper.toLogin(LongVideoDetailActivity2.this);
                    LoginActivity.start(LongVideoDetailActivity2.this);
                    return;
                }
                mToolbarCollectIV.setClickable(false);
                if (null != mLongVideoDetail) {
                    showLoveAnim(mLongVideoDetail.is_collect == 1 ? false : true);
                    collect(mLongVideoDetail.is_collect == 1 ? 2 : 1);
                }
                submitLikeUMData();
                MobclickAgent.onEvent(LongVideoDetailActivity2.this, CLICK_LONG_DETAIL_COLLECT);
            }
        });

        mTitleTV = findViewById(R.id.long_video_title);

        mLoadTipsView = findViewById(R.id.long_video_detail_load_tips_view);
        mLoadTipsView.setLoadTipsOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getVideoDetail();
            }
        });

        mRecyclerView = findViewById(R.id.long_video_recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new LongVideoItemDecoration());
        ((SimpleItemAnimator) mRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        mAdapter = new LongVideoRecyclerAdapter(this, mLongVideoCallback);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mCurY = mCurY + dy;
                float appBarH = getResources().getDimension(R.dimen.long_video_detail_app_bar_height);
                float actionbarH = getResources().getDimension(R.dimen.actionbar_height);
                //oppo挖孔手机上没有变全透明
                float alpha = mCurY / (appBarH - actionbarH) < 0.2f ? 0f : mCurY / (appBarH - actionbarH);
                mToolbarLayout.setAlpha(alpha);
            }
        });

        mBackIV = findViewById(R.id.long_video_img_back);
        mBackIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mToolbarBackIV = findViewById(R.id.long_video_toolbar_back);
        mToolbarBackIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        if (mNPAppletInfo != null) {
            ViewGroup oldTitle = findViewById(R.id.long_video_actionbar_layout);
            if (oldTitle != null) {
                oldTitle.setVisibility(View.GONE);//小程序标题显示时，原本标题需要隐藏
            }
            ViewGroup oldTitle2 = findViewById(R.id.long_video_toolbar_layout);
            if (oldTitle2 != null) {
                oldTitle2.setVisibility(View.GONE); //小程序标题显示时，原本标题需要隐藏
            }
        }
    }

    private LongVideoRecyclerAdapter.LongVideoCallback mLongVideoCallback = new LongVideoRecyclerAdapter.LongVideoCallback() {
        @Override
        public void onSelected(Episode episode, int position) {
            pushVideoToTv(episode, position);
            submitMoviePush(mLongVideoDetail.third_album_id, mLongVideoDetail.album_title);
        }
    };

    private void pushVideoToTv(Episode episode, int position) {
        if (null != episode) {
            int connectState = SSConnectManager.getInstance().getConnectState();
            final ISmartDeviceInfo deviceInfo = SmartApi.getConnectDeviceInfo();
            Log.d(TAG, "pushToTv: connectState" + connectState);
            Log.d(TAG, "pushToTv: deviceInfo" + deviceInfo);
            //未连接
            if(connectState == CONNECT_NOTHING || deviceInfo == null){
                ConnectDialogActivity.start(LongVideoDetailActivity2.this);
                return;
            }
            //本地连接不通
            if(!(connectState == CONNECT_LOCAL || connectState == CONNECT_BOTH)){
                WifiConnectActivity.start(LongVideoDetailActivity2.this);
                return;
            }

            Log.d(TAG, "onEpisodeClicked: " + new Gson().toJson(episode));
            ToastUtils.getInstance().showGlobalLong("已共享");
            CmdUtil.sendVideoCmd(PlayParams.CMD.ONLINE_VIDEO.toString(),
                    episode.third_album_id, episode.segment_index - 1 + "");
            VirtualInputStarter.show(this, false);
            ReportUtil.reportPushHistory(episode, "1");
        }
    }

    private void updataViews() {
        mAdapter.addDetail(mLongVideoDetail);

        mTitleTV.setText(mLongVideoDetail.album_title);
        showLoveAnim(mLongVideoDetail.is_collect == 1);
        setTitle(mLongVideoDetail.album_title);
    }

    private void setVipIcon(String source_sign) {
        if (null != source_sign && null != mVipIV) {
            if (source_sign.equals(LongVideoDetailModel.VIP_QiYiGuo)) {
                mVipIV.setImageResource(R.drawable.icon_vip_qiyiguo);
            } else if (source_sign.equals(LongVideoDetailModel.VIP_GOLD)) {
                mVipIV.setImageResource(R.drawable.icon_vip_gold);
            } else if (source_sign.equals(LongVideoDetailModel.VIP_TENCENT)) {
                mVipIV.setImageResource(R.drawable.icon_vip_tencent);
            } else if (source_sign.equals(LongVideoDetailModel.VIP_DingJiJuChang)) {
                mVipIV.setImageResource(R.drawable.icon_vip_dingjijuchang);
            } else if (source_sign.equals(LongVideoDetailModel.VIP_TENCENT_SPORT)) {
                mVipIV.setImageResource(R.drawable.icon_vip_tencent_sport);
            }
        }
    }

    private void getVideoDetail() {
        mLoadTipsView.setVisibility(View.VISIBLE);
        mLoadTipsView.setLoadTipsIV(LoadTipsView.TYPE_LOADING);
        HashMap<String, Object> params = new HashMap<>();
        params.put("third_album_id", mThirdAlbumId);
        NetWorkManager.getInstance()
                .getApiService()
                .getVideoDetail(ParamsUtil.getQueryMap(params))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultObserver<ResponseBody>() {
                    @Override
                    public void onNext(ResponseBody responseBody) {
                        String response = "";
                        try {
                            response = responseBody.string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        IRLog.d(TAG, "onSuccess. response = " + response);
                        if (!TextUtils.isEmpty(response)) {
                            LongVideoDetailResp longVideoDetailResp = BaseData.load(response, LongVideoDetailResp.class);
                            if (longVideoDetailResp != null && longVideoDetailResp.data != null) {
                                mLongVideoDetail = longVideoDetailResp.data;
                                updataViews();
                                mLoadTipsView.setVisibility(View.GONE);
                                getRelateLongVideo(0);
                            } else {
                                mLoadTipsView.setLoadTips("", LoadTipsView.TYPE_NODATA);
                            }
                        } else {
                            mLoadTipsView.setLoadTips("", LoadTipsView.TYPE_NODATA);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (null != e) {
                            IRLog.d(TAG, "onFailure,statusCode:" + e.toString());
                        }
                        mLoadTipsView.setLoadTips("", LoadTipsView.TYPE_FAILED);
                    }

                    @Override
                    public void onComplete() {
                        IRLog.d(TAG, "onComplete");
                    }
                });
    }

    private void getRelateLongVideo(int page_index) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("third_album_id", mThirdAlbumId);
        params.put("page_index", page_index);
        params.put("page_size", 10);
        NetWorkManager.getInstance()
                .getApiService()
                .getRelateLong(ParamsUtil.getQueryMap(params))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultObserver<ResponseBody>() {
                    @Override
                    public void onNext(ResponseBody responseBody) {
                        String response = "";
                        try {
                            response = responseBody.string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        IRLog.d(TAG, "onSuccess. response = " + response);
                        if (!TextUtils.isEmpty(response)) {
                            LongVideoListResp resp = BaseData.load(response, LongVideoListResp.class);
                            if (resp.code == 0 && resp != null && !resp.data.isEmpty()) {
//                        getRelateShortVideo(0);
                                mAdapter.addRelateLongVideo(resp.data);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (null != e)
                            IRLog.d(TAG, "onFailure,statusCode:" + e.toString());
                    }

                    @Override
                    public void onComplete() {
                        IRLog.d(TAG, "onComplete");
                    }
                });
    }

    /*
     * 收藏 1 取消2
     * */
    private void collect(final int collect_type) {
        HashMap<String, Object> queryMap = new HashMap<>();
        queryMap.put("collect_type", collect_type);

        HashMap<String, Object> params = new HashMap<>();
        params.put("video_type", 1);//0短片1正片
        params.put("third_album_id", mLongVideoDetail.third_album_id);
        params.put("video_title", mLongVideoDetail.album_title);
        params.put("video_poster", mLongVideoDetail.video_poster);
        RequestBody requestBody = RequestBody.create(MediaType.parse(
                "Content-Type, application/json"), new JSONObject(params).toString());
        NetWorkManager.getInstance()
                .getApiService()
                .addOrCancelCollect(ParamsUtil.getQueryMap(queryMap), requestBody)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultObserver<ResponseBody>() {
                    @Override
                    public void onNext(ResponseBody responseBody) {
                        String response = "";
                        try {
                            response = responseBody.string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        IRLog.d(TAG, "onSuccess. response = " + response);

                        if (!TextUtils.isEmpty(response)) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                int code = jsonObject.getInt("code");
                                if (code == 0) {
                                    mLongVideoDetail.is_collect = collect_type;
                                } else {
                                    mLongVideoDetail.is_collect = collect_type == 1 ? 2 : 1;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                mLongVideoDetail.is_collect = collect_type == 1 ? 2 : 1;
                            }
                        } else {
                            mLongVideoDetail.is_collect = collect_type == 1 ? 2 : 1;
                        }
//                mCollectIV.setSelected(mLongVideoDetail.is_collect == 1);
                        showLoveAnim(mLongVideoDetail.is_collect == 1);
                        mCollectIV.setClickable(true);
                        mToolbarCollectIV.setClickable(true);
                    }

                    @Override
                    public void onError(Throwable e) {
                        IRLog.d(TAG, "onFailure,statusCode:" + e.toString());
                        showLoveAnim(mLongVideoDetail.is_collect == 1);
                        mCollectIV.setClickable(true);
                        mToolbarCollectIV.setClickable(true);
                    }

                    @Override
                    public void onComplete() {
                        IRLog.d(TAG, "onComplete");
                    }
                });
    }

    private boolean localLoved;

    private void showLoveAnim(boolean isLoved) {
        if (localLoved == isLoved)
            return;
        localLoved = isLoved;
        if (isLoved) {
            mCollectIV.setBackgroundResource(R.drawable.icon_tab_love_selected);
            mToolbarCollectIV.setBackgroundResource(R.drawable.icon_tab_love_selected);
        } else {
            mCollectIV.setBackgroundResource(R.drawable.icon_tab_love_normal_white);
            mToolbarCollectIV.setBackgroundResource(R.drawable.icon_tab_love_normal_white);
        }
        PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("alpha", 1f, 1f);
        PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat("scaleX", 1f, 0.6f, 1f);
        PropertyValuesHolder pvhZ = PropertyValuesHolder.ofFloat("scaleY", 1f, 0.6f, 1f);
        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(mCollectIV, pvhX, pvhY, pvhZ).setDuration(300);
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }
        });
        objectAnimator.start();
        ObjectAnimator objectAnimator2 = ObjectAnimator.ofPropertyValuesHolder(mToolbarCollectIV, pvhX, pvhY, pvhZ).setDuration(300);
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }
        });
        objectAnimator2.start();
    }

    private void submitLikeUMData() {
        Map<String, String> map = new HashMap<>();
        map.put("page_name", "long_video_detail");
        MobclickAgent.onEvent(PublibHelper.getContext(), UMEventId.LIKE_CLICK, map);
    }

    private void submitMovieDetailShow(String id, String name) {
        Device device = SSConnectManager.getInstance().getDevice();
        CoocaaUserInfo coocaaUserInfo = UserInfoCenter.getInstance().getCoocaaUserInfo();
        LogParams params = LogParams.newParams()
                .append("ss_device_id", device == null ? "disconnected" : device.getLsid())
                .append("ss_device_type", device == null ? "disconnected" : device.getZpRegisterType())
                .append("account", coocaaUserInfo == null ? "not_login" : coocaaUserInfo.getOpen_id())
                .append("block_id", id)
                .append("block_name", name);
        LogSubmit.event("movie_detail_page_show", params.getParams());
    }

    private void submitMoviePush(String id, String name) {
        Device device = SSConnectManager.getInstance().getDevice();
        CoocaaUserInfo coocaaUserInfo = UserInfoCenter.getInstance().getCoocaaUserInfo();
        LogParams params = LogParams.newParams()
                .append("ss_device_id", device == null ? "disconnected" : device.getLsid())
                .append("ss_device_type", device == null ? "disconnected" : device.getZpRegisterType())
                .append("account", coocaaUserInfo == null ? "not_login" : coocaaUserInfo.getOpen_id())
                .append("block_id", id)
                .append("block_name", name);
        LogSubmit.event("movie_cast_btn_clicked", params.getParams());
    }
}
