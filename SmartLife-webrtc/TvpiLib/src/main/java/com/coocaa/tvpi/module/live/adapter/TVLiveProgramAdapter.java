package com.coocaa.tvpi.module.live.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.publib.data.tvlive.TVLiveChannelListData;
import com.coocaa.publib.data.tvlive.TVLiveChannelsData;
import com.coocaa.publib.utils.SpUtil;
import com.coocaa.tvpi.module.live.TVLiveFragment;
import com.coocaa.tvpi.module.live.adapter.holder.TVLiveProgramHolder;
import com.coocaa.tvpi.module.live.listener.ItemTouchMoveListener;
import com.coocaa.tvpilib.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @ClassName TVLiveProgramAdapter
 * @Description TODO (write something)
 * @User heni
 * @Date 2019/1/10
 */
public class TVLiveProgramAdapter extends RecyclerView.Adapter implements ItemTouchMoveListener,
        TVLiveProgramHolder.OnLiveProgramItemClickListener {

    private static final String TAG = TVLiveProgramAdapter.class.getSimpleName();
    private static final int TYPE_CHANNEL_ITEM = 1;
    private static final int TYPE_EDIT_BTN_ITEM = 2;

    private Context mContext;
    private TVLiveChannelListData mChannelListData;
    private List<TVLiveChannelsData> mChannelsDatas;
    private boolean isInEditState;
    private boolean isClickBtnState;
    private OnDeleteAllItemListener mDeleteAllItemListener;
    private String networkForceKey;

    public interface OnDeleteAllItemListener {
        void onDeleteAllItem();
    }

    public TVLiveProgramAdapter(Context context, OnDeleteAllItemListener listener) {
        this.mContext = context;
        mDeleteAllItemListener = listener;
        mChannelsDatas = new ArrayList<>();
    }

    public void addAll(TVLiveChannelListData channelListData) {
        if (channelListData != null) {
            this.mChannelListData = channelListData;
            mChannelsDatas = channelListData.channels;
        }
        isInEditState = false;
        isClickBtnState = false;
        notifyDataSetChanged();
    }

    public void setNetworkForceKey(String networkForceKey) {
        this.networkForceKey = networkForceKey;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = null;
        if (viewType == TYPE_CHANNEL_ITEM) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_tvlive_program,
                    parent, false);
            holder = new TVLiveProgramHolder(view, this);
        } else if (viewType == TYPE_EDIT_BTN_ITEM) {
            View view = LayoutInflater.from(mContext).inflate(R.layout
                    .item_tvlive_program_collect_editbtn, parent, false);
            holder = new EditBtnViewHolder(view);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_CHANNEL_ITEM) {
            if (mChannelsDatas != null && mChannelsDatas.size() > 0) {
                if (holder instanceof TVLiveProgramHolder) {
                    ((TVLiveProgramHolder) holder).onBind(isClickBtnState, isInEditState,
                            mChannelListData.channels_class, mChannelsDatas.get(position),networkForceKey);
                }
            }
        } else if (getItemViewType(position) == TYPE_EDIT_BTN_ITEM) {
            if (holder instanceof EditBtnViewHolder) {
                if(!isInEditState) {
                    ((EditBtnViewHolder) holder).mTextView.setText("编辑");
                }
                ((EditBtnViewHolder) holder).mTextView.setOnClickListener(new View
                        .OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        TextView textView = (TextView) view;
                        if (textView.getText().equals("编辑")) {
                            textView.setText("完成");
                            isInEditState = true;
                        } else {
                            textView.setText("编辑");
                            isInEditState = false;
                        }
                        isClickBtnState = true;
                        notifyDataSetChanged();
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return mChannelsDatas == null ? 0 : mChannelsDatas.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (mChannelListData != null) {
            if (TVLiveFragment.MY_LOCAL_COLLECT.equals(mChannelListData.channels_class) &&
                    position == mChannelsDatas.size() - 1) {
                return TYPE_EDIT_BTN_ITEM;
            }
        }
        return TYPE_CHANNEL_ITEM;
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        //1.数据交换 2.刷新
        Collections.swap(mChannelsDatas, fromPosition, toPosition);
        writeProgramDataToSP();
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public boolean onItemRemove(int position) {
        mChannelsDatas.remove(position);
        notifyItemRemoved(position);
        return true;
    }

    @Override
    public void onLiveProgramItemClick(int position) {
        mChannelsDatas.remove(position);
        notifyItemRemoved(position);
        // 显示提示图，隐藏编辑btn
        if(mChannelsDatas.size() == 1) {
            mChannelsDatas.remove(0);
            notifyItemRemoved(0);
            mDeleteAllItemListener.onDeleteAllItem();
        }
    }

    class EditBtnViewHolder extends RecyclerView.ViewHolder {

        public TextView mTextView;

        public EditBtnViewHolder(View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.tvlive_program_collect_editbtn);
        }
    }

    /**
     * 交换数据后重新写数据到sp存储
     */
    private void writeProgramDataToSP() {
        List<TVLiveChannelsData> tvLiveChannelsDataList = new ArrayList<>();
        SpUtil.putList(mContext, SpUtil.Keys.TVLIVE_COLLECT_PROGRAMS, tvLiveChannelsDataList);

        tvLiveChannelsDataList.addAll(mChannelsDatas);
        tvLiveChannelsDataList.remove(mChannelsDatas.size()-1);
        SpUtil.putList(mContext, SpUtil.Keys.TVLIVE_COLLECT_PROGRAMS, tvLiveChannelsDataList);

        //test
        /*Log.d(TAG, "swap after: writeProgramDataToSP: sp size: " + tvLiveChannelsDataList.size());
        for (TVLiveChannelsData data : tvLiveChannelsDataList) {
            Log.d(TAG, "writeProgramDataToSP: " + data.channel_name + ", " + data.channel_poster);
        }*/
    }
}
