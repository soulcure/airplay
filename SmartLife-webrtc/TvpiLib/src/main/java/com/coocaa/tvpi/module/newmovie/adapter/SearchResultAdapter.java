package com.coocaa.tvpi.module.newmovie.adapter;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseDelegateMultiAdapter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.delegate.BaseMultiTypeDelegate;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.coocaa.publib.base.BaseActivity;
import com.coocaa.publib.base.GlideApp;
import com.coocaa.publib.data.channel.PlayParams;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.data.movie.Episode;
import com.coocaa.smartscreen.data.movie.LongVideoSearchResultModel;
import com.coocaa.smartscreen.utils.CmdUtil;
import com.coocaa.tvpi.module.connection.ConnectDialogActivity;
import com.coocaa.tvpi.module.movie.LongVideoDetailActivity2;
import com.coocaa.tvpi.module.newmovie.fragment.EpisodeDialogFragment;
import com.coocaa.tvpi.util.ReportUtil;
import com.coocaa.tvpi.view.decoration.CommonHorizontalItemDecoration;
import com.coocaa.tvpilib.R;
import com.umeng.analytics.MobclickAgent;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.coocaa.tvpi.common.UMEventId.CLICK_PUSH_TO_TV;

/**
 * 搜索结果适配器
 * Created by songxing on 2020/7/14
 */
public class SearchResultAdapter extends BaseDelegateMultiAdapter<LongVideoSearchResultModel, BaseViewHolder> {
    public static final int TYPE_MOVIE = 0; //电影
    public static final int TYPE_TV_OR_VARIETY = 1; //电视剧或综艺

    private String keyword;
    private SearchListener searchListener;


    public SearchResultAdapter() {
        super();
        setMultiTypeDelegate(new MyMultiTypeDelegate());
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, LongVideoSearchResultModel model) {
        switch (holder.getItemViewType()) {
            case TYPE_MOVIE:
                GlideApp.with(getContext())
                        .load(model.video_poster)
                        .dontAnimate()
                        .into((ImageView) holder.getView(R.id.ivCover));

                holder.setText(R.id.tvScore, model.video_detail.score + "分");
//                holder.setText(R.id.tvName, model.video_detail.album_title);
                TextView tvTitle = holder.getView(R.id.tvName);
                setTitle(model.video_detail.album_title, tvTitle);

                if (!TextUtils.isEmpty(model.video_detail.publish_date)) {
                    holder.setText(R.id.tvTime, "年份：" + model.video_detail.publish_date);
                    holder.setGone(R.id.tvTime, false);
                } else {
                    holder.setGone(R.id.tvTime, true);
                }

                if (!TextUtils.isEmpty(model.video_detail.director)) {
                    holder.setText(R.id.tvDirector, "导演：" + model.video_detail.director);
                    holder.setGone(R.id.tvDirector, false);
                } else {
                    holder.setGone(R.id.tvDirector, true);
                }

                if (!TextUtils.isEmpty(model.video_detail.actor)) {
                    holder.setText(R.id.tvActor, "主演：" + model.video_detail.actor);
                    holder.setGone(R.id.tvActor, false);
                } else {
                    holder.setGone(R.id.tvActor, true);
                }

                if (!TextUtils.isEmpty(model.video_detail.video_tags)) {
                    holder.setText(R.id.tvTags, "类型：" + model.video_detail.video_tags);
                    holder.setGone(R.id.tvTags, false);
                } else {
                    holder.setGone(R.id.tvTags, true);
                }

//                holder.setText(R.id.tvEpisodeInfo,"更新至"+ model.video_detail.updated_segment + "集/共"
//                        +model.video_detail.publist_segment + "集");
                if (model.video_detail.is_collect == 1) {
                    holder.setImageResource(R.id.ivCollection, R.drawable.movie_collect_red);
                } else {
                    holder.setImageResource(R.id.ivCollection, R.drawable.movie_collect_gray);
                }


                holder.getView(R.id.ivPushToTv).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Episode episode = new Episode();
                        episode.third_album_id = model.video_detail.third_album_id;
                        episode.segment_index = 1;
                        episode.source = model.video_detail.source;
                        episode.video_title = model.video_detail.album_title;
                        pushVideoToTv(episode);
                    }
                });

                holder.getView(R.id.ivCollection).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (searchListener != null) {
                            searchListener.onCollectionClick(model);
                        }
                    }
                });

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LongVideoDetailActivity2.start(getContext(), model.video_detail.third_album_id);
                    }
                });

                break;
            case TYPE_TV_OR_VARIETY:
                GlideApp.with(getContext())
                        .load(model.video_poster)
                        .dontAnimate()
                        .into((ImageView) holder.getView(R.id.ivCover));
