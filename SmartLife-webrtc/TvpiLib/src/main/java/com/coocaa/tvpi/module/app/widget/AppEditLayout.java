package com.coocaa.tvpi.module.app.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.coocaa.tvpilib.R;

/**
 * 电视应用编辑栏
 * Created by songxing on 2020/7/17
 */
public class AppEditLayout extends FrameLayout {
    private RelativeLayout selectAllLayout;
    private TextView tvSelectAll;
    private RelativeLayout uninstallLayout;
    private ImageView ivUninstall;
    private TextView tvUninstall;

    private boolean isSelectAll;
    private EditListener editListener;

    public AppEditLayout(Context context) {
        this(context, null, 0);
    }

    public AppEditLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AppEditLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_app_edit, this, true);
        selectAllLayout =findViewById(R.id.selectAllLayout);
        tvSelectAll = findViewById(R.id.tvSelectedAll);
        uninstallLayout = findViewById(R.id.uninstallLayout);
        ivUninstall = findViewById(R.id.ivUninstall);
        tvUninstall = findViewById(R.id.tvUninstall);

        selectAllLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editListener != null){
                    isSelectAll = !isSelectAll;
                    setSelectView(!isSelectAll);
                    setUninstallView(isSelectAll);
                    editListener.onSelectAllClick(isSelectAll);
                }
            }
        });

        uninstallLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editListener != null){
                    editListener.onUninstallClick();
                }
            }
        });
    }

    public void onSelectItemChange(int selectSize,int totalSize){
        setSelectView(selectSize < totalSize);
        setUninstallView(selectSize > 0);
    }

    public void setEditListener(EditListener editListener){
        this.editListener = editListener;
    }

    private void setUninstallView(boolean enable){
        if (enable) {
            tvUninstall.setTextColor(getResources().getColor(R.color.color_origin));
            ivUninstall.setBackgroundResource(R.drawable.app_uninstall_all);
            uninstallLayout.setClickable(true);
        } else {
            tvUninstall.setTextColor(getResources().getColor(R.color.color_origin_unable));
            ivUninstall.setBackgroundResource(R.drawable.app_uninstall_all_unable);
            uninstallLayout.setClickable(false);
        }
    }

    private void setSelectView(boolean showSelectAllView){
        if (showSelectAllView) {
            tvSelectAll.setText("全选");
        } else {
            tvSelectAll.setText("取消全选");
        }
    }


    public interface EditListener{
        void onUninstallClick();

        void onSelectAllClick(boolean isSelect);
    }
}
