package com.coocaa.tvpi.module.homepager.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.publib.base.GlideApp;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.publib.utils.UIHelper;
import com.coocaa.smartscreen.data.movie.LongVideoListModel;
import com.coocaa.tvpi.module.movie.LongVideoDetailActivity2;
import com.coocaa.tvpilib.R;

import java.util.ArrayList;
import java.util.List;
/**
 * 智屏首页视频适配器
 * Created by songxing on 2020/3/18
 */
public class RecommendVideoAdapter extends RecyclerView.Adapter<RecommendVideoAdapter.VideoHolder> {
    private Context context;
    private List<LongVideoListModel> data = new ArrayList<>();

    public RecommendVideoAdapter(Context context) {
        this.context = context;
    }

    public void setData(List<LongVideoListModel> data) {
        if (data != null) {
            this.data = data;
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public VideoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false);
        return new VideoHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoHolder holder, int position) {

        LongVideoListModel longVideoListModel = data.get(position);
        holder.tvName.setText(longVideoListModel.album_title);
        GlideApp.with(context)
                .load(longVideoListModel.video_poster)
                .centerCrop()
                .into(holder.ivCover);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LongVideoDetailActivity2.start(context,longVideoListModel.third_album_id);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class VideoHolder extends RecyclerView.ViewHolder {
        ConstraintLayout root;
        ImageView ivCover;
        TextView tvName;

        public VideoHolder(@NonNull View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.root);
            ivCover = itemView.findViewById(R.id.iv_video_cover);
            tvName = itemView.findViewById(R.id.tv_video_name);
            ViewGroup.LayoutParams layoutParams = root.getLayoutParams();
            layoutParams.width = (int) ((DimensUtils.getDeviceWidth(context) - DimensUtils.dp2Px(context,50)) / 3f);
        }
    }
}
