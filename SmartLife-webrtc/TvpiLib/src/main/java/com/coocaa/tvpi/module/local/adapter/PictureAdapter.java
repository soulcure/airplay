package com.coocaa.tvpi.module.local.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.coocaa.publib.base.GlideApp;
import com.coocaa.publib.data.local.ImageData;
import com.coocaa.tvpi.module.local.album.AlbumPreviewActivity2;
import com.coocaa.tvpi.module.log.LogParams;
import com.coocaa.tvpi.module.log.LogSubmit;
import com.coocaa.tvpilib.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

/**
 * @ClassName AlbumAdapter
 * @Description TODO (write something)
 * @User WHY
 * @Date 2018/7/24
 * @Version TODO (write something)
 */
public class PictureAdapter extends RecyclerView.Adapter {

    private static final String TAG = PictureAdapter.class.getSimpleName();

    private Context mContext;
    private List<ImageData> imageDataList = new ArrayList<>();
    private String mAlbumName;
    private String id;
    private String name;

    public PictureAdapter(Context context, String id, String name) {
        mContext = context;
        this.id = id;
        this.name = name;
    }

    public void setData(List<ImageData> imageDataList) {
        if (imageDataList != null) {
            this.imageDataList = imageDataList;
            notifyDataSetChanged();
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.local_item_picture, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((ViewHolder) holder).setData(position);
    }

    @Override
    public int getItemCount() {
        return imageDataList == null ? 0 : imageDataList.size();
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView coverIV;

        public ViewHolder(View view) {
            super(view);
            coverIV = view.findViewById(R.id.item_picture_cover);
        }

        public void setData(final int positon) {
            GlideApp.with(mContext)
                    .load(imageDataList.get(positon).url)
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

            ImageData imageData = imageDataList.get(pos);
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
            Intent intent = new Intent(mContext, AlbumPreviewActivity2.class);
            Bundle bundle = new Bundle();
            bundle.putString("ALBUMNAME", mAlbumName);
            bundle.putInt("POSITION", pos);
            bundle.putParcelable("IMAGEDATA", imageData);
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

    private void submitLocalPushUMData(int pos) {
//        Map<String, String> map = new HashMap<>();
//        map.put("type", "picture");
//        MobclickAgent.onEvent(mContext, CAST_LOCAL_RESOURCE, map);
        DecimalFormat df = new DecimalFormat("#0.0");
        String size = String.valueOf(df.format(Double.valueOf(imageDataList.get(pos).size) / 1024 / 1024));
        LogParams params = LogParams.newParams().append("applet_id", id)
                .append("applet_name", name)
                .append("file_size", size)
                .append("file_format", imageDataList.get(pos).url.substring(imageDataList.get(pos).url.lastIndexOf('.') + 1))
                .append("pos_id", String.valueOf(pos + 1));
        LogSubmit.event("local_file_clicked", params.getParams());
        Log.d(TAG, "submitLocalPushUMData: " + size);
    }

    public void setAlbumName(String albumName) {
        mAlbumName = albumName;
    }
}
