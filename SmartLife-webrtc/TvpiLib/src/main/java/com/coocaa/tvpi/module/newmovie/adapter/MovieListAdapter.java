package com.coocaa.tvpi.module.newmovie.adapter;

import android.view.View;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.coocaa.publib.base.GlideApp;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.data.account.CoocaaUserInfo;
import com.coocaa.smartscreen.data.movie.LongVideoListModel;
import com.coocaa.tvpi.module.log.LogParams;
import com.coocaa.tvpi.module.log.LogSubmit;
import com.coocaa.tvpi.module.login.UserInfoCenter;
import com.coocaa.tvpi.module.movie.LongVideoDetailActivity2;
import com.coocaa.tvpilib.R;

import org.jetbrains.annotations.NotNull;

import swaiotos.channel.iot.ss.device.Device;

/**
 * 影视列表适配器
 * Created by songxing on 2020/7/9
 */
public class MovieListAdapter extends BaseQuickAdapter<LongVideoListModel, BaseViewHolder> {

    public MovieListAdapter() {
        super(R.layout.item_movie);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, LongVideoListModel longVideoListModel) {
        holder.setText(R.id.name, longVideoListModel.album_title);
        ImageView imageView = holder.getView(R.id.cover);
        imageView.setTag(holder.getAdapterPosition());
        Object tag = imageView.getTag();
        if (!(tag instanceof String && tag.equals(longVideoListModel.video_poster))) {
            imageView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.place_holder_bg_movie));
        }
        imageView.setTag(longVideoListModel.video_poster);
        GlideApp.with(getContext())
                .load(longVideoListModel.video_poster)
                .error(R.drawable.place_holder_bg_movie)
                .dontAnimate()
                .into(imageView);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LongVideoDetailActivity2.start(getContext(), longVideoListModel.third_album_id);
                submitMovieClick(longVideoListModel.third_album_id,longVideoListModel.album_title,holder.getAdapterPosition());
            }
        });
    }


    private void submitMovieClick(String id,String name,int position){
        Device device = SSConnectManager.getInstance().getDevice();
        CoocaaUserInfo coocaaUserInfo = UserInfoCenter.getInstance().getCoocaaUserInfo();
        LogParams params = LogParams.newParams()
                .append("ss_device_id", device == null ? "disconnected" : device.getLsid())
                .append("ss_device_type", device == null ? "disconnected" : device.getZpRegisterType())
                .append("account", coocaaUserInfo == null ? "not_login" : coocaaUserInfo.getOpen_id())
                .append("block_id", id)
                .append("block_name", name)
                .append("pos_id", position+"");
        LogSubmit.event("movie_block_clicked", params.getParams());
    }
}
