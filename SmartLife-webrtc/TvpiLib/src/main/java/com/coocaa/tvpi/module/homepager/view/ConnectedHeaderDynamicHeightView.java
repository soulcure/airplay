package com.coocaa.tvpi.module.homepager.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.coocaa.smartscreen.data.function.FunctionBean;
import com.coocaa.tvpi.view.viewpager.DynamicHeightViewPager;
import com.coocaa.tvpilib.R;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;
import net.lucode.hackware.magicindicator.buildins.circlenavigator.CircleNavigator;

import java.util.ArrayList;
import java.util.List;

/**
 * 已连接的状态下智屏头部UI
 * Created by songxing on 2020/10/21
 */
public class ConnectedHeaderDynamicHeightView extends RelativeLayout {
    private static final String TAG = ConnectedHeaderDynamicHeightView.class.getSimpleName();
    private static final int ITEM_SIZE = 8;
    private DynamicHeightViewPager viewPager;
    private MagicIndicator indicator;

    public ConnectedHeaderDynamicHeightView(Context context) {
        this(context, null);
    }

    public ConnectedHeaderDynamicHeightView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_smartscreen_header_connected_dynamic, this, true);
        viewPager = findViewById(R.id.dynamicHeightViewPager);
        indicator = findViewById(R.id.indicator);
    }


    public void setFunctionList(List<FunctionBean> functionList) {
       /* List<ConnectedHeaderPagerItem> pagerItemViews = new ArrayList<>();
        int size = functionList.size();
        int pageSize = functionList.size() / ITEM_SIZE + 1;
        for (int i = 0; i < pageSize; i++) {
            int fromIndex = i * ITEM_SIZE;
            int endIndex = (i + 1) * ITEM_SIZE;
            if (endIndex > size) {
                endIndex = size;
            }
            List<FunctionBean> functionBeans = functionList.subList(fromIndex, endIndex);
            ConnectedHeaderPagerItem pagerItemView = new ConnectedHeaderPagerItem(getContext(), functionBeans);
            pagerItemViews.add(pagerItemView);
        }
        FunctionDynamicPageAdapter pageAdapter = new FunctionDynamicPageAdapter(pagerItemViews);
        viewPager.setAdapter(pageAdapter);
        viewPager.init(pagerItemViews,0);
        CircleNavigator circleNavigator = new CircleNavigator(getContext());
        circleNavigator.setCircleCount(pagerItemViews.size());
        circleNavigator.setCircleColor(getResources().getColor(R.color.color_FF8E3E));
        indicator.setNavigator(circleNavigator);
        ViewPagerHelper.bind(indicator, viewPager);*/
    }

    private static class FunctionDynamicPageAdapter extends PagerAdapter {
      /*  private List<ConnectedHeaderPagerItem> pagerItemViews;

        public FunctionDynamicPageAdapter(List<ConnectedHeaderPagerItem> pagerItemViews) {
            this.pagerItemViews = pagerItemViews;
        }*/

        @Override
        public int getCount() {
//            return pagerItemViews.size();
            return 0;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
//            ConnectedHeaderPagerItem pagerItemView = pagerItemViews.get(position);
//            View itemView = pagerItemView.getItemView();
//            container.addView(itemView);
//            return itemView;
            return null;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }
}
