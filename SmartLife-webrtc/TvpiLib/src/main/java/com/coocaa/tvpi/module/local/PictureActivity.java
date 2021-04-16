package com.coocaa.tvpi.module.local;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.coocaa.tvpi.module.local.adapter.PictureAdapter;
import com.coocaa.tvpi.module.local.adapter.PictureItemDecoration;
import com.coocaa.tvpi.module.local.utils.LocalMediaHelp;
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

import static com.coocaa.tvpi.module.local.utils.MediaStoreHelper.MAIN_ALBUM_NAME;

/**
 * @ClassName DLNAAlbumActivity
 * @Description TODO (write something)
 * @User WHY
 * @Date 2018/7/24
 * @Version TODO (write something)
 */
public class PictureActivity extends BaseAppletActivity {

    public static final String KEY_ALBUM_NAME = "KEY_ALBUM_NAME";

    private CommonTitleBar titleBar;
    private RecyclerView mRecyclerView;
    private PictureAdapter mAdapter;
    private LocalResStatesView localResLoadStatesView;

    List<String> mImageCovers;
    HashMap<String, ArrayList<ImageData>> mImageMap;

    private String mAlbumName = MAIN_ALBUM_NAME;
    private String MAIN_SUB_NAME = "最近项目";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.local_activity_picture);
        StatusBarHelper.translucent(this);
        StatusBarHelper.setStatusBarLightMode(this);
        if (null != getIntent()) {
            String albumName = getIntent().getStringExtra(KEY_ALBUM_NAME);
            if (!TextUtils.isEmpty(albumName)) {
                mAlbumName = albumName;
            }
        }
//        setRightButton("相册");
        initView();
        initTitle();
        checkPermission();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (null != getIntent()) {
            String albumName = getIntent().getStringExtra(KEY_ALBUM_NAME);
            if (!TextUtils.isEmpty(albumName)) {
                mAlbumName = albumName;
            }
        }
//        setRightButton("相册");
        initView();
        checkPermission();
        setTitle("相册投电视", TextUtils.equals(mAlbumName, MAIN_ALBUM_NAME) ? MAIN_SUB_NAME : mAlbumName);
    }

    private void checkPermission() {
        PermissionsUtil.getInstance().requestPermission(this, new PermissionListener() {
            @Override
            public void permissionGranted(String[] permission) {
                initData();
            }

            @Override
            public void permissionDenied(String[] permission) {
                localResLoadStatesView.setViewLoadState(LocalResStatesView.STATE_NO_PERMISSION);
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
//        Log.d(TAG, "initData: start");
//        MediaStoreHelper.init(this);
//        mImageCovers = MediaStoreHelper.getImageGroup();
//        mImageMap = MediaStoreHelper.getImageCacheMap();
//        Log.d(TAG, "initData: end");
//        ArrayList<ImageData> imageData = mImageMap.get(mAlbumName);
        LocalMediaHelp.init(this, new LocalMediaHelp.Callback() {
            @Override
            public void onImageResult(HashMap<String, ArrayList<ImageData>> imageMap) {
                ArrayList<ImageData> imageData = imageMap.get(mAlbumName);
                if (imageData == null || imageData.isEmpty()) {
                    localResLoadStatesView.setViewLoadState(LocalResStatesView.STATE_NO_DATA,"暂未搜索到相关图片");
                } else {
                    mAdapter.setData(imageData);
                    localResLoadStatesView.setViewLoadState(LocalResStatesView.STATE_LOAD_FINISH);
                }
            }

            @Override
            public void onVideoResult(List<VideoData> videoDataList) {

            }

            @Override
            public void onAllResult(List<MediaData> mediaDataList) {

            }
        });

    }

    private void initView() {
        if (mNPAppletInfo != null) {
            ViewGroup content = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.local_activity_picture, null);
            titleBar = content.findViewById(R.id.titleBar);
            mRecyclerView = content.findViewById(R.id.activity_picture_recyclerview);
            localResLoadStatesView = content.findViewById(R.id.local_res_state_view);
            content.removeView(titleBar);
            setContentView(content);
        } else {
            titleBar = findViewById(R.id.titleBar);
            mRecyclerView = findViewById(R.id.activity_picture_recyclerview);
            localResLoadStatesView = findViewById(R.id.local_res_state_view);
        }

        LinearLayoutManager linearLayoutManager = new GridLayoutManager(this, 3);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        PictureItemDecoration pictureItemDecoration = new PictureItemDecoration(3, DimensUtils.dp2Px(this, 4f), DimensUtils.dp2Px(this, 4f));
        mRecyclerView.addItemDecoration(pictureItemDecoration);
        mAdapter = new PictureAdapter(this,mApplet == null ? "" :mApplet.getId(),mApplet == null ? "" :mApplet.getName());
        mAdapter.setAlbumName(mAlbumName);
        mRecyclerView.setAdapter(mAdapter);

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
                    startActivity(new Intent(PictureActivity.this, AlbumActivity2.class));
                }
            });
        }
    }

    private void initTitle() {
        if (mNPAppletInfo == null) {
            titleBar.setOnClickListener(new CommonTitleBar.OnClickListener() {
                @Override
                public void onClick(CommonTitleBar.ClickPosition position) {
                    if (position == CommonTitleBar.ClickPosition.LEFT) {
                        finish();
                    } else {
                        finish();
                        startActivity(new Intent(PictureActivity.this, AlbumActivity2.class));
                    }
                }
            });
            titleBar.setText(CommonTitleBar.TextPosition.TITLE, mAlbumName);
        }
        setTitle("相册投电视", TextUtils.equals(mAlbumName, MAIN_ALBUM_NAME) ? MAIN_SUB_NAME : mAlbumName);
    }
}
