package com.coocaa.tvpi.module.local.media;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.coocaa.publib.base.BaseActivity;
import com.coocaa.publib.data.local.MediaData;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.publib.views.SDialog;
import com.coocaa.tvpi.event.LocalAlbumLoadEvent;
import com.coocaa.tvpi.module.base.VirtualInputable;
import com.coocaa.tvpi.module.local.adapter.PictureItemDecoration;
import com.coocaa.tvpi.module.local.utils.LocalMediaHelper;
import com.coocaa.tvpi.module.remote.RemoteVirtualInputManager;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpilib.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.airbnb.lottie.LottieDrawable.INFINITE;

/**
 * @ClassName LocalMediaActivity
 * @Description TODO (write something)
 * @User heni
 * @Date 4/6/21
 */
public class LocalMediaActivity extends BaseActivity implements LocalMediaAdapter.OnMediaItemCheckListener, VirtualInputable {
    private final String TAG = LocalMediaActivity.class.getSimpleName();

    public static final String TYPE_PICTURE = "picture";
    public static final String TYPE_VIDEO = "video";

    //标题栏
    private TextView title;
    private View toolbarDefault, toolbarDelete;
    private ImageView backImg;
    private ImageView selectMoreImg;
    private TextView cancelTv, selectAllTv;
    //内容布局
    private View emptyContentView;
    private LottieAnimationView emptyImgAnim;
    private TextView emptyTip;
    private TextView addBtn;
    private View contentView;
    private RecyclerView mRecyclerView;
    private LocalMediaAdapter mAdapter;
    private ImageView floatActionBtn;
    private View deleteView;
    private ImageView deleteImg;
    private TextView deleteTv;


    private String mType;
    private List<MediaData> collectedMediaDatas = new ArrayList<>();
    private List<MediaData> deleteMediaDatas = new ArrayList<>();
    private List<MediaData> shareMediaDatas = new ArrayList<>();
    private boolean isAllItemChecked;

    private final static String PATH_PICTURE = "com.coocaa.smart.mypicture";
    private final static String PATH_VIDEO = "com.coocaa.smart.myvideo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarHelper.translucent(this);
        StatusBarHelper.setStatusBarLightMode(this);
        EventBus.getDefault().register(this);

        if (getIntent() != null) {
            Uri uri = getIntent().getData();
            Log.d(TAG, "LocalMediaActivity onCreate, uri=" + uri);
            if(uri != null) {
                String id = uri.getAuthority();
                if(PATH_PICTURE.equals(id)) {
                    mType = TYPE_PICTURE;
                } else {
                    mType = TYPE_VIDEO;
                }
            } else {
                Log.d(TAG, "onCreate: processExtraData...");
                processExtraData();
            }
            Log.d(TAG, "LocalMediaActivity onCreate, parse mType=" + mType);
        }

        setContentView(R.layout.activity_local_media);
        initEmptyView();
        initContentView();
        initListener();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        Log.d(TAG, "onNewIntent: processExtraData...");
        if (getIntent() != null) {
            processExtraData();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
        if (!TextUtils.isEmpty(mType)) {
            if (mType.equals(TYPE_PICTURE)) {
                emptyImgAnim.setAnimation("local_picture_tips.json");
                emptyImgAnim.setRepeatCount(INFINITE);
            } else {
                emptyImgAnim.setAnimation("local_video_tips.json");
                emptyImgAnim.setRepeatCount(INFINITE);
            }
        }
    }

