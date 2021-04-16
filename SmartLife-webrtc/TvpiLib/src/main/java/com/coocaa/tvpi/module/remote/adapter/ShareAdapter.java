package com.coocaa.tvpi.module.remote.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.coocaa.tvpilib.R;
import com.umeng.analytics.MobclickAgent;
import com.umeng.socialize.bean.SHARE_MEDIA;

import java.util.HashMap;
import java.util.Map;

import androidx.recyclerview.widget.RecyclerView;

import static com.coocaa.tvpi.common.UMEventId.CLICK_SCREEN_SHARE;

/**
 * Created by WHY on 2018/3/7.
 */

public class ShareAdapter extends RecyclerView.Adapter {

    private static final String TAG = ShareAdapter.class.getSimpleName();

    private Context context;
    private boolean clickable;
    private OnItemClickListener onItemClickListener;

    public ShareAdapter(Context context) {
        this.context = context;
    }

    public void setClickable(boolean clickable) {
        this.clickable = clickable;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(SHARE_MEDIA share_media);
    }

    public void setOnItemClickListener (OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.share_list_item_layout, parent, false);

        /*ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        int width = DimensUtils.getDeviceWidth(context) / 4;
        layoutParams.width = width;
        Log.d(TAG, "onCreateViewHolder: width: " + width);
        view.setLayoutParams(layoutParams);*/

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((ViewHolder) holder).onBind(position);
    }

    @Override
    public int getItemCount() {
        return 4;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        View share_layout;
        ImageView share_iv;
        TextView share_tv;
        public ViewHolder(View itemView) {
            super(itemView);
            share_layout = itemView.findViewById(R.id.share_layout);
            share_tv = itemView.findViewById(R.id.share_tv);
            share_iv = itemView.findViewById(R.id.share_iv);
        }

        public void onBind(final int position) {
            int resId = 0;
            String str = "";
            if (clickable) {
                switch (position) {
                    case 0:
                        resId = R.drawable.icon_share_wechat;
                        str = "微信好友";
                        break;
                    case 1:
                        resId = R.drawable.icon_share_wechat_circle;
                        str = "朋友圈";
                        break;
                    case 2:
                        resId = R.drawable.icon_share_qq;
                        str = "QQ";
                        break;
                    case 3:
                        resId = R.drawable.icon_share_qzone;
                        str = "QQ空间";
                        break;
                }
                share_tv.setTextColor(context.getResources().getColor(R.color.c_3));
            } else {
                switch (position) {
                    case 0:
                        resId = R.drawable.icon_share_wechat_disable;
                        str = "微信好友";
                        break;
                    case 1:
                        resId = R.drawable.icon_share_wechat_circle_disable;
                        str = "朋友圈";
                        break;
                    case 2:
                        resId = R.drawable.icon_share_qq_disable;
                        str = "QQ";
                        break;
                    case 3:
                        resId = R.drawable.icon_share_qzone_disable;
                        str = "QQ空间";
                        break;
                }
                share_tv.setTextColor(context.getResources().getColor(R.color.c_5));
            }
            share_iv.setBackgroundResource(resId);
            share_tv.setText(str);
            share_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clickable) {
//                        Log.d(TAG, "onClick: share" + position);
                        if (null != onItemClickListener) {
                            Map<String, String> map = new HashMap<>();
                            SHARE_MEDIA share_media = null;
                            switch (position) {
                                case 0:
                                    share_media = SHARE_MEDIA.WEIXIN;
                                    map.put("type", "weixin");
                                    break;
                                case 1:
                                    share_media = SHARE_MEDIA.WEIXIN_CIRCLE;
                                    map.put("type", "weixin_circle");
                                    break;
                                case 2:
                                    share_media = SHARE_MEDIA.QQ;
                                    map.put("type", "qq");
                                    break;
                                case 3:
                                    share_media = SHARE_MEDIA.QZONE;
                                    map.put("type", "qzone");
                                    break;
                            }
                            MobclickAgent.onEvent(context, CLICK_SCREEN_SHARE, map);
                            if (null != share_media)
                                onItemClickListener.onItemClick(share_media);
                        }
                    }
                }
            });
        }
    }
}
