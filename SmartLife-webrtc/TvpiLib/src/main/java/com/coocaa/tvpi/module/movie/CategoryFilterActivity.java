package com.coocaa.tvpi.module.movie;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.coocaa.publib.base.BaseActivity;
import com.coocaa.publib.data.BaseData;
import com.coocaa.publib.data.category.CategoryFilterPagerDataSubModel;
import com.coocaa.publib.data.category.CategoryFilterPagerResp;
import com.coocaa.publib.network.NetWorkManager;
import com.coocaa.publib.network.util.ParamsUtil;
import com.coocaa.publib.utils.IRLog;
import com.coocaa.tvpi.view.CustomViewPager;
import com.coocaa.tvpi.view.LoadTipsView;
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

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DefaultObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

/**
 * Created by IceStorm on 2017/12/14.
 */

public class CategoryFilterActivity extends BaseActivity {

    public final static String KEY_CLASSIFY_ID = "classify_id";
    public final static String KEY_CLASSIFY_NAME = "KEY_CLASSIFY_NAME";

    private static final String TAG = "CategoryFilterActivity";

    private LoadTipsView loadTipsView;

    private MagicIndicator magicIndicator;

    private CustomViewPager mViewPager;
    private CategoryFilterActivity.SectionsPagerAdapter mSectionsPagerAdapter;
    List<CategoryFilterWallFragment> fragments = new ArrayList<>();
    private CategoryFilterWallFragment curFragment = null;

    private int default_index = 0;

    private CategoryFilterPagerResp pagerResp;

