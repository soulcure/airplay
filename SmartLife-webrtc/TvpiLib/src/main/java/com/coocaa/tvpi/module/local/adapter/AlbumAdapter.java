package com.coocaa.tvpi.module.local.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.coocaa.publib.base.GlideApp;
import com.coocaa.publib.data.local.ImageData;
import com.coocaa.tvpi.module.local.PictureActivity;
import com.coocaa.tvpilib.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

/**
 * @ClassName AlbumAdapter
 * @Description TODO (write something)
 * @User WHY
 * @Date 2018/7/24
 * @Version TODO (write something)
 */
public class AlbumAdapter extends RecyclerView.Adapter {

    private static final String TAG = AlbumAdapter.class.getSimpleName();

    private Context mContext;
    private List<String> groupList = new ArrayList<String>();
    private HashMap<String,ArrayList<ImageData>> allImageMap = new HashMap<String,ArrayList<ImageData>>();

    public AlbumAdapter(Context context, List<String> groupList, HashMap data){
        mContext  = context;
        this.groupList = groupList;
        allImageMap = data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.local_item_album, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((ViewHolder) holder).setData(position);
    }

    @Override
    public int getItemCount() {
        return groupList == null ? 0 : groupList.size();
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTV;
        public TextView numTV;
        public ImageView coverIV;
        public View itemView;

        public ViewHolder(View view) {
            super(view);
            itemView = view;
            nameTV = view.findViewById(R.id.item_album_name);
            numTV = view.findViewById(R.id.item_album_num);
            coverIV = view.findViewById(R.id.item_album_cover);
        }

        public void setData(int positon) {
            try {
                final String nameStr = groupList.get(positon);
                nameTV.setText(nameStr);
                ArrayList<ImageData> imageDataList = allImageMap.get(nameStr);
                numTV.setText(imageDataList.size() + "");
                GlideApp.with(mContext)
                        .load(imageDataList.get(0).url)
                        .centerCrop()
                        .into(coverIV);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mContext.startActivity(new Intent(mContext, PictureActivity.class).putExtra(PictureActivity.KEY_ALBUM_NAME, nameStr));
                        ((Activity) mContext).finish();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
