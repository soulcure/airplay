package com.coocaa.tvpi.module.homepager.main.vy21m4.holder;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.coocaa.publib.data.local.DocumentData;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.tvpi.module.homepager.main.vy21m4.adapter.RecentAdapter;
import com.coocaa.tvpi.module.local.document.DocumentDataApi;
import com.coocaa.tvpi.view.decoration.CommonVerticalItemDecoration;
import com.coocaa.tvpilib.R;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @ClassName MyContentHolder
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 4/7/21
 * @Version TODO (write something)
 */
public class RecentHolder extends RecyclerView.ViewHolder {

    private View itemView;
    private RecyclerView recyclerView;
    private RecentAdapter adapter;

    private Context context;
    List<DocumentData> mDataList = new ArrayList<>();

    public RecentHolder(@NonNull View itemView) {
        super(itemView);

        this.itemView = itemView;
        this.context = itemView.getContext();

        recyclerView = itemView.findViewById(R.id.recent_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        int spaceV =DimensUtils.dp2Px(context, 9f);

        recyclerView.addItemDecoration(
                new CommonVerticalItemDecoration(0, spaceV)
        );
        adapter = new RecentAdapter(context);
        recyclerView.setAdapter(adapter);
    }

    public void onBind() {
        initDataList();
        adapter.addAll(mDataList);
    }

    private void initDataList() {
        mDataList = DocumentDataApi.getRecordList(context);
        //for test
//        mDataList.add(new DocumentData());
//        mDataList.add(new DocumentData());
    }


}