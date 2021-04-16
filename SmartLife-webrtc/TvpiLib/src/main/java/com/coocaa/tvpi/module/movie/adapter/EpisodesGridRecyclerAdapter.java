package com.coocaa.tvpi.module.movie.adapter;

import android.content.Context;
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

/**
 * Created by IceStorm on 2017/12/21.
 */

public class EpisodesGridRecyclerAdapter extends RecyclerView.Adapter <EpisodesGridRecyclerAdapter.ViewHolder> implements View.OnClickListener {

    private static final String TAG = "SearchResultRecyclerCol";
    private String video_type;
    private List<Episode> dataList = new ArrayList<>();
    private EpisodesGridRecyclerAdapter.OnItemClickListener mOnItemClickListener = null;

    private Context context;
    private int selectedIndex = -1;

    public void setVideoType(String type){
        video_type = type;
    }
    //define interface
    public interface OnItemClickListener {
        void onItemClick(View view, int position, Episode data);
    }

    public void setOnItemClickListener(EpisodesGridRecyclerAdapter.OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public EpisodesGridRecyclerAdapter(Context context) {
        this.context = context;
    }

    public void addAll(List<Episode> videoList) {
        if (null != videoList) {
            dataList.clear();
            dataList.addAll(videoList);
            notifyDataSetChanged();
        }
    }

    public void addMore(List<Episode> videoList) {
        if (null != videoList) {
            dataList.addAll(videoList);
            notifyDataSetChanged();
        }
    }

    public void setSelectedIndex(int index){
        try {
            if (selectedIndex != -1) {
                dataList.get(selectedIndex).isSelected = false;
                notifyItemChanged(selectedIndex);
            }
            dataList.get(index).isSelected = true;
            selectedIndex = index;
            notifyItemChanged(index);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Episode getSelectedItem() {
        try {
            return dataList.get(selectedIndex);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int getItemViewType(int position) {
        //加载多个布局，需要复写该方法
        if (dataList.size() > 1) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public EpisodesGridRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: ");
        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.episode_list_item3, parent, false);
        //将创建的View注册点击事件
        view.setOnClickListener(this);
        return new EpisodesGridRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(EpisodesGridRecyclerAdapter.ViewHolder viewHolder, int position) {
        Log.d(TAG, "onBindViewHolder: ");

        Episode episode = dataList.get(position);
        viewHolder.setData(episode, position);
        //将position保存在itemView的Tag中，以便点击时进行获取
        viewHolder.itemView.setTag(position);
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
            int position = (int)v.getTag();
            mOnItemClickListener.onItemClick(v, position, dataList.get(position));
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView indexTv;
        public TextView titleTv;
        public TextView movieTittleTv;
        public View itemLayout;

        ViewHolder(View view) {
            super(view);
            itemLayout = view.findViewById(R.id.episode_item_ll);
            indexTv = view.findViewById(R.id.episode_index_tv);
            titleTv = view.findViewById(R.id.episode_title_tv);
            movieTittleTv = view.findViewById(R.id.episode_movie_title_tv);
        }

        public void setData(final Episode data, int position) {
            if ("电影".equals(video_type)) {
                indexTv.setVisibility(View.GONE);
                titleTv.setVisibility(View.GONE);
                movieTittleTv.setVisibility(View.VISIBLE);
            } else {
                indexTv.setVisibility(View.VISIBLE);
                titleTv.setVisibility(View.VISIBLE);
                movieTittleTv.setVisibility(View.GONE);
            }

            if ("综艺".equals(video_type)) {
                indexTv.setText("第 "+ data.segment_index + " 期");
            } else {
                indexTv.setText("第 "+ data.segment_index + " 集");
            }

            if (!TextUtils.isEmpty(data.video_subtitle)) {
                titleTv.setText(data.video_subtitle);
                movieTittleTv.setText(data.video_subtitle);
            } else {
                titleTv.setText(data.video_title);
                movieTittleTv.setText(data.video_title);
            }

            if(dataList.get(position).isSelected) {
//                indexTv.setTextColor(indexTv.getResources().getColor(R.color.c_7));
                titleTv.setTextColor(titleTv.getResources().getColor(R.color.c_2));
                movieTittleTv.setTextColor(titleTv.getResources().getColor(R.color.c_2));
                itemLayout.setBackgroundResource(R.drawable.bg_episode_item_rect_selected);
            } else {
//                indexTv.setTextColor(indexTv.getResources().getColor(R.color.c_3));
                titleTv.setTextColor(titleTv.getResources().getColor(R.color.c_4));
                movieTittleTv.setTextColor(titleTv.getResources().getColor(R.color.c_3));
                itemLayout.setBackgroundResource(R.drawable.bg_episode_item_rect_normal);
            }

        }
    }

}
