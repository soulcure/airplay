package com.coocaa.tvpi.module.web.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.coocaa.tvpilib.R;


public class SmartBrowserBottomDeleteDialog extends Dialog implements View.OnClickListener {

    private TextView tvDelete;
    private onDeleteClickListener onDeleteClickListener;

    public SmartBrowserBottomDeleteDialog(Context context) {
        super(context, R.style.SmartBrowserBottomDeleteDialog);
    }

    public void setOnDeleteClickListener(SmartBrowserBottomDeleteDialog.onDeleteClickListener onDeleteClickListener) {
        this.onDeleteClickListener = onDeleteClickListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_browser_bottom_delete);

        Window dialogWindow = getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes(); // 获取对话框当前的参数值
        dialogWindow.setWindowAnimations(R.style.dialogWindowAnim); // 添加动画
        lp.width = WindowManager.LayoutParams.MATCH_PARENT; // 宽度
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT; // 高度
        dialogWindow.setAttributes(lp);
        //FLAG_NOT_TOUCH_MODAL作用：即使该window可获得焦点情况下，仍把该window之外的
        // 任何event发送到该window之后的其他window
        dialogWindow.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);

        //FLAG_WATCH_OUTSIDE_TOUCH作用：如果点击事件发生在window之外，就会收到一个特殊的MotionEvent，
        // 为ACTION_OUTSIDE
        dialogWindow.setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);

        initView();
        setListener();
    }

    private void initView() {
        tvDelete = findViewById(R.id.tv_delete);
    }

    private void setListener() {
        tvDelete.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tv_delete) {
            if (onDeleteClickListener != null) {
                onDeleteClickListener.onDeleteClick();
            }
        }
    }

    public void changeTextColor(boolean hasSelected) {
        if (hasSelected) {
            tvDelete.setTextColor(Color.parseColor("#FF326C"));
            tvDelete.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    getContext().getResources().getDrawable(R.drawable.smart_browser_delete_red_icon)
                    , null, null, null);
        } else {
            tvDelete.setTextColor(Color.parseColor("#7f000000"));
            tvDelete.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    getContext().getResources().getDrawable(R.drawable.smart_browser_delete_gary_icon)
                    , null, null, null);
        }

    }

    public interface onDeleteClickListener {

        void onDeleteClick();
    }

}
