package com.coocaa.tvpi.module.mall.adapter;

import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.coocaa.smartmall.data.mobile.data.ProductRecommendResult;
import com.coocaa.tvpi.util.ImageUtils;
import com.coocaa.tvpilib.R;

import org.jetbrains.annotations.NotNull;

import static com.coocaa.tvpi.util.DisplayMetricsUtils.getPx;

public class MallMainAdapter extends BaseQuickAdapter<ProductRecommendResult.DataBean, BaseViewHolder> {
    public static class StaggeredGridDivider extends RecyclerView.ItemDecoration {
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            if(parent.getChildAdapterPosition(view)==0){
                return;
            }
            StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams();
            // 获取item在span中的下标
            int spanIndex = params.getSpanIndex();
            // 中间间隔
            if (spanIndex % 2 == 0) {
                outRect.left = getPx(view.getContext(),15);
                outRect.right=getPx(view.getContext(),5);
            } else {
                // item为奇数位，设置其左间隔
                outRect.left = getPx(view.getContext(),5);
                outRect.right=getPx(view.getContext(),15);
            }
            // 上方间隔
        }
    }
    public MallMainAdapter() {
        super(R.layout.item_store_main);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, ProductRecommendResult.DataBean bean) {
        holder.setText(R.id.name,bean.getProduct_title());

        TextView tvPrice=holder.itemView.findViewById(R.id.price);
        tvPrice.getPaint().setFlags(Paint. STRIKE_THRU_TEXT_FLAG ); //中间横线
        if(!TextUtils.isEmpty(bean.getProduct_price())){
//            holder.setText(R.id.price,"¥"+getPriceIntStr(bean.getProduct_price()));
            tvPrice.setText("¥"+getPriceIntStr(bean.getProduct_price()));
            tvPrice.setVisibility(View.VISIBLE);
        } else {
            tvPrice.setVisibility(View.GONE);
        }

        String priceStr=bean.getProduct_discount_price();
        if(TextUtils.isEmpty(priceStr)){
            priceStr="0";
        }
        holder.setText(R.id.discount_price,"¥"+getPriceIntStr(priceStr));

        ImageUtils.load(holder.itemView.findViewById(R.id.cover),bean.getImage_url());


    }

    //整數就去掉小數點
    public static String getPriceIntStr(String price){
       float valueFloat= Float.parseFloat(price);
       int valueInt= (int) valueFloat;
       String result=(valueFloat==valueInt)?valueInt+"":valueFloat+"";
       return result;
    }
}
