package com.coocaa.swaiotos.virtualinput.module.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cocaa.swaiotos.virtualinput.R;
import com.coocaa.swaiotos.virtualinput.data.RemoteSubtitleBean;
import com.coocaa.swaiotos.virtualinput.utils.DimensUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @ClassName RemoteControlAdapter
 * @Description TODO (write something)
 * @User heni
 * @Date 2020/12/16
 */
public class RemoteControlAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private String TAG = RemoteControlAdapter.class.getSimpleName();
    private List<RemoteSubtitleBean> dataList = new ArrayList<>();
    private Map<Integer, Boolean> stateMap = new HashMap<>(); //存放选中状态
    private int selectPosition = 0;
    private OnSubTitleClickListener mListener;

    public interface OnSubTitleClickListener {
        void onSubTitleClick(int position);
    }

    public void setOnSubTitleClickListener(OnSubTitleClickListener listener) {
        this.mListener = listener;
    }

    public void addAll(List<RemoteSubtitleBean> subtitleBeans) {
        if (subtitleBeans != null && subtitleBeans.size() > 0) {
            dataList.clear();
            dataList.addAll(subtitleBeans);
            notifyDataSetChanged();

            stateMap.put(0, true);
            for (int i = 1; i < subtitleBeans.size(); i++) {
                stateMap.put(i, false);
            }
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.remote_control_subtitle_item, parent, false);
        return new SubtitleHodler(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        RemoteSubtitleBean subtitleBean = dataList.get(position);
        if (subtitleBean == null) {
            return;
        }
        ((SubtitleHodler) holder).onBind(subtitleBean, position);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public class SubtitleHodler extends RecyclerView.ViewHolder {
        private Context mContext;
        private ImageView img;
        private TextView title;

        public SubtitleHodler(@NonNull View itemView) {
            super(itemView);
            mContext = itemView.getContext();
            img = itemView.findViewById(R.id.img_subtitle);
            title = itemView.findViewById(R.id.tv_subtitle);

            int screenWidth = DimensUtils.getDeviceWidth(mContext);
            ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
            layoutParams.width = screenWidth / 5;
            itemView.setLayoutParams(layoutParams);
        }

        public void onBind(final RemoteSubtitleBean data, final int position) {
            if (data != null) {
                Log.d(TAG, "onBind: " + position);
                title.setText(data.title);
                title.setTextColor(mContext.getResources().getColor(stateMap.get(position) ?
                        R.color.white : R.color.color_white_60));
                img.setImageResource(stateMap.get(position) ? data.idSelectState :
                        data.idNormalState);

                img.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(TAG, "onClick: " + position);
                        if (mListener != null) {
                            mListener.onSubTitleClick(position);
                        }

                        stateMap.put(position, true);
                        title.setTextColor(mContext.getResources().getColor(R.color.white));
                        img.setImageResource(data.idSelectState);
                        if (selectPosition >= 0 && selectPosition != position) {
                            stateMap.put(selectPosition, false);
                            notifyItemChanged(selectPosition);
                            selectPosition = position;
                        }
                    }
                });
            }
        }
    }
}
