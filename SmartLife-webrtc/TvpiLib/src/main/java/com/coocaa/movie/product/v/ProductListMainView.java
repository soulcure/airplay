package com.coocaa.movie.product.v;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.coocaa.movie.util.MovieConstant;
import com.coocaa.movie.util.UtilMovie;
import com.coocaa.movie.product.m.entity.ProductAccountInfo;
import com.coocaa.movie.product.m.entity.ProductEntity;
import com.coocaa.movie.product.m.entity.ProductItem;
import com.coocaa.tvpilib.R;

import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;

public class ProductListMainView extends FrameLayout implements IProductListMainView{

    TextView title;

    FrameLayout payBtn;
    TextView price;
    TextView discountText;
    TextView payBtnDiscountText;

    ImageView headIconView;
    TextView nickName;
    TextView vipDateTip;

    TextView productTitle;
    TextView giftTipView;
    LinearLayout productListContent;

    WeakReference<ClickPayBtnListener> clickPayBtnListenerRef;

    public ProductListMainView(@NonNull Context context) {
        super(context);

        addProductList();

        addTitltBar();

        addPayContent();
    }

    @Override
    public void updateAccountInfo(ProductAccountInfo info) {
        if(info == null)
            return;
        if(TextUtils.isEmpty(info.headIcon))
            headIconView.setBackgroundResource(R.drawable.id_movie_product_headicon_default);
        else
            Glide.with(getContext()).load(info.headIcon).apply(RequestOptions.bitmapTransform(new CircleCrop())).into(headIconView);

        nickName.setText(info.nickName);

        vipDateTip.setText(info.vipTime);
    }

