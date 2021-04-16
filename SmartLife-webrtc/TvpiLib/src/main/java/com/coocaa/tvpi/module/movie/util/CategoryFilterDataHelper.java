package com.coocaa.tvpi.module.movie.util;

import com.coocaa.publib.data.category.MultiTypeEnum;
import com.coocaa.smartscreen.data.movie.LongVideoListModel;
import com.coocaa.tvpi.module.movie.widget.Category3VideoColumn;
import com.coocaa.tvpi.module.movie.widget.Category3VideoColumnViewBinder;

import java.util.ArrayList;
import java.util.List;

import me.drakeet.multitype.MultiTypeAdapter;

/**
 * Created by IceStorm on 2017/12/14.
 */

public class CategoryFilterDataHelper {

    private MultiTypeAdapter adapter;
    private List<Object> items;

    public CategoryFilterDataHelper(MultiTypeAdapter adapter) {
        this.adapter = adapter;
        this.adapter.register(Category3VideoColumn.class, new Category3VideoColumnViewBinder());
        items = new ArrayList<>();
    }

    public CategoryFilterDataHelper(MultiTypeAdapter adapter, String fromTagName) {
     this.adapter = adapter;
        Category3VideoColumnViewBinder videoColumnViewBinder = new Category3VideoColumnViewBinder();
        videoColumnViewBinder.setFromTagName(fromTagName);
        this.adapter.register(Category3VideoColumn.class, videoColumnViewBinder);
        items = new ArrayList<>();
    }

    public int getCount() {
        return items.size();
    }

    public void addAll(List<Object> items) {
        this.items.clear();
        List<Object> data = getData(items);
        this.items.addAll(data);
        adapter.setItems(this.items);
        adapter.notifyDataSetChanged();
    }

    public void addMore(List<Object> items) {
        List<Object> data = getData(items);
        this.items.addAll(data);
        adapter.setItems(this.items);
        adapter.notifyDataSetChanged();
    }

    private List<Object> getData(List<Object> items) {
        List<Object> data = new ArrayList<>();

        if(items.size() == 0) {
            return items;
        }

        LongVideoListModel model = (LongVideoListModel)items.get(0);
//        if (model.container_type == MultiTypeEnum.COLUMS_3) {
            //插入3列的数据
            int unit = 3;
            int column3Size = items.size() / unit;

            List<LongVideoListModel> videoList;
            for (int i = 0; i < column3Size; i++) {
                videoList = new ArrayList<>();
                videoList.add((LongVideoListModel) items.get(i*unit));
                videoList.add((LongVideoListModel) items.get(i*unit+1));
                videoList.add((LongVideoListModel) items.get(i*unit+2));

                Category3VideoColumn video3Column = new Category3VideoColumn(videoList);
                data.add(video3Column);
//            }

            // 没有除尽
            if(items.size() % unit != 0) {
                int outCompleteNum = items.size() % unit;

                videoList = new ArrayList<>();

                for(int j=items.size()-outCompleteNum; j<items.size(); j++) {
                    videoList.add((LongVideoListModel) items.get(j));
                }

//                Category3VideoColumn video3Column = new Category3VideoColumn(videoList);
//                data.add(video3Column);
            }
        }

        return data;
    }
}
