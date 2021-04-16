package com.coocaa.tvpi.module.local.document.page;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.coocaa.tvpilib.R;

/**
 * @Description: 文档帮助1
 * @Author: Luoxi
 * @CreateDate: 2020/12/11
 */
public class DocumentHelp1View extends FrameLayout implements View.OnClickListener {
    public DocumentHelp1View(Context context) {
        this(context, null);
    }

    public DocumentHelp1View(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DocumentHelp1View(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        View.inflate(getContext(), R.layout.layout_document_help1, this);
        findViewById(R.id.bt_play_video_1).setOnClickListener(this);
        findViewById(R.id.bt_play_video_2).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_play_video_1) {
            DocumentVideoPlayerActivity.start(getContext(), R.raw.document_help_video1);
        } else if (v.getId() == R.id.bt_play_video_2) {
            DocumentVideoPlayerActivity.start(getContext(), R.raw.document_help_video2);
        }
    }
}

