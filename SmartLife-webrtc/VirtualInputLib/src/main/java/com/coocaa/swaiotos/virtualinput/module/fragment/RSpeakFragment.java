package com.coocaa.swaiotos.virtualinput.module.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.cocaa.swaiotos.virtualinput.R;
import com.coocaa.swaiotos.virtualinput.module.adapter.ControlNavigatorAdapter;
import com.coocaa.swaiotos.virtualinput.module.adapter.SpeakViewPagerAdapter;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.buildins.UIUtil;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


public class RSpeakFragment extends Fragment {

    private static final String TAG = RSpeakFragment.class.getSimpleName();

    private MagicIndicator indicator;
    private ViewPager2 viewPager2;
    private List<Fragment> fragmentList = new ArrayList<>();
    private View mView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.remote_speak_fragment, container, false);
        initView();
        return mView;
    }

    public void switchOff() {
        if (viewPager2 != null) {
            viewPager2.setUserInputEnabled(false);
        }
    }

    public void switchOn() {
        if (viewPager2 != null) {
            viewPager2.setUserInputEnabled(true);
        }
    }

    private void initView() {
        indicator = mView.findViewById(R.id.indicator);
        viewPager2 = mView.findViewById(R.id.viewpager2);
        delayViewPager2();
        initFragment();
        viewPager2.setAdapter(new SpeakViewPagerAdapter(getChildFragmentManager(), getLifecycle(), fragmentList));
        CommonNavigator commonNavigator = new CommonNavigator(getContext());
        ControlNavigatorAdapter controlNavigatorAdapter = new ControlNavigatorAdapter(fragmentList, getContext());
        controlNavigatorAdapter.setOnItemClickListener(new ControlNavigatorAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int pos) {
                viewPager2.setCurrentItem(pos);
            }
        });
        indicator.setNavigator(commonNavigator);
        commonNavigator.setAdapter(controlNavigatorAdapter);
        //设置分隔线
        LinearLayout titleContainer = commonNavigator.getTitleContainer();
        titleContainer.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        titleContainer.setDividerPadding(UIUtil.dip2px(getContext(), 16));
        titleContainer.setDividerDrawable(getResources().getDrawable(R.drawable.simple_splitter));
        viewPager2.registerOnPageChangeCallback(onPageChangeCallback);
    }

    private void delayViewPager2() {
        //反射降低ViewPager的灵敏度
        try {
            final Field recyclerViewField = ViewPager2.class.getDeclaredField("mRecyclerView");
            recyclerViewField.setAccessible(true);
            final RecyclerView recyclerView = (RecyclerView) recyclerViewField.get(viewPager2);
            final Field touchSlopField = RecyclerView.class.getDeclaredField("mTouchSlop");
            touchSlopField.setAccessible(true);
            final int touchSlop = (int) touchSlopField.get(recyclerView);
            touchSlopField.set(recyclerView, touchSlop * 5);
            //6 is empirical value
        } catch (Exception ignore) {
        }
    }

    private void initFragment() {


        RLaserPenFragment rLaserPenFragment = new RLaserPenFragment();
        fragmentList.add(rLaserPenFragment);

        //表情包
        RH5Fragment rh5FragmentEmo = new RH5Fragment();
        rh5FragmentEmo.setContentUrl("https://webapp.skyworthiot.com/barrage/h5v2/#/emoji");
        fragmentList.add(rh5FragmentEmo);
        //表情包
        RH5Fragment rh5FragmentText = new RH5Fragment();
        rh5FragmentText.setContentUrl("https://webapp.skyworthiot.com/barrage/h5v2/#/text");
        fragmentList.add(rh5FragmentText);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private final ViewPager2.OnPageChangeCallback onPageChangeCallback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            indicator.onPageScrolled(position, positionOffset, positionOffsetPixels);
            Log.d(TAG, "onPageScrolled: ");
        }

        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            indicator.onPageSelected(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            super.onPageScrollStateChanged(state);
            indicator.onPageScrollStateChanged(state);
        }
    };

}
