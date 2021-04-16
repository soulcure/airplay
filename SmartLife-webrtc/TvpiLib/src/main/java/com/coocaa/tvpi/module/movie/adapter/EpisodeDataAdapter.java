package com.coocaa.tvpi.module.movie.adapter;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.smartscreen.data.movie.Episode;
import com.coocaa.tvpilib.R;

import java.util.ArrayList;
import java.util.List;

public class EpisodeDataAdapter extends RecyclerView.Adapter<EpisodeDataAdapter.EpisodeViewHolder> implements View.OnClickListener{
    private static final String TAG = "EpisodeDataAdapter";
    private int curSelectedPosition = -1;
    private String video_type;
    private List<Episode> dataList = new ArrayList<>();
    private OnItemClickListener mOnItemClickListener = null;

    public void setVideoType(String type){
        video_type = type;
    }

    //define interface
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public EpisodeDataAdapter() {
        /*this.count = count;
        for (int i = 0; i < count; i++) {
            dataList.add(new Video());
        }*/
    }

    public void addAll(List<Episode> videoList) {
        curSelectedPosition = -1;
        dataList.clear();
        dataList.addAll(videoList);
        notifyDataSetChanged();
    }

    public int getCurSelectedPosition() {
        return curSelectedPosition;
    }

    public void setSelected(int position) {
        try {
            if (curSelectedPosition != -1) {
                dataList.get(curSelectedPosition).isSelected = false;
                notifyItemChanged(curSelectedPosition);
            }
            dataList.get(position).isSelected = true;
            curSelectedPosition = position;
            notifyItemChanged(position);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Episode getSelected() {
        try {
            return dataList.get(curSelectedPosition);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public EpisodeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: ");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.episode_list_item2, parent, false);
        //将创建的View注册点击事件
        view.setOnClickListener(this);
        return new EpisodeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(EpisodeViewHolder viewHolder, int position) {
        if ("电影".equals(video_type)) {
            viewHolder.indexTv.setVisibility(View.GONE);
            viewHolder.titleTv.setVisibility(View.GONE);
            viewHolder.movieTittleTv.setVisibility(View.VISIBLE);
        } else {
            viewHolder.indexTv.setVisibility(View.VISIBLE);
            viewHolder.titleTv.setVisibility(View.VISIBLE);
            viewHolder.movieTittleTv.setVisibility(View.GONE);
        }

        if ("综艺".equals(video_type)) {
            viewHolder.indexTv.setText("第 "+dataList.get(position).segment_index + " 期");
        } else {
            viewHolder.indexTv.setText("第 "+dataList.get(position).segment_index + " 集");
        }

        if (!TextUtils.isEmpty(dataList.get(position).video_subtitle)) {
            viewHolder.titleTv.setText(dataList.get(position).video_subtitle);
            viewHolder.movieTittleTv.setText(dataList.get(position).video_subtitle);
        } else {
            viewHolder.titleTv.setText(dataList.get(position).video_title);
            viewHolder.movieTittleTv.setText(dataList.get(position).video_title);
        }
        //将position保存在itemView的Tag中，以便点击时进行获取
        viewHolder.itemView.setTag(position);
        if(dataList.get(position).isSelected) {
//            viewHolder.indexTv.setTextColor(viewHolder.indexTv.getResources().getColor(R.color.c_7));
            viewHolder.titleTv.setTextColor(viewHolder.titleTv.getResources().getColor(R.color.c_2));
            viewHolder.movieTittleTv.setTextColor(viewHolder.titleTv.getResources().getColor(R.color.c_2));
            viewHolder.itemLayout.setBackgroundResource(R.drawable.bg_episode_item_rect_selected);
        } else {
//            viewHolder.indexTv.setTextColor(viewHolder.indexTv.getResources().getColor(R.color.c_3));
            viewHolder.titleTv.setTextColor(viewHolder.titleTv.getResources().getColor(R.color.c_4));
            viewHolder.movieTittleTv.setTextColor(viewHolder.titleTv.getResources().getColor(R.color.c_3));
            viewHolder.itemLayout.setBackgroundResource(R.drawable.bg_episode_item_rect_normal);
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick: ");
        if (mOnItemClickListener != null) {
            //注意这里使用getTag方法获取position
            mOnItemClickListener.onItemClick(v,(int)v.getTag());
        }
    }

    public class EpisodeViewHolder extends RecyclerView.ViewHolder {
        public TextView indexTv;
        public TextView titleTv;
        public TextView movieTittleTv;
        public View itemLayout;

        EpisodeViewHolder(View view) {
            super(view);
            itemLayout = view.findViewById(R.id.episode_item_ll);
            indexTv = view.findViewById(R.id.episode_index_tv);
            titleTv = view.findViewById(R.id.episode_title_tv);
            movieTittleTv = view.findViewById(R.id.episode_movie_title_tv);
        }
    }
    
}
