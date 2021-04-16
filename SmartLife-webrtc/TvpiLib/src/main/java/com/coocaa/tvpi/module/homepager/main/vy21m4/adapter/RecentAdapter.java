package com.coocaa.tvpi.module.homepager.main.vy21m4.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.coocaa.publib.base.GlideApp;
import com.coocaa.publib.data.local.DocumentData;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.tvpi.module.local.document.DocumentUtil;
import com.coocaa.tvpi.module.local.document.FormatEnum;
import com.coocaa.tvpi.module.local.document.page.DocumentMainActivity;
import com.coocaa.tvpi.module.local.document.page.DocumentPlayerActivity;
import com.coocaa.tvpilib.R;

import java.io.File;
import java.sql.Date;
import java.text.SimpleDateFormat;
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
public class RecentAdapter extends RecyclerView.Adapter {

    private Context mContext;

    private List<DocumentData> mDataList = new ArrayList<>();

    public RecentAdapter(Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.recent_item_holder_layout, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((ViewHolder) holder).onBind(mDataList.get(position));
    }

    @Override
    public int getItemCount() {
        if (mDataList.size() > 2) {
            return 2;
        }
        return mDataList.size();
    }

    public void addAll(List<DocumentData> dataList) {
        mDataList.clear();
        mDataList.addAll(dataList);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        protected String mDatePattern = "yyyy-MM-dd HH:mm:ss";
        protected SimpleDateFormat mDateFormat;

        View itemView;
        ImageView icon;
        TextView title;
        TextView date;
        TextView size;
        ImageView push;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            icon = itemView.findViewById(R.id.recent_item_icon);
            title = itemView.findViewById(R.id.recent_item_title);
            date = itemView.findViewById(R.id.recent_item_date);
            size = itemView.findViewById(R.id.recent_item_size);
            push = itemView.findViewById(R.id.recent_item_push);
        }

        public void onBind(DocumentData data) {
            if (data == null)
                return;
            icon.setImageResource(FormatEnum.getFormat(data.suffix).icon);
            title.setText(data.tittle);
            mDateFormat = new SimpleDateFormat(mDatePattern);
            String time = mDateFormat.format(new Date(data.lastModifiedTime));
            date.setText(time);
            size.setText(Formatter.formatFileSize(mContext, data.size));
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        File file = new File(data.url);
                        if (file.exists()) {
                            if (file.length() > 0) {
                                Intent intent = new Intent(mContext, DocumentPlayerActivity.class);
                                intent.putExtra(DocumentUtil.KEY_FILE_PATH, data.url);
                                intent.putExtra(DocumentUtil.KEY_FILE_SIZE, String.valueOf(data.size));
                                intent.putExtra(DocumentUtil.KEY_SOURCE_PAGE, DocumentUtil.SOURCE_PAGE_DOC_MAIN);
                                mContext.startActivity(intent);
                            } else {
                                ToastUtils.getInstance().showGlobalLong("文件已损坏");
                            }
                        } else {
                            ToastUtils.getInstance().showGlobalLong("文件不存在");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            push.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        File file = new File(data.url);
                        if (file.exists()) {
                            if (file.length() > 0) {
                                DocumentPlayerActivity.pushDoc(mContext, data.url);
//                                submitLocalPushUMData(position, documentData);
                            } else {
                                ToastUtils.getInstance().showGlobalLong("文件已损坏");
                            }
                        } else {
                            ToastUtils.getInstance().showGlobalLong("文件不存在");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}