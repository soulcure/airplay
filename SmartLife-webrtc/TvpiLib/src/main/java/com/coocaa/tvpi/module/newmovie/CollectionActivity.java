package com.coocaa.tvpi.module.newmovie;

import android.content.Context;
import android.content.Intent;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.smartscreen.data.movie.CollectionModel;
import com.coocaa.tvpi.base.mvvm.list.AbsListViewModelActivity;
import com.coocaa.tvpi.module.newmovie.adapter.CollectionAdapter;
import com.coocaa.tvpi.module.newmovie.viewmodel.CollectionViewModel;
import com.coocaa.tvpi.view.CommonTitleBar;
import com.coocaa.tvpi.view.decoration.PictureItemDecoration;
import com.coocaa.tvpilib.R;

/**
 * 影视收藏
 * Created by songxing on 2020/9/25
 */
public class CollectionActivity extends AbsListViewModelActivity<CollectionViewModel,CollectionModel> {

    public static void start(Context context) {
        Intent starter = new Intent(context, CollectionActivity.class);
        // 视频类型,0:短片,1:正片
        starter.putExtra("key",String.valueOf(1));
        context.startActivity(starter);
    }

    @Override
    protected void initTitleBar(CommonTitleBar titleBar) {
        titleBar.setText(CommonTitleBar.TextPosition.TITLE,"我的收藏");
        titleBar.setImageButtonResId(CommonTitleBar.ImagePosition.RIGHT, R.drawable.movie_search);
        titleBar.setOnClickListener(new CommonTitleBar.OnClickListener() {
            @Override
            public void onClick(CommonTitleBar.ClickPosition position) {
                if(position == CommonTitleBar.ClickPosition.LEFT){
                    finish();
                }else {
                    MovieSearchActivity.start(CollectionActivity.this);
                }
            }
        });
    }

    @Override
    protected RecyclerView.LayoutManager createLayoutManager() {
        return new GridLayoutManager(this, 3);
    }

    @Override
    protected RecyclerView.ItemDecoration createItemDecoration() {
        return new PictureItemDecoration(3,
                DimensUtils.dp2Px(this,10),
                DimensUtils.dp2Px(this,15));
    }

    @Override
    protected BaseQuickAdapter<CollectionModel, BaseViewHolder> createAdapter() {
        return new CollectionAdapter();
    }
}
