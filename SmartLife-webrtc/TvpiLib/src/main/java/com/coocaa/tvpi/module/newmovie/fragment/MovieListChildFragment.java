package com.coocaa.tvpi.module.newmovie.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.smartscreen.data.movie.LongVideoListModel;
import com.coocaa.tvpi.base.mvvm.BaseViewModelFragment;
import com.coocaa.tvpi.base.mvvm.view.DefaultLoadStateView;
import com.coocaa.tvpi.base.mvvm.view.LoadStateViewProvide;
import com.coocaa.tvpi.module.newmovie.adapter.MovieListAdapter;
import com.coocaa.tvpi.module.newmovie.viewmodel.MovieListChildViewModel;
import com.coocaa.tvpi.view.CustomFooter;
import com.coocaa.tvpi.view.CustomHeader;
import com.coocaa.tvpi.view.decoration.PictureItemDecoration;
import com.coocaa.tvpilib.R;
import com.liaoinstan.springview.widget.SpringView;

import java.util.List;

/**
 * 影视具体类型列表
 * Created by songxing on 2020/7/9
 */
public class MovieListChildFragment extends BaseViewModelFragment<MovieListChildViewModel> {
    private static final String TAG = MovieListChildFragment.class.getSimpleName();
    private SpringView springView;
    private DefaultLoadStateView loadStateView;
    private RecyclerView rvMovie;
    private MovieListAdapter movieListAdapter;

    private String classifyId;
    private int pageIndex = 0;
    private int pageSize = 18;
    private boolean isHasMoreData;

    public static MovieListChildFragment newInstance(String classifyId) {
        Bundle args = new Bundle();
        args.putString("classifyId", classifyId);
        MovieListChildFragment fragment = new MovieListChildFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_movie_list_child, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            classifyId = getArguments().getString("classifyId");
        }
        initView(view);
        initViewModel();
        getMovieList(true);
    }

    @Override
    protected LoadStateViewProvide createLoadStateViewProvide() {
        return loadStateView;
    }

    private void initView(View view) {
        springView = view.findViewById(R.id.springView);
        springView.setHeader(new CustomHeader(getContext()));
        springView.setFooter(new CustomFooter(getContext()));
        loadStateView = view.findViewById(R.id.loadStateView);
        rvMovie = view.findViewById(R.id.rvMovie);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
        PictureItemDecoration decoration = new PictureItemDecoration(3,
                DimensUtils.dp2Px(getContext(),10),
                DimensUtils.dp2Px(getContext(),15));
        movieListAdapter = new MovieListAdapter();
        rvMovie.setLayoutManager(layoutManager);
        rvMovie.addItemDecoration(decoration);
        rvMovie.setAdapter(movieListAdapter);
//        movieListAdapter.setEmptyView(getEmptyView());
        springView.setListener(new SpringView.OnFreshListener() {
            @Override
            public void onRefresh() {
                pageIndex = 0;
                getMovieList(false);
            }

            @Override
            public void onLoadmore() {
                if (isHasMoreData) {
                    pageIndex++;
                    getMovieList(false);
                } else {
                    springView.onFinishFreshAndLoad();
                    ToastUtils.getInstance().showGlobalShort(getResources().getString(R.string.pull_no_more_msg));
                }
            }
        });

        loadStateView.setLoadTipsOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMovieList(true);
            }
        });
    }


    private void getMovieList(boolean showLoading) {
        viewModel.getMovieList(showLoading,classifyId, pageIndex, pageSize)
                .observe(getViewLifecycleOwner(), movieListObserver);
    }

    private Observer<List<LongVideoListModel>> movieListObserver = new Observer<List<LongVideoListModel>>() {
        @Override
        public void onChanged(List<LongVideoListModel> longVideoListModels) {
            if (pageIndex == 0) {     //下拉刷新
                movieListAdapter.setList(longVideoListModels);
            } else {
                movieListAdapter.addData(longVideoListModels);
            }
            springView.onFinishFreshAndLoad();
            isHasMoreData = longVideoListModels.size() == pageSize;
        }
    };

}
