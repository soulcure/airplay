package com.coocaa.tvpi.module.homepager.main.vy21m4.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.coocaa.publib.base.GlideApp;
import com.coocaa.publib.data.local.DocumentData;
import com.coocaa.publib.data.local.ImageData;
import com.coocaa.publib.data.local.MediaData;
import com.coocaa.publib.data.local.VideoData;
import com.coocaa.smartscreen.data.function.FunctionBean;
import com.coocaa.tvpi.event.AppAreaRefreshEvent;
import com.coocaa.tvpi.module.io.HomeIOThread;
import com.coocaa.tvpi.module.io.HomeUIThread;
import com.coocaa.tvpi.module.local.document.DocumentDataApi;
import com.coocaa.tvpi.module.local.utils.LocalMediaHelper;
import com.coocaa.tvpi.module.web.BrowserRecordUtils;
import com.coocaa.tvpi.module.web.WebRecordBean;
import com.coocaa.tvpi.util.TvpiClickUtil;
import com.coocaa.tvpilib.R;

import org.greenrobot.eventbus.EventBus;

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
public class MyContentAdapter extends RecyclerView.Adapter {

    private Context mContext;

    private List<FunctionBean> mDataList = new ArrayList<>();

    public MyContentAdapter(Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.my_content_item_holder_layout, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((ViewHolder) holder).onBind(mDataList.get(position));
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public void addAll(List<FunctionBean> myContentDataList) {
        mDataList.clear();
        mDataList.addAll(myContentDataList);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        View itemView;
        ImageView icon;
        TextView title;
        TextView count;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            icon = itemView.findViewById(R.id.my_content_item_icon);
            title = itemView.findViewById(R.id.my_content_item_title);
            count = itemView.findViewById(R.id.my_content_item_count);
        }

        public void onBind(FunctionBean data) {
            if (data == null)
                return;

            GlideApp.with(icon).load(data.icon).into(icon);
            title.setText(data.name);

            if("com.coocaa.smart.localdoc_guide".equals(data.id)) {//文档数量
                List<DocumentData> dataList = DocumentDataApi.getRecordList(mContext);
                if (null == dataList) {
                    count.setText("0");
                } else {
                    count.setText(dataList.size() + "");
                }
            } else if("com.coocaa.smart.mypicture".equals(data.id)) {//图片数量
                List<MediaData> dataList = LocalMediaHelper.getInstance().getCollectedMediaData_Image(mContext);
                if (null == dataList) {
                    count.setText("0");
                } else {
                    count.setText(dataList.size() + "");
                }
            } else if("com.coocaa.smart.myvideo".equals(data.id)) {//视频数量
                List<MediaData> dataList = LocalMediaHelper.getInstance().getCollectedMediaData_Video(mContext);
                if (null == dataList) {
                    count.setText("0");
                } else {
                    count.setText(dataList.size() + "");
                }
            } else if("webapp.skyworthiot.com".equals(data.id)) {//气氛数量
                count.setText(data.quantity + "");
            } else if("com.coocaa.smart.browser".equals(data.id)) {//链接数量
                HomeIOThread.execute(new Runnable() {
                    @Override
                    public void run() {
                        List<WebRecordBean> dataList = BrowserRecordUtils.getRecord(mContext);
                        HomeUIThread.execute(new Runnable() {
                            @Override
                            public void run() {
                                if (null == dataList) {
                                    count.setText("0");
                                } else {
                                    count.setText(dataList.size() + "");
                                }
                            }
                        });
                    }
                });
            } else if("com.coocaa.smart.timealbum".equals(data.id)) {//时光相册
                count.setText("即将上线");
            } else {
                count.setText("0");
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TvpiClickUtil.onClick(itemView.getContext(),data.uri());
                    if ("atmosphere/h5v2/index.html".equals(data.target)) {
                        EventBus.getDefault().post(new AppAreaRefreshEvent());
                    }
                }
            });
        }
    }
}