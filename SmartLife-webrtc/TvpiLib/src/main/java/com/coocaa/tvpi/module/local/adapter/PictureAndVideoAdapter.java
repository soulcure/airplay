package com.coocaa.tvpi.module.local.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.publib.base.GlideApp;
import com.coocaa.publib.data.local.ImageData;
import com.coocaa.publib.data.local.MediaData;
import com.coocaa.publib.data.local.VideoData;
import com.coocaa.tvpi.module.local.PictureAndVideoPreActivity;
import com.coocaa.tvpi.module.log.LogParams;
import com.coocaa.tvpi.module.log.LogSubmit;
import com.coocaa.tvpi.util.SizeConverter;
import com.coocaa.tvpi.util.TimeStringUtils;
import com.coocaa.tvpilib.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName AlbumAdapter
 * @Description 视频和相册混排
 * @User WHY
 * @Date 2018/7/24
 * @Version
 */
public class PictureAndVideoAdapter extends RecyclerView.Adapter {

    private static final String TAG = PictureAndVideoAdapter.class.getSimpleName();
    private static int TYPE_IMAGE = 0;
    private static int TYPE_VIDEO = 1;
    private Context mContext;
    private List<ImageData> imageDataList = new ArrayList<>();
    private List<VideoData> videoDataList = new ArrayList<>();
    private List<MediaData> mediaDataList = new ArrayList<>();
    private String mAlbumName;
    private String id;
    private String name;
    private String type;


