package com.coocaa.tvpi.module.live.adapter.holder;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.tvpilib.R;

/**
 * @ClassName TVLiveCategoryHolder
 * @Description TODO (write something)
 * @User heni
 * @Date 2019/1/10
 * @Version TODO (write something)
 */
public class TVLiveCategoryHolder extends RecyclerView.ViewHolder {

    private String TAG = TVLiveCategoryHolder.class.getSimpleName();
    private Context mContext;

    public View itemView;
    private TextView mTvCategory;

    private ImageView mCollectIcon;
    private OnClickListener mOnClickListener;

    public interface OnClickListener {
        void onClick();
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.mOnClickListener = onClickListener;
    }

    public TVLiveCategoryHolder(View itemView) {
        super(itemView);
        mContext = itemView.getContext();
        this.itemView = itemView;
        mTvCategory = itemView.findViewById(R.id.item_tvlive_channel_category);
        mCollectIcon = itemView.findViewById(R.id.item_tvlive_channel_collect_icon);
    }

    public void onBind(String categoryName, final int position) {
        Log.d(TAG, "onBind: " + categoryName + position);
        mTvCategory.setText(categoryName);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mOnClickListener) {
                    mOnClickListener.onClick();
                    updateItem(true, position, position);
                }
            }
        });
    }

    public void updateItem(boolean isSelected, int position, int curPosition) {

        if (isSelected) {
            mTvCategory.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            mTvCategory.setTextColor(mContext.getResources().getColor(R.color.black_80));
        } else {
            mTvCategory.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            mTvCategory.setTextColor(mContext.getResources().getColor(R.color.color_black_a50));
        }

        if (position == curPosition) {
            itemView.setBackgroundResource(R.color.color_white);
        } else if (position == 0 && curPosition - 1 == 0) {
            itemView.setBackgroundResource(R.drawable.bg_gray_round_right_16);
        } else if (position == 0) {
            itemView.setBackgroundResource(R.drawable.bg_gray_round_top_right_16);
        } else if (position == curPosition + 1) {
            itemView.setBackgroundResource(R.drawable.bg_gray_round_top_right_16);
        } else if (position == curPosition - 1) {
            itemView.setBackgroundResource(R.drawable.bg_gray_round_bottom_right_16);
        } else {
            itemView.setBackgroundResource(R.color.b_tvlive);
        }

        if (position == 0) {
            mTvCategory.setText("收藏");
            mCollectIcon.setVisibility(View.VISIBLE);
            mCollectIcon.setImageResource(R.drawable.tv_live_category_collect_focus);
        } else {
            mTvCategory.setVisibility(View.VISIBLE);
            mCollectIcon.setVisibility(View.GONE);
        }
    }
}
