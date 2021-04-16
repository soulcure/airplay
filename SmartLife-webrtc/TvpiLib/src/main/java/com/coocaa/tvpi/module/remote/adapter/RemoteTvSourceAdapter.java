package com.coocaa.tvpi.module.remote.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartscreen.data.device.Source;
import com.coocaa.tvpilib.R;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

/**
 * @ClassName RemoteTvSourceAdapter
 * @Description 遥控器更多页面，信号源列表适配器
 * @User heni
 * @Date 18-8-8
 */
public class RemoteTvSourceAdapter extends RecyclerView.Adapter<RemoteTvSourceAdapter.ViewHolder>{

    private static final String TAG = RemoteTvSourceAdapter.class.getSimpleName();

    private Context context;
    private List<Source> dataList;
    private String currentSource;
    private int selectedPosition = -1;
    private ViewHolder curHolder;

    public RemoteTvSourceAdapter(Context context) {
        this.context = context;
        this.dataList = new ArrayList<>();
    }

    public void addAll(List<Source> dataLists, String currentSource) {
        this.currentSource = currentSource;
        dataList.clear();
        dataList.addAll(dataLists);
        notifyDataSetChanged();

        for(int i=0; i<dataLists.size(); i++) {
            if(currentSource.equals(dataLists.get(i))) {
                selectedPosition = i;
            }
        }
    }

    public int getSelectedPosition () {
        return selectedPosition;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.remote_more_tvsource_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
       holder.onBind(position);
    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvSource;
        public ImageView imgFlag;

        ViewHolder(View view) {
            super(view);
            tvSource = view.findViewById(R.id.remote_more_tvsource_item);
            imgFlag = view.findViewById(R.id.remote_more_flag_item);
            imgFlag.setVisibility(View.GONE);
        }

        public void onBind(final int position) {
            String data = dataList.get(position).name;
            if(TextUtils.isEmpty(data))
                return;

            tvSource.setText(data);
            if(data.equals(currentSource)) {
                selectedPosition = position;
            }
            if(position != selectedPosition) {
                tvSource.setTextColor(context.getResources().getColor(R.color.c_4));
                tvSource.setSelected(false);
                // imgFlag.setVisibility(View.GONE);
            }else{
                tvSource.setTextColor(context.getResources().getColor(R.color.c_7));
                tvSource.setSelected(true);
                // imgFlag.setVisibility(View.VISIBLE);
                curHolder = this;
            }

            tvSource.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*if(position == selectedPosition) {
                        return;
                    }*/
                    notifyItemChanged(selectedPosition);

                    String sourceText = ((TextView)v).getText().toString();
                    if(!TextUtils.isEmpty(sourceText)) {
                        selectedPosition  = position;
                        currentSource = sourceText;
                        notifyItemChanged(position);
                        ToastUtils.getInstance().showGlobalShort("切换信号源为：" + sourceText);
                       /* DeviceControllerManager.getInstance().setTVSource(sourceText, new IDeviceConnectListener.GetTvSourceListener() {
                            @Override
                            public void onSourceInfo(String s) {
                                Log.d(TAG, "onSourceInfo: s:"+s);
                            }

                            @Override
                            public void onSourceInfoError(Exception e) {
                                e.printStackTrace();
                            }
                        });*/
                    }
                }
            });
        }
    }
}
