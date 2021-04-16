package com.coocaa.tvpi.module.local.media;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.coocaa.publib.base.GlideApp;
import com.coocaa.publib.data.local.ImageData;
import com.coocaa.publib.data.local.MediaData;
import com.coocaa.publib.data.local.VideoData;
import com.coocaa.tvpi.util.TimeStringUtils;
import com.coocaa.tvpilib.R;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

/**
 * @ClassName LocalMediaAddAdapter
 * @Description TODO (write something)
 * @User heni
 * @Date 4/7/21
 */
public class LocalMediaAddAdapter extends RecyclerView.Adapter {

    private static final String TAG = LocalMediaAddAdapter.class.getSimpleName();

    private Context mContext;
    private List<? extends MediaData> mMediaDataList = new ArrayList<>();
    private int mCheckedNum;
    private OnMediaItemCheckListener mListener;

    public interface OnMediaItemCheckListener {
        void onMediaItemCheck(int checkedNum, boolean isChecked, MediaData mediaData);
    }

    public void setOnMediaItemCheckLis(OnMediaItemCheckListener listener) {
        mListener = listener;
    }

    public LocalMediaAddAdapter(Context context) {
        mContext = context;
        mCheckedNum = 0;
    }

    public void setData(List<? extends MediaData> dataList) {
        if (mMediaDataList != null) {
            this.mMediaDataList = dataList;
            notifyDataSetChanged();
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.local_item_media,
                parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((ViewHolder) holder).setData(position);
    }

    @Override
    public int getItemCount() {
        return mMediaDataList == null ? 0 : mMediaDataList.size();
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        public View rootView;
        public ImageView posterIV;
        //点击范围太小，选不中，加底扩大选中范围
        public View checkView;
        public TextView checkbox;
        public View bottomView;
        public TextView playLengthTV;

        public ViewHolder(View view) {
            super(view);
            rootView = view;
            posterIV = view.findViewById(R.id.item_video_poster);
            checkView = view.findViewById(R.id.check_view);
            checkbox = view.findViewById(R.id.checkbox);
            bottomView = view.findViewById(R.id.bottom_layout);
            playLengthTV = view.findViewById(R.id.item_video_play_length);
        }

        public void setData(final int positon) {
            final MediaData mediaData = mMediaDataList.get(positon);
            if (mediaData == null) {
                return;
            }

            if (mediaData instanceof ImageData) {
                bottomView.setVisibility(View.GONE);
                GlideApp.with(mContext)
                        .load(mMediaDataList.get(positon).url)
                        .centerCrop()
                        .into(posterIV);

            } else if (mediaData instanceof VideoData) {
                final VideoData videoData = (VideoData) mediaData;
                bottomView.setVisibility(View.VISIBLE);
                GlideApp.with(mContext)
                        .load(videoData.thumbnailPath)
                        .centerCrop()
                        .into(posterIV);
                playLengthTV.setText(TimeStringUtils.secToTime(videoData.duration / 1000));
            }

            rootView.setOnClickListener(v -> {
                if (checkbox.isSelected()) {
                    mediaData.isCheck = false;
                    mCheckedNum -= 1;
                    if (mListener != null) {
                        mListener.onMediaItemCheck(mCheckedNum, false, mediaData);
                    }
                } else {
                    mediaData.isCheck = true;
                    mCheckedNum += 1;
                    if (mListener != null) {
                        mListener.onMediaItemCheck(mCheckedNum, true, mediaData);
                    }
                }
                notifyItemChanged(positon);
            });

            if(mediaData.isCheck) {
                checkbox.setSelected(true);
            }else {
                checkbox.setSelected(false);
            }

//            checkView.setOnClickListener(v -> {
//                if (checkbox.isSelected()) {
//                    mediaData.isCheck = false;
//                    mCheckedNum -= 1;
//                    if (mListener != null) {
//                        mListener.onMediaItemCheck(mCheckedNum, false, mediaData);
//                    }
//                } else {
//                    mediaData.isCheck = true;
//                    mCheckedNum += 1;
//                    if (mListener != null) {
//                        mListener.onMediaItemCheck(mCheckedNum, true, mediaData);
//                    }
//                }
//                notifyItemChanged(positon);
//            });
        }

/*        private void itemOnClick(int pos, View view) {
//            DeviceConnectionManager deviceConnectionManager = DeviceConnectionManager
//            .getInstance(mContext);

            ImageData imageData = imageDataList.get(pos);
            String title = imageData.tittle;

            *//*if (!SSConnectManager.getInstance().isConnected()) {
                ToastUtils.getInstance().showGlobalShort(R.string.tip_connected_tv);
                new ConnectDialogFragment2().with((AppCompatActivity) mContext).show();
                return;
            }
            if(!SSConnectManager.getInstance().isSameWifi()) {
                ToastUtils.getInstance().showGlobalShort(R.string.not_same_wifi_tips);
                return ;
            }

            SSConnectManager.getInstance().sendImageMessage(imageData.tittle, new File(imageData
            .data), TARGET_CLIENT_APP_STORE, new IMMessageCallback() {
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

            submitLocalPushUMData();*//*
//            submitLocalPushUMData(pos);
            Intent intent = new Intent(mContext, AlbumPreviewActivity2.class);
            Bundle bundle = new Bundle();
            bundle.putString("ALBUMNAME", mAlbumName);
            bundle.putInt("POSITION", pos);
            bundle.putParcelable("IMAGEDATA", imageData);
            intent.putExtras(bundle);
            mContext.startActivity(intent);
        }*/

//        private void btnClickAnim(View view) {
//            view.setVisibility(VISIBLE);
//            PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("alpha",1f);
//            PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat("scaleX", 1f, 1.1f, 1f);
//            PropertyValuesHolder pvhZ = PropertyValuesHolder.ofFloat("scaleY", 1f, 1.1f, 1f);
//            ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(view, pvhX,
//            pvhY,pvhZ).setDuration(400);
//            objectAnimator.addListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    super.onAnimationEnd(animation);
//                }
//            });
//            objectAnimator.start();
//        }

    }

//    private void submitLocalPushUMData(int pos) {
////        Map<String, String> map = new HashMap<>();
////        map.put("type", "picture");
////        MobclickAgent.onEvent(mContext, CAST_LOCAL_RESOURCE, map);
//        DecimalFormat df = new DecimalFormat("#0.0");
//        String size = String.valueOf(df.format(Double.valueOf(imageDataList.get(pos).size) /
//        1024 / 1024));
//        LogParams params = LogParams.newParams().append("applet_id", id)
//                .append("applet_name", name)
//                .append("file_size", size)
//                .append("file_format", imageDataList.get(pos).url.substring(imageDataList.get
//                (pos).url.lastIndexOf('.') + 1))
//                .append("pos_id", String.valueOf(pos + 1));
//        LogSubmit.event("local_file_clicked", params.getParams());
//        Log.d(TAG, "submitLocalPushUMData: " + size);
//    }

}
