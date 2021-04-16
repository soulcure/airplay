package com.coocaa.tvpi.module.local.document.page;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.coocaa.publib.base.BaseActivity;
import com.coocaa.publib.data.local.DocumentData;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.publib.views.SDialog;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.data.account.CoocaaUserInfo;
import com.coocaa.smartscreen.utils.SpUtil;
import com.coocaa.tvpi.module.local.adapter.DocumentMainAdapter;
import com.coocaa.tvpi.module.local.document.DocLogSubmit;
import com.coocaa.tvpi.module.local.document.DocumentBrowser;
import com.coocaa.tvpi.module.local.document.DocumentConfig;
import com.coocaa.tvpi.module.local.document.DocumentResManager;
import com.coocaa.tvpi.module.local.document.DocumentUtil;
import com.coocaa.tvpi.module.local.document.FormatEnum;
import com.coocaa.tvpi.module.local.document.fragment.DocumentSourceDialogFragment;
import com.coocaa.tvpi.module.log.LogParams;
import com.coocaa.tvpi.module.login.UserInfoCenter;
import com.coocaa.tvpi.util.FileCalculatorUtil;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpi.util.permission.PermissionListener;
import com.coocaa.tvpi.util.permission.PermissionsUtil;
import com.coocaa.tvpi.view.CustomPagerTitleView;
import com.coocaa.tvpi.view.decoration.CommonVerticalItemDecoration;
import com.coocaa.tvpilib.R;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.buildins.UIUtil;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import swaiotos.channel.iot.ss.device.Device;

import static com.airbnb.lottie.LottieDrawable.INFINITE;

/**
 * @Description:
 * @Author: wzh
 * @CreateDate: 4/7/21
 */
public class DocumentMainActivity extends BaseActivity implements View.OnClickListener {

