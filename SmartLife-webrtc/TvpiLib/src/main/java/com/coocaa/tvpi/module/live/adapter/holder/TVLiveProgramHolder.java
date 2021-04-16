package com.coocaa.tvpi.module.live.adapter.holder;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coocaa.publib.base.GlideApp;
import com.coocaa.publib.data.tvlive.TVLiveAnim;
import com.coocaa.publib.data.tvlive.TVLiveChannelsData;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.publib.utils.SpUtil;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.utils.CmdUtil;
import com.coocaa.smartsdk.SmartApi;
import com.coocaa.smartsdk.object.ISmartDeviceInfo;
import com.coocaa.swaiotos.virtualinput.utils.VirtualInputUtils;
import com.coocaa.tvpi.event.StartPushEvent;
import com.coocaa.tvpi.module.connection.ConnectDialogActivity;
import com.coocaa.tvpi.module.connection.WifiConnectActivity;
import com.coocaa.tvpi.module.live.TVLiveFragment;
import com.coocaa.tvpilib.R;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.recyclerview.widget.RecyclerView;

import static com.coocaa.smartscreen.connect.SSConnectManager.CONNECT_BOTH;
import static com.coocaa.smartscreen.connect.SSConnectManager.CONNECT_LOCAL;
import static com.coocaa.smartscreen.connect.SSConnectManager.CONNECT_NOTHING;
import static com.coocaa.tvpi.common.UMengEventId.CHANNEL_CAST;

/**
 * @ClassName TVLiveProgramHolder
 * @Description TODO (write something)
 * @User heni
 * @Date 2019/1/10
 */
public class TVLiveProgramHolder extends RecyclerView.ViewHolder {

    private String TAG = TVLiveProgramHolder.class.getSimpleName();
    private Context mContext;

    private ImageView mImgChannelIcon;
    private LinearLayout mTitleLayout; //titleLayout 负责点击实现推送
    private TextView mTvTitle;
    private TextView mTvSubTitle;
    private ImageView mImgCollect;
    private ImageView mImgCollectAnim1;
    private ImageView mImgCollectAnim2;
    private View mMarkCollectView; //mImgCollect 太小了，不好点击，给它放一个大一点的view，处理点击。

    private boolean mIsCollected;
    private boolean mIsInEditState;
    TVLiveChannelsData tempChannnalData;
    OnLiveProgramItemClickListener mItemClickListener;

    public interface OnLiveProgramItemClickListener {
        void onLiveProgramItemClick(int position);
    }

    public TVLiveProgramHolder(View itemView, OnLiveProgramItemClickListener mItemClickListener) {
        super(itemView);
        mContext = itemView.getContext();
        this.mItemClickListener = mItemClickListener;
        tempChannnalData = new TVLiveChannelsData();

        mImgChannelIcon = itemView.findViewById(R.id.item_tvlive_channel_icon);
        mTitleLayout = itemView.findViewById(R.id.item_tvlive_channel_title_ll);
        mTvTitle = itemView.findViewById(R.id.item_tvlive_channel_title);
        mTvSubTitle = itemView.findViewById(R.id.item_tvlive_channel_subtitle);
        mImgCollect = itemView.findViewById(R.id.item_tvlive_channel_mark_icon);
        mImgCollectAnim1 = itemView.findViewById(R.id.item_tvlive_channel_mark_icon_anim1);
        mImgCollectAnim2 = itemView.findViewById(R.id.item_tvlive_channel_mark_icon_anim2);
        mMarkCollectView = itemView.findViewById(R.id.item_tvlive_channel_mark_view);
    }

