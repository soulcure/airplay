package com.coocaa.tvpi.module.live;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;

import com.coocaa.publib.data.BaseData;
import com.coocaa.publib.data.tvlive.TVLiveAnim;
import com.coocaa.publib.data.tvlive.TVLiveCategoryResp;

import com.coocaa.publib.network.util.ParamsUtil;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.publib.utils.IRLog;
import com.coocaa.smartscreen.network.NetWorkManager;
import com.coocaa.tvpi.base.mvvm.BaseViewModelAppletFragment;
import com.coocaa.tvpi.module.live.adapter.TVLiveCategoryAdapter;
import com.coocaa.tvpi.view.CustomViewPager;
import com.coocaa.tvpi.view.LoadTipsView;
import com.coocaa.tvpi.view.decoration.CommonVerticalItemDecoration;
import com.coocaa.tvpilib.R;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DefaultObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import swaiotos.runtime.base.AppletActivity;
import swaiotos.runtime.np.NPAppletActivity;

/**
 * @ClassName TVLiveFragment
 * @Description 直播页面
 * @User heni
 * @Date 2019/1/10
 */
public class TVLiveFragment extends Fragment {
    private static final String TAG = TVLiveFragment.class.getSimpleName();
    public static final String MY_LOCAL_COLLECT = "MyLocalCollect";

    private View mLayout;
    private RecyclerView mCategoryRecyclerView;
    private TVLiveCategoryAdapter mCategoryAdapter;
    private LoadTipsView mLoadTipsView;
    private ImageView mAnimImg;//anim_mark_icon

    private CustomViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    List<TVProgramFragment> fragments = new ArrayList<>();

    private TVLiveCategoryResp mCategoryResp;

    protected NPAppletActivity.NPAppletInfo mNPAppletInfo;
    protected AppletActivity.HeaderHandler mHeaderHandler;
    private String networkForceKey;

    public TVLiveFragment setAppletInfo(NPAppletActivity.NPAppletInfo appletInfo) {
        this.mNPAppletInfo = appletInfo;
        return this;
    }

