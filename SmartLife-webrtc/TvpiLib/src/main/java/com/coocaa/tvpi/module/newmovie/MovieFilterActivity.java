package com.coocaa.tvpi.module.newmovie;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.smartscreen.data.movie.CategoryFilterModel;
import com.coocaa.smartscreen.data.movie.LongVideoListModel;
import com.coocaa.tvpi.base.mvvm.BaseViewModelAppletActivity;
import com.coocaa.tvpi.base.mvvm.view.DefaultLoadStateView;
import com.coocaa.tvpi.base.mvvm.view.LoadStateViewProvide;
import com.coocaa.tvpi.module.newmovie.adapter.MovieListAdapter;
import com.coocaa.tvpi.module.newmovie.viewmodel.MovieFilterViewModel;
import com.coocaa.tvpi.module.newmovie.widget.FilterConditionView;
import com.coocaa.tvpi.view.CommonTitleBar;
import com.coocaa.tvpi.view.SmartRefreshFooter;
import com.coocaa.tvpi.view.SmartRefreshHeader;
import com.coocaa.tvpi.view.decoration.PictureItemDecoration;
import com.coocaa.tvpilib.R;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 影视筛选
 * Created by songxing on 2020/7/15
 */
public class MovieFilterActivity extends BaseViewModelAppletActivity<MovieFilterViewModel> {
    private static final String TAG = MovieFilterActivity.class.getSimpleName();
    private CommonTitleBar titleBar;
    private FilterConditionView filterConditionView;
    private SmartRefreshLayout smartRefreshLayout;
    private RecyclerView rvMovie;
    private DefaultLoadStateView loadStateView;
    private MovieListAdapter movieListAdapter;
    private ViewGroup contentView;

    private String classifyId;
    private String classifyName;

    private int pageIndex = 0;
    private int pageSize = 18;
    private boolean isHasMoreData;
    private List<String> filterValues = new ArrayList<>();
    private List<String> sortValues = new ArrayList<>();
    private List<String> extraConditions = new ArrayList<>();

    public static void start(Context context, String classifyId, String classifyName) {
        Intent starter = new Intent(context, MovieFilterActivity.class);
        starter.putExtra("classifyId", classifyId);
        starter.putExtra("classifyName", classifyName);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contentView = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.activity_movie_filter_new, null);
        setContentView(contentView);
        parseIntent();
        initView();
        getFilterCondition();
        getMovieList(true);
    }

    @Override
    protected LoadStateViewProvide createLoadStateViewProvide() {
        return loadStateView;
    }

    private void parseIntent() {
        if (getIntent() != null) {
            classifyId = getIntent().getStringExtra("classifyId");
            classifyName = getIntent().getStringExtra("classifyName");
        }
    }

    private void initView() {
        titleBar = findViewById(R.id.titleBar);
        filterConditionView = findViewById(R.id.filterView);
        smartRefreshLayout = findViewById(R.id.smartRefreshLayout);
        smartRefreshLayout.setRefreshHeader(new SmartRefreshHeader(this));
        smartRefreshLayout.setRefreshFooter(new SmartRefreshFooter(this));

        loadStateView = findViewById(R.id.loadStateView);
        rvMovie = findViewById(R.id.rvMovie);
        rvMovie.setLayoutManager(new GridLayoutManager(this, 3));
        movieListAdapter = new MovieListAdapter();
        PictureItemDecoration decoration = new PictureItemDecoration(3,
                DimensUtils.dp2Px(this, 10),
                DimensUtils.dp2Px(this, 15));
        rvMovie.setAdapter(movieListAdapter);
        rvMovie.addItemDecoration(decoration);
        titleBar.setText(CommonTitleBar.TextPosition.TITLE, classifyName);
        titleBar.setOnClickListener(new CommonTitleBar.OnClickListener() {
            @Override
            public void onClick(CommonTitleBar.ClickPosition position) {
                if (position == CommonTitleBar.ClickPosition.LEFT) {
                    finish();
                } else {
                    MovieSearchActivity.start(MovieFilterActivity.this);
                }
            }
        });

        filterConditionView.setFilterConditionListener(new FilterConditionView.FilterConditionListener() {
            @Override
            public void onFilerConditionChange(List<String> filterValueList, List<String> sortValueList, List<String> extraConditionList) {
                filterValues = filterValueList;
                sortValues = sortValueList;
                extraConditions = extraConditionList;
                getMovieList(false);
            }
        });

        smartRefreshLayout.setEnableAutoLoadMore(false);
        smartRefreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                pageIndex = 0;
                getMovieList(false);
            }

            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                if (isHasMoreData) {
                    pageIndex++;
                    getMovieList(false);
                } else {
                    smartRefreshLayout.finishLoadMore();
                    ToastUtils.getInstance().showGlobalShort(getResources().getString(R.string.pull_no_more_msg));
                }
            }
        });

        loadStateView.setLoadTipsOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFilterCondition();
                getMovieList(true);
            }
        });

        initAppletTitle();
    }

    private void initAppletTitle() {
        if(mNPAppletInfo != null) {
            titleBar.setVisibility(View.GONE);
            if(mHeaderHandler != null) {
                mHeaderHandler.setTitle(TextUtils.isEmpty(classifyName) ? "搜索" : classifyName);
            }
            addEditView();
        }
    }

    private void addEditView() {
        View lineView = findViewById(R.id.titleBarLine);
        lineView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DimensUtils.dp2Px(this, 44)));

        View searchView = LayoutInflater.from(this).inflate(R.layout.movie_filter_search_view, null);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DimensUtils.dp2Px(this, 44));
        params.leftMargin = DimensUtils.dp2Px(this, 20);
        params.rightMargin = DimensUtils.dp2Px(this, 20);
        searchView.setLayoutParams(params);
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MovieSearchActivity.start(MovieFilterActivity.this);
            }
        });
        contentView.addView(searchView);
    }

    private void getFilterCondition() {
        viewModel.getFilterCondition(classifyId).observe(this,filterConditionObserver);
    }

    private final Observer<List<List<CategoryFilterModel>>> filterConditionObserver = new Observer<List<List<CategoryFilterModel>>>() {
        @Override
        public void onChanged(List<List<CategoryFilterModel>> lists) {
            filterConditionView.setFilterConditionList(lists);
        }
    };

    private void getMovieList(boolean showLoading) {
        viewModel.getMovieList(showLoading, classifyId, pageIndex, pageSize, filterValues, sortValues, extraConditions)
                .observe(this, movieListObserver);
    }

    private final Observer<List<LongVideoListModel>> movieListObserver = new Observer<List<LongVideoListModel>>() {
        @Override
        public void onChanged(List<LongVideoListModel> longVideoListModels) {
            if (pageIndex == 0) {
                movieListAdapter.setList(longVideoListModels);
                smartRefreshLayout.finishRefresh();
            } else {
                movieListAdapter.addData(longVideoListModels);
                smartRefreshLayout.finishLoadMore();
            }
            isHasMoreData = longVideoListModels.size() == pageSize;
        }
    };
}
