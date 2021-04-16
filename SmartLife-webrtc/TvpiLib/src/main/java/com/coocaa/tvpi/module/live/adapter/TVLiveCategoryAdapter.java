package com.coocaa.tvpi.module.live.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.publib.data.tvlive.TVLiveAnim;
import com.coocaa.tvpi.module.live.adapter.holder.TVLiveCategoryHolder;
import com.coocaa.tvpilib.R;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName TVLiveCategoryAdapter
 * @Description TODO (write something)
 * @User heni
 * @Date 2019/1/10
 */
public class TVLiveCategoryAdapter extends RecyclerView.Adapter {
    private static final String TAG = TVLiveCategoryAdapter.class.getSimpleName();
    private Context mContext;
    private List<String> items;
    private OnItemClickListener mOnItemClickListener;
    private int mPreFocusPosition = 1;
    LinearLayoutManager mLayoutManager;
    RecyclerView mRecyclerView;

    public TVLiveCategoryAdapter(Context context, RecyclerView recyclerView, LinearLayoutManager
            layoutManager) {
        this.mContext = context;
        items = new ArrayList<>();
        mRecyclerView = recyclerView;
        mLayoutManager = layoutManager;
    }

    public void addAll(List<String> items) {
        this.items.clear();
        this.items.addAll(items);
        notifyDataSetChanged();
    }

    public void addMore(List<String> items) {
        this.items.addAll(items);
        notifyDataSetChanged();
    }

    //define interface
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = null;
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_tvlive_category, parent, false);
        holder = new TVLiveCategoryHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: " + position);
        ((TVLiveCategoryHolder) holder).onBind(items.get(position), position);
        ((TVLiveCategoryHolder) holder).updateItem(mPreFocusPosition == position ? true : false,
                position,mPreFocusPosition);

        ((TVLiveCategoryHolder) holder).setOnClickListener(new TVLiveCategoryHolder.OnClickListener() {
            @Override
            public void onClick() {
                if (null != mOnItemClickListener) {
//                    Toast.makeText(mContext, items.get(position) + ", " + position, Toast
//                            .LENGTH_SHORT).show();
                    if (position != mPreFocusPosition) {
                        try {
                            View viewOld = mLayoutManager.findViewByPosition(mPreFocusPosition);
                            if (viewOld != null) {
                                TVLiveCategoryHolder holder = (TVLiveCategoryHolder)
                                        mRecyclerView.getChildViewHolder(viewOld);
                                holder.updateItem(false, mPreFocusPosition,position);
                            } else {
                                notifyItemChanged(mPreFocusPosition);
                            }
                            //更新相邻VIew
                            updateAdjacentView(mPreFocusPosition-1,position);
                            updateAdjacentView(mPreFocusPosition+1,position);
                            updateAdjacentView(position+1,position);
                            updateAdjacentView(position-1,position);
                            mPreFocusPosition = position;
                            mOnItemClickListener.onItemClick(position);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        if (position == 0) {
            TVLiveAnim tvLiveAnim = new TVLiveAnim();
            tvLiveAnim.mType = 1;
            tvLiveAnim.mView = ((TVLiveCategoryHolder) holder).itemView.findViewById(R.id
                    .item_tvlive_channel_collect_icon);
            EventBus.getDefault().post(tvLiveAnim);
        }
    }

    private void updateAdjacentView(int mPreFocusPosition, int position) {
        View viewOld = mLayoutManager.findViewByPosition(mPreFocusPosition);
        if (viewOld != null) {
            TVLiveCategoryHolder holder = (TVLiveCategoryHolder)
                    mRecyclerView.getChildViewHolder(viewOld);
            holder.updateItem(false, mPreFocusPosition,position);
        } else {
            notifyItemChanged(mPreFocusPosition);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
