package com.coocaa.tvpi.module.newmovie;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.coocaa.publib.base.BaseActivity;
import com.coocaa.tvpi.module.newmovie.fragment.CollectWallFragment;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpi.view.ScaleTransitionPagerTitleView;
import com.coocaa.tvpilib.R;
import com.umeng.analytics.MobclickAgent;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;
import net.lucode.hackware.magicindicator.buildins.UIUtil;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static com.coocaa.tvpi.common.UMEventId.CLICK_COLLECT_TYPE_SWITCH;

/**
 * Created by IceStorm on 2017/12/8.
 */

public class CollectActivity extends BaseActivity {

    private static final String TAG = "CollectActivity";

    private ImageView backIV;
    private TextView rightBtn;
    private MagicIndicator magicIndicator;
    private ViewPager viewPager;
    private List<String> titles = new ArrayList<>();
    private List<CollectWallFragment> fragments = new ArrayList<>();
    private int default_index = 0;
    private CollectWallFragment curFragment = null;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private boolean isInEditMode = false;


    public static void start(Context context) {
        Intent starter = new Intent(context, CollectActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect);
        StatusBarHelper.translucent(this);
        StatusBarHelper.setStatusBarLightMode(this);
        /*setTitle("喜欢");

        setRightButton("编辑");*/

        initData();

        initViews();


    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(TAG); // 统计页面
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(TAG); // 统计页面
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        /*EventBus.getDefault().unregister(this);*/
    }

    /*public void onEvent(UserLoginEvent userLoginEvent) {
        if (userLoginEvent.isLogin) {
            if(mLoadTipsView != null) {
                mLoadTipsView.setVisibility(View.VISIBLE);
                mLoadTipsView.setLoadTipsIV(LoadTipsView.TYPE_LOADING);
            }

            pageIndex = 0;
            isAddMore = false;
            queryData(pageIndex, pageSize);
        }
    }*/

    private void initData() {
    }

    private void initViews() {
        backIV = findViewById(R.id.collect_back_iv);
        backIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        rightBtn = findViewById(R.id.collect_right_btn);
        rightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isInEditMode = !isInEditMode;

                for (CollectWallFragment temp:fragments) {
                    temp.setEditMode(isInEditMode);
                }

                if(isInEditMode) {
                    rightBtn.setText("取消");
                } else {
                    rightBtn.setText("编辑");
                }
            }
        });

        magicIndicator = findViewById(R.id.collect_magicindicator);
        viewPager = findViewById(R.id.collect_viewpager);

        final CommonNavigator commonNavigator = new CommonNavigator(this);

        if(titles.size() > 0) {
            titles.clear();
        }
        titles.add("短视频");
        titles.add("正片");

        if(fragments.size() > 0) {
            fragments.clear();
        }
        for(int i=0; i<2; i++) {
            final CollectWallFragment temp = new CollectWallFragment();
            // 视频类型,0:短片,1:正片
            temp.setVideoType(i==0? 0: 1);

            fragments.add(temp);

            final int index = i;
            temp.setOnCollectDataNumberCallback(new CollectWallFragment.CollectDataNumberCallback() {
                @Override
                public void onCollectDataNumber(int dataNumber) {
                    Log.d(TAG, "onCollectDataNumber: " + dataNumber);
                    if(index == 0) {
                        /*if(dataNumber == 0) {
                            titles.remove(0);
                            titles.add(0, "短视频");
                        } else {
                            titles.remove(0);
                            titles.add(0, "短视频 " + dataNumber);
                        }*/
                        titles.remove(0);
                        titles.add(0, "短视频 " + dataNumber);
                    } else {
                        /*if(dataNumber == 0) {
                            titles.remove(1);
                            titles.add(1, "正片");
                        } else {
                            titles.remove(1);
                            titles.add(1, "正片 " + dataNumber);
                        }*/
                        titles.remove(1);
                        titles.add(1, "正片 " + dataNumber);
                    }

                    commonNavigator.notifyDataSetChanged();
                }
            });
        }

        viewPager.addOnPageChangeListener(onPageChangeListener);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), fragments);
        viewPager.setAdapter(mSectionsPagerAdapter);

       // magicIndicator.setBackgroundColor(Color.WHITE);

//        commonNavigator.setAdjustMode(true);
//        commonNavigator.setLeftPadding(DimensUtils.dp2Px(this, 10));
        commonNavigator.setAdapter(new CommonNavigatorAdapter() {
            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public IPagerTitleView getTitleView(Context context, final int index) {
                ScaleTransitionPagerTitleView titleView = new ScaleTransitionPagerTitleView(context);
                titleView.setText(titles.get(index));
                titleView.setSelectedTextSize(20);
                titleView.setUnSelectedSize(16);
                titleView.setSelectedBold(true);
                titleView.setNormalColor(getResources().getColor(R.color.color_black_a60));
                titleView.setSelectedColor(getResources().getColor(R.color.color_black));
                titleView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)  {
                        viewPager.setCurrentItem(index, false);
                    }
                });
                return titleView;
            }

            @Override
            public IPagerIndicator getIndicator(Context context) {
                LinePagerIndicator indicator = new LinePagerIndicator(context);
                indicator.setMode(LinePagerIndicator.MODE_EXACTLY);
                indicator.setColors(getResources().getColor(R.color.color_main_red));
                indicator.setLineHeight(UIUtil.dip2px(context, 3));
                indicator.setLineWidth(UIUtil.dip2px(context, 10));
                indicator.setRoundRadius(UIUtil.dip2px(context, 2));
                indicator.setStartInterpolator(new AccelerateInterpolator());
                indicator.setEndInterpolator(new DecelerateInterpolator(2.0f));
                indicator.setYOffset(UIUtil.dip2px(context, 0));
                return indicator;
            }
        });
        magicIndicator.setNavigator(commonNavigator);
        ViewPagerHelper.bind(magicIndicator, viewPager);

        viewPager.setCurrentItem(default_index, false);
        curFragment = fragments.get(default_index);


    }


    /*@Override
    public void onRightButtonClicked(View view) {
        super.onRightButtonClicked(view);

        isInEditMode = !isInEditMode;

        for (CollectWallFragment temp:fragments) {
            temp.setEditMode(isInEditMode);
        }

        if(isInEditMode) {
            setRightButton("取消");
            setTvToolBarVisibility(GONE);
        } else {
            setRightButton("编辑");
            setTvToolBarVisibility(VISIBLE);
        }


    }*/

    private class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        private List<CollectWallFragment> mFragments;

        public SectionsPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        public SectionsPagerAdapter(FragmentManager fragmentManager, List<CollectWallFragment> fragments) {
//            super(getChildFragmentManager());
            super(fragmentManager);
            mFragments = fragments;
        }

        @Override
        public CollectWallFragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public int getItemPosition(Object object) {
            // TODO Auto-generated method stub
            return PagerAdapter.POSITION_NONE;
        }
    }

    private ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            curFragment = fragments.get(position);

            Map<String, String> map = new HashMap<>();
            map.put("item_name", position==0? "短视频": "正片");
            MobclickAgent.onEvent(CollectActivity.this, CLICK_COLLECT_TYPE_SWITCH, map);


        }

        @Override
        public void onPageScrolled(int position, float positionOffset,
                                   int positionOffsetPixels) {
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };


}
