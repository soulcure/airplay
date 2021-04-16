package com.coocaa.tvpi.module.app.adapter;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseDelegateMultiAdapter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.delegate.BaseMultiTypeDelegate;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.coocaa.publib.base.GlideApp;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.smartscreen.data.app.AppModel;
import com.coocaa.tvpi.module.app.AppDetailActivity;
import com.coocaa.tvpi.module.app.bean.AppDetailWrapBean;
import com.coocaa.tvpi.module.app.widget.AppStateButton;
import com.coocaa.tvpi.module.app.widget.DetailDescFragmentDialog;
import com.coocaa.tvpi.util.SizeConverter;
import com.coocaa.tvpi.view.decoration.CommonHorizontalItemDecoration;
import com.coocaa.tvpi.view.decoration.CommonVerticalItemDecoration;
import com.coocaa.tvpilib.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AppDetailAdapter extends BaseDelegateMultiAdapter<AppDetailWrapBean, BaseViewHolder> {
    private StateButtonClickListener stateButtonClickListener;

    public AppDetailAdapter() {
        super();
        setMultiTypeDelegate(new AppDetailAdapter.MyMultiTypeDelegate());
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, AppDetailWrapBean appDetailWrapBean) {
        switch (holder.getItemViewType()) {
            case AppDetailWrapBean.APP_DETAIL:
                AppModel appModel = appDetailWrapBean.getAppModel();
                if (appModel == null) {
                    return;
                }
                holder.setText(R.id.tvName, appModel.appName);

                ImageView ivCover = holder.findView(R.id.ivCover);
                if (ivCover != null) {
                    GlideApp.with(getContext())
                            .load(appModel.icon)
                            .centerCrop()
                            .placeholder(R.drawable.place_holder_app)
                            .into(ivCover);
                }
                RatingBar ratingBar = holder.findView(R.id.rbScore);
                if (ratingBar != null) {
                    ratingBar.setRating(appModel.grade);
                }

                AppStateButton stateButton = holder.findView(R.id.stateButton);
                if (stateButton != null) {
                    stateButton.setState(appModel.status);
                    stateButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (stateButtonClickListener != null) {
                                stateButtonClickListener.onStateButtonClick(appModel, holder.getAdapterPosition());
                            }
                        }
                    });
                }

                if(!TextUtils.isEmpty(appModel.appVersion) && appModel.downloads != 0) {
                    holder.setText(R.id.tvVersionSizeCount, getAppVersionSizeCount(appModel));
                    holder.setVisible(R.id.tvVersionSizeCount,true);
                }else {
                    holder.setVisible(R.id.tvVersionSizeCount,false);
                }

                TextView tvDesc = holder.findView(R.id.tvDesc);
                if (tvDesc != null) {
                    tvDesc.setText(appModel.desc);
                    ViewTreeObserver observer = tvDesc.getViewTreeObserver();
                    observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            ViewTreeObserver obs = tvDesc.getViewTreeObserver();
                            obs.removeOnGlobalLayoutListener(this);
                            if (tvDesc.getLineCount() >= 2) {
                                int lineEndIndex = tvDesc.getLayout().getLineEnd(1);
                                String html = tvDesc.getText().subSequence(0, lineEndIndex - 3) + "…<font color='#FF5525'>更多</font>";
                                tvDesc.setText(Html.fromHtml(html));
                            }
                        }
                    });

                    tvDesc.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DetailDescFragmentDialog descDialogFragment = new DetailDescFragmentDialog();
                            Bundle bundle = new Bundle();
                            bundle.putString("version", getAppVersionSizeCount(appModel));
                            bundle.putString("desc", appModel.desc);
                            descDialogFragment.setArguments(bundle);
                            descDialogFragment.show(((Activity) getContext()).getFragmentManager(), DetailDescFragmentDialog.DIALOG_FRAGMENT_TAG);
                        }
                    });
                }

                RecyclerView rvPreview = holder.findView(R.id.rvPreview);
                if (rvPreview != null) {
                    if (rvPreview.getLayoutManager() == null) {
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),
                                LinearLayoutManager.HORIZONTAL, false);
                        rvPreview.setLayoutManager(linearLayoutManager);
                    }
                    if (rvPreview.getItemDecorationCount() == 0) {
                        CommonHorizontalItemDecoration decoration = new CommonHorizontalItemDecoration(
                                DimensUtils.dp2Px(getContext(), 20f), DimensUtils.dp2Px(getContext(), 10f));
                        rvPreview.addItemDecoration(decoration);
                    }

                    if (rvPreview.getAdapter() == null) {
                        ScreenShotRecyclerAdapter adapter = new ScreenShotRecyclerAdapter(getContext());
                        rvPreview.setAdapter(adapter);
                        adapter.addAppScreenShotsData(appModel.screenshots);
                    }
                }
                break;
            case AppDetailWrapBean.APP_DETAIL_RECOMMEND:
                List<AppModel> recommendDataList = appDetailWrapBean.getRecommendDataList();
                RecyclerView rvRecommend = holder.findView(R.id.rvRecommend);
                if (rvRecommend != null) {
                    if (rvRecommend.getLayoutManager() == null) {
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),
                                LinearLayoutManager.VERTICAL, false);
                        rvRecommend.setLayoutManager(linearLayoutManager);
                    }
                    if (rvRecommend.getItemDecorationCount() == 0) {
                        CommonVerticalItemDecoration recommendDecoration = new CommonVerticalItemDecoration(
                                DimensUtils.dp2Px(getContext(), 15), DimensUtils.dp2Px(getContext(), 15));
                        rvRecommend.addItemDecoration(recommendDecoration);
                    }

                    if (rvRecommend.getAdapter() == null) {
                        RecommendListAdapter recommendListAdapter = new RecommendListAdapter();
                        rvRecommend.setAdapter(recommendListAdapter);
                        recommendListAdapter.setList(recommendDataList);
                        recommendListAdapter.setStateButtonListener(stateButtonClickListener);
                    }else {
                        rvRecommend.getAdapter().notifyDataSetChanged();
                    }
                }
                break;
            default:
                break;
        }
    }

    final static class MyMultiTypeDelegate extends BaseMultiTypeDelegate<AppDetailWrapBean> {

        public MyMultiTypeDelegate() {
            addItemType(AppDetailWrapBean.APP_DETAIL, R.layout.item_app_detail);
            addItemType(AppDetailWrapBean.APP_DETAIL_RECOMMEND, R.layout.item_app_detail_recommend);
        }

        @Override
        public int getItemType(@NotNull List<? extends AppDetailWrapBean> list, int position) {
            if (list.get(position) != null
                    && list.get(position).getRecommendDataList() != null
                    && !list.get(position).getRecommendDataList().isEmpty()) {
                return AppDetailWrapBean.APP_DETAIL_RECOMMEND;
            } else {
                return AppDetailWrapBean.APP_DETAIL;
            }
        }
    }

    public static class RecommendListAdapter extends BaseQuickAdapter<AppModel, BaseViewHolder> {
        private StateButtonClickListener listener;

        public RecommendListAdapter() {
            super(R.layout.item_app_detail_recommend_list);
        }

        @Override
        protected void convert(@NotNull BaseViewHolder holder, AppModel appModel) {
            holder.setText(R.id.tvName, appModel.appName);
            holder.setText(R.id.tvCount, SizeConverter.countConvert(appModel.downloads));
            holder.getView(R.id.ivCover).setTag(R.id.ivCover, holder.getAdapterPosition());
            GlideApp.with(getContext())
                    .load(appModel.icon)
                    .into((ImageView) holder.getView(R.id.ivCover));
            AppStateButton stateButton = holder.getView(R.id.appStateButton);
            stateButton.setState(appModel.status);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AppDetailActivity.start(getContext(), appModel);
                }
            });

            stateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onStateButtonClick(appModel, 1);
                    }
                }
            });
        }

        public void setStateButtonListener(StateButtonClickListener stateButtonClickListener) {
            this.listener = stateButtonClickListener;
        }
    }

    private String getAppVersionSizeCount(AppModel appModel) {
        if (appModel != null) {
            return "版本：" + appModel.appVersion + "   |   " +
                    "大小：" + SizeConverter.BTrim.convert(Float.valueOf(appModel.fileSize)) + "   |   " +
                    SizeConverter.countConvert(appModel.downloads);
        }
        return "";
    }


    public void setStateButtonListener(StateButtonClickListener stateButtonClickListener) {
        this.stateButtonClickListener = stateButtonClickListener;
    }

    public interface StateButtonClickListener {
        void onStateButtonClick(AppModel appModel, int position);
    }
}
