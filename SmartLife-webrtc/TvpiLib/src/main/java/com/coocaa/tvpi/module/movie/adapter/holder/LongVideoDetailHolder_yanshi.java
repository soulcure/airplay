package com.coocaa.tvpi.module.movie.adapter.holder;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.coocaa.movie.MovieProductActivity;
import com.coocaa.publib.base.BaseActivity;
import com.coocaa.publib.base.GlideApp;
import com.coocaa.publib.data.BaseData;
import com.coocaa.publib.network.NetWorkManager;
import com.coocaa.publib.network.util.ParamsUtil;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.publib.utils.IRLog;
import com.coocaa.smartscreen.data.movie.Episode;
import com.coocaa.smartscreen.data.movie.EpisodeListResp;
import com.coocaa.smartscreen.data.movie.LongVideoDetailModel;
import com.coocaa.tvpi.module.movie.DescDialogFragment;
import com.coocaa.tvpi.module.movie.EpisodeDialogFragment;
import com.coocaa.tvpi.module.movie.adapter.EpisodeDataAdapter;
import com.coocaa.tvpi.view.decoration.CommonHorizontalItemDecoration;
import com.coocaa.tvpilib.R;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DefaultObserver;
import io.reactivex.schedulers.Schedulers;
import jp.wasabeef.glide.transformations.BlurTransformation;
import okhttp3.ResponseBody;

/**
 * Created by WHY on 2018/4/12.
 */

public class LongVideoDetailHolder_yanshi extends RecyclerView.ViewHolder {

    private static final String TAG = LongVideoDetailHolder_yanshi.class.getSimpleName();

    private Context mContext;

    private View mHeaderLayout;
    private ImageView mPostIV, mPostBigIV, mVipIV;
    private TextView mTitleBigTV, mScoreTV, mTagsTV, mVideoTypeTV, mSegmentTV;

    private RecyclerView recyclerView;
    private EpisodeDataAdapter adapter;

    private TextView mEpisodeTV;
    private RelativeLayout mEpisodeHeaderRL;

    private View mDirectorLayout;
    private View mActorLayout;
    private TextView mDirectorTV;
    private TextView mActorTV;
    private TextView mDescTV;

    private View episodeMore, descMore;

    private LongVideoDetailModel mLongVideoDetail;
    private List<Episode> mEpisodeList;
    private int mTryWatchTime;

    private int pageIndex = 0;
    private int pageSize = 50;

    private int selectedIndex = -1;

