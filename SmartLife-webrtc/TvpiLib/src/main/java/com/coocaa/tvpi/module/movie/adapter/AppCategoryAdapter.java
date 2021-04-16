package com.coocaa.tvpi.module.movie.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.publib.PublibHelper;
import com.coocaa.publib.base.GlideApp;
import com.coocaa.publib.data.category.CategoryMainModel;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.publib.utils.UIHelper;
import com.coocaa.tvpilib.R;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.coocaa.tvpi.common.UMEventId.CLICK_APP_CATEGORY_ITEM;

public class AppCategoryAdapter extends RecyclerView.Adapter <AppCategoryAdapter.ViewHolder> {

    private static final String TAG = AppCategoryAdapter.class.getSimpleName();

    private Context context;
    private List<CategoryMainModel> dataList;

    public AppCategoryAdapter(Context context) {
        this.context = context;
        this.dataList = new ArrayList<>();
    }

    public void addAll(List<CategoryMainModel> categoryMainModelList) {
        dataList.clear();
        dataList.addAll(categoryMainModelList);
        notifyDataSetChanged();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_category_item_layout, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        int width = (DimensUtils.getDeviceWidth(context) - DimensUtils.dp2Px(context, 60f)) / 3;
        layoutParams.width = width;
        layoutParams.height = width * 46 / 105;
        view.setLayoutParams(layoutParams);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final CategoryMainModel categoryMainModel = dataList.get(position);
        GlideApp.with(context)
                .load(categoryMainModel.classify_pic)
                .centerCrop()
                .into(holder.imgPoster);
        holder.imgPoster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIHelper.startActivityByURL(context, categoryMainModel.router);

                // 友盟统计
                Map<String, String> map = new HashMap<>();
                map.put("classify_name", categoryMainModel.classify_name);
                MobclickAgent.onEvent(PublibHelper.getContext(), CLICK_APP_CATEGORY_ITEM, map);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imgPoster;

        ViewHolder(View view) {
            super(view);
            imgPoster = view.findViewById(R.id.app_category_item_iv);
        }
    }

}
