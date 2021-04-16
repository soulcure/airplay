package com.coocaa.tvpi.module.newmovie.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;


import com.coocaa.smartscreen.data.movie.CollectionModel;
import com.coocaa.tvpi.module.newmovie.widget.CollectItemView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IceStorm on 2017/12/28.
 */

public class CollectListAdapter extends BaseAdapter {
    private static final String TAG = "CollectListAdapter";

    private List<CollectionModel> mDatas;
    private Context mContext;
    private boolean isEditMode;

    public CollectListAdapter(Context context) {
        mContext = context;
        mDatas = new ArrayList<CollectionModel>();
    }

    public void setMode (boolean isEditMode) {
        this.isEditMode = isEditMode;

        for (CollectionModel model: mDatas) {
            model.isInEditMode = isEditMode;
            model.isSelected = false;
        }

        notifyDataSetChanged();
    }

    public CollectionModel getItemAtIndex(int index) {
        if(index < mDatas.size()) {
            return mDatas.get(index);
        } else {
            return null;
        }
    }

    public void setData(List<CollectionModel> datas) {
        mDatas = datas;
        notifyDataSetChanged();
    }

    public void updateEditModeSelectStatus(int index) {
        if(isEditMode && index < mDatas.size()) {
            CollectionModel temp = mDatas.get(index);
            temp.isSelected = !temp.isSelected;

            notifyDataSetChanged();
        }
    }

    // 可能是全选也可能是全反选
    public void updateEditModeSelectAllStatus() {
        if(isEditMode) {
            for (CollectionModel tempModel: mDatas){
                if(!tempModel.isSelected)
                    tempModel.isSelected = true;
            }

            notifyDataSetChanged();
        }
    }

    public void updateEditModeCancelSelectAllStatus() {
        if(isEditMode) {
            for (CollectionModel tempModel: mDatas){
                if(tempModel.isSelected)
                    tempModel.isSelected = false;
            }

            notifyDataSetChanged();
        }
    }

    public void addAll(List<CollectionModel> dataList) {
        mDatas.clear();
        mDatas.addAll(dataList);
        notifyDataSetChanged();
    }

    public void addMore(List<CollectionModel> dataList) {
        mDatas.addAll(dataList);
        notifyDataSetChanged();
    }

    public void clearAll() {
        mDatas.clear();
        notifyDataSetChanged();
    }

    public List<Integer> getSelectedVideoIdList() {
        List<Integer> ids = new ArrayList<Integer>();
        if(isEditMode) {
            for (CollectionModel tempModel: mDatas) {
                if(tempModel.isSelected) {
                    ids.add(new Integer(tempModel.collect_id));
                }
            }
        }

        return ids;
    }

    public void updateDataAfterDeleteSuccess(List<Integer> idsList) {
        if(idsList.size() == 0) {
            return;
        }

        if(idsList.size() == mDatas.size()) {
            clearAll();
            return;
        }

        List<CollectionModel> toDeleteList = new ArrayList<CollectionModel>();
        for (CollectionModel tempModel: mDatas) {
            for (Integer id: idsList) {
                if(tempModel.collect_id == id) {
                    toDeleteList.add(tempModel);
                }
            }
        }

        Log.d(TAG, "updateDataAfterDeleteSuccess: " + toDeleteList.toString());
        mDatas.removeAll(toDeleteList);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (mDatas == null)
            return 0;

        return mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = new CollectItemView(mContext);
        }

        CollectItemView collectItemView = (CollectItemView) convertView;
        collectItemView.setMode(isEditMode);
        collectItemView.setCollectData(mDatas.get(position));
        collectItemView.setOnItemClickSelectListener(new CollectItemView.OnItemClickSelectListener() {
            @Override
            public void onEditModeClickSelect(int dataId) {
                if (mListener != null) {
                    mListener.onEditModeClickItemSelect(dataId);
                }
            }
        });
        return convertView;
    }

    public interface OnItemClickSelectListener{
        void onEditModeClickItemSelect(int dataId);
    }

    private PlayHistoryAdapter.OnItemClickSelectListener mListener;

    public void setOnItemClickSelectListener (PlayHistoryAdapter.OnItemClickSelectListener listener) {
        mListener = listener;
    }


}