    private void processExtraData() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.getBoolean("fromShare")) {
            mType = bundle.getString("type");
            if (!TextUtils.isEmpty(mType) && mType.equals(TYPE_PICTURE)) {
                shareMediaDatas.clear();
                shareMediaDatas.addAll(bundle.getParcelableArrayList("IMAGEDATA"));
                for (MediaData data : shareMediaDatas) {
                    if (data != null) data.type = MediaData.TYPE.IMAGE;
                }
                ToastUtils.getInstance().showGlobalShort("图片添加成功");
            } else if (!TextUtils.isEmpty(mType) && mType.equals(TYPE_VIDEO)) {
                shareMediaDatas.clear();
                shareMediaDatas.addAll(bundle.getParcelableArrayList("VIDEODATA"));
                for (MediaData data : shareMediaDatas) {
                    if (data != null) data.type = MediaData.TYPE.VIDEO;
                }
                ToastUtils.getInstance().showGlobalShort("视频添加成功");
            }
            LocalMediaHelper.getInstance().collectMediaData(this, shareMediaDatas);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (emptyImgAnim != null) {
            emptyImgAnim.cancelAnimation();
            emptyImgAnim.clearAnimation();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LocalAlbumLoadEvent event) {
        if (!TextUtils.isEmpty(mType)) {
            if (mType.equals(LocalMediaActivity.TYPE_PICTURE)) {
                Log.d(TAG, "onEvent: getCollectedMediaData_Image");
                collectedMediaDatas =
                        LocalMediaHelper.getInstance().getCollectedMediaData_Image(this);
                mAdapter.setData(collectedMediaDatas);
            } else if (mType.equals(LocalMediaActivity.TYPE_VIDEO)) {
                Log.d(TAG, "onEvent: getCollectedMediaData_Video");
                collectedMediaDatas =
                        LocalMediaHelper.getInstance().getCollectedMediaData_Video(this);
                mAdapter.setData(collectedMediaDatas);
            }
        }
        if (collectedMediaDatas != null) {
            Log.d(TAG, "onEvent: " + collectedMediaDatas.size());
        }
        updateUI();

//        loadStateView.setViewLoadState(LocalResStatesView.STATE_LOAD_FINISH);
    }

    private void initEmptyView() {
        toolbarDefault = findViewById(R.id.toolbar_style_default);
        toolbarDelete = findViewById(R.id.toolbar_style_delete);
        title = findViewById(R.id.title);
        backImg = findViewById(R.id.back);
        selectMoreImg = findViewById(R.id.select_more);
        cancelTv = findViewById(R.id.cancel);
        selectAllTv = findViewById(R.id.select_all);
        emptyContentView = findViewById(R.id.empty_content);
        emptyTip = findViewById(R.id.empty_tv);
        addBtn = findViewById(R.id.add_btn);

        emptyImgAnim = findViewById(R.id.empty_iv);
        emptyImgAnim.setImageAssetsFolder("images/");

        if (!TextUtils.isEmpty(mType)) {
            if (mType.equals(TYPE_PICTURE)) {
                title.setText("图片");
                emptyTip.setText("将常用的图片添加到这里\n下次投屏时无需在相册中翻找");
                addBtn.setText("添加常用图片");
                emptyImgAnim.setAnimation("local_picture_tips.json");
                emptyImgAnim.setRepeatCount(INFINITE);
            } else {
                title.setText("视频");
                emptyTip.setText("将常用的视频添加到这里\n下次投屏时无需在相册中翻找");
                addBtn.setText("添加常用视频");
                emptyImgAnim.setAnimation("local_video_tips.json");
                emptyImgAnim.setRepeatCount(INFINITE);
            }
        }
    }

    private void initContentView() {
        contentView = findViewById(R.id.content_layout);
        floatActionBtn = findViewById(R.id.float_action_btn);
        mRecyclerView = findViewById(R.id.recyclerview);
        deleteView = findViewById(R.id.delete_layout);
        deleteImg = findViewById(R.id.delete_img);
        deleteTv = findViewById(R.id.delete_tv);

        LinearLayoutManager linearLayoutManager = new GridLayoutManager(this, 4);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        PictureItemDecoration pictureItemDecoration = new PictureItemDecoration(4,
                DimensUtils.dp2Px(this, 1f), DimensUtils.dp2Px(this, 1f));
        mRecyclerView.addItemDecoration(pictureItemDecoration);
        mAdapter = new LocalMediaAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnMediaItemCheckLis(this);

        isAllItemChecked = false;
        deleteView.setVisibility(View.GONE);
    }

    private void initListener() {
        backImg.setOnClickListener(v -> {
            finish();
        });

        addBtn.setOnClickListener(v -> {
            Intent intent = new Intent(LocalMediaActivity.this, LocalAddActivity.class);
            intent.putExtra("type", mType);
            startActivity(intent);
        });

        floatActionBtn.setOnClickListener(v -> {
            Intent intent = new Intent(LocalMediaActivity.this, LocalAddActivity.class);
            intent.putExtra("type", mType);
            startActivity(intent);
        });

        selectMoreImg.setOnClickListener(v -> {
            toolbarDefault.setVisibility(View.GONE);
            toolbarDelete.setVisibility(View.VISIBLE);
            floatActionBtn.setVisibility(View.GONE);
            mAdapter.showCheckbox();
            deleteView.setVisibility(View.VISIBLE);
            isAllItemChecked = false;
            selectAllTv.setText(("全选"));
            updateDeleteView(0);
            RemoteVirtualInputManager.INSTANCE.hideFloatViewToActivity(this);
        });

        cancelTv.setOnClickListener(v -> {
            toolbarDefault.setVisibility(View.VISIBLE);
            toolbarDelete.setVisibility(View.GONE);
            floatActionBtn.setVisibility(View.VISIBLE);
            mAdapter.hideCheckbox();
            deleteView.setVisibility(View.GONE);
            if (deleteMediaDatas != null) {
                deleteMediaDatas.clear();
            }

            if (collectedMediaDatas != null && !collectedMediaDatas.isEmpty()) {
                for (MediaData collectedMediaData : collectedMediaDatas) {
                    collectedMediaData.isCheck = false;
                }
            }
            RemoteVirtualInputManager.INSTANCE.showFloatViewToActivity(this);
        });

        selectAllTv.setOnClickListener(v -> {
            isAllItemChecked = !isAllItemChecked;
            if (isAllItemChecked) {
                selectAllTv.setText("取消全选");
                if (collectedMediaDatas != null && collectedMediaDatas.size() > 0) {
                    if (deleteMediaDatas != null) {
                        deleteMediaDatas.clear();
                        deleteMediaDatas.addAll(collectedMediaDatas);
                    }
                    updateDeleteView(collectedMediaDatas.size());
                }
            } else {
                if (deleteMediaDatas != null) {
                    deleteMediaDatas.clear();
                }
                selectAllTv.setText("全选");
                updateDeleteView(0);
            }
            mAdapter.updateAllItem(isAllItemChecked);
        });

        deleteView.setOnClickListener(v -> {
            showDeleteDialog();
        });
    }

    private void loadData() {
        LocalMediaHelper.getInstance().getLocalAlbumData(this);
    }

    private void updateUI() {
        toolbarDefault.setVisibility(View.VISIBLE);
        toolbarDelete.setVisibility(View.GONE);
        deleteView.setVisibility(View.GONE);
        if (collectedMediaDatas != null && collectedMediaDatas.size() > 0) {
            emptyContentView.setVisibility(View.GONE);
            contentView.setVisibility(View.VISIBLE);
            selectMoreImg.setVisibility(View.VISIBLE);
            floatActionBtn.setVisibility(View.VISIBLE);
            stopLottieAnim();
        } else {
            emptyContentView.setVisibility(View.VISIBLE);
            contentView.setVisibility(View.GONE);
            selectMoreImg.setVisibility(View.GONE);
            startLottieAnim();
        }
    }

    private void startLottieAnim() {
        emptyImgAnim.setVisibility(View.VISIBLE);
        emptyImgAnim.playAnimation();
    }

    private void stopLottieAnim() {
        emptyImgAnim.cancelAnimation();
    }

    private void updateDeleteView(int num) {
        if (num > 0) {
            deleteView.setEnabled(true);
            if (!TextUtils.isEmpty(mType) && mType.equals(TYPE_PICTURE)) {
                deleteTv.setText("删除图片(" + num + ")");
            } else if (!TextUtils.isEmpty(mType) && mType.equals(TYPE_VIDEO)) {
                deleteTv.setText("删除视频(" + num + ")");
            }
            deleteTv.setTextColor(Color.parseColor("#ffff326c"));
            deleteImg.setImageResource(R.drawable.local_media_delete_enable);
        } else {
            deleteView.setEnabled(false);
            if (!TextUtils.isEmpty(mType) && mType.equals(TYPE_PICTURE)) {
                deleteTv.setText("删除图片");
            } else if (!TextUtils.isEmpty(mType) && mType.equals(TYPE_VIDEO)) {
                deleteTv.setText("删除视频");
            }
            deleteTv.setTextColor(Color.parseColor("#66ff326c"));
            deleteImg.setImageResource(R.drawable.local_media_delete_unable);
        }
    }

    @Override
    public void onMediaItemCheck(int checkedNum, boolean isChecked, MediaData mediaData) {
        if (mediaData != null) {
            if (isChecked) {
                deleteMediaDatas.add(mediaData);
            } else {
                deleteMediaDatas.remove(mediaData);
            }
        }
        updateDeleteView(checkedNum);
    }

    @Override
    public void onMediaItemAllCheck(int checkedNum, boolean isChecked, List<MediaData> mediaDatas) {

    }

    private void showDeleteDialog() {
        if (!TextUtils.isEmpty(mType)) {
            String dTitle = "";
            String dSubtitle = "";
            if (mType.equalsIgnoreCase(TYPE_PICTURE)) {
                dTitle = "确定删除选中的图片";
                dSubtitle = "删除资料库图片，不影响本地的图片";
            } else {
                dTitle = "确定删除选中的视频";
                dSubtitle = "删除资料库视频，不影响本地的视频";
            }

            new SDialog(this, dTitle, dSubtitle, "取消", "确定",
                    new SDialog.SDialog2Listener() {
                        @Override
                        public void onClick(boolean left, View view) {
                            if (!left) {
                                deleteItems();
                            }
                        }
                    }).show();
        }
    }

    private void deleteItems() {
        Log.d(TAG, "deleteItems: ");
        ToastUtils.getInstance().showGlobalShort("删除成功！");
        updateDeleteView(0);
        LocalMediaHelper.getInstance().removeMediaData(this, deleteMediaDatas);
        loadData();
    }
}
