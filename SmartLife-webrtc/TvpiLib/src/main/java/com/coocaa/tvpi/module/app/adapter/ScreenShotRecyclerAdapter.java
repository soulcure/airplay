package com.coocaa.tvpi.module.app.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.publib.base.GlideApp;
import com.coocaa.smartscreen.data.app.AppModel;
import com.coocaa.tvpi.module.app.fragment.ScreenshotFragment;
import com.coocaa.tvpilib.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName ScreenShotRecyclerAdapter
 * @Description
 */
public class ScreenShotRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = ScreenShotRecyclerAdapter.class.getSimpleName();
    private List<AppModel.AppScreenShots> dataList = new ArrayList<>();
    private Context context;

    private ArrayList<String> screenShotLists = null;

    public ScreenShotRecyclerAdapter(Context context) {
        this.context = context;
        screenShotLists = new ArrayList<>();
    }

    public void addAppScreenShotsData(List<AppModel.AppScreenShots> listScreenShots) {
        if(listScreenShots != null && listScreenShots.size() != 0){
            dataList.clear();
            screenShotLists.clear();
            Log.d(TAG, "addAppScreenShotsData: sshots.size = " + listScreenShots.size());
            for (AppModel.AppScreenShots shot: listScreenShots) {
                dataList.add(shot);
                screenShotLists.add(shot.shot);
            }
            notifyDataSetChanged();
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: ");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout
                .item_my_app_detail_screenshot, parent, false);
        return new DetailScreenShotHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        AppModel.AppScreenShots appScreenShots = dataList.get(position);
        if (null == appScreenShots) {
            return;
        }
        ((DetailScreenShotHolder) holder).onBind(appScreenShots, position);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public class DetailScreenShotHolder extends RecyclerView.ViewHolder {
        private Context mContext;
        private ImageView imAppShot;

        public DetailScreenShotHolder(final View itemView) {
            super(itemView);
            mContext = itemView.getContext();
            imAppShot = itemView.findViewById(R.id.my_app_detail_screenshot_item_img);
        }



        public void onBind(AppModel.AppScreenShots shots, final int position) {
            if (shots != null) {
                if (!TextUtils.isEmpty(shots.shot)) {
                    GlideApp.with(mContext).load(shots.shot).centerCrop().into(imAppShot);
                }

                imAppShot.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(screenShotLists != null && screenShotLists.size()>0) {
                            ScreenshotFragment screenshotFragment = new ScreenshotFragment();
                            Bundle bundle = new Bundle();
                            bundle.putStringArrayList("datalist", screenShotLists);
                            bundle.putInt("position", position);
                            screenshotFragment.setArguments(bundle);
                            screenshotFragment.show(((Activity)mContext).getFragmentManager(), ScreenshotFragment.DIALOG_FRAGMENT_TAG);
                        }
                    }
                });
            }
        }
    }


}
