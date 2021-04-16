package com.coocaa.tvpi.module.movie.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.coocaa.publib.utils.IRLog;
import com.coocaa.tvpilib.R;

/**
 * Created by WHY on 2017/10/19.
 */

public class LongVideoDescView extends LinearLayout {
    private static final String TAG = LongVideoDescView.class.getSimpleName();

    private Context mContext;

    private LinearLayout mDirectorLayout;
    private LinearLayout mActorLayout;
    private TextView mDirectorTv;
    private TextView mActorTv;
    private TextView mDescTv;

    public LongVideoDescView(Context context) {
        super(context);
        mContext = context;
        initView();
    }

    public LongVideoDescView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView();
    }

    public LongVideoDescView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
    }



    private void initView() {
        IRLog.d(TAG, "initView");
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.long_video_detail_desc_view, this);

        mDirectorLayout = findViewById(R.id.ll_long_video_director);
        mActorLayout  = findViewById(R.id.ll_long_video_actor);
        mDirectorTv = findViewById(R.id.long_video_director_tv);
        mActorTv = findViewById(R.id.long_video_actor_tv);
        mDescTv = findViewById(R.id.long_video_desc_tv);

    }
    public void updateViews(String director,String actors,String desc){
        if(!TextUtils.isEmpty(director)) {
            mDirectorLayout.setVisibility(View.VISIBLE);
            mDirectorTv.setText(director);
        }else {
            mDirectorLayout.setVisibility(View.GONE);
        }
        if(!TextUtils.isEmpty(actors)) {
            mActorLayout.setVisibility(View.VISIBLE);
            mActorTv.setText(actors);
        }else {
            mActorLayout.setVisibility(View.GONE);
        }
        if(!TextUtils.isEmpty(desc))
            mDescTv.setText(desc);
    }
}
