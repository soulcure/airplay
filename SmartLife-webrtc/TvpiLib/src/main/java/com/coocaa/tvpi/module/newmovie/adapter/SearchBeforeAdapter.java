package com.coocaa.tvpi.module.newmovie.adapter;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseDelegateMultiAdapter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.delegate.BaseMultiTypeDelegate;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.smartscreen.data.movie.Keyword;
import com.coocaa.tvpi.module.newmovie.bean.MovieSearchBeforeWrapBean;
import com.coocaa.tvpi.view.decoration.CommonHorizontalItemDecoration;
import com.coocaa.tvpilib.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 搜索前界面适配器
 * Created by songxing on 2020/7/13
 */
public class SearchBeforeAdapter extends BaseDelegateMultiAdapter<MovieSearchBeforeWrapBean, BaseViewHolder> {
    private SearchBeforeListener searchBeforeListener;


    public SearchBeforeAdapter() {
        super();
        setMultiTypeDelegate(new MyMultiTypeDelegate());
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, MovieSearchBeforeWrapBean searchBeforeWrapBean) {
        switch (holder.getItemViewType()) {
            case MovieSearchBeforeWrapBean.SEARCH_BEFORE_HISTORY:
                RecyclerView rvHistory = holder.getView(R.id.rvHistory);
                rvHistory.setHasFixedSize(true);
                if (rvHistory.getLayoutManager() == null) {
                    rvHistory.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                }
                if (rvHistory.getItemDecorationCount() == 0) {
                    CommonHorizontalItemDecoration decoration = new CommonHorizontalItemDecoration(
                            DimensUtils.dp2Px(getContext(), 15f), DimensUtils.dp2Px(getContext(), 10f));
                    rvHistory.addItemDecoration(decoration);
                }
                if (rvHistory.getAdapter() == null) {
                    SearchHistoryAdapter historyAdapter = new SearchHistoryAdapter();
                    rvHistory.setAdapter(historyAdapter);
                    historyAdapter.setList(searchBeforeWrapBean.getHistoryList());
                    historyAdapter.setOnItemClickListener(new OnItemClickListener() {
                        @Override
                        public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                            if (searchBeforeListener != null) {
                                searchBeforeListener.onHistorySearchClick(searchBeforeWrapBean.historyList.get(position).keyword);
                            }
                        }
                    });
                }
                holder.getView(R.id.ivDeleteHistory).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (searchBeforeListener != null) {
                            searchBeforeListener.clearHistorySearch();
                        }
                    }
                });
                break;
            case MovieSearchBeforeWrapBean.SEARCH_BEFORE_HOT:
                RecyclerView rvHot = holder.getView(R.id.rvHot);
                rvHot.setHasFixedSize(true);
                if (rvHot.getLayoutManager() == null) {
                    rvHot.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                }
                if (rvHot.getAdapter() == null) {
                    SearchHotAdapter hotAdapter = new SearchHotAdapter();
                    rvHot.setAdapter(hotAdapter);
                    hotAdapter.setList(searchBeforeWrapBean.getHotList());
                    hotAdapter.setOnItemClickListener(new OnItemClickListener() {
                        @Override
                        public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                            if (searchBeforeListener != null) {
                                searchBeforeListener.onHotSearchClick(searchBeforeWrapBean.getHotList().get(position).keyword);
                            }
                        }
                    });
                }
                break;
            default:
                break;
        }
    }

    final static class MyMultiTypeDelegate extends BaseMultiTypeDelegate<MovieSearchBeforeWrapBean> {

        public MyMultiTypeDelegate() {
            addItemType(MovieSearchBeforeWrapBean.SEARCH_BEFORE_HISTORY, R.layout.item_search_before_hostory);
            addItemType(MovieSearchBeforeWrapBean.SEARCH_BEFORE_HOT, R.layout.item_search_before_hot);
        }

        @Override
        public int getItemType(@NotNull List<? extends MovieSearchBeforeWrapBean> list, int position) {
            if (list.get(position) != null
                    && list.get(position).getHistoryList() != null
                    && list.get(position).getHistoryList().size() > 0) {
                return MovieSearchBeforeWrapBean.SEARCH_BEFORE_HISTORY;
            } else {
                return MovieSearchBeforeWrapBean.SEARCH_BEFORE_HOT;
            }
        }
    }

    /**
     * 搜索历史适配器
     */
    public static class SearchHistoryAdapter extends BaseQuickAdapter<Keyword, BaseViewHolder> {
        public SearchHistoryAdapter() {
            super(R.layout.item_search_history);
        }

        @Override
        protected void convert(@NotNull BaseViewHolder holder, Keyword keyword) {
            holder.setText(R.id.tvHistory, keyword.keyword);
        }
    }


    /**
     * 搜索热门适配器
     */
    public static class SearchHotAdapter extends BaseQuickAdapter<Keyword, BaseViewHolder> {
        public SearchHotAdapter() {
            super(R.layout.item_search_hot);
        }

        @Override
        protected void convert(@NotNull BaseViewHolder holder, Keyword keyword) {
            holder.setText(R.id.tvIndex, String.valueOf(holder.getAdapterPosition() + 1));
            holder.setText(R.id.tvName, keyword.keyword);
            if (holder.getAdapterPosition() == 0) {
                holder.setBackgroundResource(R.id.tvIndex, R.drawable.movie_search_hot_bg1);
            } else if (holder.getAdapterPosition() == 1) {
                holder.setBackgroundResource(R.id.tvIndex, R.drawable.movie_search_hot_bg2);
            } else if (holder.getAdapterPosition() == 2) {
                holder.setBackgroundResource(R.id.tvIndex, R.drawable.movie_search_hot_bg3);
            } else {
                holder.setBackgroundResource(R.id.tvIndex, R.drawable.movie_search_hot_other);
            }
        }
    }

    public void setSearchBeforeListener(SearchBeforeListener searchBeforeListener) {
        this.searchBeforeListener = searchBeforeListener;
    }

    public interface SearchBeforeListener {
        void onHotSearchClick(String hot);

        void onHistorySearchClick(String history);

        void clearHistorySearch();
    }

}
