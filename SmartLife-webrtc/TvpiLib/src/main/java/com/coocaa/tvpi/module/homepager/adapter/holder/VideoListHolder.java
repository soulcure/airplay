package com.coocaa.tvpi.module.homepager.adapter.holder;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.smartscreen.data.movie.VideoRecommendListModel;
import com.coocaa.tvpi.module.homepager.adapter.RecommendVideoAdapter;
import com.coocaa.tvpi.module.movie.HomeRecommendActivity;
import com.coocaa.tvpi.view.decoration.CommonHorizontalItemDecoration;
import com.coocaa.tvpilib.R;
import com.umeng.commonsdk.debug.D;

/**
 * 智屏推荐影视界面布局
 * Created by songxing on 2020/3/25
 */
public class VideoListHolder extends RecyclerView.ViewHolder {
    private Context context;
    private ImageView ivArrow;
    private TextView tvTitle;
    private RecyclerView rvVideoList;
    private RecommendVideoAdapter adapter;


    public VideoListHolder(@NonNull View itemView, Context context) {
        super(itemView);
        this.context = context;
        ivArrow = itemView.findViewById(R.id.iv_arrow);
        tvTitle = itemView.findViewById(R.id.tv_title);
        rvVideoList = itemView.findViewById(R.id.rv_videolist);
        rvVideoList.setNestedScrollingEnabled(false);
        rvVideoList.setLayoutManager(new LinearLayoutManager(context,RecyclerView.HORIZONTAL,false));
        CommonHorizontalItemDecoration itemDecoration = new CommonHorizontalItemDecoration(
                DimensUtils.dp2Px(context,15), DimensUtils.dp2Px(context,10));
        rvVideoList.addItemDecoration(itemDecoration);
        adapter = new RecommendVideoAdapter(context);
        rvVideoList.setAdapter(adapter);
    }

    public void onBind(final VideoRecommendListModel beans) {
        if(beans == null || beans.video_list == null){
            return;
        }
        tvTitle.setText(beans.title);
        adapter.setData(beans.video_list);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HomeRecommendActivity.start(context,String.valueOf(beans.tag_id),beans.title);
            }
        });
    }
}
