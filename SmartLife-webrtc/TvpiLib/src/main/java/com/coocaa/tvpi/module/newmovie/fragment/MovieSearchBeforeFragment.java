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

import com.coocaa.tvpi.base.mvvm.BaseViewModelFragment;
import com.coocaa.tvpi.base.mvvm.view.DefaultLoadStateView;
import com.coocaa.tvpi.base.mvvm.view.LoadStateViewProvide;
import com.coocaa.tvpi.module.newmovie.adapter.SearchBeforeAdapter;
import com.coocaa.tvpi.module.newmovie.bean.MovieSearchBeforeWrapBean;
import com.coocaa.tvpi.module.newmovie.viewmodel.SearchBeforeViewModel;
import com.coocaa.tvpi.module.newmovie.viewmodel.share.MovieSearchShareViewModel;
import com.coocaa.tvpilib.R;

import java.util.List;

/**
 * 影视搜索前界面
 * Created by songxing on 2020/9/16
 */
public class MovieSearchBeforeFragment extends BaseViewModelFragment<SearchBeforeViewModel> {
    private static final String TAG = MovieSearchBeforeFragment.class.getSimpleName();
    private DefaultLoadStateView loadStateView;
    private SearchBeforeAdapter searchBeforeAdapter;
    private MovieSearchShareViewModel shareViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_movie_search_before, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        getSearchBeforeList();
    }

    @Override
    protected LoadStateViewProvide createLoadStateViewProvide() {
        return loadStateView;
    }

    private void initView(View view) {
        if (getActivity() == null) return;
        loadStateView = view.findViewById(R.id.loadStateView);
        RecyclerView rvSearchBefore = view.findViewById(R.id.rvSearchBefore);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        searchBeforeAdapter = new SearchBeforeAdapter();
        rvSearchBefore.setLayoutManager(manager);
        rvSearchBefore.setAdapter(searchBeforeAdapter);
        shareViewModel = ViewModelProviders.of(getActivity()).get(MovieSearchShareViewModel.class);
        searchBeforeAdapter.setSearchBeforeListener(new SearchBeforeAdapter.SearchBeforeListener() {
            @Override
            public void onHotSearchClick(String hot) {
                shareViewModel.setSearchKeyword(hot);
                shareViewModel.setShowSearchBefore(false);
            }

            @Override
            public void onHistorySearchClick(String history) {
                shareViewModel.setSearchKeyword(history);
                shareViewModel.setShowSearchBefore(false);
            }

            @Override
            public void clearHistorySearch() {
                deleteHistory();
            }
        });

        loadStateView.setLoadTipsOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSearchBeforeList();
            }
        });
    }

    private void deleteHistory() {
        viewModel.deleteSearchHistory().observe(this, deleteHistoryObserver);
    }

    private Observer<Boolean> deleteHistoryObserver = new Observer<Boolean>() {
        @Override
        public void onChanged(Boolean success) {
            Log.d(TAG, "deleteHistoryObserver onChanged: " + success);
            if (success) {
                searchBeforeAdapter.getData().remove(0);
                searchBeforeAdapter.notifyDataSetChanged();
            }
        }
    };


    private void getSearchBeforeList() {
        viewModel.getSearchBeforeList().observe(getViewLifecycleOwner(), searchBeforeObserver);
    }

    private final Observer<List<MovieSearchBeforeWrapBean>> searchBeforeObserver = new Observer<List<MovieSearchBeforeWrapBean>>() {
        @Override
        public void onChanged(List<MovieSearchBeforeWrapBean> searchBeforeWrapBeans) {
            Log.d(TAG, "getSearchBeforeList onChanged: " + searchBeforeWrapBeans);
            searchBeforeAdapter.setList(searchBeforeWrapBeans);
        }
    };
}
