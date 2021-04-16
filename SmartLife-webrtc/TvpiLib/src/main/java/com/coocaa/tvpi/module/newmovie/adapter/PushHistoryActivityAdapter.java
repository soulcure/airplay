package com.coocaa.tvpi.module.newmovie.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.publib.PublibHelper;
import com.coocaa.smartscreen.data.movie.PushHistoryModel;
import com.coocaa.tvpi.common.UMEventId;
import com.coocaa.tvpi.module.newmovie.adapter.holder.PushHistoryItemHolder;
import com.coocaa.tvpi.module.newmovie.adapter.holder.PushHistoryTitleHolder;
import com.coocaa.tvpilib.R;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName PushHistoryActivityAdapter
 * @Description
 * @User heni
 * @Date 18-8-31
 */
public class PushHistoryActivityAdapter extends RecyclerView.Adapter {

    private static final String TAG = PushHistoryActivityAdapter.class.getSimpleName();

    private static final int TYPE_VIDEO_DETAIL_INFO = 1;
    private static final int TYPE_VIDEO_TITLE_TIPS = 2;

    private Context context;
    private List<Object> dataList;
    private List<PushHistoryModel.PushHistoryVideoModel> withServenDataList;
    private List<PushHistoryModel.PushHistoryVideoModel> overServenDataList;
    private boolean isEditMode;

