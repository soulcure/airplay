package com.coocaa.movie.product.v;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coocaa.movie.util.UtilMovie;
import com.coocaa.movie.product.m.entity.ProductItem;
import com.coocaa.tvpilib.R;

import androidx.annotation.NonNull;

public class ProductItemView extends FrameLayout {

    TextView name;
    TextView descripe;
    TextView price;
    FrameLayout focus;
    ProductItem curData;

    public ProductItemView(@NonNull Context context) {
        super(context);

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        LayoutParams root_p = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        root_p.gravity = Gravity.CENTER_VERTICAL;
        root_p.leftMargin = UtilMovie.Div(15);
        addView(root, root_p);

        name = new TextView(context);
        name.setTextSize(UtilMovie.Dpi(16));
        name.setTextColor(Color.parseColor("#333333"));
        name.setText("产品包名");
        root.addView(name);

        descripe = new TextView(context);
        descripe.setTextSize(UtilMovie.Dpi(12));
        descripe.setTextColor(Color.parseColor("#999999"));
        descripe.setText("优惠描述");
        root.addView(descripe);

        LinearLayout priceContent = new LinearLayout(context);
        priceContent.setOrientation(LinearLayout.HORIZONTAL);
        LayoutParams priceContent_p = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        priceContent_p.gravity = Gravity.CENTER_VERTICAL|Gravity.END;
        priceContent_p.rightMargin = UtilMovie.Div(15);
        addView(priceContent, priceContent_p);

        TextView priceTip = new TextView(context);
        priceTip.setText("¥ ");
        priceTip.setTextSize(UtilMovie.Dpi(12));
        priceTip.setTextColor(Color.parseColor("#FF5525"));
        priceContent.addView(priceTip);

        price = new TextView(context);
        price.setTextSize(UtilMovie.Dpi(20));
        price.setTextColor(Color.parseColor("#FF5525"));
        price.setText("100.1");
        priceContent.addView(price);

        focus = new FrameLayout(getContext());
        addView(focus, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        focus.setVisibility(GONE);

        View left = new View(getContext());
        left.setBackgroundColor(Color.parseColor("#FF5525"));
        LayoutParams left_p = new LayoutParams(UtilMovie.Div(1), ViewGroup.LayoutParams.MATCH_PARENT);
        focus.addView(left, left_p);

        View right = new View(getContext());
        right.setBackgroundColor(Color.parseColor("#FF5525"));
        LayoutParams right_p = new LayoutParams(UtilMovie.Div(1), ViewGroup.LayoutParams.MATCH_PARENT);
        right_p.gravity = Gravity.END;
        focus.addView(right, right_p);

        View top = new View(getContext());
        top.setBackgroundColor(Color.parseColor("#FF5525"));
        LayoutParams top_p = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UtilMovie.Div(1));
        focus.addView(top, top_p);

        View bottom = new View(getContext());
        bottom.setBackgroundColor(Color.parseColor("#FF5525"));
        LayoutParams bottom_p = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UtilMovie.Div(1));
        bottom_p.gravity = Gravity.BOTTOM;
        focus.addView(bottom, bottom_p);

        ImageView focusIcon = new ImageView(context);
        focusIcon.setImageResource(R.drawable.id_movie_product_focus);
        LayoutParams focusIcon_p = new LayoutParams(UtilMovie.Div(22), UtilMovie.Div(22));
        focusIcon_p.gravity = Gravity.END|Gravity.BOTTOM;
        focus.addView(focusIcon, focusIcon_p);
    }

    public void setData(ProductItem productItem) {
        if(productItem == null)
            return;
        curData = productItem;
        name.setText(productItem.getProduct_name());
        price.setText(ProductItem.resetPrice(productItem.getDiscount_fee()));
        descripe.setText(productItem.getDesc());
    }

    public void setFocus(boolean focus) {
        this.focus.setVisibility(focus?VISIBLE:GONE);
    }

    public ProductItem getCurData() {
        return curData;
    }
}