    private View mEditBtn, mAddCircleBtn;
    private View mMainEmptyView;
    private LottieAnimationView mEmptyLottieView;
    private MagicIndicator mMagicIndicator;
    private View mLine;
    private RecyclerView mRecyclerView;
    private DocumentMainAdapter mAdapter;
    private boolean mPermissionGranted = false;
    private String mCurFormat = DocumentConfig.FORMAT_ALL_TEXT;//默认选择全部
    private List<DocumentData> mDocRecordList = new ArrayList<>();
    private DocumentSourceDialogFragment sourceSelectDialog;
    private String mOtherAppSharePath;//第三方应用分享过来的文档路径

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i(TAG, "onNewIntent: ");
        mOtherAppSharePath = intent.getStringExtra(DocumentUtil.KEY_FILE_PATH);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = getClass().getSimpleName();
        setContentView(R.layout.activity_document_main);
        StatusBarHelper.translucent(this);
        StatusBarHelper.setStatusBarLightMode(this);
        sourceSelectDialog = new DocumentSourceDialogFragment(this);
        mOtherAppSharePath = getIntent().getStringExtra(DocumentUtil.KEY_FILE_PATH);
        mLine = findViewById(R.id.line);
        mEditBtn = findViewById(R.id.edit_btn);
        mEditBtn.setOnClickListener(this);
        setEditButtonClickable(false);
        mAddCircleBtn = findViewById(R.id.btn_add_doc);
        mAddCircleBtn.setOnClickListener(this);
        mAddCircleBtn.setVisibility(View.GONE);
        findViewById(R.id.back_btn).setOnClickListener(this);
        findViewById(R.id.help_btn).setOnClickListener(this);
        initMainEmptyView();
        initMagicIndicator();
        initRecycleView();
        getPermission();
    }

    private void initMagicIndicator() {
        mMagicIndicator = findViewById(R.id.doc_type_magic_indicator);
        CommonNavigator commonNavigator = new CommonNavigator(this);
        commonNavigator.setAdapter(new CommonNavigatorAdapter() {
            @Override
            public int getCount() {
                return DocumentConfig.SPINNER_FORMATS.size();
            }

            @Override
            public IPagerTitleView getTitleView(Context context, final int index) {
                final String format = DocumentConfig.SPINNER_FORMATS.get(index);
                CustomPagerTitleView pagerTitleView = new CustomPagerTitleView(context, UIUtil.dip2px(context, 15));
                pagerTitleView.setText(format);
                pagerTitleView.setTextSize(15);
                pagerTitleView.setSelectedBold(true);
                pagerTitleView.setTextColor(Color.BLACK);
                pagerTitleView.setNormalColor(getResources().getColor(R.color.black_50));
                pagerTitleView.setSelectedColor(Color.parseColor("#188CFF"));
                pagerTitleView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMagicIndicator.onPageSelected(index);
                        mMagicIndicator.onPageScrolled(index, 0, 0);
                        if (mCurFormat.equals(format)) {
                            return;
                        }
                        mCurFormat = format;
                        filterData(format);
                    }
                });
                return pagerTitleView;
            }

            @Override
            public IPagerIndicator getIndicator(Context context) {
                //下方指示器
                LinePagerIndicator linePagerIndicator = new LinePagerIndicator(context);
                linePagerIndicator.setMode(LinePagerIndicator.MODE_WRAP_CONTENT);
                linePagerIndicator.setColors(Color.parseColor("#188CFF"));
                linePagerIndicator.setLineHeight(UIUtil.dip2px(context, 2));
                linePagerIndicator.setLineWidth(UIUtil.dip2px(context, 18));
                linePagerIndicator.setRoundRadius(UIUtil.dip2px(context, 2));
                linePagerIndicator.setStartInterpolator(new AccelerateInterpolator());
                linePagerIndicator.setEndInterpolator(new DecelerateInterpolator(2.0f));
//                linePagerIndicator.setYOffset(UIUtil.dip2px(context, 4));
                return linePagerIndicator;
            }
        });
        mMagicIndicator.setNavigator(commonNavigator);
        mMagicIndicator.setVisibility(View.GONE);
        mLine.setVisibility(View.GONE);
    }

    private void initRecycleView() {
        mRecyclerView = findViewById(R.id.doc_recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.addItemDecoration(new CommonVerticalItemDecoration(DimensUtils.dp2Px(this, 10), DimensUtils.dp2Px(this, 25f), DimensUtils.dp2Px(this, 35f)));
        mAdapter = new DocumentMainAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnDocItemClickListener(new DocumentMainAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, DocumentData documentData) {
                try {
                    File file = new File(documentData.url);
                    if (file.exists()) {
                        if (file.length() > 0) {
                            Intent intent = new Intent(DocumentMainActivity.this, DocumentPlayerActivity.class);
                            intent.putExtra(DocumentUtil.KEY_FILE_PATH, documentData.url);
                            intent.putExtra(DocumentUtil.KEY_FILE_SIZE, String.valueOf(documentData.size));
                            intent.putExtra(DocumentUtil.KEY_SOURCE_PAGE, DocumentUtil.SOURCE_PAGE_DOC_MAIN);
                            startActivity(intent);

                            submitLocalPushUMData(position, documentData);
                        } else {
                            ToastUtils.getInstance().showGlobalLong("文件已损坏");
                        }
                    } else {
                        ToastUtils.getInstance().showGlobalLong("文件不存在");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onDeleteClick(int positon, DocumentData data) {
                new SDialog(DocumentMainActivity.this, getString(R.string.confirm_delete_doc), R.string.cancel, R.string.delete_str, new SDialog.SDialog2Listener() {
                    @Override
                    public void onClick(boolean left, View view) {
                        if (!left) {
                            Log.i(TAG, "delete: " + positon + "---" + data.tittle);
                            deleteData(data);
                            mAdapter.removeAt(positon);
                            if (mAdapter.getData().size() == 0) {
                                setEditButtonClickable(false);
                            }
                            DocLogSubmit.submit(DocLogSubmit.EVENTID_DELETE_DOC);
                        }
                    }
                }).show();
            }
        });
        View view = new View(this);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DimensUtils.dp2Px(this, 74)));
        mAdapter.addFooterView(view);
    }

    private void initMainEmptyView() {
        mMainEmptyView = LayoutInflater.from(this).inflate(R.layout.layout_doc_main_empty2, null);
        mMainEmptyView.findViewById(R.id.empty_btn_add_doc).setOnClickListener(this);
        mEmptyLottieView = mMainEmptyView.findViewById(R.id.icon_bg_lottieview);
        mEmptyLottieView.setAnimation("doc_main_empty.json");
        mEmptyLottieView.setRepeatCount(INFINITE);
    }

    private void getPermission() {
        PermissionsUtil.getInstance().requestPermission(this, new PermissionListener() {
            @Override
            public void permissionGranted(String[] permission) {
                Log.i(TAG, "permissionGranted: ");
                mPermissionGranted = true;
                //预初始化Tbs内核
                if (!DocumentBrowser.isInited()) {
                    DocumentBrowser.init(getApplicationContext(), "DocumentMainActivity onCreate.");
                }
                loadData();
                updateDocumentHelpVideo();
            }

            @Override
            public void permissionDenied(String[] permission) {
                Log.i(TAG, "permissionDenied: ");
                mAdapter.setList(new ArrayList<>());
                mLine.setVisibility(View.GONE);
                mMagicIndicator.setVisibility(View.GONE);
                mAdapter.setEmptyView(mMainEmptyView);
                mEmptyLottieView.playAnimation();
            }
        }, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private void updateDocumentHelpVideo() {
        String url = DocumentResManager.getInstance().getVideoPath(DocumentResManager.VIDEO_TYPE_WECHAT);
        if (TextUtils.isEmpty(url) || url.startsWith("http") || url.startsWith("https")) {
            DocumentResManager.getInstance().init(getApplicationContext());
        }
    }

    private void loadData() {
        if (!TextUtils.isEmpty(mOtherAppSharePath)) {
            saveDocRecord();
        }
        mDocRecordList.clear();
        List<DocumentData> docs = SpUtil.getList(this, DocumentUtil.SP_KEY_RECORD);
        if (docs != null && docs.size() > 0) {
            for (DocumentData data : docs) {
                File file = new File(data.url);
                if (file.exists()) {
                    mDocRecordList.add(data);
                } else {
                    Log.i(TAG, "loadData file is not exists: " + data.url);
                }
            }
            Log.d(TAG, "doc record size: " + mDocRecordList.size());
            if (mDocRecordList.size() > 0) {
                Collections.sort(mDocRecordList, new Comparator<DocumentData>() {
                    @Override
                    public int compare(DocumentData o1, DocumentData o2) {
                        return Long.compare(o2.lastModifiedTime, o1.lastModifiedTime);
                    }
                });
                filterData(mCurFormat);
            }
        } else {
            Log.d(TAG, "doc record is null.");
            mAdapter.setList(mDocRecordList);
            mAdapter.setEmptyView(mMainEmptyView);
            mEmptyLottieView.playAnimation();
            setEditButtonClickable(false);
            mLine.setVisibility(View.GONE);
            mMagicIndicator.setVisibility(View.GONE);
            mAddCircleBtn.setVisibility(View.GONE);
        }
        if (mDocRecordList.size() > 0 && !TextUtils.isEmpty(mOtherAppSharePath)) {
            ToastUtils.getInstance().showGlobalShort("添加成功");
            mOtherAppSharePath = "";
//            HomeUIThread.execute(100, new Runnable() {
//                @Override
//                public void run() {
//                    setFirstItemBg(Color.parseColor("#0D188CFF"));
//                    HomeUIThread.execute(3000, mResetFirstItemBgRunnable);
//                }
//            });
        }
    }

    private void filterData(String filterFormat) {
//        HomeUIThread.removeTask(mResetFirstItemBgRunnable);
        mEmptyLottieView.pauseAnimation();
        mMagicIndicator.setVisibility(View.VISIBLE);
        mAddCircleBtn.setVisibility(View.VISIBLE);
        mLine.setVisibility(View.VISIBLE);
        mAdapter.setEmptyView(R.layout.layout_doc_common_empty);
        if (FormatEnum.contains(filterFormat)) {
            List<DocumentData> filterList = new ArrayList<>();
            for (DocumentData filterData : mDocRecordList) {
                if (filterData.format.equals(filterFormat)) {
                    filterList.add(filterData);
                }
            }
            mAdapter.setList(filterList);
            if (filterList.size() == 0) {
                setEditButtonClickable(false);
            } else {
                setEditButtonClickable(true);
            }
        } else {
            mAdapter.setList(mDocRecordList);
            setEditButtonClickable(true);
        }
    }

    private void deleteData(DocumentData data) {
        if (mDocRecordList.size() > 0) {
            for (DocumentData d : mDocRecordList) {
                if (d.url.equals(data.url)) {
                    mDocRecordList.remove(d);
                    SpUtil.putList(this, DocumentUtil.SP_KEY_RECORD, mDocRecordList);
                    break;
                }
            }
        }
        if (mDocRecordList.size() == 0) {
            mLine.setVisibility(View.GONE);
            mMagicIndicator.setVisibility(View.GONE);
            mAddCircleBtn.setVisibility(View.GONE);
            mAdapter.setEmptyView(mMainEmptyView);
            mEmptyLottieView.playAnimation();
        }
    }

    private void setEditButtonClickable(boolean clickable) {
        mEditBtn.setAlpha(clickable ? 1f : 0.5f);
        mEditBtn.setClickable(clickable);
    }

    private void saveDocRecord() {
        if (TextUtils.isEmpty(mOtherAppSharePath)) {
            return;
        }
        List<DocumentData> docs = SpUtil.getList(this, DocumentUtil.SP_KEY_RECORD);
        if (docs == null) {
            docs = new ArrayList<>();
        }
        for (DocumentData d : docs) {
            if (d.url.equals(mOtherAppSharePath)) {
                docs.remove(d);
                break;
            }
        }
        File file = new File(mOtherAppSharePath);
        if (file.exists() && file.isFile()) {
            DocumentData data = createDocData(file);
            if (data != null) {
                docs.add(data);
                SpUtil.putList(this, DocumentUtil.SP_KEY_RECORD, docs);
            } else {
                mOtherAppSharePath = "";
            }
        }
    }

    private DocumentData createDocData(File file) {
        String filePath = file.getAbsolutePath();
        String suffix = DocumentUtil.getFileType(filePath);
        long size = file.length();
        if (size <= 0) {
            return null;
        }
        int pos = filePath.lastIndexOf(File.separator);
        if (pos == -1) return null;
        Log.i(TAG, "createDocData--> path:" + filePath);
        String displayName = filePath.substring(pos + 1);
        DocumentData data = new DocumentData();
        data.tittle = displayName;
        data.url = filePath;
        data.size = size;
        data.lastModifiedTime = System.currentTimeMillis();//此处赋值当前打开文档的时间
        data.suffix = suffix;
        data.format = FormatEnum.getFormat(suffix).type;
        return data;
    }

    private void setFirstItemBg(int color) {
        try {
            mRecyclerView.getChildAt(0).setBackgroundColor(color);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    private Runnable mResetFirstItemBgRunnable = new Runnable() {
//        @Override
//        public void run() {
//            setFirstItemBg(0);
//        }
//    };

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.back_btn) {
            finish();
        } else if (id == R.id.help_btn) {
            startActivity(new Intent(DocumentMainActivity.this, DocumentHelpListActivity.class));
        } else if (id == R.id.edit_btn) {
            Intent intent = new Intent(DocumentMainActivity.this, DocumentDeleteActivity.class);
            intent.putExtra("data", (Serializable) mAdapter.getData());
            startActivity(intent);
        } else if (id == R.id.btn_add_doc || id == R.id.empty_btn_add_doc) {
            if (!sourceSelectDialog.isAdded()) {
                sourceSelectDialog.show();
            }
            Device device = SSConnectManager.getInstance().getDevice();
            CoocaaUserInfo coocaaUserInfo = UserInfoCenter.getInstance().getCoocaaUserInfo();
            LogParams params = LogParams.newParams().append("ss_device_id", device == null ? "disconnected" : device.getLsid())
                    .append("ss_device_type", device == null ? "disconnected" : device.getZpRegisterType())
                    .append("account", coocaaUserInfo == null ? "not_login" : coocaaUserInfo.getOpen_id());
            DocLogSubmit.submit(DocLogSubmit.EVENTID_CLICK_ADD_DOC_BTN, params.getParams());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPermissionGranted) {
            loadData();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mEmptyLottieView.pauseAnimation();
        if (sourceSelectDialog != null && sourceSelectDialog.isAdded()) {
            sourceSelectDialog.dismissDialog();
        }
//        HomeUIThread.removeTask(mResetFirstItemBgRunnable);
    }

    private void submitLocalPushUMData(int pos, DocumentData documentData) {
        LogParams params = LogParams.newParams().append("file_type", documentData.suffix)
                .append("file_size", FileCalculatorUtil.getFileSize(documentData.size));
        DocLogSubmit.submit(DocLogSubmit.EVENTID_CLICK_DOC, params.getParams());
    }
}