    public PictureAndVideoAdapter(Context context, String id, String name,String type) {
        mContext = context;
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public void setImageData(List<ImageData> imageDataList) {
        if (imageDataList != null) {
            this.imageDataList = imageDataList;
            sortMediaList();
            notifyDataSetChanged();
        }
    }

    public void setVideoData(List<VideoData> videoDataList) {
        if (videoDataList != null) {
            this.videoDataList = videoDataList;
            sortMediaList();
            notifyDataSetChanged();
        }
    }

    public void setData(List<VideoData> videoDataList, List<ImageData> imageDataList) {
        if (videoDataList != null) {
            this.videoDataList = videoDataList;
        }
        if (imageDataList != null) {
            this.imageDataList = imageDataList;
        }
        sortMediaList();
        notifyDataSetChanged();
    }

    public void setMediaDataList(List<MediaData> mediaDataList) {
        this.mediaDataList = mediaDataList;
        notifyDataSetChanged();
    }

    public List<MediaData> getMediaDataList(){
        return this.mediaDataList;
    }

    private void sortMediaList() {
        int imageIndex = 0;
        int videoIndex = 0;
        mediaDataList.clear();
        while (imageIndex < imageDataList.size() || videoIndex < videoDataList.size()) {

            if (videoIndex == videoDataList.size()) {
                mediaDataList.add(imageDataList.get(imageIndex));
                imageIndex++;
                continue;
            }

            if (imageIndex == imageDataList.size()) {
                mediaDataList.add(videoDataList.get(videoIndex));
                videoIndex++;
                continue;
            }

            if (imageDataList.get(imageIndex).takeTime.after(videoDataList.get(videoIndex).takeTime)) {
                mediaDataList.add(imageDataList.get(imageIndex));
                imageIndex++;
            } else {
                mediaDataList.add(videoDataList.get(videoIndex));
                videoIndex++;
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_IMAGE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.local_item_picture, parent, false);
            return new ImageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.local_item_video, parent, false);
            return new VideoViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mediaDataList.get(position).type == MediaData.TYPE.IMAGE) {
            return TYPE_IMAGE;
        } else {
            return TYPE_VIDEO;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (mediaDataList.get(position).type == MediaData.TYPE.VIDEO) {
            ((VideoViewHolder) holder).setVideoData(position);
        } else if ((mediaDataList.get(position).type == MediaData.TYPE.IMAGE)) {
            ((ImageViewHolder) holder).setImageData(position);
        }
    }

    @Override
    public int getItemCount() {
        return mediaDataList == null ? 0 : mediaDataList.size();
    }

    private class ImageViewHolder extends RecyclerView.ViewHolder {

        public ImageView coverIV;

        public ImageViewHolder(View view) {
            super(view);
            coverIV = view.findViewById(R.id.item_picture_cover);
        }

        public void setImageData(final int positon) {
            ImageData imageData = (ImageData) mediaDataList.get(positon);

            GlideApp.with(mContext)
                    .load(imageData.url)
                    .centerCrop()
                    .into(coverIV);

            coverIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemOnClick(positon, v);
                }
            });
        }

        private void itemOnClick(int pos, View view) {
//            DeviceConnectionManager deviceConnectionManager = DeviceConnectionManager.getInstance(mContext);

            ImageData imageData = (ImageData) mediaDataList.get(pos);
            String title = imageData.tittle;

            /*if (!SSConnectManager.getInstance().isConnected()) {
                ToastUtils.getInstance().showGlobalShort(R.string.tip_connected_tv);
                new ConnectDialogFragment2().with((AppCompatActivity) mContext).show();
                return;
            }
            if(!SSConnectManager.getInstance().isSameWifi()) {
                ToastUtils.getInstance().showGlobalShort(R.string.not_same_wifi_tips);
                return ;
            }

            SSConnectManager.getInstance().sendImageMessage(imageData.tittle, new File(imageData.data), TARGET_CLIENT_APP_STORE, new IMMessageCallback() {
                @Override
                public void onStart(IMMessage message) {

                }

                @Override
                public void onProgress(IMMessage message, int progress) {

                }

                @Override
                public void onEnd(IMMessage message, int code, String info) {
                    Log.d(TAG, "onEnd: code=" + code + "\n info:" + info);
                }
            });
            ToastUtils.getInstance().showGlobalShort("指令已发送，请在电视端查看");

            btnClickAnim(view);

            submitLocalPushUMData();*/
            submitLocalPushUMData(pos);
            Intent intent = new Intent(mContext, PictureAndVideoPreActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("ALBUMNAME", mAlbumName);
            bundle.putInt("POSITION", pos);
            bundle.putParcelable("MEDIADATA", mediaDataList.get(pos));
            bundle.putString(PictureAndVideoPreActivity.KEY_SHOW_TYPE,type);
            intent.putExtras(bundle);
            mContext.startActivity(intent);
        }

//        private void btnClickAnim(View view) {
//            view.setVisibility(VISIBLE);
//            PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("alpha",1f);
//            PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat("scaleX", 1f, 1.1f, 1f);
//            PropertyValuesHolder pvhZ = PropertyValuesHolder.ofFloat("scaleY", 1f, 1.1f, 1f);
//            ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(view, pvhX, pvhY,pvhZ).setDuration(400);
//            objectAnimator.addListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    super.onAnimationEnd(animation);
//                }
//            });
//            objectAnimator.start();
//        }

    }


    private class VideoViewHolder extends RecyclerView.ViewHolder {

        public View rootView;
        public ImageView posterIV;
        public TextView sizeTV;
        public TextView playLengthTV;

        public VideoViewHolder(View view) {
            super(view);
            rootView = view;
            posterIV = view.findViewById(R.id.item_video_poster);
            sizeTV = view.findViewById(R.id.item_video_size);
            playLengthTV = view.findViewById(R.id.item_video_play_length);
        }

        public void setVideoData(final int positon) {
            final VideoData videoData = (VideoData) mediaDataList.get(positon);
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
//                    VideoPreviewActivity2.start(mContext, positon);
                    submitLocalPushUMData(positon);

                    Intent intent = new Intent(mContext, PictureAndVideoPreActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("ALBUMNAME", mAlbumName);
                    bundle.putInt("POSITION", positon);
                    bundle.putParcelable("MEDIADATA", mediaDataList.get(positon));
                    bundle.putString(PictureAndVideoPreActivity.KEY_SHOW_TYPE,type);
                    intent.putExtras(bundle);
                    mContext.startActivity(intent);
                }
            });
        }
    }

    private void submitLocalPushUMData(int pos) {

        if (mediaDataList.get(pos) == null) {
            return;
        }
        DecimalFormat df = new DecimalFormat("#0.0");
        if (mediaDataList.get(pos).type == MediaData.TYPE.IMAGE) {
            ImageData imageData = (ImageData) mediaDataList.get(pos);
            String size = String.valueOf(df.format(Double.valueOf(imageData.size) / 1024 / 1024));
            LogParams params = LogParams.newParams().append("applet_id", id)
                    .append("applet_name", name)
                    .append("file_size", size)
                    .append("file_format", imageData.url.substring(imageData.url.lastIndexOf('.') + 1))
                    .append("pos_id", String.valueOf(pos + 1));
            LogSubmit.event("local_file_clicked", params.getParams());
        }

        if (mediaDataList.get(pos).type == MediaData.TYPE.VIDEO) {
            VideoData videoData = (VideoData) mediaDataList.get(pos);
            String size = String.valueOf(df.format(Double.valueOf(videoData.size) / 1024 / 1024));
            LogParams params = LogParams.newParams().append("applet_id", id)
                    .append("applet_name", name)
                    .append("file_size", size)
                    .append("file_format", videoData.url.substring(videoData.url.lastIndexOf('.') + 1))
                    .append("pos_id", String.valueOf(pos + 1));
            LogSubmit.event("local_file_clicked", params.getParams());
        }
    }

    public void setAlbumName(String albumName) {
        mAlbumName = albumName;
    }
}
