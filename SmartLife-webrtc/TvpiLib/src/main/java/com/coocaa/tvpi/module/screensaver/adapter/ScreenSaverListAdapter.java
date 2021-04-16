package com.coocaa.tvpi.module.screensaver.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.tvpilib.R;

public class ScreenSaverListAdapter extends RecyclerView.Adapter<ScreenSaverListAdapter.ViewHolder> {

//    List<ScreenSaverListBean.RecommendInfoBean> mList;
    Activity mContext;
    int visibilityCount = 0;

    public ScreenSaverListAdapter(Activity context) {
        this.mContext = context;
    }

   /* public ScreenSaverListAdapter(Activity context, List<ScreenSaverListBean.RecommendInfoBean> list) {
        this.mContext = context;
        this.mList = list;
    }*/

/*    public void setList(List<ScreenSaverListBean.RecommendInfoBean> list) {
        if(list != null) {
            this.mList = list;
            notifyDataSetChanged();
        }
    }*/

    /**
     * 控制显示数量
     * @param count
     */
    public void setVisibilityCount(int count) {
        this.visibilityCount = count;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_screen_saver_dlna, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        /*if(mList == null){
            return;
        }

        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        int width = (DimensUtils.getDeviceWidth(mContext) - DimensUtils.dp2Px(mContext, 20f) * 2);
        layoutParams.width = width;
        final ScreenSaverListBean.RecommendInfoBean infoData = mList.get(position);
        RequestOptions options = new RequestOptions()
                .transform(new RoundedCornersTransformation(DimensUtils.dp2Px(mContext, 10),
                        0, RoundedCornersTransformation.CornerType.TOP))
                .placeholder(R.drawable.icon_pic_network_failed)
                .error(R.drawable.icon_pic_network_failed);

        Glide.with(mContext)
                .load(infoData.getImgthumUrl())
                .apply(options)
                .into(holder.imgScreenSaver);
        holder.tvScreenCastPerview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.tvScreenCastPerview.setEnabled(false);
//                MobclickAgent.onEvent(MyApplication.getContext(), "TV_Screensaver");
                DeviceControllerManager.getInstance()
                        .pushInternetImg(infoData.getImg720PUrl(), new IPushResourceCallBack.IPlayCallBack() {
                            @Override
                            public void onPlaySuccess() {
                                holder.tvScreenCastPerview.setEnabled(true);
                                ToastUtils.getInstance().showGlobalLong("投屏成功");
                            }

                            @Override
                            public void onPlayFailure(Exception e) {
                                holder.tvScreenCastPerview.setEnabled(true);
//                                ConnectDialogActivity.openConnectDialog(ConnectDialogActivity.FROM_SELECT_WIFI_DEVICE);
                                ToastUtils.getInstance().showGlobalLong("投屏失败");
                            }
                        });
            }
        });
        holder.tvScreenSaverCustomized.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.tvScreenSaverCustomized.setEnabled(false);
                DeviceControllerManager.getInstance()
                        .startScreenSaver(new IPushResourceCallBack.ITransportCallBack() {
                            @Override
                            public void onTransportSuccess() {
                                holder.tvScreenSaverCustomized.setEnabled(true);
                                ToastUtils.getInstance().showGlobalLong("电视正在启动定制屏保");
                            }

                            @Override
                            public void onTransportFailure(Exception e) {
                                holder.tvScreenSaverCustomized.setEnabled(true);
//                                ConnectDialogActivity.openConnectDialog(ConnectDialogActivity.FROM_SELECT_WIFI_DEVICE);
                                ToastUtils.getInstance().showGlobalShort("操作失败，请重试", true);
                            }
                        });
            }
        });
        holder.imgScreenSaver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ScreenSaverDetailsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("recommendInfo", mList.get(position));
                intent.putExtras(bundle);
                mContext.startActivity(intent);
            }
        });
*/
    }

    @Override
    public int getItemCount() {
        if (visibilityCount != 0)
            return visibilityCount;
//        return mList == null ? 0 : mList.size();
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvScreenCastPerview,tvScreenSaverCustomized;
        ImageView imgScreenSaver;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvScreenCastPerview = itemView.findViewById(R.id.screen_cast_perview);
            tvScreenSaverCustomized = itemView.findViewById(R.id.screen_saver_customized);
            imgScreenSaver = itemView.findViewById(R.id.item_screen_saver_img);
        }
    }
}
