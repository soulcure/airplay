package com.coocaa.tvpi.module.movie.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.publib.base.GlideApp;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.smartscreen.data.movie.LongVideoDetailModel;
import com.coocaa.smartscreen.data.movie.LongVideoListModel;
import com.coocaa.tvpilib.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuhaiyuan on 2017/12/21.
 */

public class LongVideoAdapter extends RecyclerView.Adapter <LongVideoAdapter.ViewHolder> implements View.OnClickListener {

    private static final String TAG = LongVideoAdapter.class.getSimpleName();

    private List<LongVideoListModel> dataList = new ArrayList<>();
    private LongVideoAdapter.OnItemClickListener mOnItemClickListener = null;

    private Context context;

    //define interface
    public interface OnItemClickListener {
        void onItemClick(View view, int position, LongVideoListModel data);
    }

    public void setOnItemClickListener(LongVideoAdapter.OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public LongVideoAdapter(Context context) {
        this.context = context;
    }

    public void addAll(List<LongVideoListModel> videoList) {
        if (null != videoList) {
            dataList.clear();
            dataList.addAll(videoList);
            notifyDataSetChanged();
        }
    }

    public void addMore(List<LongVideoListModel> videoList) {
        if (null != videoList) {
            dataList.addAll(videoList);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemViewType(int position) {
        //加载多个布局，需要复写该方法
        if (dataList.size() > 1) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public LongVideoAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        Log.d(TAG, "onCreateViewHolder: ");
        View view  = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_long_video_recyler, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        int width = (DimensUtils.getDeviceWidth(context) - DimensUtils.dp2Px(context, 60f)) / 3;
        layoutParams.width = width;
        layoutParams.height = width * 147 / 105 + DimensUtils.dp2Px(context, 40f + 10f + 5f + 10f);//图片高度加上文字高度 加上间距
        //将创建的View注册点击事件
        view.setOnClickListener(this);
        return new LongVideoAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LongVideoAdapter.ViewHolder viewHolder, int position) {
        final LongVideoListModel videoModel = dataList.get(position);
        viewHolder.tvTitle.setText(videoModel.album_title + "");
        /*if (TextUtils.isEmpty(videoModel.album_subtitle))
            viewHolder.tvSubtitle.setText(videoModel.actor);
        else
            viewHolder.tvSubtitle.setText(videoModel.album_subtitle);
        viewHolder.tvScore.setText(videoModel.score + "分");
        viewHolder.tvPush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null == DeviceConnectionManager.getInstance(context.getApplicationContext()).getConnectedDeviceInfo()) {
                    context.startActivity(new Intent(context, ConnectActivity.class));
                } else if (null != videoModel){
                    DeviceConnectionManager.getInstance(MyApplication.getContext()).pushLongVideo(videoModel.third_album_id, 1 + "", videoModel.source, videoModel.album_title);
                    *//*Map<String, String> map = new HashMap<>();
                    map.put("source", mEpisode.source);
                    map.put("video_type", "long");
                    MobclickAgent.onEvent(context, CLICK_PUSH_TO_TV, map);*//*
                }
            }
        });*/

        /*if(!TextUtils.isEmpty( dataList.get(position).play_length)){
            String time = TimeStringUtils.secToTime(Integer.parseInt(dataList.get(position).play_length));
            viewHolder.tvDesc.setText(time);
        }*/

        GlideApp.with(context)
                .load(videoModel.video_poster)
                .centerCrop()
                .into(viewHolder.imgPoster);

        /*GlideApp.with(context)
                .load(getVipIconResId(videoModel.source_sign))
                .centerCrop()
                .transform(new RoundedCornersTransformation(DimensUtils.dp2Px(context, 4f), 0, CornerType.TOP_LEFT))
                .into(viewHolder.imgVip);*/
        if (getVipIconResId(videoModel.source_sign) > 0) {
            viewHolder.imgVip.setVisibility(View.VISIBLE);
        } else {
            viewHolder.imgVip.setVisibility(View.GONE);
        }

        //将position保存在itemView的Tag中，以便点击时进行获取
        viewHolder.itemView.setTag(position);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick: ");
        if (mOnItemClickListener != null) {
            //注意这里使用getTag方法获取position
            int position = (int)v.getTag();
            mOnItemClickListener.onItemClick(v, position, dataList.get(position));
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvTitle;
//        public TextView tvSubtitle;
//        public TextView tvScore;
//        public TextView tvPush;
        public ImageView imgPoster;
        public ImageView imgVip;

        ViewHolder(View view) {
            super(view);
            tvTitle = view.findViewById(R.id.title_tv);
//            tvSubtitle = view.findViewById(R.id.sub_title_tv);
//            tvScore = view.findViewById(R.id.score_tv);
//            tvPush = view.findViewById(R.id.push_tv);
            imgPoster = view.findViewById(R.id.poster_iv);
            imgVip = view.findViewById(R.id.vip_iv);
        }
    }

    private int getVipIconResId(String source_sign) {
        if (null != source_sign) {
            if (source_sign.equals(LongVideoDetailModel.VIP_QiYiGuo)) {
                return R.drawable.icon_vip_qiyiguo;
            } else if (source_sign.equals(LongVideoDetailModel.VIP_GOLD)) {
                return R.drawable.icon_vip_gold;
            } else if (source_sign.equals(LongVideoDetailModel.VIP_TENCENT)) {
                return R.drawable.icon_vip_tencent;
            } else if (source_sign.equals(LongVideoDetailModel.VIP_DingJiJuChang)) {
                return R.drawable.icon_vip_dingjijuchang;
            } else if (source_sign.equals(LongVideoDetailModel.VIP_TENCENT_SPORT)) {
                return R.drawable.icon_vip_tencent_sport;
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }
}