//                holder.setText(R.id.tvName, model.video_detail.album_title);

                TextView tvTitleM = holder.getView(R.id.tvName);
                setTitle(model.video_detail.album_title, tvTitleM);

                if (!TextUtils.isEmpty(model.video_detail.publish_date)) {
                    holder.setText(R.id.tvTime, "年份：" + model.video_detail.publish_date);
                    holder.setGone(R.id.tvTime, false);
                } else {
                    holder.setGone(R.id.tvTime, true);
                }

                if (!TextUtils.isEmpty(model.video_detail.director)) {
                    holder.setText(R.id.tvDirector, "导演：" + model.video_detail.director);
                    holder.setGone(R.id.tvDirector, false);
                } else {
                    holder.setGone(R.id.tvDirector, true);
                }

                if (!TextUtils.isEmpty(model.video_detail.actor)) {
                    holder.setText(R.id.tvActor, "主演：" + model.video_detail.actor);
                    holder.setGone(R.id.tvActor, false);
                } else {
                    holder.setGone(R.id.tvActor, true);
                }

                if (!TextUtils.isEmpty(model.video_detail.video_tags)) {
                    holder.setText(R.id.tvTags, "类型：" + model.video_detail.video_tags);
                    holder.setGone(R.id.tvTags, false);
                } else {
                    holder.setGone(R.id.tvTags, true);
                }

                if (model.video_detail.is_collect == 1) {
                    holder.setImageResource(R.id.ivCollection, R.drawable.movie_collect_red);
                } else {
                    holder.setImageResource(R.id.ivCollection, R.drawable.movie_collect_gray);
                }

                RecyclerView rvEpisode = holder.getView(R.id.rvEpisode);
                rvEpisode.setFocusableInTouchMode(false);
                rvEpisode.setHasFixedSize(true);
                if (rvEpisode.getLayoutManager() == null) {
                    rvEpisode.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                }
                if (rvEpisode.getItemDecorationCount() == 0) {
                    CommonHorizontalItemDecoration decoration = new CommonHorizontalItemDecoration(
                            DimensUtils.dp2Px(getContext(), 15f), DimensUtils.dp2Px(getContext(), 10f));
                    rvEpisode.addItemDecoration(decoration);
                }

                if (rvEpisode.getAdapter() != null && holder.getAdapterPosition() == (int)rvEpisode.getTag()) {
                    rvEpisode.getAdapter().notifyDataSetChanged();
                }else {
                    EpisodeAdapter episodeAdapter = new EpisodeAdapter(model.video_detail.video_type);
                    rvEpisode.setAdapter(episodeAdapter);
                    episodeAdapter.setList(model.episodes_list);
                    episodeAdapter.setOnItemClickListener(new OnItemClickListener() {
                        @Override
                        public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                            Episode episode = episodeAdapter.getData().get(position);
                            pushVideoToTv(episode);

                            if(position > 0) {
                                rvEpisode.scrollToPosition(position - 1);
                            }
                        }
                    });
                    rvEpisode.setTag(holder.getAdapterPosition());
                }


                holder.getView(R.id.tvEpisodeInfoLayout).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EpisodeDialogFragment episodeDialogFragment = new EpisodeDialogFragment();
                        episodeDialogFragment.setLongVideoList(model.episodes_list);
                        episodeDialogFragment.setPromptInfo(model.video_detail.prompt_info);
                        episodeDialogFragment.setVideoType(model.video_detail.video_type);
                        episodeDialogFragment.setOnEpisodesCallback(new EpisodeDialogFragment.OnEpisodesCallback() {
                            @Override
                            public void onSelected(Episode episode, int position) {
                                pushVideoToTv(episode);
                                if(position > 0) {
                                    rvEpisode.scrollToPosition(position - 1);
                                }
                            }
                        });
                        episodeDialogFragment.show(((BaseActivity) getContext()).getFragmentManager(),
                                EpisodeDialogFragment.DIALOG_FRAGMENT_TAG);
                    }
                });

                holder.getView(R.id.ivCollection).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (searchListener != null) {
                            searchListener.onCollectionClick(model);
                        }
                    }
                });

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LongVideoDetailActivity2.start(getContext(), model.video_detail.third_album_id);
                    }
                });

                break;
            default:
                break;
        }
    }



    private static class MyMultiTypeDelegate extends BaseMultiTypeDelegate<LongVideoSearchResultModel> {

        public MyMultiTypeDelegate() {
            addItemType(TYPE_MOVIE, R.layout.item_movie_search_movie);
            addItemType(TYPE_TV_OR_VARIETY, R.layout.item_movie_search_tv_or_variety);
        }

        @Override
        public int getItemType(@NotNull List<? extends LongVideoSearchResultModel> list, int position) {
            if (list.get(position) != null
                    && list.get(position).video_detail != null
                    && list.get(position).video_detail.video_type.equals("电影")) {
                return TYPE_MOVIE;
            } else {
                return TYPE_TV_OR_VARIETY;
            }
        }
    }

    private void setTitle(String title, TextView tv) {
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(keyword)) {
            return;
        }
        if (title.contains(keyword)) {
            int start = title.indexOf(keyword);
            int end = start + keyword.length();
            SpannableString spannableString = new SpannableString(title);
            spannableString.setSpan(new ForegroundColorSpan(tv.getResources().getColor(R.color.color_main_red)),
                    start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            //设置字体，BOLD为粗体
            spannableString.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv.setText(spannableString);
        } else {
            tv.setText(title);
        }
    }

    private void pushVideoToTv(Episode episode) {
        if (null != episode) {
            if(!SSConnectManager.getInstance().isConnected()){
                ConnectDialogActivity.start(getContext());
                return;
            }
            ToastUtils.getInstance().showGlobalLong("已共享");
            CmdUtil.sendVideoCmd(PlayParams.CMD.ONLINE_VIDEO.toString(),
                    episode.third_album_id, episode.segment_index - 1 + "");

            List<LongVideoSearchResultModel> data = getData();
            for (LongVideoSearchResultModel datum : data) {
                if(datum.episodes_list != null) {
                    for (Episode e : datum.episodes_list) {
                        e.isSelected = false;
                    }
                }
            }
            episode.isSelected = true;
            notifyDataSetChanged();


            Map<String, String> map = new HashMap<>();
            map.put("source", episode.source);
            map.put("video_type", "long");
            map.put("page_name", "long_video_detail");
            MobclickAgent.onEvent(getContext(), CLICK_PUSH_TO_TV, map);
            ReportUtil.reportPushHistory(episode, "1");
        }
    }


    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public void setSearchListener(SearchListener searchListener) {
        this.searchListener = searchListener;
    }

    public interface SearchListener {
        void onCollectionClick(LongVideoSearchResultModel model);
    }
}
