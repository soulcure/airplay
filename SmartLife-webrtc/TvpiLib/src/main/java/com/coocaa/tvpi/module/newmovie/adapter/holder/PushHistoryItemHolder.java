package com.coocaa.tvpi.module.newmovie.adapter.holder;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.publib.base.GlideApp;
import com.coocaa.publib.utils.UIHelper;
import com.coocaa.smartscreen.data.movie.PushHistoryModel;
import com.coocaa.tvpilib.R;


/**
 * @ClassName PushHistoryItemHolder
 * @Description 推送历史Activity界面对应的ItemHolder
 * @User heni
 * @Date 18-8-31
 */
public class PushHistoryItemHolder extends RecyclerView.ViewHolder{

    private static final String TAG = PushHistoryItemHolder.class.getSimpleName();
    private Context mContext;
    private PushHistoryModel.PushHistoryVideoModel mHistoryData;

    private ImageView imgSelect; //编辑选择图标
    private ImageView poster; //video海报
    private TextView videoTitle; //标题
    private RelativeLayout mItemContainer;

    private boolean isEditMode; // 是否处于编辑模式
    private OnItemClickSelectListener mListener;

    public interface OnItemClickSelectListener {
        void onEditModeClickSelect(PushHistoryModel.PushHistoryVideoModel mHistoryData);
    }

    public void setOnItemClickSelectListener (OnItemClickSelectListener listener) {
        mListener = listener;
    }

    public PushHistoryItemHolder(View itemView) {
        super(itemView);
        mContext = itemView.getContext();

        initView();
        initListener();
    }

    private void initView() {
        mItemContainer = itemView.findViewById(R.id.item_push_history_rl_root);
        imgSelect = itemView.findViewById(R.id.item_push_history_img_select);
        poster = itemView.findViewById(R.id.item_push_history_img_poster);
        videoTitle = itemView.findViewById(R.id.item_push_history_tv_title);

        imgSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEditMode) {
                    mHistoryData.isSelected = !mHistoryData.isSelected;
                    updateSelectIcon(mHistoryData.isSelected);
                }
                if (mListener != null && isEditMode) {
                    mListener.onEditModeClickSelect(mHistoryData);
                }
            }
        });
    }

    private void initListener() {
        mItemContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到播放详情页
                UIHelper.startActivityByURL(mContext, mHistoryData.router);
            }
        });
    }

    public void setHistoryData (PushHistoryModel.PushHistoryVideoModel data) {
        if (data != null) {
            mHistoryData = data;
            isEditMode = mHistoryData.isInEditMode;

            if(!TextUtils.isEmpty(mHistoryData.poster_h)) {
                GlideApp.with(mContext).load(mHistoryData.poster_h).centerCrop().into(poster);
            }else {
                GlideApp.with(mContext).load(mHistoryData.poster_v).centerCrop().into(poster);
            }

            videoTitle.setText(mHistoryData.title);

            if (isEditMode) {
                imgSelect.setVisibility(View.VISIBLE);
                updateSelectIcon(mHistoryData.isSelected);
            } else {
                // 不在编辑模式下，则隐藏选中view
                imgSelect.setVisibility(View.GONE);
            }
        }
    }

    private void updateSelectIcon (boolean isSelect) {
        imgSelect.setImageResource(isSelect ? R.drawable.movie_item_checked : R.drawable.movie_item_unchecked);
    }

    public void setMode(boolean isEditMode) {
        this.isEditMode = isEditMode;
    }
}
