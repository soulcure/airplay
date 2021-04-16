package com.coocaa.tvpi.module.newmovie.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.coocaa.movie.MovieProductActivity;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.publib.utils.UIHelper;
import com.coocaa.smartscreen.data.movie.CollectionModel;
import com.coocaa.smartscreen.data.movie.PushHistoryModel;
import com.coocaa.tvpi.base.mvvm.BaseViewModelAppletFragment;
import com.coocaa.tvpi.module.newmovie.CollectionActivity;
import com.coocaa.tvpi.module.newmovie.MovieSearchActivity;
import com.coocaa.tvpi.module.newmovie.PushHistoryActivity;
import com.coocaa.tvpi.module.newmovie.adapter.CollectionAdapter;
import com.coocaa.tvpi.module.newmovie.adapter.PushHistoryAdapter;
import com.coocaa.tvpi.module.newmovie.viewmodel.MovieTabMineViewModel;
import com.coocaa.tvpi.view.CommonTitleBar;
import com.coocaa.tvpi.view.decoration.CommonHorizontalItemDecoration;
import com.coocaa.tvpilib.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 影视投屏--Tab我的
 * Created by songxing on 2020/7/9
 */
public class MovieTabMineFragment extends BaseViewModelAppletFragment<MovieTabMineViewModel> {
    private CommonTitleBar titleBar;
    private LinearLayout historyLayout;
    private LinearLayout collectionLayout;
    private RecyclerView rvHistory;
    private RecyclerView rvCollection;
    private CollectionAdapter collectionAdapter;
    private PushHistoryAdapter pushHistoryAdapter;
    private ImageView ivVip;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_movie_tab_mine, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        setListener();
        getCollectionList();
        getPushHistoryList();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            getCollectionList();
            getPushHistoryList();
        }
    }

    private void initView(View view) {
        titleBar = view.findViewById(R.id.titleBar);
        historyLayout = view.findViewById(R.id.historyLayout);
        collectionLayout = view.findViewById(R.id.collectionLayout);
        rvHistory = view.findViewById(R.id.rvHistory);
        rvCollection = view.findViewById(R.id.rvCollection);
        RecyclerView.LayoutManager historyManager = new LinearLayoutManager(getContext(),
                RecyclerView.HORIZONTAL, false);
        CommonHorizontalItemDecoration historyDecoration = new CommonHorizontalItemDecoration(
                DimensUtils.dp2Px(getContext(), 10));
        rvHistory.setLayoutManager(historyManager);
        rvHistory.addItemDecoration(historyDecoration);
        pushHistoryAdapter = new PushHistoryAdapter();
        rvHistory.setAdapter(pushHistoryAdapter);
        RecyclerView.LayoutManager collectManager = new LinearLayoutManager(getContext(),
                RecyclerView.HORIZONTAL, false);
        CommonHorizontalItemDecoration collectDecoration = new CommonHorizontalItemDecoration(
                DimensUtils.dp2Px(getContext(), 10));
        rvCollection.setLayoutManager(collectManager);
        rvCollection.addItemDecoration(collectDecoration);
        collectionAdapter = new CollectionAdapter();
        rvCollection.setAdapter(collectionAdapter);
        ivVip = view.findViewById(R.id.ivVip);

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
            }
        } else {
//            View titleBarLine = view.findViewById(R.id.titleBarLine);
//            titleBarLine.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DimensUtils.dp2Px(getContext(), 60)));
        }
    }

    private void setListener() {
        titleBar.setOnClickListener(new CommonTitleBar.OnClickListener() {
            @Override
            public void onClick(CommonTitleBar.ClickPosition position) {
                if (position == CommonTitleBar.ClickPosition.LEFT) {
                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                }
            }
        });

        /*if (getActivity() != null
                && !TextUtils.isEmpty(((MovieHomeActivity) getActivity()).source)
                && ((MovieHomeActivity) getActivity()).source.equals("iqiyi")) {
            ivVip.setVisibility(View.VISIBLE);
        }*/

        ivVip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), MovieProductActivity.class));
            }
        });

        historyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PushHistoryActivity.start(getContext());
            }
        });

        collectionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CollectionActivity.start(getContext());
            }
        });

        collectionAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                CollectionModel model = (CollectionModel) adapter.getData().get(position);
                UIHelper.startActivityByURL(getContext(), model.router);
            }
        });

        pushHistoryAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                PushHistoryModel.PushHistoryVideoModel model = (PushHistoryModel.PushHistoryVideoModel) adapter.getData().get(position);
                UIHelper.startActivityByURL(getContext(), model.router);
            }
        });
    }

    private void getCollectionList() {
        viewModel.getCollectionList().observe(getViewLifecycleOwner(), collectionObserver);
    }

    private Observer<List<CollectionModel>> collectionObserver = new Observer<List<CollectionModel>>() {
        @Override
        public void onChanged(List<CollectionModel> collectionModels) {
            collectionAdapter.setList(collectionModels);
        }
    };

    private void getPushHistoryList() {
        viewModel.getPushHistoryModel().observe(getViewLifecycleOwner(), pushHistoryObserver);
    }

    private Observer<List<PushHistoryModel.PushHistoryVideoModel>> pushHistoryObserver = new Observer<List<PushHistoryModel.PushHistoryVideoModel>>() {
        @Override
        public void onChanged(List<PushHistoryModel.PushHistoryVideoModel> pushHistoryVideoModels) {
            pushHistoryAdapter.setList(pushHistoryVideoModels);
        }
    };
}
