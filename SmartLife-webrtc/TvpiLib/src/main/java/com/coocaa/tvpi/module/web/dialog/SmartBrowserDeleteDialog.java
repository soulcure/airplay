package com.coocaa.tvpi.module.web.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.coocaa.tvpi.module.web.WebRecordBean;
import com.coocaa.tvpilib.R;


public class SmartBrowserDeleteDialog extends Dialog implements View.OnClickListener {

    private TextView tvDelete, tvCancel;
    private onOptionClickListener onOptionClickListener;
    private WebRecordBean data;

    public SmartBrowserDeleteDialog(Context context, WebRecordBean data) {
        super(context, R.style.SmartBrowserClipboardDialog);
        this.data = data;
    }

    public void setOnDeleteClickListener(SmartBrowserDeleteDialog.onOptionClickListener onOptionClickListener) {
        this.onOptionClickListener = onOptionClickListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_browser_delete);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        initView();
        setListener();
    }

    private void initView() {
        tvDelete = findViewById(R.id.tv_delete);
        tvCancel = findViewById(R.id.tv_cancel);
    }

    private void setListener() {
        tvDelete.setOnClickListener(this);
        tvCancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tv_delete) {
            if (onOptionClickListener != null) {
                onOptionClickListener.onDeleteClick(data);
            }
        } else if (id == R.id.tv_cancel) {
            if (onOptionClickListener != null) {
                onOptionClickListener.onCancelClick();
            }
        }
        dismiss();
    }

    public interface onOptionClickListener {
        void onCancelClick();

        void onDeleteClick(WebRecordBean data);
    }

}
