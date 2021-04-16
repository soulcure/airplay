package com.coocaa.tvpi.module.mall.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.coocaa.tvpilib.R;

public class AddressItemView extends LinearLayout {
    private EditText editText;
    private String itemName;
    private String hintText;
    private String text;

    public AddressItemView(Context context) {
        this(context, null);
    }

    public AddressItemView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AddressItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initCustomAttrs(context, attrs, defStyleAttr);
        initView();
    }

    private void initCustomAttrs(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.AddressItemView, defStyleAttr, 0);
        itemName = ta.getString(R.styleable.AddressItemView_address_item_name);
        hintText = ta.getString(R.styleable.AddressItemView_address_item_hint_text);
        text = ta.getString(R.styleable.AddressItemView_address_item_text);
        ta.recycle();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_address_item, this, true);
        TextView tvItemName = findViewById(R.id.tvItemName);
        editText = findViewById(R.id.etItem);
        tvItemName.setText(itemName);
        editText.setHint(hintText);
        editText.setText(text);
    }

    public void setText(String text) {
        if (!TextUtils.isEmpty(text)) {
            editText.setText(text);
        }
    }

    public String getText(){
        return editText.getText().toString();
    }

    public void setInputType(int type){
        editText.setInputType(type);
    }
}
