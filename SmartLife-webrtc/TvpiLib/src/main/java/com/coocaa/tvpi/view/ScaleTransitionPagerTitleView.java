package com.coocaa.tvpi.view;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;

import net.lucode.hackware.magicindicator.buildins.UIUtil;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView;

/**
 * Created by IceStorm on 2018/1/8.
 */

public class ScaleTransitionPagerTitleView extends ColorTransitionPagerTitleView {
//    private float mMinScale = 0.875f;
    // 设计要求不做大小变化
    private float mMinScale = 1f;
    // 默认选中不加粗
    private boolean isSelectedBold = false;

    private int selectedSize;
    private int unSelectedSize;

    public ScaleTransitionPagerTitleView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        this.setGravity(17);
        // 左右间距是10
        int padding = UIUtil.dip2px(context, 10.0D);
        this.setPadding(padding, 0, padding, 0);
        this.setSingleLine();
        this.setEllipsize(TextUtils.TruncateAt.END);
    }

    @Override
    public void onEnter(int index, int totalCount, float enterPercent, boolean leftToRight) {
        super.onEnter(index, totalCount, enterPercent, leftToRight);    // 实现颜色渐变
        setScaleX(mMinScale + (1.0f - mMinScale) * enterPercent);
        setScaleY(mMinScale + (1.0f - mMinScale) * enterPercent);
    }

    @Override
    public void onSelected(int index, int totalCount) {
        super.onSelected(index, totalCount);

        if(isSelectedBold) {
//            TextPaint tp = this.getPaint();
//            tp.setFakeBoldText(true);
            this.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        }
        this.setTextSize(selectedSize);
    }

    @Override
    public void onDeselected(int index, int totalCount) {
        super.onDeselected(index, totalCount);

        if(isSelectedBold) {
//            TextPaint tp = this.getPaint();
//            tp.setFakeBoldText(false);
            this.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        }
        this.setTextSize(unSelectedSize);
    }

    @Override
    public void onLeave(int index, int totalCount, float leavePercent, boolean leftToRight) {
        super.onLeave(index, totalCount, leavePercent, leftToRight);    // 实现颜色渐变
        setScaleX(1.0f + (mMinScale - 1.0f) * leavePercent);
        setScaleY(1.0f + (mMinScale - 1.0f) * leavePercent);
    }

    // 是否选中态加粗
    public void setSelectedBold(boolean isBold) {
        this.isSelectedBold = isBold;
    }

    public float getMinScale() {
        return mMinScale;
    }

    public void setMinScale(float minScale) {
        mMinScale = minScale;
    }

    public void setSelectedTextSize(int size){
        selectedSize = size;
    }

    public void setUnSelectedSize(int size){
        unSelectedSize = size;
    }

}