    private String classify_id;
    private String classifyName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_filter);
        setTitle("分类名称");

        initData();

        initViews();

        Intent intent = getIntent();
        if (intent != null) {
            try {
                String extra = intent.getStringExtra(KEY_CLASSIFY_ID);
                classifyName = intent.getStringExtra(KEY_CLASSIFY_NAME);
                if (TextUtils.isEmpty(extra)) {
                    extra = "0";
                }
                classify_id = extra;

                if(TextUtils.isEmpty(classifyName)) {
                    classifyName = "";
                }
                setTitle(classifyName);
            } catch (Exception e) {
                classify_id = "0";
            }
        }

        loadTipsView.setVisibility(View.VISIBLE);
        loadTipsView.setLoadTipsIV(LoadTipsView.TYPE_LOADING);
        queryData();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(TAG); // 统计页面
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(TAG); // 统计页面
    }

    private void initData() {

    }

    private void initViews() {
        loadTipsView = findViewById(R.id.category_filter_loadtipview);
        loadTipsView.setLoadTipsOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            // 获取数据
            loadTipsView.setVisibility(View.VISIBLE);
            loadTipsView.setLoadTipsIV(LoadTipsView.TYPE_LOADING);

            queryData();
            }
        });

        mViewPager = findViewById(R.id.category_filter_viewpager);
        mViewPager.setScroll(true);
        mViewPager.setOffscreenPageLimit(3);
        magicIndicator = findViewById(R.id.category_filter_magic_indicator);

        findViewById(R.id.category_filter_back_iv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /*// 右边键被点击
    @Override
    public void onRightButtonClicked(View v) {
        super.onRightButtonClicked(v);

        Intent intent = new Intent(this, RemoteActivity.class);
        startActivity(intent);

        HashMap<String,String> map = new HashMap<String,String>();
        map.put("source_page", TAG);
        MobclickAgent.onEvent(this, CLICK_REMOTE_ENTER, map);
    }*/

    private class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        private List<CategoryFilterWallFragment> mFragments;

        public SectionsPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        public SectionsPagerAdapter(FragmentManager fragmentManager, List<CategoryFilterWallFragment> fragments) {
//            super(getChildFragmentManager());
            super(fragmentManager);
            mFragments = fragments;
        }

        @Override
        public CategoryFilterWallFragment getItem(int position) {
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
        }

        @Override
        public void onPageScrolled(int position, float positionOffset,
                                   int positionOffsetPixels) {
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };

    private void queryData() {
        HashMap<String,Object> queryParams = new HashMap<>();
        queryParams.put("classify_id", classify_id);
        NetWorkManager.getInstance()
                .getApiService()
                .getSubClassifyList(ParamsUtil.getQueryMap(queryParams))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultObserver<ResponseBody>() {
                    @Override
                    public void onNext(ResponseBody responseBody) {
                        String response = "";
                        try {
                            response = responseBody.string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        IRLog.d(TAG, "onSuccess. response = " + response);

                        if ( CategoryFilterActivity.this == null) {
                            Log.d(TAG, "onResponse: CategoryFilterActivity is destroed");
                            return;
                        }

//                if (response.equals(getLocalData())) {
//                    mLoadTipsView.setVisibility(View.GONE);
//                    return;
//                }

                        if (!TextUtils.isEmpty(response)) {
                            pagerResp = BaseData.load(response, CategoryFilterPagerResp.class);
                            if (pagerResp != null
                                    && pagerResp.data != null
                                    && pagerResp.data.sub_list != null
                                    && pagerResp.data.sub_list.size() > 0) {
//                        putLocalData(response);

                                updateViews();

                                loadTipsView.setVisibility(View.GONE);
                            }else{
//                        if (TextUtils.isEmpty(localResponse)) {
                                loadTipsView.setVisibility(View.VISIBLE);
//                        loadTipsView.setLoadTips(getString(R.string.title_loadtips_no_data), LoadTipsView.TYPE_NODATA);
                                loadTipsView.setLoadTips("", LoadTipsView.TYPE_NODATA);
//                        } else {
//                            mLoadTipsView.setVisibility(View.GONE);
//                        }

//                        Map map = new HashMap();
//                        if(pagerResp != null){
//                            map.put("errorCode", pagerResp.code);
//                        }else{
//                            map.put("errorCode","data format eror");
//                        }
//                        MobclickAgent.onEvent(getContext(),VideoUmengEventId.ERROR_H_VIDEO_GETVIDEOCLASSIFY,map);
                            }
                        } else {
                            loadTipsView.setVisibility(View.VISIBLE);
//                    loadTipsView.setLoadTips(getString(R.string.title_loadtips_no_data), LoadTipsView.TYPE_NODATA);
                            loadTipsView.setLoadTips("", LoadTipsView.TYPE_NODATA);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (null != e)
                            IRLog.d(TAG, "onFailure,statusCode:" + e.toString());

                        if ( CategoryFilterActivity.this == null) {
                            Log.d(TAG, "onResponse: CategoryFilterActivity is destroed");
                            return;
                        }

                        loadTipsView.setVisibility(View.VISIBLE);
//                loadTipsView.setLoadTips(getString(R.string.title_loadtips_login), LoadTipsView.TYPE_FAILED);
                        loadTipsView.setLoadTips("", LoadTipsView.TYPE_FAILED);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void updateViews() {
        fragments.clear();

        Log.d(TAG, "updateViews: fragments.size = " + fragments.size());
        setTitle(pagerResp.data.classify_name);

        for(int i = 0; i< pagerResp.data.sub_list.size(); i++){
            Log.d(TAG, "updateViews: title:" + pagerResp.data.sub_list.get(i).title);

            CategoryFilterPagerDataSubModel subModel = pagerResp.data.sub_list.get(i);
            CategoryFilterWallFragment fragment = new CategoryFilterWallFragment();
            fragment.setClassifyInfos(classify_id, subModel.sort_value, subModel.filter_value, subModel.extra_condition, classifyName, subModel.title);
            fragment.mCustomViewPager = new WeakReference<CustomViewPager>(mViewPager);;

            fragments.add(fragment);
        }

        Log.d(TAG, "updateViews: fragments.size = " + fragments.size());

        mViewPager.setOffscreenPageLimit(2);
        mViewPager.addOnPageChangeListener(onPageChangeListener);
        mSectionsPagerAdapter = new CategoryFilterActivity.SectionsPagerAdapter(getSupportFragmentManager(), fragments);
        mViewPager.setAdapter(mSectionsPagerAdapter);

//        magicIndicator.setBackgroundColor(getResources().getColor(R.color.colorBack_222222));
        CommonNavigator commonNavigator = new CommonNavigator(this);
//        commonNavigator.setLeftPadding(DimensUtils.dp2Px(this, 10));
        commonNavigator.setAdapter(new CommonNavigatorAdapter() {
            @Override
            public int getCount() {
                return pagerResp.data.sub_list == null ? 0 : pagerResp.data.sub_list.size();
            }

            @Override
            public IPagerTitleView getTitleView(Context context, final int index) {
                SimplePagerTitleView simplePagerTitleView = new ScaleTransitionPagerTitleView(context);
                simplePagerTitleView.setText(pagerResp.data.sub_list.get(index).title);
                simplePagerTitleView.setTextSize(18);
                ((ScaleTransitionPagerTitleView) simplePagerTitleView).setSelectedBold(true);
                simplePagerTitleView.setNormalColor(getResources().getColor(R.color.c_3));
                simplePagerTitleView.setSelectedColor(getResources().getColor(R.color.c_1));
                simplePagerTitleView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mViewPager.setCurrentItem(index, false);
                    }
                });
                return simplePagerTitleView;
            }

            @Override
            public IPagerIndicator getIndicator(Context context) {
                //下方黄线指示器
                LinePagerIndicator linePagerIndicator = new LinePagerIndicator(context);
                linePagerIndicator.setMode(LinePagerIndicator.MODE_EXACTLY);
                linePagerIndicator.setColors(getResources().getColor(R.color.b_5));
                linePagerIndicator.setLineHeight(UIUtil.dip2px(context, 2));
                linePagerIndicator.setLineWidth(UIUtil.dip2px(context, 18));
                linePagerIndicator.setRoundRadius(UIUtil.dip2px(context, 2));
                linePagerIndicator.setStartInterpolator(new AccelerateInterpolator());
                linePagerIndicator.setEndInterpolator(new DecelerateInterpolator(2.0f));
                linePagerIndicator.setYOffset(UIUtil.dip2px(context, 4));

                return linePagerIndicator;
            }
        });
        magicIndicator.setNavigator(commonNavigator);
        ViewPagerHelper.bind(magicIndicator, mViewPager);

        mViewPager.setCurrentItem(default_index, false);
        curFragment = fragments.get(default_index);
    }
}
