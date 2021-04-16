package com.coocaa.tvpi.module.local;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
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
import com.coocaa.tvpi.module.local.adapter.PictureAndVideoAdapter;
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


public class PictureAndVideoActivity extends BaseAppletActivity {

    public static final String KEY_ALBUM_NAME = "KEY_ALBUM_NAME";
    public static final String KEY_SHOW_TYPE = "KEY_SHOW_TYPE";
    public static final String SHOW_IMAGE = "SHOW_IMAGE";
    public static final String SHOW_VIDEO = "SHOW_VIDEO";
    public static final String SHOW_ALL = "SHOW_ALL";

    private CommonTitleBar titleBar;
    private RecyclerView mRecyclerView;
    private PictureAndVideoAdapter mAdapter;
    private LocalResStatesView localResLoadStatesView;
    private String mAlbumName = MAIN_ALBUM_NAME;
    private final String MAIN_SUB_NAME = "最近项目";
    private String mShowType = SHOW_ALL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.local_activity_picture);
        StatusBarHelper.translucent(this);
        StatusBarHelper.setStatusBarLightMode(this);
        parseIntent();
        initView();
        initTitle();
        checkPermission();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        parseIntent();
        initView();
        checkPermission();
        setTitle("相册投电视", TextUtils.equals(mAlbumName, MAIN_ALBUM_NAME) ? MAIN_SUB_NAME : mAlbumName);
    }

    private void parseIntent() {
        if (null != getIntent()) {
            String albumName = getIntent().getStringExtra(KEY_ALBUM_NAME);
            if (!TextUtils.isEmpty(albumName)) {
                mAlbumName = albumName;
            }
            String showType = getIntent().getStringExtra(KEY_SHOW_TYPE);
            if (!TextUtils.isEmpty(showType)) {
                mShowType = showType;
            }
        }
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
        MobclickAgent.onPageStart(TAG);
        // 统计页面
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(TAG);
        // 统计页面
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initData() {
        LocalMediaHelp.init(this, new LocalMediaHelp.Callback() {

            @Override
            public void onImageResult(HashMap<String, ArrayList<ImageData>> imageMap) {
                if (imageMap.get(mAlbumName) != null && SHOW_IMAGE.equals(mShowType)) {
                    mAdapter.setImageData(imageMap.get(mAlbumName));
                    localResLoadStatesView.setViewLoadState(LocalResStatesView.STATE_LOAD_FINISH);
                }
                if (mAdapter.getMediaDataList() == null || mAdapter.getMediaDataList().size() == 0) {
                    initLoadError();
                }
            }

            @Override
            public void onVideoResult(List<VideoData> videoDataList) {
                if (videoDataList != null && SHOW_VIDEO.equals(mShowType)) {
                    mAdapter.setVideoData(videoDataList);
                    localResLoadStatesView.setViewLoadState(LocalResStatesView.STATE_LOAD_FINISH);
                }
                if (mAdapter.getMediaDataList() == null || mAdapter.getMediaDataList().size() == 0) {
                    initLoadError();
                }
            }

            @Override
            public void onAllResult(List<MediaData> mediaDataList) {
                if (mediaDataList != null && SHOW_ALL.equals(mShowType)) {
                    mAdapter.setMediaDataList(mediaDataList);
                    localResLoadStatesView.setViewLoadState(LocalResStatesView.STATE_LOAD_FINISH);
                }
                if (mAdapter.getMediaDataList() == null || mAdapter.getMediaDataList().size() == 0) {
                    initLoadError();
                }
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
        mAdapter = new PictureAndVideoAdapter(this, mApplet == null ? "" : mApplet.getId(), mApplet == null ? "" : mApplet.getName(), mShowType);
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
            textView.setOnClickListener(v -> startActivity(new Intent(PictureAndVideoActivity.this, AlbumActivity2.class)));
        }
    }

    private void initLoadError() {
        if (SHOW_VIDEO.equals(mShowType)) {
            localResLoadStatesView.setViewLoadState(LocalResStatesView.STATE_NO_DATA, "暂未搜索到相关视频");
        }
        if (SHOW_IMAGE.equals(mShowType)) {
            localResLoadStatesView.setViewLoadState(LocalResStatesView.STATE_NO_DATA, "暂未搜索到相关图片");
        }
        if (SHOW_ALL.equals(mShowType)) {
            localResLoadStatesView.setViewLoadState(LocalResStatesView.STATE_NO_DATA, "暂未搜索到相关内容");
        }
    }

    private void initTitle() {
        if (mNPAppletInfo == null) {
            titleBar.setOnClickListener((CommonTitleBar.OnClickListener) position -> {
                if (position == CommonTitleBar.ClickPosition.LEFT) {
                    finish();
                } else {
                    finish();
                    startActivity(new Intent(PictureAndVideoActivity.this, AlbumActivity2.class));
                }
            });
            titleBar.setText(CommonTitleBar.TextPosition.TITLE, mAlbumName);
        }
        setTitle("相册投电视", TextUtils.equals(mAlbumName, MAIN_ALBUM_NAME) ? MAIN_SUB_NAME : mAlbumName);
    }
}
