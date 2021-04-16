package com.coocaa.swaiotos.virtualinput.module.view.document;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import swaiotos.runtime.h5.core.os.H5RunType;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.ObjectKey;
import com.cocaa.swaiotos.virtualinput.R;
import com.coocaa.publib.base.GlideApp;
import com.coocaa.smartscreen.constant.SmartConstans;
import com.coocaa.smartsdk.SmartApi;
import com.coocaa.swaiotos.virtualinput.utils.GlideRoundTransform;
import com.coocaa.swaiotos.virtualinput.utils.UiUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description:
 * @Author: wzh
 * @CreateDate: 2020/10/21
 */
public class DocumentPreviewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final static String TAG = "DocumentPreviewsAdapter";
    private List<String> mDatas = new ArrayList<>();
    private OnItemClickListener onItemClickListener;
    private int mCurPosition;
    private int mCurPageDirection;
    private final static int H_FOCUS_W = UiUtil.Div(104);
    private final static int H_FOCUS_H = UiUtil.Div(60);
    private final static int H_IMG_W = UiUtil.Div(100);
    private final static int H_IMG_H = UiUtil.Div(56);
    private final static int V_FOCUS_W = UiUtil.Div(68);
    private final static int V_FOCUS_H = UiUtil.Div(94);
    private final static int V_IMG_W = UiUtil.Div(64);
    private final static int V_IMG_H = UiUtil.Div(90);

    public DocumentPreviewsAdapter() {
    }

    public void setData(List<String> datas) {
        if (datas.size() > 0) {
            List<String> tempList = new ArrayList<>();
            tempList.addAll(datas);
            if (mDatas.size() > 0) {
                tempList.removeAll(mDatas);
                if (mDatas.contains("")) {
                    int start = mDatas.indexOf("");
                    int count = tempList.size();
                    mDatas.clear();
                    mDatas.addAll(datas);
                    Log.i(TAG, "setData notifyItemRangeChanged: start:" + start + "---count:" + count);
                    notifyItemRangeChanged(start, count);
                    return;
                }
            }
        }
        mDatas.clear();
        mDatas.addAll(datas);
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setCurPosition(int position) {
        mCurPosition = position;
    }

    public void setCurPageDirection(int direction) {
        mCurPageDirection = direction;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        Log.i(TAG, "onCreateViewHolder: ");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_doc_previews_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//        Log.i(TAG, "onBindViewHolder: " + position);
        ((ViewHolder) holder).setData(mDatas.get(position), position);
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
//        Log.i(TAG, "onViewRecycled: ");
        ((ViewHolder) holder).clear();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private Context mContext;
        private ImageView img;
        private ImageView defaultBg;
        private ImageView defaultIcon;
        private ImageView focus;
        private TextView pageNum;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mContext = itemView.getContext();
            focus = itemView.findViewById(R.id.preview_focus_iv);
            defaultBg = itemView.findViewById(R.id.default_bg_iv);
            defaultIcon = itemView.findViewById(R.id.default_icon);
            img = itemView.findViewById(R.id.preview_iv);
            pageNum = itemView.findViewById(R.id.corner_pagenum);
        }

        public void setData(String imgUrl, final int position) {
            if (mCurPageDirection == LinearLayout.HORIZONTAL) {
                //横向
                updateViewParams(focus, H_FOCUS_W, H_FOCUS_H);
                updateViewParams(defaultBg, H_IMG_W, H_IMG_H);
                updateViewParams(img, H_IMG_W, H_IMG_H);
            } else {
                //纵向
                updateViewParams(focus, V_FOCUS_W, V_FOCUS_H);
                updateViewParams(defaultBg, V_IMG_W, V_IMG_H);
                updateViewParams(img, V_IMG_W, V_IMG_H);
            }

            if (!TextUtils.isEmpty(imgUrl)) {
                GlideApp.with(mContext).load(imgUrl)
                        .centerInside()
                        .skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE)
                        .transform(new GlideRoundTransform(2))
                        .signature(new ObjectKey(SmartConstans.getBuildInfo().buildTimestamp))
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                defaultIcon.setVisibility(View.VISIBLE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                defaultIcon.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .into(img);
            } else {
                defaultIcon.setVisibility(View.VISIBLE);
            }
            if (mCurPosition == position) {
                focus.setVisibility(View.VISIBLE);
                pageNum.setBackgroundResource(R.drawable.doc_icon_corner_orange);
            } else {
                focus.setVisibility(View.INVISIBLE);
                pageNum.setBackgroundResource(R.drawable.doc_icon_corner_gray);
            }
            pageNum.setText(String.valueOf(position + 1));
            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mCurPosition == position) {
                        Log.i(TAG, "onClick: same position, return. " + position);
                        return;
                    }

                    //发布会需求，不在同一wifi，弹连接wifi弹窗，防止被误点
                    if (!SmartApi.isSameWifi()) {
                        SmartApi.startConnectSameWifi(H5RunType.RUNTIME_NETWORK_FORCE_LAN);
                        return;
                    }

                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(position);
                    }
//                    mCurPosition = position;
//                    focus.setVisibility(View.VISIBLE);
//                    pageNum.setBackgroundResource(R.drawable.doc_icon_corner_orange);
                }
            });
        }

        private void updateViewParams(View view, int width, int height) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
            params.width = width;
            params.height = height;
            view.setLayoutParams(params);
        }

        public void clear() {
            try {
                defaultIcon.setVisibility(View.VISIBLE);
                if (mContext != null && mContext instanceof Activity && !((Activity) mContext).isDestroyed()) {
                    GlideApp.with(mContext).load("").into(img);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}