    public void onBind(boolean isClickBtnState, final boolean isInEditState, final String
            channelClassName, final TVLiveChannelsData channelsData,final String networkForceKey) {
        if (channelsData != null) {
            mIsInEditState = isInEditState;

            mTvTitle.setText(channelsData.channel_name);
            if (channelsData.program != null) {
                mTvSubTitle.setVisibility(View.VISIBLE);
                mTvSubTitle.setText(channelsData.program.program_title);
            } else {
                mTvSubTitle.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(channelsData.channel_poster)) {
                Log.d(TAG, "onBind: " + channelsData.channel_poster);
                GlideApp.with(mContext)
                        .load(channelsData.channel_poster)
                        .centerCrop()
                        .into(mImgChannelIcon);
            }
            channelsData.isCollected = mIsCollected = programIsCollected(channelsData.channel_name);
            if (!TextUtils.isEmpty(channelClassName)) {
                if (channelClassName.equals(TVLiveFragment.MY_LOCAL_COLLECT)) {
                    mImgCollect.setVisibility(View.VISIBLE);
                    mImgCollectAnim1.setVisibility(View.GONE);
                    mImgCollect.setImageResource(mIsInEditState ? R.drawable
                            .tv_live_program_delete_collect : R.drawable
                            .tv_live_program_third_line);
                    if (isClickBtnState) {
                        if (mIsInEditState) {
                            showDeleteAnim();
                        } else {
                            hideDeleteAnim();
                        }
                    }
                } else {
                    mImgCollect.setImageResource(mIsCollected ? R.drawable
                            .tv_live_program_collect_focus
                            : R.drawable.tv_live_program_collect_nofocus);
                }
            }
        }

        mMarkCollectView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (channelClassName.equals(TVLiveFragment.MY_LOCAL_COLLECT)) {
                    if (mIsInEditState) {
                        if(TVLiveProgramHolder.this.getAdapterPosition() < 0){
                            return;
                        }
                        removeRrogramDataFromSP(channelsData.channel_name);
                        //回调回去，执行不notifyItemRemoved
                        mItemClickListener.onLiveProgramItemClick(TVLiveProgramHolder.this.getAdapterPosition());
                    }
                } else {
                    mIsCollected = !mIsCollected;
                    channelsData.isCollected = mIsCollected;
                    if (mIsCollected) {
                        showAnim();
                        TVLiveAnim tvLiveAnim = new TVLiveAnim();
                        tvLiveAnim.mView = mImgCollect;
                        tvLiveAnim.mType = 2;
                        EventBus.getDefault().post(tvLiveAnim);
                        writeProgramDataToSP(channelsData);
                    } else {
                        removeRrogramDataFromSP(channelsData.channel_name);
                        mImgCollect.setImageResource(mIsCollected ? R.drawable
                                .tv_live_program_collect_focus
                                : R.drawable.tv_live_program_collect_nofocus);
                    }
                }
            }
        });


        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VirtualInputUtils.playVibrate();
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
//                    WifiConnectTipActivity.start(mContext, networkForceKey);
                    WifiConnectActivity.start(mContext);
                    return;
                }

                EventBus.getDefault().post(new StartPushEvent("live"));

                ToastUtils.getInstance().showGlobalLong("已共享");
                CmdUtil.pushLiveVideo(channelsData.channel_class, channelsData.channel);
                Map<String, String> map = new HashMap<>();
                map.put("channel_class_name", channelClassName);
                map.put("channel_name", channelsData.channel_name);
                map.put("video_type", "live");
                map.put("page_name", "live_program_list");
                MobclickAgent.onEvent(mContext, CHANNEL_CAST, map);
            }
        });
    }

   /* private Command.Callback callback = new Command.Callback() {
        @Override
        public void onRequest(String msg) {
            Log.d(TAG, "onRequest: " + msg);
        }

        @Override
        public void onSuccess(final String msg) {
            Log.d(TAG, "onSuccess: " + msg);
            if (!TextUtils.isEmpty(msg)) {
                final TVResponse tvResponse = BaseData.load(msg, TVResponse.class);
                if (null != tvResponse && tvResponse.code == 0) {
                    Log.d(TAG, "onSuccess: 播放成功");
                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtils.getInstance().showGlobalShort(tvResponse.msg);
                        }
                    });
                    Map<String, String> map = new HashMap<>();
                    map.put("success", "0");
                    MobclickAgent.onEvent(mContext, LIVE_PROGRAM_PUSH, map);
                }
            }
        }

        @Override
        public void onError(final String msg) {
            Log.d(TAG, "onError: " + msg);
            if (!TextUtils.isEmpty(msg) && msg.toLowerCase().contains("refused")) {
                Log.d(TAG, "onError: 未安装最新版电视派");
                Intent intent = new Intent(mContext, TutorialActivity.class);
                mContext.startActivity(intent);
                Map<String, String> map = new HashMap<>();
                map.put("success", "1");
                MobclickAgent.onEvent(mContext, LIVE_PROGRAM_PUSH, map);
            }
        }

        @Override
        public void onEcho(final String msg) {
            Log.d(TAG, "onEcho: " + msg);
        }
    };*/

    /**
     * 写数据到sp存储
     * @param channelsData
     */
    private void writeProgramDataToSP(TVLiveChannelsData channelsData) {
        List<TVLiveChannelsData> tvLiveChannelsDataList = SpUtil.getList(mContext, SpUtil.Keys
                .TVLIVE_COLLECT_PROGRAMS);
        if (null == tvLiveChannelsDataList) {
            Log.d(TAG, "writeProgramDataToSP: getCollectProgramData is null!");
            tvLiveChannelsDataList = new ArrayList<>();
        }

        tempChannnalData.channel_name = channelsData.channel_name;
        tempChannnalData.channel = channelsData.channel;
        tempChannnalData.channel_class = channelsData.channel_class;
        tempChannnalData.isCollected = channelsData.isCollected;
        tempChannnalData.channel_poster = channelsData.channel_poster;
        tempChannnalData.program = null;
        tvLiveChannelsDataList.add(tempChannnalData);
        SpUtil.putList(mContext, SpUtil.Keys.TVLIVE_COLLECT_PROGRAMS, tvLiveChannelsDataList);

        //test
        /*Log.d(TAG, "writeProgramDataToSP: sp size: " + tvLiveChannelsDataList.size());
        for (TVLiveChannelsData data : tvLiveChannelsDataList) {
            Log.d(TAG, "writeProgramDataToSP: " + data.channel_name + ", " + data.channel_poster);
        }*/
    }

    /**
     * 从sp删除收藏的数据
     * @param channel_name
     */
    private void removeRrogramDataFromSP(String channel_name) {
        List<TVLiveChannelsData> tvLiveChannelsDataList = SpUtil.getList(mContext, SpUtil.Keys
                .TVLIVE_COLLECT_PROGRAMS);
        if (null == tvLiveChannelsDataList) {
            Log.d(TAG, "removeRrogramDataFromSP: getCollectProgramData is null!");
            return;
        }

        for (int i = 0; i < tvLiveChannelsDataList.size(); i++) {
            TVLiveChannelsData data = tvLiveChannelsDataList.get(i);
            if (null != data && !TextUtils.isEmpty(data.channel_name) && data.channel_name.equals
                    (channel_name)) {
                tvLiveChannelsDataList.remove(i);
                break;
            }
        }
        SpUtil.putList(mContext, SpUtil.Keys.TVLIVE_COLLECT_PROGRAMS, tvLiveChannelsDataList);

        //test
        /*Log.d(TAG, "removeRrogramDataFromSP: sp size: " + tvLiveChannelsDataList.size());
        for (TVLiveChannelsData data : tvLiveChannelsDataList) {
            Log.d(TAG, "after removeRrogramDataFromSP: " + data.channel_name + ", " + data
                    .channel_poster);
        }*/
    }

    /**
     * 遍历查询sp里是否收藏此channel_name
     */
    private boolean programIsCollected(String channel_name) {
        List<TVLiveChannelsData> tvLiveChannelsDataList = SpUtil.getList(mContext, SpUtil.Keys
                .TVLIVE_COLLECT_PROGRAMS);
        if (null == tvLiveChannelsDataList) {
            return false;
        } else {
            for (int i = 0; i < tvLiveChannelsDataList.size(); i++) {
                TVLiveChannelsData data = tvLiveChannelsDataList.get(i);
                if (null != data && !TextUtils.isEmpty(data.channel_name) && data.channel_name
                        .equals(channel_name)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void showAnim() {
        //小星星透明度从0-1
        PropertyValuesHolder pvh = PropertyValuesHolder.ofFloat("alpha",0f, 1f);
        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(mImgCollectAnim1, pvh).setDuration(300);
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mImgCollect.setImageResource(mIsCollected ? R.drawable
                        .tv_live_program_collect_focus
                        : R.drawable.tv_live_program_collect_nofocus);
                mImgCollectAnim1.setAlpha(0f);
            }
        });
        objectAnimator.start();

        //小星星0-1.5倍，同时1-0透明度
        PropertyValuesHolder pvhX1 = PropertyValuesHolder.ofFloat("alpha",1f, 0f);
        PropertyValuesHolder pvhY1 = PropertyValuesHolder.ofFloat("scaleX", 0.0f, 3.0f);
        PropertyValuesHolder pvhZ1 = PropertyValuesHolder.ofFloat("scaleY", 0.0f, 3.0f);
        ObjectAnimator objectAnimator1 = ObjectAnimator.ofPropertyValuesHolder(mImgCollectAnim2, pvhX1, pvhY1,pvhZ1).setDuration(300);
        objectAnimator1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }
        });
        objectAnimator1.start();
    }

    private void showDeleteAnim() {
        mImgCollect.setVisibility(View.GONE);
        mImgCollectAnim1.setAlpha(1.0f);
        mImgCollectAnim1.setVisibility(View.VISIBLE);
        mImgCollectAnim1.setImageResource(R.drawable.tv_live_program_delete_collect);
        ObjectAnimator translationYAnimTop = ObjectAnimator.ofFloat(mImgCollectAnim1,
                "translationX",
                DimensUtils.dp2Px(mContext, 40), 0f);
        translationYAnimTop.setDuration(200).start();
    }

    private void hideDeleteAnim() {
        //横线慢慢变亮
        mImgCollect.setVisibility(View.VISIBLE);
        PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("scaleX", 0f, 1.0f);
        PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat("scaleY", 0f, 1.0f);
        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(mImgCollect, pvhX, pvhY).setDuration(200);
        objectAnimator.start();

        mImgCollectAnim1.setVisibility(View.VISIBLE);
        mImgCollectAnim1.setAlpha(1.0f);
        mImgCollectAnim1.setImageResource(R.drawable.tv_live_program_delete_collect);
        ObjectAnimator translationYAnimTop = ObjectAnimator.ofFloat(mImgCollectAnim1,
                "translationX",
                0f, DimensUtils.dp2Px(mContext, 40));
        translationYAnimTop.setDuration(200).start();
    }
}
