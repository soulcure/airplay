package com.coocaa.tvpi.module.homepager.main.vy21m4.holder;

import android.content.Context;
import android.view.View;

import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.smartscreen.data.function.FunctionBean;
import com.coocaa.smartscreen.data.function.homepage.SSHomePageBlock;
import com.coocaa.tvpi.module.homepager.main.vy21m4.adapter.FuncAdapter;
import com.coocaa.tvpi.module.homepager.main.vy21m4.adapter.MyContentAdapter;
import com.coocaa.tvpi.module.homepager.main.vy21m4.adapter.RecentAdapter;
import com.coocaa.tvpi.module.homepager.main.vy21m4.beans.FuncData;
import com.coocaa.tvpi.module.homepager.main.vy21m4.beans.MyContentData;
import com.coocaa.tvpi.view.decoration.CommonGridItemDecoration;
import com.coocaa.tvpilib.R;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @ClassName FuncHolder
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 4/7/21
 * @Version TODO (write something)
 */
public class FuncHolder extends RecyclerView.ViewHolder {

    private View itemView;
    private RecyclerView recyclerView;
    private FuncAdapter adapter;

    private Context context;
    List<FunctionBean> mDataList = new ArrayList<>();

    public FuncHolder(@NonNull View itemView) {
        super(itemView);
        this.itemView = itemView;
        this.context = itemView.getContext();

        recyclerView = itemView.findViewById(R.id.func_recyclerview);
        recyclerView.setLayoutManager(new GridLayoutManager(context, 2));

        int spaceH = DimensUtils.dp2Px(context, 15f);
        int spaceV =DimensUtils.dp2Px(context, 16f);

        recyclerView.addItemDecoration(
                new CommonGridItemDecoration(2, spaceH, spaceV)
        );
        adapter = new FuncAdapter(context);
        recyclerView.setAdapter(adapter);
    }

    public void onBind(List<FunctionBean> functionBeanList) {
        mDataList.clear();
        mDataList.addAll(functionBeanList);
        adapter.addAll(mDataList);
    }

    private void initDataList() {
//        mDataList.add(new FuncData(R.drawable.icon_func_whiteboard, "????????????", "?????????????????????"));
//        mDataList.add(new FuncData(R.drawable.icon_func_notemark, "????????????", "?????????????????????"));
//        mDataList.add(new FuncData(R.drawable.icon_func_pc_mirror, "????????????", "????????????????????????"));
//        mDataList.add(new FuncData(R.drawable.icon_func_phone_mirror, "????????????", "????????????????????????"));
    }


}