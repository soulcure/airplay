package com.coocaa.tvpi.module.local.media;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.coocaa.publib.base.BaseActivity;
import com.coocaa.publib.data.local.MediaData;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.tvpi.event.LocalAlbumLoadEvent;
import com.coocaa.tvpi.module.base.UnVirtualInputable;
import com.coocaa.tvpi.module.local.adapter.PictureItemDecoration;
import com.coocaa.tvpi.module.local.utils.LocalMediaHelper;
import com.coocaa.tvpi.module.local.view.LocalResStatesView;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpi.util.permission.PermissionListener;
import com.coocaa.tvpi.util.permission.PermissionsUtil;
import com.coocaa.tvpilib.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @ClassName LocalAddActivity
 * @Description TODO (write something)
 * @User heni
 * @Date 4/7/21
 */
public class LocalAddActivity extends BaseActivity implements UnVirtualInputable, LocalMediaAddAdapter.OnMediaItemCheckListener {
    
    private TextView title;
    private ImageView exitImg;
    private TextView addBtn;
    private RecyclerView mRecyclerView;
    private LocalMediaAddAdapter mAdapter;
    private LocalResStatesView localResStatesView;

    private String mType;
    private List<MediaData> checkedDataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_add);
        overridePendingTransition(R.anim.push_bottom_in, 0);
        StatusBarHelper.translucent(this);
        StatusBarHelper.setStatusBarLightMode(this);
        EventBus.getDefault().register(this);

        if (getIntent() != null) {
            mType = getIntent().getStringExtra("type");
            if (TextUtils.isEmpty(mType)) {
                mType = "picture";
            }
        }

        initView();
        initListener();
        checkPermission();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.push_bottom_out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LocalAlbumLoadEvent event) {
        //所有数据
        if (!TextUtils.isEmpty(mType)) {
            if (mType.equals(LocalMediaActivity.TYPE_PICTURE)) {
                List<? extends MediaData> mediaData = LocalMediaHelper.getInstance().mImageList;
                if (mediaData != null && !mediaData.isEmpty()) {
                    mAdapter.setData(mediaData);
                    localResStatesView.setViewLoadState(LocalResStatesView.STATE_LOAD_FINISH);
                } else {
                    localResStatesView.setViewLoadState(LocalResStatesView.STATE_NO_DATA,
                            "暂未搜索到相关图片");
                }
            } else if (mType.equals(LocalMediaActivity.TYPE_VIDEO)) {
                List<? extends MediaData> mediaData = LocalMediaHelper.getInstance().mVideoList;
                if (mediaData != null && !mediaData.isEmpty()) {
                    mAdapter.setData(mediaData);
                    localResStatesView.setViewLoadState(LocalResStatesView.STATE_LOAD_FINISH);
                } else {
                    localResStatesView.setViewLoadState(LocalResStatesView.STATE_NO_DATA,
                            "暂未搜索到相关视频");
                }
            }
        }
    }

    private void initView() {
        exitImg = findViewById(R.id.exit);
        title = findViewById(R.id.title);
        addBtn = findViewById(R.id.add_btn);
        mRecyclerView = findViewById(R.id.recyclerview);
        localResStatesView = findViewById(R.id.load_state_view);

        LinearLayoutManager linearLayoutManager = new GridLayoutManager(this, 3);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        PictureItemDecoration pictureItemDecoration = new PictureItemDecoration(3,
                DimensUtils.dp2Px(this, 1f), DimensUtils.dp2Px(this, 1f));
        mRecyclerView.addItemDecoration(pictureItemDecoration);
        mAdapter = new LocalMediaAddAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnMediaItemCheckLis(this);

        if (!TextUtils.isEmpty(mType)) {
            if (mType.equals(LocalMediaActivity.TYPE_PICTURE)) {
                title.setText("添加图片");
            } else {
                title.setText("添加视频");
            }
        }
    }

    private void initListener() {
        exitImg.setOnClickListener(v -> {
            finish();
        });

        addBtn.setOnClickListener(v -> {
            if (checkedDataList != null && !checkedDataList.isEmpty()) {
                for (MediaData media : checkedDataList) {
                    media.isCheck = false;
                }
                LocalMediaHelper.getInstance().collectMediaData(this, checkedDataList);
            }
            finish();
        });
    }

    private void checkPermission() {
        PermissionsUtil.getInstance().requestPermission(this, new PermissionListener() {
            @Override
            public void permissionGranted(String[] permission) {
                addBtn.setVisibility(View.VISIBLE);
                loadData();
            }

            @Override
            public void permissionDenied(String[] permission) {
                addBtn.setVisibility(View.GONE);
                localResStatesView.setViewLoadState(LocalResStatesView.STATE_NO_PERMISSION);
            }
        }, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    private void loadData() {
        LocalMediaHelper.getInstance().getLocalAlbumData(this);
        if (checkedDataList != null) {
            checkedDataList.clear();
        }
    }

    @Override
    public void onMediaItemCheck(int checkedNum, boolean isChecked, MediaData mediaData) {
        if (checkedDataList != null && checkedNum >= 0) {
            if (isChecked) {
                checkedDataList.add(mediaData);
            } else {
                checkedDataList.remove(mediaData);
            }
            if (checkedNum > 0) {
                addBtn.setText("添加(" + checkedNum + ")");
            } else if (checkedNum == 0) {
                addBtn.setText("添加");
            }
        }
    }
}
