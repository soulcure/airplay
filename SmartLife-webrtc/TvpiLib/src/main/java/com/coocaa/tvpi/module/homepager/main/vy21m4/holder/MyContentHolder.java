package com.coocaa.tvpi.module.homepager.main.vy21m4.holder;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.smartscreen.data.function.FunctionBean;
import com.coocaa.smartscreen.data.function.homepage.SSHomePageBlock;
import com.coocaa.tvpi.module.homepager.main.vy21m4.adapter.MyContentAdapter;
import com.coocaa.tvpi.module.homepager.main.vy21m4.beans.MyContentData;
import com.coocaa.tvpi.view.decoration.CommonGridItemDecoration;
import com.coocaa.tvpilib.R;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @ClassName MyContentHolder
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 4/7/21
 * @Version TODO (write something)
 */
public class MyContentHolder extends RecyclerView.ViewHolder {

    private View itemView;
    private RecyclerView recyclerView;
    private MyContentAdapter adapter;

    private Context context;

    private List<FunctionBean> mDataList = new ArrayList<>();

    public MyContentHolder(@NonNull View itemView) {
        super(itemView);
        this.itemView = itemView;
        this.context = itemView.getContext();

        recyclerView = itemView.findViewById(R.id.my_content_recyclerview);
        recyclerView.setLayoutManager(new GridLayoutManager(context, 4));

        int screenW = DimensUtils.getDeviceWidth(context);
        //41 * 2 + 50 * 4
        int spaceH = (screenW - DimensUtils.dp2Px(context, 282f)) / 3;
        int spaceV =DimensUtils.dp2Px(context, 9f);

        recyclerView.addItemDecoration(
                new CommonGridItemDecoration(4, spaceH, spaceV)
        );
        adapter = new MyContentAdapter(context);
        recyclerView.setAdapter(adapter);
    }

    public void onBind(List<FunctionBean> functionBeanList) {
        mDataList.clear();
        mDataList.addAll(functionBeanList);
        adapter.addAll(mDataList);
    }

    private void initDataList() {
//        mDataList.add(new MyContentData(R.drawable.icon_my_content_doc, "文档", 0));
//        mDataList.add(new MyContentData(R.drawable.icon_my_content_img, "图片", 0));
//        mDataList.add(new MyContentData(R.drawable.icon_my_content_video, "视频", 0));
//        mDataList.add(new MyContentData(R.drawable.icon_my_content_atmosphere, "气氛", 0));
//        mDataList.add(new MyContentData(R.drawable.icon_my_content_link, "链接", 0));
    }
}