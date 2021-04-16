package com.coocaa.tvpi.module.app.adapter;

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
import com.coocaa.smartscreen.data.app.AppModel;
import com.coocaa.tvpi.module.app.bean.AppSearchBeforeWrapBean;
import com.coocaa.tvpi.view.decoration.CommonHorizontalItemDecoration;
import com.coocaa.tvpi.view.decoration.CommonVerticalItemDecoration;
import com.coocaa.tvpilib.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AppSearchBeforeAdapter extends BaseDelegateMultiAdapter<AppSearchBeforeWrapBean, BaseViewHolder> {
    private SearchBeforeListener searchBeforeListener;

    public AppSearchBeforeAdapter() {
        super();
        setMultiTypeDelegate(new MyMultiTypeDelegate());
    }
    @Override
    protected void convert(@NotNull BaseViewHolder holder, AppSearchBeforeWrapBean appSearchBeforeWrapBean) {
        switch (holder.getItemViewType()) {
            case AppSearchBeforeWrapBean.SEARCH_BEFORE_HISTORY:
                RecyclerView rvHistory = holder.getView(R.id.rvHistory);
                if (rvHistory.getLayoutManager() == null) {
                    rvHistory.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                }
                if (rvHistory.getItemDecorationCount() == 0) {
                    CommonHorizontalItemDecoration decoration = new CommonHorizontalItemDecoration(
                            DimensUtils.dp2Px(getContext(), 15f), DimensUtils.dp2Px(getContext(), 10f));
                    rvHistory.addItemDecoration(decoration);
                }
                if (rvHistory.getAdapter() == null) {
                    AppSearchHistoryAdapter historyAdapter = new AppSearchHistoryAdapter();
                    rvHistory.setAdapter(historyAdapter);
                    historyAdapter.setList(appSearchBeforeWrapBean.getHistoryList());
                    historyAdapter.setOnItemClickListener(new OnItemClickListener() {
                        @Override
                        public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                            if (searchBeforeListener != null) {
                                searchBeforeListener.onHistorySearchClick(appSearchBeforeWrapBean.historyList.get(position));
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
            case AppSearchBeforeWrapBean.SEARCH_BEFORE_RECOMMEND:
                RecyclerView rvRecommend = holder.getView(R.id.rvRecommend);
                if (rvRecommend.getLayoutManager() == null) {
                    rvRecommend.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                }
                if(rvRecommend.getItemDecorationCount() == 0){
                    CommonVerticalItemDecoration decoration = new CommonVerticalItemDecoration(
                            DimensUtils.dp2Px(getContext(), 10),DimensUtils.dp2Px(getContext(), 10));
                    rvRecommend.addItemDecoration(decoration);
                }
                if (rvRecommend.getAdapter() == null) {
                    AppStoreListAdapter recommendAdapter = new AppStoreListAdapter();
                    rvRecommend.setAdapter(recommendAdapter);
                    recommendAdapter.setList(appSearchBeforeWrapBean.getRecommend());
                    recommendAdapter.setStateButtonClickListener(new AppStoreListAdapter.StateButtonClickListener() {
                        @Override
                        public void onStateButtonClick(AppModel appModel, int pos) {
                            if (searchBeforeListener != null) {
                                searchBeforeListener.onAppClick(appSearchBeforeWrapBean.getRecommend().get(pos));
                            }
                        }
                    });
                }else {
                    rvRecommend.getAdapter().notifyDataSetChanged();
                }
                break;
            default:
                break;
        }
    }


    final static class MyMultiTypeDelegate extends BaseMultiTypeDelegate<AppSearchBeforeWrapBean> {

        public MyMultiTypeDelegate() {
            addItemType(AppSearchBeforeWrapBean.SEARCH_BEFORE_HISTORY, R.layout.item_search_before_hostory);
            addItemType(AppSearchBeforeWrapBean.SEARCH_BEFORE_RECOMMEND, R.layout.item_search_before_recommend);
        }

        @Override
        public int getItemType(@NotNull List<? extends AppSearchBeforeWrapBean> list, int position) {
            if (list.get(position) != null
                    && list.get(position).getHistoryList() != null
                    && list.get(position).getHistoryList().size() > 0) {
                return AppSearchBeforeWrapBean.SEARCH_BEFORE_HISTORY;
            } else {
                return AppSearchBeforeWrapBean.SEARCH_BEFORE_RECOMMEND;
            }
        }
    }

    public static class AppSearchHistoryAdapter extends BaseQuickAdapter<String, BaseViewHolder> {

        public AppSearchHistoryAdapter() {
            super(R.layout.item_search_history);
        }

        @Override
        protected void convert(@NotNull BaseViewHolder holder, String s) {
            holder.setText(R.id.tvHistory, s);
        }
    }
    public void setSearchBeforeListener(SearchBeforeListener searchBeforeListener) {
        this.searchBeforeListener = searchBeforeListener;
    }

    public interface SearchBeforeListener {
        void onAppClick(AppModel appModel);

        void onHistorySearchClick(String history);

        void clearHistorySearch();
    }
}
