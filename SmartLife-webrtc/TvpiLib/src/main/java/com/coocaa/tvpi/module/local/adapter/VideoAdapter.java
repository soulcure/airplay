package com.coocaa.tvpi.module.local.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.publib.base.GlideApp;
import com.coocaa.publib.data.local.VideoData;
import com.coocaa.tvpi.module.local.VideoPlayerActivity;
import com.coocaa.tvpi.module.local.VideoPreviewActivity2;
import com.coocaa.tvpi.module.log.LogParams;
import com.coocaa.tvpi.module.log.LogSubmit;
import com.coocaa.tvpi.util.SizeConverter;
import com.coocaa.tvpi.util.TimeStringUtils;
import com.coocaa.tvpilib.R;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName AlbumAdapter
 * @Description TODO (write something)
 * @User WHY
 * @Date 2018/7/24
 * @Version TODO (write something)
 */
public class VideoAdapter extends RecyclerView.Adapter {

    private static final String TAG = VideoAdapter.class.getSimpleName();

    private Context mContext;
    private List<VideoData> dataList = new ArrayList<>();
    private String id;
    private String name;

    public VideoAdapter(Context context, String id, String name) {
        mContext = context;
        this.id = id;
        this.name = name;
    }

    public void addAll(List<VideoData> dataList) {
        this.dataList = dataList;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.local_item_video, parent,
                false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((ViewHolder) holder).setData(position);
    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        public View rootView;
        public ImageView posterIV;
        public TextView sizeTV;
        public TextView playLengthTV;

        public ViewHolder(View view) {
            super(view);
            rootView = view;
            posterIV = view.findViewById(R.id.item_video_poster);
            sizeTV = view.findViewById(R.id.item_video_size);
            playLengthTV = view.findViewById(R.id.item_video_play_length);
        }

        public void setData(final int positon) {
            final VideoData videoData = dataList.get(positon);
            if (videoData == null) {
                return;
            }

            GlideApp.with(mContext)
                    .load(videoData.thumbnailPath)
                    .centerCrop()
                    .into(posterIV);

            playLengthTV.setText(TimeStringUtils.secToTime(videoData.duration / 1000));
            sizeTV.setText(SizeConverter.BTrim.convert(Float.valueOf(videoData.size)));

            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Intent intent = new Intent(mContext, VideoPlayerActivity.class);
//                    intent.putExtra(VideoPlayerActivity.KEY_VIDEO_DATAS, (Serializable) videoData);
//                    mContext.startActivity(intent);
                    VideoPreviewActivity2.start(mContext,positon);
                    submitLocalPushUMData(positon);
                }
            });
        }
    }

    private void submitLocalPushUMData(int pos) {
//        Map<String, String> map = new HashMap<>();
//        map.put("type", "picture");
//        MobclickAgent.onEvent(mContext, CAST_LOCAL_RESOURCE, map);
        DecimalFormat df = new DecimalFormat("#0.0");
        String size = String.valueOf(df.format(Double.valueOf(dataList.get(pos).size) / 1024 / 1024));
        LogParams params = LogParams.newParams().append("applet_id", id)
                .append("applet_name", name)
                .append("file_size", size)
                .append("file_format", dataList.get(pos).url.substring(dataList.get(pos).url.lastIndexOf('.') + 1))
                .append("pos_id", String.valueOf(pos + 1));
        LogSubmit.event("local_file_clicked", params.getParams());

        Log.d(TAG, "submitLocalPushUMData: " + dataList.get(pos).url);

    }

}
