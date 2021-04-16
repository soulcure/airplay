package com.coocaa.swaiotos.virtualinput.module.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;

import androidx.fragment.app.Fragment;

import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView;

import java.util.List;

public class ControlNavigatorAdapter extends CommonNavigatorAdapter {

    private List<Fragment> fragmentList;
    private Context context;

    public ControlNavigatorAdapter(List<Fragment> fragmentList, Context context) {
        this.fragmentList = fragmentList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

    @Override
    public IPagerTitleView getTitleView(Context context, final int index) {
        ColorTransitionPagerTitleView itemView = new ColorTransitionPagerTitleView(context);
        switch (index) {
            case 0:
                itemView.setText("激光笔");
                break;
            case 1:
                itemView.setText("发表情");
                break;
            case 2:
                itemView.setText("发弹幕");
                break;
            default:
                break;
        }
        itemView.setNormalColor(Color.parseColor("#33ffffff"));
        itemView.setSelectedColor(Color.parseColor("#FFFFFFFF"));
        itemView.setTextSize(16);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(index);
            }
        });
        return itemView;
    }

    @Override
    public IPagerIndicator getIndicator(Context context) {
        LinePagerIndicator indicator = new LinePagerIndicator(context);
        indicator.setColors(Color.parseColor("#00ffffff"));
        return indicator;
    }

    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(int pos);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
