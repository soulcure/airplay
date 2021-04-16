package com.coocaa.tvpi.module.movie.adapter.holder;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.publib.data.category.HomeRecommend;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.publib.utils.UIHelper;
import com.coocaa.smartscreen.data.movie.LongVideoListModel;
import com.coocaa.tvpi.module.movie.adapter.LongVideoAdapter;
import com.coocaa.tvpi.util.ReportUtil;
import com.coocaa.tvpi.view.decoration.CommonHorizontalItemDecoration;
import com.coocaa.tvpilib.R;

import java.util.HashMap;
import java.util.Map;

import static com.coocaa.tvpi.common.UMEventId.CLICK_CATEGORY_FILTER_RESULT;

public class RecommendHolder extends RecyclerView.ViewHolder {
    private static final String TAG = RecommendHolder.class.getSimpleName();

    private Context context;

    private TextView title;
    private View more;
    private RecyclerView recyclerView;
    private LongVideoAdapter adapter;

    public RecommendHolder(final View itemView) {
        super(itemView);
        context = itemView.getContext();
        title = itemView.findViewById(R.id.recommend_holder_title);
        more = itemView.findViewById(R.id.recommend_holder_more);

        recyclerView = itemView.findViewById(R.id.recommend_holder_recyclerview);
        recyclerView.setHasFixedSize(true);
        CommonHorizontalItemDecoration decoration = new CommonHorizontalItemDecoration(DimensUtils.dp2Px(context,20f), DimensUtils.dp2Px(context,10f));
        recyclerView.addItemDecoration(decoration);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    public void onBind(final HomeRecommend homeRecommend) {
        if (null != homeRecommend
                && null != homeRecommend.video_list
                && homeRecommend.video_list.size() > 0) {
            title.setText(homeRecommend.title);
            more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UIHelper.startActivityByURL(context, homeRecommend.router);
                }
            });

            adapter = new LongVideoAdapter(context);
            adapter.setOnItemClickListener(new LongVideoAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position, LongVideoListModel data) {
                    if (null != data) {
                        UIHelper.startActivityByURL(context, data.router);

                        Map<String, String> map = new HashMap<>();
                        map.put("from_tag_name", "首页_"+ homeRecommend.title);
                        ReportUtil.reportEventToThird(CLICK_CATEGORY_FILTER_RESULT,map);
//                        MobclickAgent.onEvent(context, CLICK_CATEGORY_FILTER_RESULT, map);
                        Log.d(TAG, "submitUmengData: map ：" + map.toString());
                    }
                }
            });

            recyclerView.setAdapter(adapter);
            adapter.addAll(homeRecommend.video_list);
        }
    }

}
