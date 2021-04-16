package com.coocaa.tvpi.base.mvvm.list;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.tvpi.base.mvvm.BaseViewModelActivity;
import com.coocaa.tvpi.base.mvvm.view.DefaultLoadStateView;
import com.coocaa.tvpi.base.mvvm.view.LoadStateViewProvide;
import com.coocaa.tvpi.view.CommonTitleBar;
import com.coocaa.tvpi.view.SmartRefreshFooter;
import com.coocaa.tvpi.view.SmartRefreshHeader;
import com.coocaa.tvpilib.R;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener;

import java.util.List;

/**
 * 简单列表Activity基类
 * Created by songxing on 2020/9/25
 */
public abstract class AbsListViewModelActivity<VM extends AbsListViewModel<ItemBean>, ItemBean> extends BaseViewModelActivity<VM> {
    private static final String TAG = AbsListViewModelActivity.class.getSimpleName();

    private DefaultLoadStateView loadStateView;
    private SmartRefreshLayout smartRefreshLayout;

    private String key;
    private int pageIndex = 0;
    private int pageSize = 15;
    private boolean isHasMoreData;
    private BaseQuickAdapter<ItemBean, BaseViewHolder> adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_abs_list);
        parserIntent();
        initView();
        setListener();
        getListData(true);
    }

    @Override
    protected LoadStateViewProvide createLoadStateViewProvide() {
        return loadStateView;
    }

    private void parserIntent() {
        if (getIntent() != null) {
            key = getIntent().getStringExtra("key");
        }
    }

    private void initView() {
        CommonTitleBar titleBar = findViewById(R.id.titleBar);
        initTitleBar(titleBar);
        loadStateView = findViewById(R.id.loadStateView);
        smartRefreshLayout = findViewById(R.id.smartRefreshLayout);
        smartRefreshLayout.setRefreshHeader(new SmartRefreshHeader(this));
        smartRefreshLayout.setRefreshFooter(new SmartRefreshFooter(this));
        smartRefreshLayout.setEnableAutoLoadMore(false);
        smartRefreshLayout.setEnableOverScrollBounce(true);

        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        RecyclerView.ItemDecoration decoration = createItemDecoration();
        if (decoration != null) {
            recyclerView.addItemDecoration(decoration);
        }
        RecyclerView.LayoutManager layoutManager = createLayoutManager();
        recyclerView.setLayoutManager(layoutManager);
        adapter = createAdapter();
        recyclerView.setAdapter(adapter);
    }

    protected abstract void initTitleBar(CommonTitleBar titleBar);

    protected abstract RecyclerView.LayoutManager createLayoutManager();

    protected abstract RecyclerView.ItemDecoration createItemDecoration();

    protected abstract BaseQuickAdapter<ItemBean, BaseViewHolder> createAdapter();

    private void setListener() {
        smartRefreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                pageIndex = 0;
                getListData(false);
            }

            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                if (isHasMoreData) {
                    pageIndex++;
                    getListData(false);
                } else {
                    smartRefreshLayout.finishLoadMore();
                    ToastUtils.getInstance().showGlobalShort(getResources().getString(R.string.pull_no_more_msg));
                }
            }
        });
    }

    private void getListData(boolean showLoading) {
        viewModel.getListData(showLoading, key, pageIndex, pageSize).observe(this, listObserver);
    }

    private Observer<List<ItemBean>> listObserver = new Observer<List<ItemBean>>() {
        @Override
        public void onChanged(List<ItemBean> listData) {
            Log.d(TAG, "listObserver onChanged: " + listData);
            if (pageIndex == 0) {
                adapter.setList(listData);
                smartRefreshLayout.finishRefresh();
            } else {
                adapter.addData(listData);
                smartRefreshLayout.finishLoadMore();
            }
            isHasMoreData = listData.size() == pageSize;
        }
    };
}
