package com.coocaa.tvpi.module.newmovie.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.coocaa.smartscreen.data.movie.Episode;
import com.coocaa.tvpilib.R;

import org.jetbrains.annotations.NotNull;

public class EpisodeAdapter extends BaseQuickAdapter<Episode, BaseViewHolder> {
    private String videoType;

    public EpisodeAdapter(String videoType) {
        super(videoType.equals("电视剧") ? R.layout.item_episode_tv : R.layout.item_episode_variety);
        this.videoType = videoType;
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, Episode episode) {
        if (videoType.equals("电视剧")) {
//            holder.setVisible(R.id.ivVipFlag, episode.charge_type == 1);
            holder.setText(R.id.tvIndex, String.valueOf(episode.segment_index));
            if (episode.isSelected) {
                holder.setGone(R.id.ivPlaying, false);
                holder.setTextColorRes(R.id.tvIndex, R.color.color_main_red);
            } else {
                holder.setGone(R.id.ivPlaying, true);
                holder.setTextColorRes(R.id.tvIndex, R.color.colorText_333333);
            }
        } else {
//            holder.setGone(R.id.ivVipFlag, episode.charge_type == 1);
            holder.setText(R.id.tvIndex, "第 " + episode.segment_index + " 期");
            holder.setText(R.id.tvDesc, episode.video_title);
            if (episode.isSelected) {
                holder.setGone(R.id.ivPlaying, false);
                holder.setTextColorRes(R.id.tvIndex, R.color.color_main_red);
                holder.setTextColorRes(R.id.tvDesc, R.color.color_main_red);
            } else {
                holder.setGone(R.id.ivPlaying, true);
                holder.setTextColorRes(R.id.tvIndex, R.color.colorText_333333);
                holder.setTextColorRes(R.id.tvDesc, R.color.colorText_999999);
            }
        }
    }
}