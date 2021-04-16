package com.coocaa.tvpi.module.local.document.page;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.coocaa.publib.base.BaseActivity;
import com.coocaa.publib.data.local.DocumentData;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartscreen.utils.SpUtil;
import com.coocaa.tvpi.module.local.adapter.CustomSpinnerAdapter;
import com.coocaa.tvpi.module.local.adapter.DocumentMultiSelectAdapter;
import com.coocaa.tvpi.module.local.document.DocumentBrowser;
import com.coocaa.tvpi.module.local.document.DocumentConfig;
import com.coocaa.tvpi.module.local.document.DocumentUtil;
import com.coocaa.tvpi.module.local.document.FormatEnum;
import com.coocaa.tvpi.module.local.utils.DocumentAsyncTask;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpi.util.permission.PermissionListener;
import com.coocaa.tvpi.util.permission.PermissionsUtil;
import com.coocaa.tvpi.view.decoration.CommonVerticalItemDecoration;
import com.coocaa.tvpilib.R;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.Executors;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import swaiotos.channel.iot.utils.ThreadManager;

/**
 * @Description: 文档列表页面（doc,docx,xls,xlsx,ppt,pptx,pdf等）
 * @Author: wzh
 * @CreateDate: 2020/10/21
 */
public class DocumentListActivity extends BaseActivity {

    private DocumentAsyncTask mDocumentAsyncTask;
    private View mLoadingView;
    private ViewGroup contentView;
    private ImageView mLoadingIconView;
    private TextView mSearchTv;
    private View mScanTipsLayout, mScanTipsCloseBtn;
    private ObjectAnimator mRotateAnim = null;
    private RecyclerView mRecyclerView;
    private DocumentMultiSelectAdapter mAdapter;
    private TextView mAddBtn;
    private View mDocHelpBtn;
    private View mEmptyLayout;
    private ImageView mEmptyIconV;
    private TextView mEmptyTipsV, mEmptySubTipsV, mEmptyButtonV;
    private Spinner mSpinnerAllSource, mSpinnerAllFormat;
    private CustomSpinnerAdapter mSpinnerSourceAdapter, mSpinnerFormatAdapter;
    private boolean isPermissionGranted = true;
    private String mCurSource = DocumentConfig.Source.ALL.text;
    private String mCurFormat = DocumentConfig.FORMAT_ALL_TEXT;
    private final static String BTN_TEXT_HELP = "查看文档帮助";
    private final static String BTN_TEXT_SCAN = "扫描文档";
    private final static String BTN_TEXT_PERMISSION = "去开启权限";

    private Runnable mShowLoadingRunable = new Runnable() {
        @Override
        public void run() {
            mRecyclerView.setVisibility(View.GONE);
            mLoadingView.setVisibility(View.VISIBLE);
            startRotateAnim();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = DocumentListActivity.class.getSimpleName();
        contentView = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.activity_document, null);
        setContentView(contentView);
        StatusBarHelper.translucent(this);
        StatusBarHelper.setStatusBarLightMode(this);
        mCurSource = getIntent().getStringExtra(DocumentUtil.KEY_SCAN_SOURCE);
        initView();
        getPermission();
    }

