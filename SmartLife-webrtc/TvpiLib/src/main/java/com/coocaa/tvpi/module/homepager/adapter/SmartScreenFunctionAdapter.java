package com.coocaa.tvpi.module.homepager.adapter;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.coocaa.publib.base.GlideApp;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.smartscreen.data.function.FunctionBean;
import com.coocaa.tvpi.util.TvpiClickUtil;
import com.coocaa.tvpilib.R;

import static com.coocaa.tvpi.util.DisplayMetricsUtils.getPx;


public class SmartScreenFunctionAdapter extends BaseQuickAdapter<FunctionBean, BaseViewHolder> {

    public SmartScreenFunctionAdapter() {
        super(R.layout.item_smart_screen_function);
    }

    @Override
    protected void convert(BaseViewHolder holder, FunctionBean functionBean) {
        holder.setText(R.id.tvTitle, functionBean.name);
        ImageView image = holder.findView(R.id.ivCover);
        assert image != null;
        GlideApp.with(getContext())
                .load(functionBean.icon)
                .centerCrop()
                .into(image);

        RelativeLayout contentLayout = holder.findView(R.id.contentLayout);
        assert contentLayout != null;
        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) contentLayout.getLayoutParams();
        int position = holder.getAdapterPosition();
        if (position == 0) {
            lp.dimensionRatio = "h,166:220";
            contentLayout.setLayoutParams(lp);
            holder.setVisible(R.id.tvSubTitle, true);
        } else {
            lp.dimensionRatio = "h,166:104";
            contentLayout.setLayoutParams(lp);
            holder.setVisible(R.id.tvSubTitle, false);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TvpiClickUtil.onClick(getContext(), functionBean.uri());
            }
        });
    }
}
