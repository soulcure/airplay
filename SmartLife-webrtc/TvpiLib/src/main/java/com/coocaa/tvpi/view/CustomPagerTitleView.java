package com.coocaa.tvpi.view;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;

import net.lucode.hackware.magicindicator.buildins.UIUtil;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView;

/**
 * @Description: 可自定义扩展接口
 * @Author: wzh
 * @CreateDate: 4/11/21
 */
public class CustomPagerTitleView extends SimplePagerTitleView {

    private int mPadding;
    // 默认选中不加粗
    private boolean isSelectedBold = false;

    public CustomPagerTitleView(Context context) {
        super(context);
        mPadding = UIUtil.dip2px(context, 10);//默认左右间距为10
        init();
    }

    public CustomPagerTitleView(Context context, int padding) {
        super(context);
        mPadding = padding;
        init();
    }

    private void init() {
        setGravity(Gravity.CENTER);
        setPadding(mPadding, 0, mPadding, 0);
        setSingleLine();
        setEllipsize(TextUtils.TruncateAt.END);
    }

    @Override
    public void onSelected(int index, int totalCount) {
        super.onSelected(index, totalCount);
        if (isSelectedBold) {
            this.getPaint().setFakeBoldText(true);
        }
    }

    @Override
    public void onDeselected(int index, int totalCount) {
        super.onDeselected(index, totalCount);
        if (isSelectedBold) {
            this.getPaint().setFakeBoldText(false);
        }
    }

    // 是否选中态加粗
    public void setSelectedBold(boolean isBold) {
        this.isSelectedBold = isBold;
    }
}
