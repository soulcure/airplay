package com.coocaa.tvpi.module.local.media;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.coocaa.publib.base.GlideApp;
import com.coocaa.publib.data.local.ImageData;
import com.coocaa.publib.data.local.MediaData;
import com.coocaa.publib.data.local.VideoData;
import com.coocaa.tvpi.module.local.album2.PreviewActivityW7;
import com.coocaa.tvpi.module.local.utils.LocalMediaHelper;
import com.coocaa.tvpi.util.TimeStringUtils;
import com.coocaa.tvpilib.R;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

import static com.coocaa.tvpi.module.local.album2.PreviewActivityW7.SHOW_COLLECT_IMAGE;
import static com.coocaa.tvpi.module.local.album2.PreviewActivityW7.SHOW_COLLECT_VIDEO;

/**
 * @ClassName LocalMediaAdapter
 * @Description TODO (write something)
 * @User heni
 */
public class LocalMediaAdapter extends RecyclerView.Adapter {

    private static final String TAG = LocalMediaAdapter.class.getSimpleName();

    private Context mContext;
    private List<? extends MediaData> mMediaDataList = new ArrayList<>();
    private boolean isSelectedState;
    private boolean isAllItemChecked;
    private int mCheckedNum;
    private OnMediaItemCheckListener mListener;

    public interface OnMediaItemCheckListener {
        void onMediaItemCheck(int checkedNum, boolean isChecked, MediaData mediaData);

        void onMediaItemAllCheck(int checkedNum, boolean isChecked, List<MediaData> mediaDatas);
    }

    public void setOnMediaItemCheckLis(OnMediaItemCheckListener listener) {
        mListener = listener;
    }

    public LocalMediaAdapter(Context context) {
        mContext = context;
        resetData();
    }

    private void resetData() {
        isSelectedState = false;
        isAllItemChecked = false;
        mCheckedNum = 0;
    }

    public void setData(List<? extends MediaData> dataList) {
        if (dataList != null) {
            Log.d(TAG, "setData: " + dataList.size());
            resetData();
            this.mMediaDataList = dataList;
            notifyDataSetChanged();
        }
    }

    public void showCheckbox() {
        mCheckedNum = 0;
        isSelectedState = true;
        notifyDataSetChanged();
    }

    public void hideCheckbox() {
        isSelectedState = false;
        notifyDataSetChanged();
    }

    public void updateAllItem(boolean isAllItemChecked) {
        this.isAllItemChecked = isAllItemChecked;
        if (mMediaDataList != null && isAllItemChecked) {
            mCheckedNum = mMediaDataList.size();
        } else {
            mCheckedNum = 0;
        }

        if (mMediaDataList != null && !mMediaDataList.isEmpty()) {
            for (MediaData mediaData : mMediaDataList) {
                mediaData.isCheck = isAllItemChecked;
            }
        }
        notifyDataSetChanged();
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
        if (mMediaDataList != null) {
            Log.d(TAG, "getItemCount: " + mMediaDataList.size());
        }
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

            if (isSelectedState) {
                checkbox.setVisibility(View.VISIBLE);
            } else {
                checkbox.setVisibility(View.GONE);
            }
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
                //处理选中
                if(checkbox != null && checkbox.getVisibility() == View.VISIBLE) {
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
                    return;
                }

                //处理点击
                if (mediaData instanceof ImageData) {
                    PreviewActivityW7.start(mContext, LocalMediaHelper.COLLECTION_IMAGE_ALBUM_NAME, positon,
                            SHOW_COLLECT_IMAGE);
                } else if (mediaData instanceof VideoData) {
                    PreviewActivityW7.start(mContext, LocalMediaHelper.COLLECTION_VIDEO_ALBUM_NAME, positon,
                            SHOW_COLLECT_VIDEO);
                }
            });

            //处理全选和取消全选
            if (isSelectedState) {
                checkbox.setVisibility(View.VISIBLE);
            } else {
                checkbox.setVisibility(View.GONE);
            }

            if (mediaData.isCheck) {
                checkbox.setSelected(true);
            } else {
                checkbox.setSelected(false);
            }

            //处理单选
           /* checkView.setOnClickListener(v -> {
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
            });*/
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
