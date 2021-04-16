package com.coocaa.tvpi.module.movie.adapter.holder;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.publib.base.GlideApp;
import com.coocaa.publib.data.category.CategoryMainModel;
import com.coocaa.tvpi.module.login.UserInfoCenter;
import com.coocaa.publib.utils.UIHelper;
import com.coocaa.tvpi.util.ReportUtil;
import com.coocaa.tvpilib.R;

import java.util.HashMap;
import java.util.Map;

import static com.coocaa.tvpi.common.UMEventId.CLICK_CATEGORY_ITEM;

public class Category2RowHolder extends RecyclerView.ViewHolder {
    private static final String TAG = Category2RowHolder.class.getSimpleName();

    private Context context;

    private View layout;
    private TextView title;
    private ImageView imageView;
    private TextView title1;
    private ImageView imageView1;
    private View layout1;

    public Category2RowHolder(final View itemView) {
        super(itemView);
        context = itemView.getContext();
        layout = itemView.findViewById(R.id.layout);
        title = itemView.findViewById(R.id.title_tv);
        imageView = itemView.findViewById(R.id.poster_iv);
        title1 = itemView.findViewById(R.id.title_tv1);
        imageView1 = itemView.findViewById(R.id.poster_iv1);
        layout1 = itemView.findViewById(R.id.layout1);
    }

    public void onBind(final CategoryMainModel categoryMainModel, final CategoryMainModel categoryMainModel1) {
        try {
            GlideApp.with(context)
                    .load(categoryMainModel.classify_pic)
                    .centerCrop()
                    .into(imageView);
            title.setText(categoryMainModel.classify_name);
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        UIHelper.startActivityByURL(context, categoryMainModel.router);    //路由器转发链接

                        // 友盟统计
                        Map<String, String> map = new HashMap<>();
                        map.put("source", TextUtils.isEmpty(UserInfoCenter.getInstance().getTvSource())? "defaultSource": UserInfoCenter.getInstance().getTvSource());
                        map.put("video_class", categoryMainModel.classify_name);
                        ReportUtil.reportEventToThird(CLICK_CATEGORY_ITEM,map);
//                        MobclickAgent.onEvent(MyApplication.getContext(), CLICK_CATEGORY_ITEM, map);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            if (null == categoryMainModel1) {
                GlideApp.with(context)
                        .load(R.color.transparent)
                        .centerCrop()
                        .into(imageView1);
                title1.setText("");
                return;
            }
            GlideApp.with(context)
                    .load(categoryMainModel1.classify_pic)
                    .centerCrop()
                    .into(imageView1);
            title1.setText(categoryMainModel1.classify_name);
            layout1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        UIHelper.startActivityByURL(context, categoryMainModel1.router);    //路由器转发链接

                        // 友盟统计
                        Map<String, String> map = new HashMap<>();
                        map.put("source", TextUtils.isEmpty(UserInfoCenter.getInstance().getTvSource())? "defaultSource": UserInfoCenter.getInstance().getTvSource());
                        map.put("video_class", categoryMainModel1.classify_name);
                        ReportUtil.reportEventToThird(CLICK_CATEGORY_ITEM,map);
//                        MobclickAgent.onEvent(MyApplication.getContext(), CLICK_CATEGORY_ITEM, map);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onBind(final CategoryMainModel categoryMainModel) {
        try {
            GlideApp.with(context)
                    .load(categoryMainModel.classify_pic)
                    .centerCrop()
                    .into(imageView);
            title.setText(categoryMainModel.classify_name);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        UIHelper.startActivityByURL(context, categoryMainModel.router);    //路由器转发链接

                        // 友盟统计
                        Map<String, String> map = new HashMap<>();
                        map.put("source", TextUtils.isEmpty(UserInfoCenter.getInstance().getTvSource())? "defaultSource": UserInfoCenter.getInstance().getTvSource());
                        map.put("video_class", categoryMainModel.classify_name);
                        ReportUtil.reportEventToThird(CLICK_CATEGORY_ITEM,map);
//                        MobclickAgent.onEvent(MyApplication.getContext(), CLICK_CATEGORY_ITEM, map);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
