package com.coocaa.tvpi.module.newmovie.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.coocaa.tvpilib.R;


/**
 * Created by wuhaiyuan on 2018/2/1.
 */
//给listview增加foot空间 为了底部的语音遥控不遮盖掉列表的内容
public class ListFooterView extends LinearLayout {

    private static final String TAG = ListFooterView.class.getSimpleName();

    private Context mContext;

    public ListFooterView(Context context) {
        super(context);
        mContext = context;

        initView();
    }

    public ListFooterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        initView();
    }

    public ListFooterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;

        initView();
    }

    private void initView() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.list_footer_view_layout, this);

    }

    public void setData(String title){

    }
}
