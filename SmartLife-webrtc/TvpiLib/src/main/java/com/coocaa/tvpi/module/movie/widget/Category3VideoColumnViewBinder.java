package com.coocaa.tvpi.module.movie.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.publib.utils.UIHelper;
import com.coocaa.smartscreen.data.movie.LongVideoListModel;
import com.coocaa.tvpi.util.ReportUtil;
import com.coocaa.tvpilib.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.drakeet.multitype.ItemViewBinder;

import static com.coocaa.tvpi.common.UMEventId.CLICK_CATEGORY_FILTER_RESULT;

/**
 * Created by IceStorm on 2018/1/26.
 */

public class Category3VideoColumnViewBinder extends ItemViewBinder<Category3VideoColumn, Category3VideoColumnViewBinder.ViewHolder> {

    private static final String TAG = Category3VideoColumnViewBinder.class.getSimpleName();
    private String mFromTagName;

    @NonNull
    @Override
    protected Category3VideoColumnViewBinder.ViewHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        View view = inflater.inflate(R.layout.category_3_video_column_item_view, parent, false);
        return new Category3VideoColumnViewBinder.ViewHolder(view, mFromTagName);
    }

    @Override
    protected void onBindViewHolder(@NonNull Category3VideoColumnViewBinder.ViewHolder holder, @NonNull Category3VideoColumn post) {
        holder.setData(post);
    }

    public void setFromTagName(String fromTagName) {
        mFromTagName = fromTagName;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        List<LongVideoListModel> videoList;
        Context context;
        String fromTagName;
        Category3VideoColumnChildView columnView0;
        Category3VideoColumnChildView columnView1;
        Category3VideoColumnChildView columnView2;

        ViewHolder(@NonNull View itemView, String fromTagName) {
            super(itemView);
            context = itemView.getContext();
            this.fromTagName = fromTagName;
            columnView0 = (Category3VideoColumnChildView) itemView.findViewById(R.id.category_3_video_column_item_column_0);
            columnView1 = (Category3VideoColumnChildView) itemView.findViewById(R.id.category_3_video_column_item_column_1);
            columnView2 = (Category3VideoColumnChildView) itemView.findViewById(R.id.category_3_video_column_item_column_2);
        }


        void setData(@NonNull final Category3VideoColumn video2Column) {
            videoList = video2Column.videoList;

            if(videoList.size() > 0) {
                columnView0.setVisibility(View.VISIBLE);
                columnView0.setData(videoList.get(0));
                columnView0.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            UIHelper.startActivityByURL(context, videoList.get(0).router);    //路由器转发链接
                            submitUmengData();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                // 让第2 3不可见
                columnView0.setVisibility(View.GONE);
                columnView1.setVisibility(View.GONE);
                columnView2.setVisibility(View.GONE);
            }

            if(videoList.size() > 1){
                columnView1.setVisibility(View.VISIBLE);
                columnView1.setData(videoList.get(1));
                columnView1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            UIHelper.startActivityByURL(context, videoList.get(1).router);    //路由器转发链接
                            submitUmengData();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                columnView1.setVisibility(View.GONE);
                columnView2.setVisibility(View.GONE);
            }

            if(videoList.size() > 2){
                columnView2.setVisibility(View.VISIBLE);
                columnView2.setData(videoList.get(2));
                columnView2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            UIHelper.startActivityByURL(context, videoList.get(2).router); //路由器转发链接
                            submitUmengData();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }else {
                columnView2.setVisibility(View.GONE);
            }
        }

        //友盟统计
        private void submitUmengData() {
            Map<String, String> map = new HashMap<>();
            map.put("from_tag_name", fromTagName);
            ReportUtil.reportEventToThird(CLICK_CATEGORY_FILTER_RESULT,map);
//            MobclickAgent.onEvent(context, CLICK_CATEGORY_FILTER_RESULT, map);
        }
    }
}
