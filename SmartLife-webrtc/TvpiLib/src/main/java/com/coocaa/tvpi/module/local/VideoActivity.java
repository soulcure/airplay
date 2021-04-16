package com.coocaa.tvpi.module.local;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.publib.base.BaseAppletActivity;
import com.coocaa.publib.data.local.ImageData;
import com.coocaa.publib.data.local.MediaData;
import com.coocaa.publib.data.local.VideoData;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.tvpi.module.local.adapter.PictureItemDecoration;
import com.coocaa.tvpi.module.local.adapter.VideoAdapter;
import com.coocaa.tvpi.module.local.utils.LocalMediaHelp;
import com.coocaa.tvpi.module.local.utils.VideoBrowseAsyncTask;
import com.coocaa.tvpi.module.local.view.LocalResStatesView;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpi.util.permission.PermissionListener;
import com.coocaa.tvpi.util.permission.PermissionsUtil;
import com.coocaa.tvpi.view.CommonTitleBar;
import com.coocaa.tvpilib.R;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @ClassName VideoActivity
 * @Description TODO (write something)
 * @User WHY
 * @Date 2018/7/26
 * @Version TODO (write something)
 */
public class VideoActivity extends BaseAppletActivity {

    private static final String TAG = VideoActivity.class.getSimpleName();

    private VideoBrowseAsyncTask mVideoBrowseAsyncTask;
    private RecyclerView mRecyclerView;
    private VideoAdapter mAdapter;
    private CommonTitleBar titleBar;
    private LocalResStatesView localResStatesView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.local_activity_video);
        StatusBarHelper.translucent(this);
        StatusBarHelper.setStatusBarLightMode(this);
        initView();
        initTitle();
        checkPermission();
    }

    private void checkPermission() {
        PermissionsUtil.getInstance().requestPermission(this, new PermissionListener() {
            @Override
            public void permissionGranted(String[] permission) {
                initData();
            }

            @Override
            public void permissionDenied(String[] permission) {
                localResStatesView.setViewLoadState(LocalResStatesView.STATE_NO_PERMISSION);
            }
        }, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(TAG); // 统计页面
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(TAG); // 统计页面
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initData() {
//        mVideoBrowseAsyncTask = new VideoBrowseAsyncTask(this, new VideoBrowseAsyncTask.VideoBrowseCallback() {
//            @Override
//            public void onResult(List<VideoData> result) {
//                if (null != result && result.size() > 0) {
//                    mAdapter.addAll(result);
//                    localResStatesView.setViewLoadState(LocalResStatesView.STATE_LOAD_FINISH);
//                }else {
//                    localResStatesView.setViewLoadState(LocalResStatesView.STATE_NO_DATA,"暂未搜索到相关视频");
//                }
//            }
//        });
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//            mVideoBrowseAsyncTask.executeOnExecutor(Executors.newCachedThreadPool());
//        } else {
//            mVideoBrowseAsyncTask.execute();
//        }

        LocalMediaHelp.init(this, new LocalMediaHelp.Callback() {
            @Override
            public void onImageResult(HashMap<String, ArrayList<ImageData>> imageMap) {

            }

            @Override
            public void onVideoResult(List<VideoData> videoDataList) {
                if (null != videoDataList && videoDataList.size() > 0) {
                    mAdapter.addAll(videoDataList);
                    localResStatesView.setViewLoadState(LocalResStatesView.STATE_LOAD_FINISH);
                }else {
                    localResStatesView.setViewLoadState(LocalResStatesView.STATE_NO_DATA,"暂未搜索到相关视频");
                }
            }

            @Override
            public void onAllResult(List<MediaData> mediaDataList) {

            }
        });
    }

    private void initView() {
        if (mNPAppletInfo != null) {
            ViewGroup content = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.local_activity_video, null);
            mRecyclerView = content.findViewById(R.id.activity_video_recyclerview);
            titleBar = content.findViewById(R.id.titleBar);
            localResStatesView = content.findViewById(R.id.local_res_state_view);
            content.removeView(titleBar);
            setContentView(content);
        } else {
            mRecyclerView = findViewById(R.id.activity_video_recyclerview);
            titleBar = findViewById(R.id.titleBar);
            localResStatesView = findViewById(R.id.local_res_state_view);
        }
        LinearLayoutManager linearLayoutManager = new GridLayoutManager(this, 3);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        PictureItemDecoration pictureItemDecoration = new PictureItemDecoration(3, DimensUtils.dp2Px(this, 4f), DimensUtils.dp2Px(this, 4f));
        mRecyclerView.addItemDecoration(pictureItemDecoration);
        mAdapter = new VideoAdapter(this,mApplet == null ? "" :mApplet.getId(),mApplet == null ? "" :mApplet.getName());
        mRecyclerView.setAdapter(mAdapter);
    }

    private void initTitle() {
        if (mNPAppletInfo != null) {
            TextView textView = new TextView(this);
            textView.setText("相册");
            textView.setTextSize(16);
            textView.setTextColor(Color.BLACK);
            mHeaderHandler.setCustomHeaderLeftView(textView);
            mHeaderHandler.setBackButtonVisible(true);
            textView.setClickable(true);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(VideoActivity.this, AlbumActivity2.class));
                }
            });
        }
        if (mNPAppletInfo != null && mHeaderHandler != null) {
            mHeaderHandler.setTitle("相册投电视");
//            if (TextUtils.isEmpty(mApplet.getName())) {
//            }
            mHeaderHandler.setHeaderVisible(true);
        } else {
            titleBar.setOnClickListener(new CommonTitleBar.OnClickListener() {
                @Override
                public void onClick(CommonTitleBar.ClickPosition position) {
                    if (position == CommonTitleBar.ClickPosition.LEFT) {
                        finish();
                    }
                }
            });
        }
    }
}