    public TVLiveFragment setAppletHeaderHandler(AppletActivity.HeaderHandler headerHandler) {
        this.mHeaderHandler = headerHandler;
        return this;
    }
    public TVLiveFragment setNetworkForceKey(String networkForceKey) {
        this.networkForceKey = networkForceKey;
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        mLayout = inflater.inflate(R.layout.fragment_tvlive, container, false);
        return mLayout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initViews();
        queryCategoryData();
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

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    public void scrollToTop() {
        /*if (null != mCategoryRecyclerView)
            mCategoryRecyclerView.smoothScrollToPosition(0);
        if (null != mProgramRecyclerView)
            mProgramRecyclerView.smoothScrollToPosition(0);*/
    }

    private void initViews() {
        mAnimImg = mLayout.findViewById(R.id.anim_mark_icon);
        mLoadTipsView = mLayout.findViewById(R.id.tvlive_loadtipview);
        mLoadTipsView.setLoadTipsOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLoadTipsView.setVisibility(View.VISIBLE);
                mLoadTipsView.setLoadTipsIV(LoadTipsView.TYPE_LOADING);
                queryCategoryData();
            }
        });

        mCategoryRecyclerView = mLayout.findViewById(R.id.tvlive_category_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mCategoryRecyclerView.setLayoutManager(layoutManager);
        CommonVerticalItemDecoration decoration = new CommonVerticalItemDecoration(0, 0, DimensUtils.dp2Px(getActivity(), 50f));
        mCategoryRecyclerView.addItemDecoration(decoration);
        mCategoryAdapter = new TVLiveCategoryAdapter(getActivity(), mCategoryRecyclerView,
                layoutManager);
        mCategoryAdapter.setOnItemClickListener(new TVLiveCategoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
//                mViewPager.setCurrentItem(position);
                switchFragment(fragments.get(position));
            }
        });
        mCategoryRecyclerView.setAdapter(mCategoryAdapter);

        mViewPager = mLayout.findViewById(R.id.tvlive_viewpager);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setScroll(false);
    }

    private void queryCategoryData() {
        NetWorkManager.getInstance()
                .getApiService()
                .getTVLiveCategory(ParamsUtil.getQueryMap(null))
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
                        Log.d(TAG, "URL_TVLIVE_CHANNEL_CLASS onResponse: " + response);

                        if (TVLiveFragment.this == null || getActivity() == null) {
                            IRLog.e(TAG, "fragment or activity was destroyed");
                            return;
                        }

                        if (!TextUtils.isEmpty(response)) {
                            mCategoryResp = BaseData.load(response, TVLiveCategoryResp.class);
                            if (mCategoryResp != null && mCategoryResp.data != null) {
                                updateViews();
                            } else {
                                mLoadTipsView.setLoadTips("", LoadTipsView.TYPE_NODATA);
                                mLoadTipsView.setVisibility(View.VISIBLE);
                            }
                        } else {
                            mLoadTipsView.setLoadTips("", LoadTipsView.TYPE_NODATA);
                            mLoadTipsView.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e != null)
                            IRLog.e(TAG, "tvlive channel class onError: " + e.getMessage());

                        if (TVLiveFragment.this == null || getActivity() == null) {
                            IRLog.e(TAG, "fragment or activity was destroyed");
                            return;
                        }
                        mLoadTipsView.setVisibility(View.VISIBLE);
                        mLoadTipsView.setLoadTips("", LoadTipsView.TYPE_FAILED);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void updateViews() {
        mCategoryResp.data.add(0, TVLiveFragment.MY_LOCAL_COLLECT);
        mCategoryAdapter.addAll(mCategoryResp.data);
        mLoadTipsView.setVisibility(View.GONE);

        fragments.clear();
        Log.d(TAG, "updateViews: fragments.size = " + fragments.size());

        for(int i = 0; i< mCategoryResp.data.size(); i++){
            Log.d(TAG, "updateViews: stype_id:" + mCategoryResp.data.get(i));

            TVProgramFragment fragment = new TVProgramFragment();
            fragment.setClassName(mCategoryResp.data.get(i));
            fragment.setNetworkForceKey(networkForceKey);
            fragments.add(fragment);
        }

        Log.d(TAG, "updateViews: fragments.size = " + fragments.size());

        /*mViewPager.setOffscreenPageLimit(1);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager(), fragments);
        mViewPager.setAdapter(mSectionsPagerAdapter);*/
        if(fragments.size() > 1){
            switchFragment(fragments.get(1));
        }else {
            switchFragment(fragments.get(0));
        }

    }

    private class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        private List<TVProgramFragment> mFragments;

        public SectionsPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        public SectionsPagerAdapter(FragmentManager fragmentManager, List<TVProgramFragment> fragments) {
//            super(getChildFragmentManager());
            super(fragmentManager);
            mFragments = fragments;
        }

        @Override
        public TVProgramFragment getItem(int position) {
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

    private  View mBigStar;

    @Subscribe
    public void onEvent(TVLiveAnim tvLiveAnim) {
        if (tvLiveAnim != null && tvLiveAnim.mView != null) {
            if(tvLiveAnim.mType == 1) {
                mBigStar = tvLiveAnim.mView;
            }
            if (tvLiveAnim.mType == 2 && mBigStar != null) {
                showAnim(tvLiveAnim.mView);
            }
        }
    }

    private void showAnim(final View smallView) {
        int[] location = new int[2];
        smallView.getLocationOnScreen(location);
        float smallStarX = location[0]; //px
        float smallStarY = location[1] - getResources().getDimension(R.dimen.actionbar_height)
                - DimensUtils.getStatusBarHeight(getActivity());

        mBigStar.getLocationOnScreen(location);
        float bigStarX = location[0];
        float bigStarY = location[1] - getResources().getDimension(R.dimen.actionbar_height);

        //大星星动画
        PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("scaleX", 1.0f, 1.5f, 1.0f);
        PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat("scaleY", 1.0f, 1.5f, 1.0f);

        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(mBigStar, pvhX, pvhY).setDuration(300);
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }
        });
        objectAnimator.start();

        //小星星位移
        PropertyValuesHolder pvhX1 = PropertyValuesHolder.ofFloat("alpha", 1f, 0f);
        PropertyValuesHolder pvhY1 = PropertyValuesHolder.ofFloat("translationX", smallStarX,
                DimensUtils.dp2Px(getActivity(), bigStarX - smallStarX));
        PropertyValuesHolder pvhZ1 = PropertyValuesHolder.ofFloat("translationY",  smallStarY,
                DimensUtils.dp2Px(getActivity(), bigStarY - smallStarY));
        ObjectAnimator objectAnimator1 = ObjectAnimator.ofPropertyValuesHolder(mAnimImg, pvhX1,
                pvhY1, pvhZ1).setDuration(300);
        objectAnimator1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }
        });
        objectAnimator1.start();
    }

    private  Fragment  currentFragment;
    private void switchFragment(Fragment targetFragment) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        if (!targetFragment.isAdded()) {
            //第一次使用switchFragment()时currentFragment为null，所以要判断一下
            if (currentFragment != null) {
                transaction.hide(currentFragment);
            }
            transaction.add(R.id.tvlive_fragment_frame, targetFragment,targetFragment.getClass().getName());
        } else {
            transaction.hide(currentFragment).show(targetFragment);
        }
        currentFragment = targetFragment;
        transaction.commitAllowingStateLoss();
    }

}
