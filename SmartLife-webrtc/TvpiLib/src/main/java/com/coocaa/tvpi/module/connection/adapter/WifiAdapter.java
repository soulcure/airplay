package com.coocaa.tvpi.module.connection.adapter;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.tvpilib.R;

import org.jetbrains.annotations.NotNull;

public class WifiAdapter extends BaseQuickAdapter<ScanResult, BaseViewHolder> {
    private static final String TAG = WifiAdapter.class.getSimpleName();

    public WifiAdapter() {
        super(R.layout.item_wifi_list);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, ScanResult scanResult) {
        holder.setText(R.id.tvWifiName, scanResult.SSID);
        int level = WifiManager.calculateSignalLevel(scanResult.level, 5);
        if (level == 4) {
            holder.setImageResource(R.id.ivWifiStrength, R.drawable.connet_wifi_4);
        } else if (level == 3) {
            holder.setImageResource(R.id.ivWifiStrength, R.drawable.connet_wifi_3);
        } else if (level == 2) {
            holder.setImageResource(R.id.ivWifiStrength, R.drawable.connet_wifi_2);
        } else if (level == 1) {
            holder.setImageResource(R.id.ivWifiStrength, R.drawable.connet_wifi_1);
        } else if (level == 0) {
            holder.setImageResource(R.id.ivWifiStrength, R.drawable.connet_wifi_1);
        }
    }


    public static class WifiListDivider extends RecyclerView.ItemDecoration {
        Context context;
        Paint paint;

        public WifiListDivider(Context context) {
            this.context = context;
            this.paint = new Paint();
            paint.setColor(Color.parseColor("#1A000000"));
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            RecyclerView.Adapter adapter = parent.getAdapter();
            if (adapter == null) {
                return;
            }
            outRect.top = DimensUtils.dp2Px(context, 1f);
            if (parent.getChildAdapterPosition(view) == adapter.getItemCount() - 1) {
                outRect.bottom = DimensUtils.dp2Px(context, 1f);
            } else {
                outRect.bottom = 0;
            }
        }

        @Override
        public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            final int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                c.drawLine(
                        i == 0 ? 0 : DimensUtils.dp2Px(context, 49),
                        child.getTop(),
                        parent.getMeasuredWidth(),
                        child.getTop() + DimensUtils.dp2Px(context, 1f),
                        paint
                );
                if (i == childCount - 1) {
                    c.drawLine(
                            0,
                            child.getBottom(),
                            parent.getMeasuredWidth(),
                            child.getBottom() + DimensUtils.dp2Px(context, 1f),
                            paint
                    );
                }
            }
        }
    }
}
