package com.coocaa.tvpi.module.newmovie.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.smartscreen.data.movie.LongVideoSearchResultModel;
import com.coocaa.tvpi.base.mvvm.BaseViewModelFragment;
import com.coocaa.tvpi.base.mvvm.view.DefaultLoadStateView;
import com.coocaa.tvpi.base.mvvm.view.LoadStateViewProvide;
import com.coocaa.tvpi.module.newmovie.adapter.SearchResultAdapter;
import com.coocaa.tvpi.module.newmovie.viewmodel.SearchResultViewModel;
import com.coocaa.tvpi.module.newmovie.viewmodel.share.MovieSearchShareViewModel;
import com.coocaa.tvpi.view.CustomFooter;
import com.coocaa.tvpi.view.CustomHeader;
import com.coocaa.tvpi.view.decoration.CommonVerticalItemDecoration;
import com.coocaa.tvpilib.R;
import com.liaoinstan.springview.widget.SpringView;

import java.util.List;

/**
 * 搜索结果界面
 * Created by songxing on 2020/7/14
 */
public class MovieSearchResultFragment extends BaseViewModelFragment<SearchResultViewModel> {
    private static final String TAG = MovieSearchResultFragment.class.getSimpleName();
    private DefaultLoadStateView loadStateView;
    private SpringView springView;
    private RecyclerView rvSearch;
    private SearchResultAdapter searchResultAdapter;

    private String keyword;
    private int pageIndex = 0;
    private int pageSize = 10;
    private boolean isHasMoreData;

    private LongVideoSearchResultModel collectionModel;
    private MovieSearchShareViewModel shareViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_movie_search_result, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        getKeyword();
    }

    @Override
    protected LoadStateViewProvide createLoadStateViewProvide() {
        return loadStateView;
    }

    private void initView(View view) {
        if(getActivity() == null) return;
        springView = view.findViewById(R.id.springView);
        springView.setHeader(new CustomHeader(getContext()));
        springView.setFooter(new CustomFooter(getContext()));
        loadStateView = view.findViewById(R.id.loadStateView);
        rvSearch = view.findViewById(R.id.rvSearchResult);
        LinearLayoutManager manager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        CommonVerticalItemDecoration decoration = new CommonVerticalItemDecoration(DimensUtils.dp2Px(getContext(), 10));
        searchResultAdapter = new SearchResultAdapter();
        rvSearch.setLayoutManager(manager);
        rvSearch.addItemDecoration(decoration);
        rvSearch.setAdapter(searchResultAdapter);
        shareViewModel = ViewModelProviders.of(getActivity()).get(MovieSearchShareViewModel.class);
        springView.setListener(new SpringView.OnFreshListener() {
            @Override
            public void onRefresh() {
                pageIndex = 0;
                search(false);
            }

            @Override
            public void onLoadmore() {
                if (isHasMoreData) {
                    pageIndex++;
                    search(false);
                } else {
                    springView.onFinishFreshAndLoad();
                    ToastUtils.getInstance().showGlobalShort(getResources().getString(R.string.pull_no_more_msg));
                }
            }
        });

        searchResultAdapter.setSearchListener(new SearchResultAdapter.SearchListener() {
            @Override
            public void onCollectionClick(LongVideoSearchResultModel longVideoSearchResultModel) {
                collectionModel = longVideoSearchResultModel;
                viewModel.collection(collectionModel).observe(getViewLifecycleOwner(), collectionObserver);
            }
        });

        loadStateView.setLoadTipsOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search(true);
            }
        });
    }

    private final Observer<Boolean> collectionObserver = new Observer<Boolean>() {
        @Override
        public void onChanged(Boolean success) {
            if (success) {
                collectionModel.video_detail.is_collect = collectionModel.video_detail.is_collect == 1 ? 2 : 1;
                searchResultAdapter.notifyDataSetChanged();
            }
        }
    };

    private void getKeyword() {
        shareViewModel.getKeywordLiveData().observe(getViewLifecycleOwner(), keyObserver);
    }

    private final Observer<String> keyObserver = new Observer<String>() {
        @Override
        public void onChanged(String s) {
            Log.d(TAG, "onChanged: keyword " + s);
            keyword = s;
            searchResultAdapter.setKeyword(s);
            search(true);
        }
    };

    private void search(boolean showLoading) {
        viewModel.search(showLoading,keyword, pageIndex, pageSize)
                .observe(getViewLifecycleOwner(), searchResultObserver);
    }

    private final Observer<List<LongVideoSearchResultModel>> searchResultObserver = new Observer<List<LongVideoSearchResultModel>>() {
        @Override
        public void onChanged(List<LongVideoSearchResultModel> longVideoSearchResultModels) {
            Log.d(TAG, "onChanged: longVideoSearchResultModels " + longVideoSearchResultModels.size());
            if (pageIndex == 0) {
                searchResultAdapter.setList(longVideoSearchResultModels);
            } else {
                searchResultAdapter.addData(longVideoSearchResultModels);
            }
            springView.onFinishFreshAndLoad();
            isHasMoreData = longVideoSearchResultModels.size() == pageSize;
        }
    };
}