    public PushHistoryActivityAdapter(Context context) {
        this.context = context;
        this.dataList = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        RecyclerView.ViewHolder holder = null;
        if(viewType == TYPE_VIDEO_TITLE_TIPS) {
            view = LayoutInflater.from(context).inflate(R.layout.item_push_history_title_view, parent, false);
            holder = new PushHistoryTitleHolder(view);
        }else if(viewType == TYPE_VIDEO_DETAIL_INFO) {
            view = LayoutInflater.from(context).inflate(R.layout.item_push_history, parent, false);
            holder = new PushHistoryItemHolder(view);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if(getItemViewType(position) == TYPE_VIDEO_DETAIL_INFO) {
            PushHistoryItemHolder itemHolder = (PushHistoryItemHolder) holder;
            itemHolder.setHistoryData((PushHistoryModel.PushHistoryVideoModel) dataList.get(position));
            itemHolder.setMode(isEditMode);
            itemHolder.setOnItemClickSelectListener(new PushHistoryItemHolder.OnItemClickSelectListener() {
                @Override
                public void onEditModeClickSelect(PushHistoryModel.PushHistoryVideoModel mHistoryData) {
                    if (mListener != null) {
                        submitUMData(position);
                        mListener.onEditModeClickItemSelect(mHistoryData);
                    }
                }
            });
        }else if(getItemViewType(position)  == TYPE_VIDEO_TITLE_TIPS) {
            ((PushHistoryTitleHolder) holder).onBind((String) dataList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    @Override
    public int getItemViewType(int position) {
        try {
            Object item = dataList.get(position);
            if (item instanceof PushHistoryModel.PushHistoryVideoModel) {
                return TYPE_VIDEO_DETAIL_INFO;
            } else if (item instanceof String) {
                return TYPE_VIDEO_TITLE_TIPS;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.getItemViewType(position);
    }

    public void addWithinServenData(List<PushHistoryModel.PushHistoryVideoModel> withinServenDatas) {
        if(withinServenDatas != null && withinServenDatas.size() > 0) {
            withServenDataList = withinServenDatas;
            dataList.clear();
            dataList.add("7天以内");
            dataList.addAll(withServenDataList);
            notifyDataSetChanged();
        }
    }

    public void addOverServenData(List<PushHistoryModel.PushHistoryVideoModel> overServenDatas) {
        if(overServenDatas != null && overServenDatas.size() > 0) {
            overServenDataList = overServenDatas;
            dataList.add("更早");
            dataList.addAll(overServenDataList);
            notifyDataSetChanged();
        }
    }

    public void setMode (boolean isEditMode) {
        this.isEditMode = isEditMode;
        if(dataList != null) {
            for(int i=0; i<dataList.size(); i++) {
                if(dataList.get(i) instanceof PushHistoryModel.PushHistoryVideoModel) {
                    ((PushHistoryModel.PushHistoryVideoModel) dataList.get(i)).isInEditMode = isEditMode;
                    ((PushHistoryModel.PushHistoryVideoModel) dataList.get(i)).isSelected = false;
                }
            }
            notifyDataSetChanged();
        }
    }

//    public VideoInfo getItemAtIndex(int index) {
//        if(index < mDatas.size()) {
//            return mDatas.get(index);
//        } else {
//            return null;
//        }
//    }
//
//    public void updateEditModeSelectStatus(int index) {
//        if(isEditMode && index < mDatas.size()) {
//            VideoInfo temp = mDatas.get(index);
//            temp.isSelected = !temp.isSelected;
//
//            notifyDataSetChanged();
//        }
//    }

    // 可能是全选也可能是全反选
    public void updateEditModeSelectAllStatus() {
        if(dataList != null && isEditMode) {
            for(int i=0; i<dataList.size(); i++) {
                if(dataList.get(i) instanceof PushHistoryModel.PushHistoryVideoModel) {
                    if(((PushHistoryModel.PushHistoryVideoModel) dataList.get(i)).isSelected == false)
                        ((PushHistoryModel.PushHistoryVideoModel) dataList.get(i)).isSelected = true;
                }
            }
            notifyDataSetChanged();
        }
    }

    public void updateEditModeCancelSelectAllStatus() {
        if(dataList != null && isEditMode) {
            for(int i=0; i<dataList.size(); i++) {
                if(dataList.get(i) instanceof PushHistoryModel.PushHistoryVideoModel) {
                    if(((PushHistoryModel.PushHistoryVideoModel) dataList.get(i)).isSelected == true)
                        ((PushHistoryModel.PushHistoryVideoModel) dataList.get(i)).isSelected = false;
                }
            }
            notifyDataSetChanged();
        }
    }

    public List<String> getSelectedVideoIdList() {
        List<String> ids = new ArrayList<String>();
        if(dataList != null && isEditMode) {
            for(int i=0; i<dataList.size(); i++) {
                if(dataList.get(i) instanceof PushHistoryModel.PushHistoryVideoModel) {
                    if(((PushHistoryModel.PushHistoryVideoModel) dataList.get(i)).isSelected == true)
                        ids.add(((PushHistoryModel.PushHistoryVideoModel) dataList.get(i)).video_id);
                }
            }
        }
        return ids;
    }

    public void updateDataAfterDeleteSuccess(List<String> idsList) {
        if(idsList.size() == 0) {
            return;
        }

        if(idsList.size() == (dataList.size()-2)){
            clearAll();
            return;
        }

        List<PushHistoryModel.PushHistoryVideoModel> toDeleteList = new ArrayList<>();

        if(withServenDataList != null) {
            for (PushHistoryModel.PushHistoryVideoModel tempModel: withServenDataList) {
                for (String id: idsList) {
                    if(tempModel.video_id.equals(id)) {
                        toDeleteList.add(tempModel);
                    }
                }
            }
        }

        if(overServenDataList != null) {
            for (PushHistoryModel.PushHistoryVideoModel tempModel: overServenDataList) {
                for (String id: idsList) {
                    if(tempModel.video_id.equals(id)) {
                        toDeleteList.add(tempModel);
                    }
                }
            }
        }

        Log.d(TAG, "updateDataAfterDeleteSuccess: " + toDeleteList.toString());
        dataList.removeAll(toDeleteList);
        notifyDataSetChanged();
    }

    private void clearAll() {
        dataList.clear();
        notifyDataSetChanged();
    }

    public interface OnItemClickSelectListener{
        void onEditModeClickItemSelect(PushHistoryModel.PushHistoryVideoModel mHistoryData);
    }

    private OnItemClickSelectListener mListener;

    public void setOnItemClickSelectListener (OnItemClickSelectListener listener) {
        mListener = listener;
    }

    private void submitUMData(int position) {
        Map<String, String> map = new HashMap<>();
        map.put("position", position + "");
        if(withServenDataList != null){
            if(position <= withServenDataList.size()) {
                map.put("period", "7天内");
            }else {
                map.put("period", "更早");
            }
        }else if(overServenDataList != null) {
            map.put("period", "更早");
        }
        MobclickAgent.onEvent(PublibHelper.getContext(), UMEventId.PUSH_HISTORY_CLICK, map);
    }
}
