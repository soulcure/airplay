package com.coocaa.tvpi.module.local.document.page;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.tvpilib.R;

/**
 * @Description: 文档帮助2
 * @Author: luoxi
 * @CreateDate: 2020/12/11
 */
public class DocumentHelp2View extends FrameLayout {
    public DocumentHelp2View(Context context) {
        this(context, null);
    }

    public DocumentHelp2View(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DocumentHelp2View(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        View.inflate(getContext(), R.layout.layout_document_help2, this);
        findViewById(R.id.bt_scan).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getContext() instanceof Activity) {
                    ((Activity) getContext()).finish();
                }
            }
        });
    }
}

