package com.coocaa.tvpi.module.movie.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coocaa.publib.base.GlideApp;
import com.coocaa.publib.utils.IRLog;
import com.coocaa.smartscreen.data.movie.LongVideoListModel;
import com.coocaa.tvpilib.R;

/**
 * Created by wuhaiyuan on 2018/2/1.
 */

public class Relate3ColumnChildView extends LinearLayout {

    private static final String TAG = "Category3ColumnChildVie";

    private Context mContext;
    private LongVideoListModel mVideo;

    private ImageView poster;
    private TextView tvTilte;

    public Relate3ColumnChildView(Context context) {
        super(context);
        mContext = context;

        initView();
    }

    public Relate3ColumnChildView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        initView();
    }

    public Relate3ColumnChildView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;

        initView();
    }

    private void initView() {
        IRLog.d(TAG,"initView");
        this.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.relate_3_column_child_view, this);

        poster = (ImageView) findViewById(R.id.relate_3_column_child_iv_poster);
        tvTilte = findViewById(R.id.relate_3_column_child_tv_title);
    }

    public void setData(LongVideoListModel item){
        mVideo = item;

        GlideApp.with(mContext)
                .load(mVideo.video_poster)
                .centerCrop()
                .into(poster);

        tvTilte.setText(mVideo.album_title);
    }
}
