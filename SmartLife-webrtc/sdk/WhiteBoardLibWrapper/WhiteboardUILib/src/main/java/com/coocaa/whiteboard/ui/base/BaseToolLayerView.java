package com.coocaa.whiteboard.ui.base;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class BaseToolLayerView extends FrameLayout implements IBaseView {
    protected Context mContext;

    public BaseToolLayerView(@NonNull Context context) {
        this(context,null);
    }

    public BaseToolLayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @Override
    public abstract int getLayoutId();


    private void init(Context context) {
        mContext = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(getLayoutId(), this);
    }


}
