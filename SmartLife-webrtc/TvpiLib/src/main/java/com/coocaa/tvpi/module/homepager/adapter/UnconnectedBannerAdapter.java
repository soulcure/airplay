package com.coocaa.tvpi.module.homepager.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.publib.base.GlideApp;
import com.coocaa.tvpi.module.homepager.adapter.bean.UnConnectBannerBean;
import com.youth.banner.adapter.BannerAdapter;

import java.util.List;


/**
 * 自定义布局，图片
 */
public class UnconnectedBannerAdapter extends BannerAdapter<UnConnectBannerBean, UnconnectedBannerAdapter.ImageHolder> {
    private Context context;

    public UnconnectedBannerAdapter(Context context,List<UnConnectBannerBean> mDatas) {
        //设置数据，也可以调用banner提供的方法,或者自己在adapter中实现
        super(mDatas);
        this.context = context;
    }

    //更新数据
    public void updateData(List<UnConnectBannerBean> data) {
        //这里的代码自己发挥，比如如下的写法等等
        mDatas.clear();
        mDatas.addAll(data);
        notifyDataSetChanged();
    }


    //创建ViewHolder，可以用viewType这个字段来区分不同的ViewHolder
    @Override
    public ImageHolder onCreateHolder(ViewGroup parent, int viewType) {
        ImageView imageView = new ImageView(parent.getContext());
        //注意，必须设置为match_parent，这个是viewpager2强制要求的
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        imageView.setLayoutParams(params);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return new ImageHolder(imageView);
    }

    @Override
    public void onBindView(ImageHolder holder, UnConnectBannerBean data, int position, int size) {
        GlideApp.with(context)
                .load(data.imageUrl)
                .centerCrop()
                .into(holder.imageView);
    }

    public class ImageHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;

        public ImageHolder(@NonNull View view) {
            super(view);
            this.imageView = (ImageView) view;
        }
    }

}
