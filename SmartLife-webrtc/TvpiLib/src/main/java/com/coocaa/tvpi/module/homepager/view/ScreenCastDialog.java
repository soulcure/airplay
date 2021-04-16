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
 * @ClassName: ScreenCastDialog
 * @Description: java类作用描述
 * @Author: lfz
 * @Date: 2019/9/12 15:39
 */
public class ScreenCastDialog extends Dialog implements View.OnClickListener {

    private String tips;
    private String btLeftText;
    private String btRightText;
    private OnSureButtonClick listener;
    private TextView btTips, btnLeft, btnRight;

    public ScreenCastDialog(@NonNull Context context, String s, String s1, String s2,
                            OnSureButtonClick onSureButtonClick) {
        super(context, R.style.dialog_style_dim_3);
        this.tips = s;
        this.btLeftText = s1;
        this.btRightText = s2;
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
        if (!TextUtils.isEmpty(btLeftText))
            btnLeft.setText(btLeftText);
        if (!TextUtils.isEmpty(btRightText))
            btnRight.setText(btRightText);
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
        int id = v.getId();
        if (id == R.id.btnL) {
            dismiss();
        } else if (id == R.id.btnR) {
            if (listener != null) {
                listener.onClick(v);
            }
            dismiss();
        }
    }

}
