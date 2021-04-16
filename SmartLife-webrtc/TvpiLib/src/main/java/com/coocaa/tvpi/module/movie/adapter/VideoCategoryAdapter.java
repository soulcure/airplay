package com.coocaa.tvpi.module.movie.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.publib.data.category.CategoryMainModel;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.tvpi.module.movie.adapter.holder.Category1RowHolder;
import com.coocaa.tvpi.module.movie.adapter.holder.Category2RowHolder;
import com.coocaa.tvpilib.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName VideoCategoryAdapter
 * @Description TODO (write something)
 * @User WHY
 * @Date 2019/3/21
 * @Version TODO (write something)
 */
public class VideoCategoryAdapter extends RecyclerView.Adapter {

    private static final String TAG = VideoCategoryAdapter.class.getCanonicalName();

    private static int TYPE_1 = 0;
    private static int TYPE_2 = 1;

    private Context context;
    private List<CategoryMainModel> data;

    int count;

    public VideoCategoryAdapter(Context context) {
        this.context = context;
        data = new ArrayList<>();
    }

    public void addAll(List<CategoryMainModel> data) {
        this.data.clear();
        this.data.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        RecyclerView.ViewHolder holder = null;
        if (viewType == TYPE_1) {
            v = LayoutInflater.from(context).inflate(R.layout.category_1_row_holder_layout, parent, false);
            ViewGroup.LayoutParams params = v.getLayoutParams();
            int screenW = DimensUtils.getDeviceWidth(context);
            int width = (screenW - DimensUtils.dp2Px(context, 11*2) - DimensUtils.dp2Px(context, 13*4)) /  5;
            params.width = width;
            holder = new Category1RowHolder(v);
        } else if (viewType == TYPE_2) {
            v = LayoutInflater.from(context).inflate(R.layout.category_2_row_holder_layout, parent, false);
            ViewGroup.LayoutParams params = v.getLayoutParams();
            int screenW = DimensUtils.getDeviceWidth(context);
            int width = (screenW - DimensUtils.dp2Px(context, 11*2) - DimensUtils.dp2Px(context, 13*4)) /  5;
            params.width = width;
            holder = new Category2RowHolder(v);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_1) {
            ((Category1RowHolder) holder).onBind(data.get(position));
        } else if (getItemViewType(position) == TYPE_2) {
            if (position < getItemCount() - 1) {
                ((Category2RowHolder) holder).onBind(data.get(position*2), data.get(position*2 + 1));
            } else {
                ((Category2RowHolder) holder).onBind(data.get(position*2), null);
            }
        }
    }

    @Override
    public int getItemCount() {
        int size = 0;
        if (data.size() > 0 && data.size() < 10)
            size = data.size();
        else if (data.size() >= 10) {
            size = data.size() / 2 + data.size() % 2;
        }
        return size;
    }

    @Override
    public int getItemViewType(int position) {
        if (data.size() >= 10) {
            return TYPE_2;
        } else {
            return TYPE_1;
        }
    }

}
