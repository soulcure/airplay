package com.coocaa.tvpi.module.homepager.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.coocaa.tvpilib.R;


/**
 * @ClassName: RotateScreenDialog
 * @Description: java类作用描述
 * @Author: lfz
 */
public class RotateScreenDialog extends Dialog implements View.OnClickListener {

    private String tips;
    private String btLeftText;
    private String btRightText;
    private OnSureButtonClick listener;
    private TextView btTips, btnLeft, btnRight;

    public RotateScreenDialog(@NonNull Context context, String tips, String btLeftText,
                              String btRightText, OnSureButtonClick onSureButtonClick) {
        super(context, R.style.dialog_style_dim_3);
        this.tips = tips;
        this.btLeftText = btLeftText;
        this.btRightText = btRightText;
        this.listener = onSureButtonClick;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dlna_t_dialog);
        initView();
        initData();
        setListener();
    }

    private void initData() {
        if (!TextUtils.isEmpty(tips))
            btTips.setText(tips);
        if (!TextUtils.isEmpty(btLeftText)) {
            btnLeft.setText(btLeftText);
        }else {
            btnLeft.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(btRightText)) {
            btnRight.setText(btRightText);
        }else {
            btnRight.setVisibility(View.GONE);
        }
    }

    private void setListener() {
        btnLeft.setOnClickListener(this);
        btnRight.setOnClickListener(this);
    }

    private void initView() {
        btTips = (TextView) findViewById(R.id.dialog_tips);
        btnLeft = (TextView) findViewById(R.id.btnL);
        btnRight = (TextView) findViewById(R.id.btnR);
    }

    public interface OnSureButtonClick {
        void onClick(View view);
    }

    @Override
    public void onClick(View v) {
        if (listener != null) {
            listener.onClick(v);
        }
        dismiss();
    }

}
