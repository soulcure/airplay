package com.coocaa.tvpi.module.movie.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.percentlayout.widget.PercentRelativeLayout;

import com.coocaa.publib.base.GlideApp;
import com.coocaa.smartscreen.data.movie.LongVideoListModel;
import com.coocaa.tvpilib.R;

/**
 * Created by IceStorm on 2018/1/26.
 */

public class Category3VideoColumnChildView extends PercentRelativeLayout {
    private final static String TAG = Category3VideoColumnChildView.class.getSimpleName();
    private Context mContext;
    private LongVideoListModel mVideo;

    private ImageView poster;
    private TextView title;
    private TextView tvUpdateSeries;


    public Category3VideoColumnChildView(Context context) {
        super(context);
        mContext = context;
        initView();
    }

    public Category3VideoColumnChildView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView();
    }

    public Category3VideoColumnChildView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
    }

    private void initView() {
        this.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.category_3_video_column_child_view, this);

        poster = (ImageView) findViewById(R.id.category_3_video_column_child_iv_poster);
        title = (TextView) findViewById(R.id.category_3_video_column_child_tv_title);
        tvUpdateSeries = (TextView) findViewById(R.id.category_3_video_column_child_tv_update_series);
    }

    public void setData(LongVideoListModel item){
        mVideo = item;

        GlideApp.with(mContext)
                .load(mVideo.video_poster)
                .centerCrop()
                .into(poster);

        title.setText(mVideo.album_title);
    }
}
