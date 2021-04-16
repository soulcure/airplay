package com.coocaa.tvpi.module.movie.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.smartscreen.data.movie.Episode;
import com.coocaa.smartscreen.data.movie.LongVideoDetailModel;
import com.coocaa.smartscreen.data.movie.LongVideoListModel;
import com.coocaa.tvpi.module.movie.adapter.holder.LongVideoContainerHolder;
import com.coocaa.tvpi.module.movie.adapter.holder.LongVideoDetailHolder;
import com.coocaa.tvpi.module.movie.adapter.holder.LongVideoItemHolder;
import com.coocaa.tvpi.module.movie.widget.Relate3Column;
import com.coocaa.tvpilib.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by WHY on 2018/4/12.
 */

public class LongVideoRecyclerAdapter extends RecyclerView.Adapter {

    private static final String TAG = LongVideoRecyclerAdapter.class.getSimpleName();

    private static final int TYPE_DETAIL = 1;
    private static final int TYPE_CONTAINER = 2;
    private static final int TYPE_LONG_VIDEO_ITEM = 3;

    private List<Object> dataList;
    private Context context;
    private LongVideoCallback longVideoCallback;

    public LongVideoRecyclerAdapter (Context context, LongVideoCallback longVideoCallback) {
        this.context = context;
        this.dataList = new ArrayList<>();
        this.longVideoCallback = longVideoCallback;
    }

    public void addDetail(LongVideoDetailModel longVideoDetail) {
        Log.d(TAG, "addDetail: ");
        dataList.clear();
        dataList.add(longVideoDetail);
        notifyDataSetChanged();
    }

    public void addRelateLongVideo(List<LongVideoListModel> data) {
        Log.d(TAG, "addRelateLongVideo: ");
        //插入精彩正片
        dataList.add("猜你喜欢");
        int colum3Size = data.size() / 3;
        List<LongVideoListModel> videoList;
        for (int i = 0; i < colum3Size; i++) {
            videoList = new ArrayList<>();;
            videoList.add(data.get(i*3));
            videoList.add(data.get(i*3+1));
            videoList.add(data.get(i*3+2));
            Relate3Column relate3Column = new Relate3Column(videoList);
            dataList.add(relate3Column);
            notifyItemInserted(i+1);//解决刷新数据闪的问题
        }
        //notifyDataSetChanged();
    }

    public void clear() {
        dataList.clear();
        notifyDataSetChanged();
    }

    public interface LongVideoCallback {
        void onSelected(Episode episode, int position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: ");
        View v;
        RecyclerView.ViewHolder holder = null;
        if (viewType == TYPE_DETAIL) {
            v = LayoutInflater.from(context).inflate(R.layout.long_video_detail_holder, parent, false);
            holder = new LongVideoDetailHolder(v);
        } else if (viewType == TYPE_CONTAINER) {
            v = LayoutInflater.from(context).inflate(R.layout.relate_container_view, parent, false);
            holder = new LongVideoContainerHolder(v);
        } else if (viewType == TYPE_LONG_VIDEO_ITEM) {
            v = LayoutInflater.from(context).inflate(R.layout.relate_3_column_item_view, parent, false);
            holder = new LongVideoItemHolder(v);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: ");
        if (getItemViewType(position) == TYPE_DETAIL) {
            ((LongVideoDetailHolder)holder).onBind((LongVideoDetailModel) dataList.get(position));
            ((LongVideoDetailHolder)holder).setOnEpisodesCallback(onEpisodesCallback);
        } else if (getItemViewType(position) == TYPE_CONTAINER) {
            ((LongVideoContainerHolder)holder).onBind();
        } else if (getItemViewType(position) == TYPE_LONG_VIDEO_ITEM) {
            ((LongVideoItemHolder)holder).onBind((Relate3Column) dataList.get(position));
        }
    }

    @Override
    public int getItemCount() {
//        Log.d(TAG, "getItemCount: " + dataList.size());
        return dataList.size();
    }

    @Override
    public int getItemViewType(int position) {
//        Log.d(TAG, "getItemViewType: ");
        try {
            Object item = dataList.get(position);
            if (item instanceof LongVideoDetailModel)
                return TYPE_DETAIL;
            else if (item instanceof String)
                return TYPE_CONTAINER;
            else if (item instanceof Relate3Column)
                return TYPE_LONG_VIDEO_ITEM;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.getItemViewType(position);
    }

    private LongVideoDetailHolder.OnEpisodesCallback onEpisodesCallback = new LongVideoDetailHolder.OnEpisodesCallback() {
        @Override
        public void onEpisodesUpdate(List<Episode> data) {

        }

        @Override
        public void onSelected(Episode episode, int position) {
            if (null != longVideoCallback)
                longVideoCallback.onSelected(episode, position);
        }
    };

}
