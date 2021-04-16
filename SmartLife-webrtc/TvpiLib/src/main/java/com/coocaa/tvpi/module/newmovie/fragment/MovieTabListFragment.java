package com.coocaa.tvpi.module.newmovie.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.smartscreen.data.movie.CategoryMainModel;
import com.coocaa.tvpi.base.mvvm.BaseViewModelAppletFragment;
import com.coocaa.tvpi.base.mvvm.view.DefaultLoadStateView;
import com.coocaa.tvpi.base.mvvm.view.LoadStateViewProvide;
import com.coocaa.tvpi.module.newmovie.MovieFilterActivity;
import com.coocaa.tvpi.module.newmovie.MovieHomeActivity;
import com.coocaa.tvpi.module.newmovie.MovieSearchActivity;
import com.coocaa.tvpi.module.newmovie.adapter.MoviePagerAdapter;
import com.coocaa.tvpi.module.newmovie.viewmodel.MovieTabListViewModel;
import com.coocaa.tvpi.view.CommonTitleBar;
import com.coocaa.tvpi.view.ScaleTransitionPagerTitleView;
import com.coocaa.tvpilib.R;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;
import net.lucode.hackware.magicindicator.buildins.UIUtil;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.viewpager.widget.ViewPager;

/**
 * 影视投屏--Tab影视
 * Created by songxing on 2020/7/9
 */
public class MovieTabListFragment extends BaseViewModelAppletFragment<MovieTabListViewModel> {

    private DefaultLoadStateView loadStateView;
    private CommonTitleBar titleBar;
    private FrameLayout filterLayout;
    private MagicIndicator indicator;
    private ViewPager viewPager;
    private MoviePagerAdapter pagerAdapter;
    private List<Fragment> fragments = new ArrayList<>();

    private int selectedPosition;
    private List<CategoryMainModel> categoryMainModelList;

    @Override
    protected LoadStateViewProvide createLoadStateViewProvide() {
        return loadStateView;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_movie_tab_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        setListener();
        getMainCategory();
    }

    private void initView(View view) {
        titleBar = view.findViewById(R.id.titleBar);
        filterLayout = view.findViewById(R.id.filterLayout);
        indicator = view.findViewById(R.id.magicIndicator);
        viewPager = view.findViewById(R.id.viewPager);
        loadStateView = view.findViewById(R.id.loadStateView);
        if(mNPAppletInfo != null) {
            titleBar.setVisibility(View.GONE);
            if(mHeaderHandler != null) {
                mHeaderHandler.setBackButtonIcon(getResources().getDrawable(R.drawable.movie_search));
                mHeaderHandler.setBackButtonOnClickListener(new Runnable() {
                    @Override
                    public void run() {
                        MovieSearchActivity.start(getContext());
                    }
                });
                mHeaderHandler.setBackButtonVisible(true);
                mHeaderHandler.setTitle("看影视");

            }
        } else {
            View titleBarLine = view.findViewById(R.id.titleBarLine);
            titleBarLine.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, DimensUtils.dp2Px(getContext(), 60)));
        }

        loadStateView.setLoadTipsOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMainCategory();
            }
        });
    }

    private void setListener() {
        titleBar.setOnClickListener(new CommonTitleBar.OnClickListener() {
            @Override
            public void onClick(CommonTitleBar.ClickPosition position) {
                if (position == CommonTitleBar.ClickPosition.LEFT) {
                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                } else {
                    MovieSearchActivity.start(getContext());
                }
            }
        });

        filterLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != categoryMainModelList) {
                    CategoryMainModel categoryModel = categoryMainModelList.get(selectedPosition);
                    MovieFilterActivity.start(getContext(), categoryModel.classify_id, categoryModel.classify_name);
                }
            }
        });
    }

    private void getMainCategory() {
        if(getActivity() == null) return;
        String source = ((MovieHomeActivity) getActivity()).source;
        viewModel.getMainCategoryList(source).observe(getViewLifecycleOwner(), new Observer<List<CategoryMainModel>>() {
            @Override
            public void onChanged(List<CategoryMainModel> categoryMainModels) {
                categoryMainModelList = categoryMainModels;
                setIndicatorAndViewPage(categoryMainModels);
            }
        });
    }

    private void setIndicatorAndViewPage(List<CategoryMainModel> categoryList) {
        fragments.clear();
        for (CategoryMainModel categoryMainModel : categoryList) {
            MovieListChildFragment fragment = MovieListChildFragment.newInstance(categoryMainModel.classify_id);
            fragments.add(fragment);
        }
        pagerAdapter = new MoviePagerAdapter(getChildFragmentManager(), fragments);
        viewPager.setAdapter(pagerAdapter);
        CommonNavigator commonNavigator = new CommonNavigator(getContext());
        commonNavigator.setAdapter(new CommonNavigatorAdapter() {
            @Override
            public int getCount() {
                return fragments.size();
            }

            @Override
            public IPagerTitleView getTitleView(Context context, final int index) {
                ScaleTransitionPagerTitleView titleView = new ScaleTransitionPagerTitleView(context);
                titleView.setText(categoryList.get(index).classify_name);
                titleView.setSelectedTextSize(20);
                titleView.setUnSelectedSize(16);
                titleView.setSelectedBold(true);
                titleView.setNormalColor(getResources().getColor(R.color.color_black_a60));
                titleView.setSelectedColor(getResources().getColor(R.color.color_black));
                titleView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
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
        indicator.setNavigator(commonNavigator);
        ViewPagerHelper.bind(indicator, viewPager);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setCurrentItem(0, false);
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                selectedPosition = position;
            }
        });
    }
}