    private void getPermission() {
        PermissionsUtil.getInstance().requestPermission(this, new PermissionListener() {
            @Override
            public void permissionGranted(String[] permission) {
                isPermissionGranted = true;
                //预初始化Tbs内核
                if (!DocumentBrowser.isInited()) {
                    DocumentBrowser.init(getApplicationContext(), "DocumentActivity onCreate.");
                }
                if (isAndroidR()) {
                    DocumentConfig.Source source = DocumentConfig.getSourceByText(mCurSource);
                    if (DocumentConfig.Source.QQ.equals(source) || DocumentConfig.Source.WEIXIN.equals(source)) {
                        showEmptyView(R.drawable.doc_icon_empty, BTN_TEXT_SCAN, "由于手机系统限制，我们无法读取" + mCurSource + "文档", "解决方案\n把文档保存至手机的documents路径，然后点击扫描文档，即可找到。");
                        return;
                    }
                }
                initData();
            }

            @Override
            public void permissionDenied(String[] permission) {
                isPermissionGranted = false;
                showEmptyView(R.drawable.doc_icon_no_permission, BTN_TEXT_PERMISSION, "未获得权限，请设置“存储”权限为开启");
            }
        }, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private void initView() {
        mLoadingView = findViewById(R.id.loading_rl);
        mLoadingIconView = findViewById(R.id.loading_img);
        mDocHelpBtn = findViewById(R.id.btn_doc_help);
        mSearchTv = findViewById(R.id.search_tv);
        mScanTipsLayout = findViewById(R.id.scan_tips_layout);
        mScanTipsCloseBtn = findViewById(R.id.scan_tips_close_btn);
        mAddBtn = findViewById(R.id.btn_add_doc);
        mDocHelpBtn.setVisibility(View.GONE);
        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mDocHelpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoDocumentHelp();
            }
        });
        mScanTipsCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mScanTipsLayout.setVisibility(View.GONE);
            }
        });
        mAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAdapter.getSelectList().size() > 0) {
                    saveDocRecord();
                    finish();
//                    new SDialog(DocumentListActivity.this, getString(R.string.confirm_add_doc), R.string.cancel, R.string.add_str, new SDialog.SDialog2Listener() {
//                        @Override
//                        public void onClick(boolean left, View view) {
//                            if (!left) {
//                                saveDocRecord();
//                                finish();
//                            }
//                        }
//                    }).show();
                }
            }
        });
        mAddBtn.setBackgroundResource(R.drawable.bg_8fc7ff_round_12);
        mAddBtn.setClickable(false);
        initEmptyView();
        initSpinner();
        initRecycleView();
    }

    private void initEmptyView() {
        mEmptyLayout = findViewById(R.id.empty_layout);
        mEmptyIconV = mEmptyLayout.findViewById(R.id.iv_icon);
        mEmptyTipsV = mEmptyLayout.findViewById(R.id.tips);
        mEmptySubTipsV = mEmptyLayout.findViewById(R.id.sub_tips);
        mEmptyButtonV = mEmptyLayout.findViewById(R.id.button);
        mEmptyButtonV.setVisibility(View.VISIBLE);
        mEmptyButtonV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String btnText = mEmptyButtonV.getText().toString().trim();
                if (BTN_TEXT_HELP.equals(btnText)) {
                    gotoDocumentHelp();
                } else if (BTN_TEXT_SCAN.equals(btnText)) {
                    mCurSource = DocumentConfig.Source.ALL.text;
                    mCurFormat = DocumentConfig.FORMAT_ALL_TEXT;
                    mSpinnerAllSource.setSelection(0);
                    mSpinnerAllFormat.setSelection(0);
                    initData();
                } else if (BTN_TEXT_PERMISSION.equals(btnText)) {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void initRecycleView() {
        mRecyclerView = findViewById(R.id.activity_document_recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.addItemDecoration(new CommonVerticalItemDecoration(DimensUtils.dp2Px(this, 10), DimensUtils.dp2Px(this, 25f), DimensUtils.dp2Px(this, 35f)));
        mAdapter = new DocumentMultiSelectAdapter();
        mAdapter.setOnSelectListener(new DocumentMultiSelectAdapter.OnSelectListener() {
            @Override
            public void onSelectChange(LinkedHashMap<String, DocumentMultiSelectAdapter.DocSelectData> selectDatas) {
                if (selectDatas.size() > 0) {
                    mAddBtn.setClickable(true);
                    mAddBtn.setBackgroundResource(R.drawable.bg_188cff_round_12);
                } else {
                    mAddBtn.setClickable(false);
                    mAddBtn.setBackgroundResource(R.drawable.bg_8fc7ff_round_12);
                }
            }
        });
        mRecyclerView.setAdapter(mAdapter);
        View view = new View(this);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DimensUtils.dp2Px(this, 70)));
        mAdapter.addFooterView(view);
    }

    private void initSpinner() {
        mSpinnerAllSource = findViewById(R.id.spinner_all_source);
        mSpinnerSourceAdapter = new CustomSpinnerAdapter(this, DocumentConfig.SPINNER_SOURCES);
        spinnerSetting(mSpinnerAllSource, mSpinnerSourceAdapter, "source");
        int pos = DocumentConfig.SPINNER_SOURCES.indexOf(mCurSource);
        mSpinnerAllSource.setSelection(pos != -1 ? pos : 0);

        mSpinnerAllFormat = findViewById(R.id.spinner_all_type);
        mSpinnerFormatAdapter = new CustomSpinnerAdapter(this, DocumentConfig.SPINNER_FORMATS);
        spinnerSetting(mSpinnerAllFormat, mSpinnerFormatAdapter, "format");
    }

    private void spinnerSetting(Spinner spinner, CustomSpinnerAdapter adapter, String filter) {
        spinner.setDropDownHorizontalOffset(-DimensUtils.dp2Px(this, 5));
        spinner.setDropDownVerticalOffset(DimensUtils.dp2Px(this, 30));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String select = (String) parent.getItemAtPosition(position);
                Log.i(TAG, "onItemSelected: " + select);
                adapter.setSelect(position);
                if (filter.equals("source")) {
                    if (mCurSource.equals(select)) {
                        return;
                    }
                    mCurSource = select;
                } else {
                    if (mCurFormat.equals(select)) {
                        return;
                    }
                    mCurFormat = select;
                    if (isAndroidR()) {
                        DocumentConfig.Source source = DocumentConfig.getSourceByText(mCurSource);
                        if (DocumentConfig.Source.QQ.equals(source) || DocumentConfig.Source.WEIXIN.equals(source)) {
                            return;
                        }
                    }
                }
                if (!isPermissionGranted) {
                    ToastUtils.getInstance().showGlobalLong("请开启“存储”权限");
                    return;
                }
                filterData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinner.setAdapter(adapter);
    }

    private void saveDocRecord() {
        //保存记录动作
        List<DocumentData> docs = SpUtil.getList(this, DocumentUtil.SP_KEY_RECORD);
        if (docs == null) {
            docs = new ArrayList<>();
        }
        List<DocumentData> remainList = new ArrayList<>();
        LinkedHashMap<String, DocumentMultiSelectAdapter.DocSelectData> selectDataMap = mAdapter.getSelectList();
        for (DocumentData d : docs) {
            DocumentMultiSelectAdapter.DocSelectData selectData = selectDataMap.get(d.url);
            if (selectData == null) {
                //已存在但不是当前选中的
                remainList.add(d);
            }
        }
        for (DocumentMultiSelectAdapter.DocSelectData selectData : selectDataMap.values()) {
            File file = new File(selectData.filePath);
            if (file.exists() && file.isFile()) {
                DocumentData data = createDocData(file);
                if (data != null) {
                    //补上当前选中的
                    remainList.add(data);
                }
            }
        }
        SpUtil.putList(this, DocumentUtil.SP_KEY_RECORD, remainList);
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
        data.lastModifiedTime = System.currentTimeMillis();//此处赋值当前添加文档的时间
        data.suffix = suffix;
        data.format = FormatEnum.getFormat(suffix).type;
        return data;
    }

    private void filterData() {
        if (mDocumentAsyncTask != null) {
            List<DocumentData> dataList = mDocumentAsyncTask.filterFiles(mCurSource, mCurFormat);
            Log.d(TAG, "filterData --> source:" + mCurSource + "--format:" + mCurFormat + "--size: " + dataList.size());
            mAdapter.setList(dataList);
            if (dataList.size() > 0) {
                hideEmptyView();
                mAddBtn.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.VISIBLE);
            } else {
                mAddBtn.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.GONE);
                showEmptyView(R.drawable.doc_icon_empty, BTN_TEXT_HELP, "暂未扫描到相关文档");
            }
        } else {
            initData();
        }
    }

    private void showEmptyView(int icon, String btnText, String... tips) {
        mEmptyTipsV.setText(tips[0]);
        if (tips.length > 1) {
            mEmptySubTipsV.setText(tips[1]);
            mEmptySubTipsV.setVisibility(View.VISIBLE);
        } else {
            mEmptySubTipsV.setVisibility(View.GONE);
        }
        mEmptyIconV.setImageResource(icon);
        mEmptyButtonV.setText(btnText);
        mEmptyLayout.setVisibility(View.VISIBLE);
    }

    private void hideEmptyView() {
        mEmptyLayout.setVisibility(View.GONE);
    }

    @Override
    public void showLoading() {
        ThreadManager.getInstance().uiThread(mShowLoadingRunable, 1000);
    }

    @Override
    public void dismissLoading() {
        ThreadManager.getInstance().removeUiThread(mShowLoadingRunable);
        if (mRotateAnim != null) mRotateAnim.cancel();
        mLoadingView.setVisibility(View.GONE);
    }

    private void startRotateAnim() {
        if (mRotateAnim != null && mRotateAnim.isRunning()) return;
        mRotateAnim = ObjectAnimator.ofFloat(mLoadingIconView, "rotation", 0f, 360f);
        mRotateAnim.setRepeatCount(ValueAnimator.INFINITE);
        mRotateAnim.setDuration(2000);
        mRotateAnim.setInterpolator(null);
        mRotateAnim.start();
    }

    private void initData() {
        showLoading();
        mDocumentAsyncTask = new DocumentAsyncTask(this, mCurSource, new DocumentAsyncTask.DocumentBrowseCallback() {
            @Override
            public void onResult(final List<DocumentData> result) {
                dismissLoading();
                if (null != result && result.size() > 0) {
                    Log.d(TAG, "onResult size: " + result.size());
                    hideEmptyView();
                    mAddBtn.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.VISIBLE);
                    mAdapter.setList(result);
                } else {
                    Log.d(TAG, "onResult null.");
                    mAddBtn.setVisibility(View.GONE);
                    mRecyclerView.setVisibility(View.GONE);
                    showEmptyView(R.drawable.doc_icon_empty, BTN_TEXT_HELP, "暂未扫描到相关文档");
                }
            }

            @Override
            public void onProgress(List<DocumentData> datas) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideEmptyView();
                        mAddBtn.setVisibility(View.VISIBLE);
                        mRecyclerView.setVisibility(View.VISIBLE);
                        mAdapter.setList(datas);
                    }
                });
            }

            @Override
            public void onPathProgress(String currentScanPath) {
                Log.i(TAG, "onPathProgress: " + currentScanPath);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSearchTv.setText("正在搜索文档：" + currentScanPath);
                    }
                });
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mDocumentAsyncTask.executeOnExecutor(Executors.newCachedThreadPool());
        } else {
            mDocumentAsyncTask.execute();
        }
    }

    private void gotoDocumentHelp() {
        startActivity(new Intent(this, DocumentAddHelpActivity.class));
    }

    private boolean isAndroidR() {
        return DocumentUtil.isAndroidR();
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
        dismissLoading();
        if (mDocumentAsyncTask != null && !mDocumentAsyncTask.isCancelled()) {
            mDocumentAsyncTask.cancel(true);
        }
    }
}