    @Override
    public void updateProductList(ProductEntity productEntity) {

        if(productEntity == null)
            return;

        title.setText(productEntity.productSource);
        productTitle.setText(productEntity.productSource);

        giftTipView.setText(productEntity.giftTip);

        if(productEntity.products != null && productEntity.products.size() > 0) {
            for (int i=0; i< productEntity.products.size(); i++) {
                ProductItem item = productEntity.products.get(i);
                if(item == null)
                    continue;
                ProductItemView itemView = new ProductItemView(getContext());
                LinearLayout.LayoutParams item_p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UtilMovie.Div(70));
                productListContent.addView(itemView, item_p);
                itemView.setData(item);
                itemView.setOnClickListener(onItemClickListener);

                if(i==0) {
                    curFocusItemView = itemView;
                    curFocusItemView.setFocus(true);
                    price.setText(ProductItem.resetPrice(item.getDiscount_fee()));
                    if(item.getDiscount_fee() != item.getUnit_fee()) {
                        discountText.setVisibility(VISIBLE);
                        discountText.setText("| 已优惠" + ProductItem.resetPrice(item.getUnit_fee() - item.getDiscount_fee()) + "元");
                        payBtnDiscountText.setVisibility(VISIBLE);
                        payBtnDiscountText.setText("节省"+ProductItem.resetPrice(item.getUnit_fee() - item.getDiscount_fee()) + "元");
                    } else {
                        discountText.setVisibility(GONE);
                        payBtnDiscountText.setVisibility(GONE);
                    }
                }
            }
        }
    }

    @Override
    public void setPayBtnClickListener(ClickPayBtnListener ll) {
        clickPayBtnListenerRef = new WeakReference<>(ll);
    }

    ProductItemView curFocusItemView;
    OnClickListener onItemClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v == curFocusItemView)
                return;
            curFocusItemView.setFocus(false);
            curFocusItemView = (ProductItemView) v;
            curFocusItemView.setFocus(true);
            price.setText(ProductItem.resetPrice(curFocusItemView.getCurData().getDiscount_fee()));
            if(curFocusItemView.getCurData().getDiscount_fee() != curFocusItemView.getCurData().getUnit_fee()) {
                discountText.setVisibility(VISIBLE);
                discountText.setText("| 已优惠" + ProductItem.resetPrice(curFocusItemView.getCurData().getUnit_fee() - curFocusItemView.getCurData().getDiscount_fee()) + "元");
                payBtnDiscountText.setVisibility(VISIBLE);
                payBtnDiscountText.setText("节省"+ProductItem.resetPrice(curFocusItemView.getCurData().getUnit_fee() - curFocusItemView.getCurData().getDiscount_fee()) + "元");
            } else {
                discountText.setVisibility(GONE);
                payBtnDiscountText.setVisibility(GONE);
            }
        }
    };

    private void addProductList() {
        ScrollView scrollView = new ScrollView(getContext());
        if("qq".equals(MovieConstant.getSource()))
            scrollView.setBackgroundResource(R.drawable.id_movie_product_bg_qq);
        else if("iqiyi".equals(MovieConstant.getSource()))
            scrollView.setBackgroundResource(R.drawable.id_movie_product_bg_iqiyi);
        else
            scrollView.setBackgroundResource(R.drawable.id_movie_product_bg);
        LayoutParams scrollView_p = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        scrollView_p.topMargin = UtilMovie.Div(44);
        scrollView_p.bottomMargin = UtilMovie.Div(90);
        addView(scrollView, scrollView_p);

        LinearLayout mainContent = new LinearLayout(getContext());
        mainContent.setPadding(0,UtilMovie.Div(175),0,0);
        mainContent.setOrientation(LinearLayout.VERTICAL);
        LayoutParams mainContent_p = new LayoutParams(UtilMovie.Div(345), ViewGroup.LayoutParams.WRAP_CONTENT);
        mainContent_p.gravity = Gravity.CENTER_HORIZONTAL;
        scrollView.addView(mainContent, mainContent_p);

        productListContent = new LinearLayout(getContext());
        productListContent.setOrientation(LinearLayout.VERTICAL);
        productListContent.setBackground(getResources().getDrawable(R.drawable.movie_product_shape_corner));
        LinearLayout.LayoutParams listcontent_p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mainContent.addView(productListContent, listcontent_p);

        addAccountView(productListContent);

        addProductTitleView(productListContent);

        ImageView power = new ImageView(getContext());
        power.setPadding(0,UtilMovie.Div(15),0,0);
        power.setScaleType(ImageView.ScaleType.FIT_XY);
        power.setImageResource(R.drawable.id_movie_product_power);
        LinearLayout.LayoutParams power_p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UtilMovie.Div(280));
        mainContent.addView(power, power_p);
    }

    private void addProductTitleView(LinearLayout root) {
        FrameLayout rootContent = new FrameLayout(getContext());
        LinearLayout.LayoutParams rootContent_p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UtilMovie.Div(51));
        root.addView(rootContent, rootContent_p);

        LinearLayout content = new LinearLayout(getContext());
        content.setOrientation(LinearLayout.HORIZONTAL);
        content.setPadding(UtilMovie.Div(15), 0,0,0);
        LayoutParams content_p = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        content_p.gravity = Gravity.CENTER_VERTICAL;
        rootContent.addView(content, content_p);

        View shuXian = new View(getContext());
        shuXian.setBackgroundColor(Color.parseColor("#FCB232"));
        LinearLayout.LayoutParams shuXian_p = new LinearLayout.LayoutParams(UtilMovie.Div(4), UtilMovie.Div(25));
        content.addView(shuXian, shuXian_p);

        productTitle = new TextView(getContext());
        productTitle.setPadding(UtilMovie.Div(5),0,0,0);
        productTitle.setTextSize(UtilMovie.Dpi(18));
        productTitle.setTextColor(Color.parseColor("#333333"));
        content.addView(productTitle);

        giftTipView = new TextView(getContext());
        giftTipView.setTextSize(UtilMovie.Dpi(14));
        giftTipView.setTextColor(Color.parseColor("#FCB232"));
        content.addView(giftTipView);
    }

    private void addAccountView(LinearLayout content) {
        FrameLayout accountContent = new FrameLayout(getContext());
        LinearLayout.LayoutParams accountContent_p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UtilMovie.Div(58));
        content.addView(accountContent, accountContent_p);

        headIconView = new ImageView(getContext());
        headIconView.setBackgroundResource(R.drawable.id_movie_product_headicon_default);
        LayoutParams headIconView_p = new LayoutParams(UtilMovie.Div(38), UtilMovie.Div(38));
        headIconView_p.gravity = Gravity.CENTER_VERTICAL;
        headIconView_p.leftMargin = UtilMovie.Div(15);
        accountContent.addView(headIconView, headIconView_p);

        nickName = new TextView(getContext());
        nickName.setTextSize(UtilMovie.Dpi(16));
        nickName.setText("昵称");
        nickName.setTextColor(Color.parseColor("#333333"));
        LayoutParams nickName_p = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        nickName_p.gravity = Gravity.CENTER_VERTICAL;
        nickName_p.leftMargin = UtilMovie.Div(64);
        accountContent.addView(nickName, nickName_p);

        vipDateTip = new TextView(getContext());
        vipDateTip.setTextSize(UtilMovie.Dpi(12));
        vipDateTip.setTextColor(Color.parseColor("#999990"));
        vipDateTip.setText("您还不是VIP");
        LayoutParams vipDateTip_p = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        vipDateTip_p.gravity = Gravity.CENTER_VERTICAL|Gravity.END;
        vipDateTip_p.rightMargin = UtilMovie.Div(15);
        accountContent.addView(vipDateTip, vipDateTip_p);

        View divideLine = new View(getContext());
        divideLine.setBackgroundColor(Color.parseColor("#ECECEC"));
        LayoutParams divideLine_p = new LayoutParams(UtilMovie.Div(315), UtilMovie.Div(1));
        divideLine_p.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        accountContent.addView(divideLine, divideLine_p);
    }

    private void addPayContent() {
        FrameLayout payContent = new FrameLayout(getContext());
        payContent.setBackgroundColor(Color.WHITE);
        LayoutParams payContent_p = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UtilMovie.Div(60));
        payContent_p.gravity = Gravity.BOTTOM;
        addView(payContent, payContent_p);

        LinearLayout firstLine = new LinearLayout(getContext());
        firstLine.setOrientation(LinearLayout.HORIZONTAL);
        firstLine.setGravity(Gravity.BOTTOM);
        firstLine.setPadding(UtilMovie.Div(18),0,0,0);
        payContent.addView(firstLine, new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, UtilMovie.Div(33)));

        TextView costTitle = new TextView(getContext());
        costTitle.setTextSize(UtilMovie.Dpi(18));
        costTitle.setTextColor(Color.parseColor("#333333"));
        costTitle.setText("付款");
        firstLine.addView(costTitle);

        price = new TextView(getContext());
        price.setPadding(UtilMovie.Div(5),0,UtilMovie.Div(5),0);
        price.setTextSize(UtilMovie.Dpi(18));
        price.setTextColor(Color.parseColor("#FF5525"));
        firstLine.addView(price);

        discountText = new TextView(getContext());
        discountText.setTextColor(Color.parseColor("#999999"));
        discountText.setTextSize(UtilMovie.Dpi(14));
        firstLine.addView(discountText);

        LinearLayout secondLine = new LinearLayout(getContext());
        LayoutParams secondLine_p = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        secondLine_p.gravity = Gravity.BOTTOM;
        secondLine_p.leftMargin = UtilMovie.Div(18);
        secondLine_p.bottomMargin = UtilMovie.Div(11);
        payContent.addView(secondLine, secondLine_p);

        TextView tip = new TextView(getContext());
        tip.setTextSize(UtilMovie.Dpi(10));
        tip.setTextColor(Color.parseColor("#CCCCCC"));
        tip.setText("确认支付则视为同意");
        secondLine.addView(tip);

        TextView tipBtn = new TextView(getContext());
        tipBtn.setTextSize(UtilMovie.Dpi(10));
        tipBtn.setTextColor(Color.parseColor("#999999"));
        tipBtn.setText("服务协议");
        tipBtn.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        secondLine.addView(tipBtn);
        tipBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(clickPayBtnListenerRef != null && clickPayBtnListenerRef.get() != null)
                    clickPayBtnListenerRef.get().clickServiceAgreement();
            }
        });

        FrameLayout payBtn = new FrameLayout(getContext());
        payBtn.setClickable(true);
        payBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(clickPayBtnListenerRef != null && clickPayBtnListenerRef.get() != null)
                    clickPayBtnListenerRef.get().clickPayBtn(curFocusItemView.getCurData());
            }
        });
        payBtn.setBackgroundColor(Color.parseColor("#FF5525"));
        LayoutParams payBtn_p = new LayoutParams(UtilMovie.Div(120), ViewGroup.LayoutParams.MATCH_PARENT);
        payBtn_p.gravity = Gravity.END;
        payContent.addView(payBtn, payBtn_p);

        LinearLayout payBtnContent = new LinearLayout(getContext());
        payBtnContent.setOrientation(LinearLayout.VERTICAL);
        LayoutParams payBtnContent_p = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        payBtnContent_p.gravity = Gravity.CENTER;
        payBtn.addView(payBtnContent, payBtnContent_p);

        TextView firText = new TextView(getContext());
        firText.setText("立即支付");
        firText.setTextSize(UtilMovie.Dpi(18));
        firText.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams firText_p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        firText_p.gravity = Gravity.CENTER_HORIZONTAL;
        payBtnContent.addView(firText, firText_p);

        payBtnDiscountText = new TextView(getContext());
        payBtnDiscountText.setTextSize(UtilMovie.Dpi(14));
        payBtnDiscountText.setTextColor(Color.WHITE);
        payBtnDiscountText.setText("(测试节省100元)");
        LinearLayout.LayoutParams payBtnDiscountText_p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        payBtnDiscountText_p.gravity = Gravity.CENTER_HORIZONTAL;
        payBtnContent.addView(payBtnDiscountText, payBtnDiscountText_p);

    }

    private void addTitltBar() {
        FrameLayout titleContent = new FrameLayout(getContext());
        titleContent.setBackgroundColor(Color.WHITE);
        addView(titleContent, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UtilMovie.Div(44)));

        ImageView backBtn = new ImageView(getContext());
        backBtn.setBackgroundResource(R.drawable.id_movie_product_titlebar_back_btn_bg);
        backBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickPayBtnListenerRef != null && clickPayBtnListenerRef.get() != null) {
                    clickPayBtnListenerRef.get().clickBack();
                }
            }
        });
        LayoutParams backBtn_p = new LayoutParams(UtilMovie.Div(40), UtilMovie.Div(40));
        backBtn_p.gravity = Gravity.CENTER_VERTICAL;
        titleContent.addView(backBtn, backBtn_p);

        title = new TextView(getContext());
        title.setText("VIP");
        title.setTextSize(UtilMovie.Dpi(18));
        title.getPaint().setFakeBoldText(true);
        title.setTextColor(Color.BLACK);
        LayoutParams title_p = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        title_p.gravity = Gravity.CENTER;
        titleContent.addView(title, title_p);
    }
}
