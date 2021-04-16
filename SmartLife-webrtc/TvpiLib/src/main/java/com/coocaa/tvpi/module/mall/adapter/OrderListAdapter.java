package com.coocaa.tvpi.module.mall.adapter;

import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.coocaa.smartmall.data.mobile.data.OrderResult;
import com.coocaa.tvpi.util.ImageUtils;
import com.coocaa.tvpilib.R;

import org.jetbrains.annotations.NotNull;

public class OrderListAdapter extends BaseQuickAdapter<OrderResult.DataBean, BaseViewHolder> {

    private static final String TAG = OrderListAdapter.class.getSimpleName();

    private OrderListCallback orderListCallback;

    public OrderListAdapter() {
        super(R.layout.item_order);
    }

    public interface OrderListCallback {
        void onConfirmClick(OrderResult.DataBean data);
        void onCancelClick(OrderResult.DataBean data);
    }

    public void setOrderListCallback(OrderListCallback orderListCallback) {
        this.orderListCallback = orderListCallback;
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, OrderResult.DataBean data) {
        holder.setText(R.id.item_order_no, "订单编号：" + data.getOrder_no());
        holder.setText(R.id.item_order_status, data.getOrder_status_msg());
        holder.setText(R.id.item_order_name, data.getProduct_name());
        holder.setText(R.id.item_order_type, data.getProduct_type());
        holder.setText(R.id.item_order_count, "x" + data.getProduct_count());
        holder.setText(R.id.item_order_price, data.getProduct_price());
        holder.setText(R.id.item_order_total_price, data.getTotal_price());

        ImageUtils.load(holder.itemView.findViewById(R.id.item_order_icon), data.getProduct_icon());

        TextView confirmTV, cancelTV, totalPriceTitleTV;
        confirmTV = holder.getView(R.id.item_order_confirm);
        cancelTV = holder.getView(R.id.item_order_cancel);
        totalPriceTitleTV = holder.getView(R.id.item_order_total_price_title);

        //order_status : 1待付款 2待发货 3待收货 4已完成 5未付款取消 6已付款取消 7待出库
        switch (data.getOrder_status()) {
            case 1:
                confirmTV.setVisibility(View.VISIBLE);
                cancelTV.setVisibility(View.VISIBLE);
                confirmTV.setTextColor(getContext().getColor(R.color.color_red));
                confirmTV.setBackground(getContext().getDrawable(R.drawable.bg_order_red_round_15));
                confirmTV.setText("立即付款");
                totalPriceTitleTV.setText("待付金额");
                break;
            case 2:
                confirmTV.setVisibility(View.VISIBLE);
                cancelTV.setVisibility(View.GONE);
                confirmTV.setTextColor(getContext().getColor(R.color.black_80));
                confirmTV.setBackground(getContext().getDrawable(R.drawable.bg_order_black_round_15));
                confirmTV.setText("提醒发货");
                totalPriceTitleTV.setText("实付金额");
                break;
            case 3:
                confirmTV.setVisibility(View.VISIBLE);
                cancelTV.setVisibility(View.GONE);
                confirmTV.setTextColor(getContext().getColor(R.color.color_red));
                confirmTV.setBackground(getContext().getDrawable(R.drawable.bg_order_red_round_15));
                confirmTV.setText("确认收货");
                totalPriceTitleTV.setText("实付金额");
                break;
            case 4:
                confirmTV.setVisibility(View.VISIBLE);
                cancelTV.setVisibility(View.GONE);
                confirmTV.setTextColor(getContext().getColor(R.color.black_80));
                confirmTV.setBackground(getContext().getDrawable(R.drawable.bg_order_black_round_15));
                confirmTV.setText("申请售后");
                totalPriceTitleTV.setText("实付金额");
                break;
            case 5:
                confirmTV.setVisibility(View.GONE);
                cancelTV.setVisibility(View.GONE);
                totalPriceTitleTV.setText("待付金额");
                break;
            case 6:
                confirmTV.setVisibility(View.VISIBLE);
                cancelTV.setVisibility(View.GONE);
                totalPriceTitleTV.setText("实付金额");
                break;
            case 7:
                confirmTV.setVisibility(View.VISIBLE);
                cancelTV.setVisibility(View.GONE);
                confirmTV.setTextColor(getContext().getColor(R.color.black_80));
                confirmTV.setBackground(getContext().getDrawable(R.drawable.bg_order_black_round_15));
                confirmTV.setText("提醒发货");
                totalPriceTitleTV.setText("实付金额");
                break;
        }

        confirmTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != orderListCallback) {
                    orderListCallback.onConfirmClick(data);
                }
            }
        });

        cancelTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != orderListCallback) {
                    orderListCallback.onCancelClick(data);
                }
            }
        });
    }




}
