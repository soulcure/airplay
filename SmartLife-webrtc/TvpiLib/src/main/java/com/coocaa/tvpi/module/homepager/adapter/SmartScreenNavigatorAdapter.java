package com.coocaa.tvpi.module.homepager.adapter;

import android.content.Context;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.coocaa.tvpi.module.homepager.adapter.bean.PlayMethodTypeBean;
import com.coocaa.tvpi.view.ScaleTransitionPagerTitleView;
import com.coocaa.tvpilib.R;

import net.lucode.hackware.magicindicator.buildins.UIUtil;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;

import java.util.ArrayList;
import java.util.List;

/**
 * 智屏首页Indicator
 * Created by songxing on 2020/10/20
 */
public class SmartScreenNavigatorAdapter extends CommonNavigatorAdapter {

    private List<PlayMethodTypeBean> data = new ArrayList<>();
    private NavigatorClickListener navigatorClickListener;

    public void setData(List<PlayMethodTypeBean> data){
        if (data != null) {
            this.data = data;
            notifyDataSetChanged();
        }
    }

    public void setNavigatorClickListener(NavigatorClickListener navigatorClickListener) {
        this.navigatorClickListener = navigatorClickListener;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public IPagerTitleView getTitleView(Context context, int index) {
        ScaleTransitionPagerTitleView titleView = new ScaleTransitionPagerTitleView(context);
        titleView.setText(data.get(index).typeName);
        titleView.setSelectedTextSize(16);
        titleView.setUnSelectedSize(16);
        titleView.setSelectedBold(true);
        titleView.setNormalColor(context.getResources().getColor(R.color.color_black_a60));
        titleView.setSelectedColor(context.getResources().getColor(R.color.color_black));
        titleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(navigatorClickListener != null){
                    navigatorClickListener.onNavigatorClick(index);
                }
            }
        });
        return titleView;
    }

    @Override
    public IPagerIndicator getIndicator(Context context) {
        LinePagerIndicator indicator = new LinePagerIndicator(context);
        indicator.setMode(LinePagerIndicator.MODE_WRAP_CONTENT);
        indicator.setColors(context.getResources().getColor(R.color.color_FF8E3E));
        indicator.setLineHeight(UIUtil.dip2px(context, 8));
        indicator.setRoundRadius(UIUtil.dip2px(context, 4));
        indicator.setStartInterpolator(new AccelerateInterpolator());
        indicator.setEndInterpolator(new DecelerateInterpolator(2.0f));
        indicator.setYOffset(UIUtil.dip2px(context, 10));
        indicator.setXOffset(UIUtil.dip2px(context,-3));
        return indicator;
    }

    public interface NavigatorClickListener {
        void onNavigatorClick(int index);
    }
}