    public LongVideoDetailHolder_yanshi(View itemView) {
        super(itemView);
        mContext = itemView.getContext();

        mHeaderLayout = itemView.findViewById(R.id.long_video_header_layout);
        mPostIV = itemView.findViewById(R.id.long_video_poster);
        mPostBigIV = itemView.findViewById(R.id.long_video_poster_big);
        mVipIV = itemView.findViewById(R.id.long_video_vip);
        mTitleBigTV = itemView.findViewById(R.id.long_video_title_big);
        mScoreTV = itemView.findViewById(R.id.long_video_score_tv);
        mTagsTV = itemView.findViewById(R.id.long_video_tags_tv);
        mVideoTypeTV = itemView.findViewById(R.id.long_video_type_tv);
        mSegmentTV = itemView.findViewById(R.id.long_video_segment_tv);

        itemView.findViewById(R.id.buy_vip_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.startActivity(new Intent(mContext, MovieProductActivity.class));
            }
        });

        adapter = new EpisodeDataAdapter();
        recyclerView = itemView.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        CommonHorizontalItemDecoration decoration = new CommonHorizontalItemDecoration(DimensUtils.dp2Px(mContext,20f), DimensUtils.dp2Px(mContext,9.6f));
        recyclerView.addItemDecoration(decoration);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);


        mEpisodeTV = itemView.findViewById(R.id.long_video_episode_more_tv);
        mEpisodeHeaderRL = itemView.findViewById(R.id.episode_header_rl);

        mDirectorLayout = itemView.findViewById(R.id.long_video_director_layout);
        mActorLayout = itemView.findViewById(R.id.long_video_actor_layout);
        mDirectorTV = itemView.findViewById(R.id.long_video_director_tv);
        mActorTV = itemView.findViewById(R.id.long_video_actor_tv);
        mDescTV = itemView.findViewById(R.id.long_video_desc_tv);

        episodeMore = itemView.findViewById(R.id.long_video_episode_more_rl);
        descMore = itemView.findViewById(R.id.long_video_desc_more_rl);
    }

    public void onBind(LongVideoDetailModel longVideoDetail) {
        selectedIndex = -1;
        mLongVideoDetail = longVideoDetail;
        if (null != mLongVideoDetail) {

            try {
                if (mLongVideoDetail.album_title.length() > 12) {
                    float appBarH = mContext.getResources().getDimension(R.dimen.long_video_detail_app_bar_height);
                    float textH = mContext.getResources().getDimension(R.dimen.s_7);
                    mHeaderLayout.getLayoutParams().height = (int) (appBarH + textH);
                }
                mTitleBigTV.setText(mLongVideoDetail.album_title);
                mScoreTV.setText(mLongVideoDetail.score + "分");

                if (TextUtils.isEmpty(mLongVideoDetail.video_type))
                    mVideoTypeTV.setText("");
                else
                    mVideoTypeTV.setText(" · " + mLongVideoDetail.video_type);

                if (TextUtils.isEmpty(mLongVideoDetail.video_type))
                    mTagsTV.setText("");
                else
                    mTagsTV.setText(" · " + mLongVideoDetail.video_tags);

                String segmentStr = "";
                if (!"电影".equals(mLongVideoDetail.video_type)) {
                    if (mLongVideoDetail.updated_segment > 0 && mLongVideoDetail.updated_segment != mLongVideoDetail.publist_segment) {
                        segmentStr = "更新至" + mLongVideoDetail.updated_segment + "集";
                    }
                    if (mLongVideoDetail.publist_segment > 0) {
                        if (!TextUtils.isEmpty(segmentStr))
                            segmentStr += "/共 " + mLongVideoDetail.publist_segment + " 集";
                        else
                            segmentStr = "共 "+ mLongVideoDetail.publist_segment + " 集";
                    }
                }
                mSegmentTV.setText(segmentStr);

                GlideApp.with(mContext)
                        .load(mLongVideoDetail.video_poster)
                        .centerCrop()
                        .into(mPostIV);

                GlideApp.with(mContext)
                        .load(mLongVideoDetail.video_poster)
                        .centerCrop()
                        .transform(new BlurTransformation(25, 5))
                        .into(mPostBigIV);
                setVipIcon(mLongVideoDetail.source_sign, mVipIV);
            } catch (Exception e) {
                e.printStackTrace();
            }

            getVideoEpisodes(mLongVideoDetail.third_album_id, pageIndex, mLongVideoDetail.updated_segment);

            if ("电影".equals(mLongVideoDetail.video_type) || "电视剧".equals(mLongVideoDetail.video_type)) {
                mActorTV.setText(mLongVideoDetail.actor);
                mDirectorTV.setText(mLongVideoDetail.director);
                mActorLayout.setVisibility(View.VISIBLE);
                mDirectorLayout.setVisibility(View.VISIBLE);
                mDescTV.setVisibility(View.GONE);
            } else if (!TextUtils.isEmpty(mLongVideoDetail.description)) {
                mDescTV.setText(mLongVideoDetail.description);
                mActorLayout.setVisibility(View.GONE);
                mDirectorLayout.setVisibility(View.GONE);
                mDescTV.setVisibility(View.VISIBLE);
            } else {
                mActorLayout.setVisibility(View.GONE);
                mDirectorLayout.setVisibility(View.GONE);
                mDescTV.setVisibility(View.GONE);
            }


        }
        initListener();
    }

    private void setVipIcon(String source_sign, View vipIV) {
        if (null != source_sign && null != vipIV) {
            if (source_sign.equals(LongVideoDetailModel.VIP_QiYiGuo)) {
                vipIV.setBackgroundResource(R.drawable.icon_vip_qiyiguo);
            } else if (source_sign.equals(LongVideoDetailModel.VIP_GOLD)) {
                vipIV.setBackgroundResource(R.drawable.icon_vip_gold);
            } else if (source_sign.equals(LongVideoDetailModel.VIP_TENCENT)) {
                vipIV.setBackgroundResource(R.drawable.icon_vip_tencent);
            } else if (source_sign.equals(LongVideoDetailModel.VIP_DingJiJuChang)) {
                vipIV.setBackgroundResource(R.drawable.icon_vip_dingjijuchang);
            } else if (source_sign.equals(LongVideoDetailModel.VIP_TENCENT_SPORT)) {
                vipIV.setBackgroundResource(R.drawable.icon_vip_tencent_sport);
            }
        }
    }

    private void initListener() {
        adapter.setOnItemClickListener(new EpisodeDataAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
//                if (adapter.getCurSelectedPosition() == position) {//防止重复点击进入
//                    return;
//                }
                selectedIndex = position;
                adapter.setSelected(position);
                Episode episode = adapter.getSelected();
                if (null != mOnEpisodesCallback)
                    mOnEpisodesCallback.onSelected(episode, position);
//                mPushTV.setText("推送第" + video.episodes + "集至电视");
            }
        });

        episodeMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EpisodeDialogFragment episodeDialogFragment = new EpisodeDialogFragment();
                episodeDialogFragment.setLongVideoList(mEpisodeList, selectedIndex);
                episodeDialogFragment.setPromptInfo(mLongVideoDetail.prompt_info);
                episodeDialogFragment.setVideoType(mLongVideoDetail.video_type);
                episodeDialogFragment.setOnEpisodesCallback(new EpisodeDialogFragment.OnEpisodesCallback() {
                    @Override
                    public void onSelected(Episode episode, int position) {
                        selectedIndex = position;
                        adapter.setSelected(position);
                        if (null != mOnEpisodesCallback)
                            mOnEpisodesCallback.onSelected(episode, position);
                    }
                });
                episodeDialogFragment.show(((BaseActivity)itemView.getContext()).getFragmentManager(), EpisodeDialogFragment.DIALOG_FRAGMENT_TAG);
            }
        });
        descMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DescDialogFragment descDialogFragment = new DescDialogFragment();
                descDialogFragment.setLongVideoDetial(mLongVideoDetail);
                descDialogFragment.show(((BaseActivity)itemView.getContext()).getFragmentManager(), DescDialogFragment.DIALOG_FRAGMENT_TAG);
            }
        });
    }

    private void getVideoEpisodes(String third_album_id, int page_index, int page_size) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("third_album_id", third_album_id);
        params.put("page_index", page_index);
        params.put("page_size", page_size);
        NetWorkManager.getInstance()
                .getApiService()
                .getVideoEpisodesList(ParamsUtil.getQueryMap(params))
                .subscribeOn(Schedulers.newThread())
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
                        IRLog.d(TAG, "getVideoEpisodes,onSuccess. response = " + response);
                        if(!TextUtils.isEmpty(response)){
                            EpisodeListResp episodeListResp = BaseData.load(response, EpisodeListResp.class);
                            if(episodeListResp != null && episodeListResp.data != null){
                                updateEpisodeList(episodeListResp.data);
                                if ("电影".equals(mLongVideoDetail.video_type)) {
                                    mEpisodeTV.setText("");
                                } else if (!TextUtils.isEmpty(mLongVideoDetail.prompt_info)) {
                                    mEpisodeTV.setText(mLongVideoDetail.prompt_info);
                                } else {
                                    if ("综艺".equals(mLongVideoDetail.video_type)) {
                                        mEpisodeTV.setText("共 " + episodeListResp.data.size() + " 期");
                                    } else {
                                        mEpisodeTV.setText("共 " + episodeListResp.data.size() + " 集");
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (null != e)
                            IRLog.d(TAG, "getVideoEpisodes,onFailure,statusCode:" + e.toString());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void updateEpisodeList(List<Episode> data) {
        adapter.setVideoType(mLongVideoDetail.video_type);
        mEpisodeList = data;
        if (data != null && data.size() > 0) {
            adapter.addAll(data);
            /*if(data.size() > 1){
                mEpisodeHeaderRL.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.VISIBLE);
            }else{
                mEpisodeHeaderRL.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
            }

            int position = 0;//data.size() - 1
            adapter.setSelected(position);
            recyclerView.scrollToPosition(position);*/

            if (null != mOnEpisodesCallback) {
                mOnEpisodesCallback.onEpisodesUpdate(data);
                //mOnEpisodesCallback.onSelected(episode, position);
            }
            mEpisodeHeaderRL.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            mEpisodeHeaderRL.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    private OnEpisodesCallback mOnEpisodesCallback;
    public void setOnEpisodesCallback(OnEpisodesCallback onEpisodesCallback) {
        mOnEpisodesCallback = onEpisodesCallback;
    }
    public interface OnEpisodesCallback {
        void onEpisodesUpdate(List<Episode> data);
        void onSelected(Episode episode, int position);
    }

}
