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

public class Category1RowHolder extends RecyclerView.ViewHolder {
    private static final String TAG = Category1RowHolder.class.getSimpleName();

    private Context context;

    private View itemView;
    private TextView title;
    private ImageView imageView;

    public Category1RowHolder(final View itemView) {
        super(itemView);
        context = itemView.getContext();
        this.itemView = itemView;
        title = itemView.findViewById(R.id.title_tv);
        imageView = itemView.findViewById(R.id.poster_iv);
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
