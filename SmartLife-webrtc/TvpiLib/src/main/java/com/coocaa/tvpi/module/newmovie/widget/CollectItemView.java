package com.coocaa.tvpi.module.newmovie.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.coocaa.publib.base.GlideApp;
import com.coocaa.smartscreen.data.movie.CollectionModel;
import com.coocaa.smartscreen.data.movie.PlayRecordListModel;
import com.coocaa.tvpilib.R;


/**
 * Created by IceStorm on 2017/12/8.
 */

public class CollectItemView extends RelativeLayout {

    private Context mContext;
    private PlayRecordListModel mHistoryData;
    private CollectionModel mCollectData;

    private ImageView imgSelect;
    private ImageView poster;
    private TextView videoTitle;

    private boolean isEditMode;
    private RelativeLayout mItemContainer;

    public CollectItemView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public CollectItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public CollectItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    public void setHistoryData (PlayRecordListModel data) {
        if (data != null) {
            mHistoryData = data;
            isEditMode = mHistoryData.isInEditMode;

            updateHistoryView();
        }
    }

    private void updateHistoryView() {
        GlideApp.with(mContext)
                .load(mHistoryData.video_poster)
                .centerCrop()
                .into(poster);
        videoTitle.setText(mHistoryData.video_title);

        if (isEditMode) {
            imgSelect.setVisibility(VISIBLE);
            updateSelectIcon(mHistoryData.isSelected);
        } else {
            // 不在编辑模式下，则隐藏选中view
            imgSelect.setVisibility(GONE);
        }
    }

    private void updateCollectView(){
        GlideApp.with(mContext)
                .load(mCollectData.video_poster)
                .centerCrop()
                .into(poster);
        videoTitle.setText(mCollectData.video_title);
//        videoTitle.setText(mCollectData.video_title + mCollectData.collect_id);

        if (isEditMode) {
            imgSelect.setVisibility(VISIBLE);
            updateSelectIcon(mCollectData.isSelected);
        } else {
            // 不在编辑模式下，则隐藏选中view
            imgSelect.setVisibility(GONE);
        }
    }

    public void setCollectData(CollectionModel collectData) {
        if (collectData != null) {
            mCollectData = collectData;
            isEditMode = mCollectData.isInEditMode;

            updateCollectView();
        }
    }

    private void init() {
        initView();
    }

    private void initView() {
        LayoutInflater.from(mContext).inflate(R.layout.item_collect_activity,this);
        mItemContainer = (RelativeLayout) findViewById(R.id.item_collect_rl_root);
        imgSelect = (ImageView) findViewById(R.id.item_collect_img_select);
        poster = (ImageView) findViewById(R.id.item_collect_img_poster);
        videoTitle = (TextView) findViewById(R.id.item_collect_tv_title);

//        imgSelect.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (isEditMode) {
//                    mData.isSelected = !mData.isSelected;
//                    updateSelectIcon(mData.isSelected);
//                }

//                if (mListener != null && isEditMode) {
//                    mListener.onEditModeClickSelect(mData.palyrecord_id);
//                }
//            }
//        });

        initListener();
    }

    private void initListener() {
//        poster.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
                // 跳转到播放详情页
//                Intent intent = new Intent(mContext, DetailActivity.class);
//                intent.putExtra("service_id",mData.service_id + "");
//                ((Activity)mContext).startActivityForResult(intent,0);
//            }
//        });
    }

    private void updateSelectIcon (boolean isSelect) {
        imgSelect.setImageResource(isSelect ? R.drawable.movie_item_checked : R.drawable.movie_item_unchecked);
    }

    public void setMode(boolean isEditMode) {
        this.isEditMode = isEditMode;
    }

    public interface OnItemClickSelectListener {
        void onEditModeClickSelect(int dataId);
    }

    private OnItemClickSelectListener mListener;

    public void setOnItemClickSelectListener (OnItemClickSelectListener listener) {
        mListener = listener;
    }

}
