package com.coocaa.tvpi.module.homepager.main.vy21m4.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.coocaa.publib.data.local.DocumentData;
import com.coocaa.smartscreen.data.function.homepage.SSHomePageBlock;
import com.coocaa.tvpi.module.homepager.main.vy21m4.holder.FuncHolder;
import com.coocaa.tvpi.module.homepager.main.vy21m4.holder.MyContentHolder;
import com.coocaa.tvpi.module.homepager.main.vy21m4.holder.RecentHolder;
import com.coocaa.tvpi.module.local.document.DocumentDataApi;
import com.coocaa.tvpilib.R;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @ClassName SharedSpaceAdapter
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 4/7/21
 * @Version TODO (write something)
 */
public class SharedSpaceAdapter extends RecyclerView.Adapter {

    private static final int POSITION_MY_CONTENT = 0;
    private static final int POSITION_RECENT = 1;
    private static final int POSITION_FUNC = 2;

    private Context mContext;

    private List<SSHomePageBlock> mDataList = new ArrayList<>();

    public void addAll(List<SSHomePageBlock> ssHomePageBlockList) {
        mDataList.clear();
        mDataList.addAll(ssHomePageBlockList);
        notifyDataSetChanged();
    }

    public SharedSpaceAdapter(Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = null;
        if (viewType == POSITION_MY_CONTENT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.my_content_holder_layout,
                    parent, false);
            holder = new MyContentHolder(view);
        } else if (viewType == POSITION_RECENT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.recent_holder_layout,
                    parent, false);
            holder = new RecentHolder(view);
        } else if (viewType == POSITION_FUNC) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.func_holder_layout,
                    parent, false);
            holder = new FuncHolder(view);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == POSITION_MY_CONTENT) {
            if (mDataList != null && mDataList.size() > 0) {
                ((MyContentHolder) holder).onBind(mDataList.get(0).contents);
            }
        } else if (getItemViewType(position) == POSITION_RECENT) {
            ((RecentHolder) holder).onBind();
        } else if (getItemViewType(position) == POSITION_FUNC) {
            if (mDataList != null && mDataList.size() > 1) {
                ((FuncHolder) holder).onBind(mDataList.get(1).contents);
            }
        }
    }

    @Override
    public int getItemCount() {
        List<DocumentData> dataList = DocumentDataApi.getRecordList(mContext);
        if (null != dataList && !dataList.isEmpty()) {
            return 3;
        }
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (getItemCount() == 2) {
            if (position == 0) {
                return POSITION_MY_CONTENT;
            } else {
                return POSITION_FUNC;
            }
        } else {
            return position;
        }
    }
}