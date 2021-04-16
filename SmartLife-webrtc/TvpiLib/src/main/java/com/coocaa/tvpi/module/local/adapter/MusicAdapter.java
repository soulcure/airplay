package com.coocaa.tvpi.module.local.adapter;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.coocaa.publib.data.local.AudioData;
import com.coocaa.tvpi.util.TimeStringUtils;
import com.coocaa.tvpilib.R;
import com.umeng.analytics.MobclickAgent;

import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.recyclerview.widget.RecyclerView;

import static com.coocaa.tvpi.common.UMengEventId.CAST_LOCAL_RESOURCE;

/**
 * @ClassName MusicAdapter
 * @Description 本地音乐适配器
 * @User WHY
 * @Date 2018/7/24
 */
public class MusicAdapter extends RecyclerView.Adapter {

    private static final String TAG = MusicAdapter.class.getSimpleName();

    private Context mContext;
    private OnMusicItemClickListener listener;
    private List<AudioData> dataList = new ArrayList<>();
    private int selectedPosition = -1;
    private String curPlayTime = "00:00", totalPlayTime;
    private ViewHolder curHolder;

    public interface OnMusicItemClickListener {
        void onMusicItemClick(int position, AudioData audioData);

        void onMusicPush(AudioData audioData);
    }

    public void setOnMusicItemClickLis(OnMusicItemClickListener listener) {
        this.listener = listener;
    }

    public MusicAdapter(Context context) {
        mContext = context;
    }

    public void addAll(List<AudioData> dataList) {
        this.dataList = dataList;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.local_item_music,
                parent, false);
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

        public RelativeLayout layout;
        public ImageView cover;
        public TextView titleTV;
        public TextView playLengthTV;
        public TextView allLengthTV;
        public TextView singerTV;
        public TextView pushTV;

        public ViewHolder(View view) {
            super(view);
            layout = itemView.findViewById(R.id.item_music_rl);
            cover = view.findViewById(R.id.item_music_default_icon);
            titleTV = view.findViewById(R.id.item_music_title);
            playLengthTV = view.findViewById(R.id.item_music_play_length);
            allLengthTV = view.findViewById(R.id.item_music_play_length_2);
            singerTV = view.findViewById(R.id.item_music_singer_name);
            pushTV = view.findViewById(R.id.item_music_push);
        }

        public void setData(final int position) {
            final AudioData audioData = dataList.get(position);
            if (audioData == null) {
                return;
            }

            titleTV.setText(audioData.tittle);
            playLengthTV.setText("00:00");
            allLengthTV.setText(" / " + TimeStringUtils.secToTime(audioData.duration / 1000));
            singerTV.setText(audioData.singer);

            if (position != selectedPosition) {
                titleTV.setTextColor(mContext.getResources().getColor(R.color.c_1));
                playLengthTV.setTextColor(mContext.getResources().getColor(R.color.black_40));
                allLengthTV.setTextColor(mContext.getResources().getColor(R.color.black_40));
                singerTV.setTextColor(mContext.getResources().getColor(R.color.black_40));
            } else {
                titleTV.setTextColor(mContext.getResources().getColor(R.color.ff4681ff));
                playLengthTV.setTextColor(mContext.getResources().getColor(R.color.ff4681ff));
                allLengthTV.setTextColor(mContext.getResources().getColor(R.color.ff4681ff));
                singerTV.setTextColor(mContext.getResources().getColor(R.color.ff4681ff));
                curHolder = this;
                totalPlayTime = TimeStringUtils.secToTime(audioData.duration / 1000);
            }

            RequestOptions options = new RequestOptions()
                    .placeholder(R.drawable.local_icon_music_default)
                    .error(R.drawable.local_icon_music_default);
            Glide.with(mContext)
                    .load(loadAlbum(position, audioData))
                    .apply(options)
                    .into(cover);

            pushTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //pushMusic(audioData);
                    listener.onMusicPush(audioData);
                }
            });

            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onMusicItemClick(position, audioData);
                        if (selectedPosition != position) { //切换了item点击，curPaly置初始值
                            curPlayTime = "00:00";
                            if (selectedPosition != -1) {
                                //原来的置灰色，当前的置黄色
                                notifyItemChanged(selectedPosition);
                            }
                            selectedPosition = position;
                            notifyItemChanged(selectedPosition);
                        }
                    }
                }
            });
        }
    }

    public void refreshPlayTime(String time) {
        curPlayTime = time;
        if (null != curHolder && null != curHolder.playLengthTV) {
            curHolder.playLengthTV.setText(curPlayTime);
        }
    }

    private HashMap<Integer, Bitmap> tempMap = new HashMap<>();

    /**
     * 获取专辑封面
     *
     * @param audioData
     * @return
     */
    private Bitmap loadAlbum(int position, AudioData audioData) {

        if (tempMap == null)
            tempMap = new HashMap<>();

        Bitmap tempBitmap = tempMap.get(position);
        if (tempMap.size() > 0 && tempBitmap != null) {
            return tempBitmap;
        }
        Uri uri = null;
        FileDescriptor fileDescriptor = null;
        if (audioData.albumId < 0) {
            uri = Uri.parse("content://media/external/audio/media/"
                    + audioData.albumId + "/albumart");
        } else {
            uri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), audioData.albumId);
        }
        try {
            ParcelFileDescriptor pfd = mContext.getContentResolver().openFileDescriptor(uri, "r");
            if (pfd != null) {
                fileDescriptor = pfd.getFileDescriptor();
            }
            Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            tempMap.put(position, bitmap);
            return bitmap;
        } catch (Exception | OutOfMemoryError e) {
            e.printStackTrace();
        }
        return null;
    }


    private void submitLocalPushUMData() {
        Map<String, String> map = new HashMap<>();
        map.put("type", "music");
        MobclickAgent.onEvent(mContext, CAST_LOCAL_RESOURCE, map);
    }
}
