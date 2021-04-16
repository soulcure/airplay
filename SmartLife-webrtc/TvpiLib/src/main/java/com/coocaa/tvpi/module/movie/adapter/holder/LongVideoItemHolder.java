package com.coocaa.tvpi.module.movie.adapter.holder;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.publib.PublibHelper;
import com.coocaa.publib.utils.UIHelper;
import com.coocaa.tvpi.module.movie.widget.Relate3Column;
import com.coocaa.tvpi.module.movie.widget.Relate3ColumnChildView;
import com.coocaa.tvpilib.R;
import com.umeng.analytics.MobclickAgent;

import static com.coocaa.tvpi.common.UMEventId.CLICK_LONG_DETAIL_RELATED_VIDEO;

/**
 * Created by WHY on 2018/4/12.
 */

public class LongVideoItemHolder extends RecyclerView.ViewHolder {

    private static final String TAG = LongVideoItemHolder.class.getSimpleName();

    Context mContext;
    Relate3ColumnChildView columnView0;
    Relate3ColumnChildView columnView1;
    Relate3ColumnChildView columnView2;

    public LongVideoItemHolder(View itemView) {
        super(itemView);
        mContext = itemView.getContext();
        columnView0 = itemView.findViewById(R.id.relate_3_column_item_column_0);
        columnView1 = itemView.findViewById(R.id.relate_3_column_item_column_1);
        columnView2 = itemView.findViewById(R.id.relate_3_column_item_column_2);
    }


    public void onBind(final Relate3Column data) {
        if (data.videoList.size() > 0) {
            columnView0.setData(data.videoList.get(0));
            columnView0.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        UIHelper.startActivityByURL(mContext, data.videoList.get(0).router);    //路由器转发链接
                        //友盟统计
                        MobclickAgent.onEvent(PublibHelper.getContext(), CLICK_LONG_DETAIL_RELATED_VIDEO);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        if (data.videoList.size() > 1) {
            columnView1.setData(data.videoList.get(1));
            columnView1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        UIHelper.startActivityByURL(mContext, data.videoList.get(1).router);    //路由器转发链接
                        //友盟统计
                        MobclickAgent.onEvent(PublibHelper.getContext(), CLICK_LONG_DETAIL_RELATED_VIDEO);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        if (data.videoList.size() > 2) {
            columnView2.setData(data.videoList.get(2));
            columnView2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        UIHelper.startActivityByURL(mContext, data.videoList.get(2).router);    //路由器转发链接
                        //友盟统计
                        MobclickAgent.onEvent(PublibHelper.getContext(), CLICK_LONG_DETAIL_RELATED_VIDEO);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
