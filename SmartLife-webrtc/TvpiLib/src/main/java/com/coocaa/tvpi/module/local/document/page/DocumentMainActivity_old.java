package com.coocaa.tvpi.module.local.document.page;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.publib.base.BaseAppletActivity;
import com.coocaa.publib.data.local.DocumentData;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.publib.views.SDialog;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.data.account.CoocaaUserInfo;
import com.coocaa.smartscreen.utils.SpUtil;
import com.coocaa.tvpi.module.local.adapter.CustomSpinnerAdapter;
import com.coocaa.tvpi.module.local.adapter.DocumentMainAdapter;
import com.coocaa.tvpi.module.local.document.DocumentBrowser;
import com.coocaa.tvpi.module.local.document.DocumentConfig;
import com.coocaa.tvpi.module.local.document.DocLogSubmit;
import com.coocaa.tvpi.module.local.document.DocumentResManager;
import com.coocaa.tvpi.module.local.document.DocumentUtil;
import com.coocaa.tvpi.module.local.document.FormatEnum;
import com.coocaa.tvpi.module.local.document.fragment.DocumentSourceDialogFragment;
import com.coocaa.tvpi.module.log.LogParams;
import com.coocaa.tvpi.module.login.UserInfoCenter;
import com.coocaa.tvpi.util.FileCalculatorUtil;
import com.coocaa.tvpi.util.Utils;
import com.coocaa.tvpi.util.permission.PermissionListener;
import com.coocaa.tvpi.util.permission.PermissionsUtil;
import com.coocaa.tvpi.view.decoration.CommonVerticalItemDecoration;
import com.coocaa.tvpilib.R;
import com.scwang.smart.refresh.layout.api.RefreshHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.simple.SimpleMultiListener;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import swaiotos.channel.iot.ss.device.Device;

/**
 * @Description: 文档管理主页
 * @Author: wzh
 * @CreateDate: 12/1/20
 */
public class DocumentMainActivity_old extends BaseAppletActivity {
    private RefreshLayout mRefreshLayout;
    private NestedScrollView mScrollView;
    private ImageView mMainBg;
    private ImageView mHelpBlueBtn, mHelpBtn;
    private View mAddBtnLayout;
    private View mTopMenuLayout;
    private View mMenuLayout;
    private Spinner mTopSpinner, mSpinner;
    private View mTopMultiSelectBtn, mMultiSelectBtn;
    private CustomSpinnerAdapter mTopSpinnerAdapter, mSpinnerAdapter;
    private RecyclerView mRecyclerView;
    private DocumentMainAdapter mAdapter;
    private int mScrollY = 0;
    private boolean mPermissionGranted = false;
    private String mCurFormat = DocumentConfig.FORMAT_ALL_TEXT;//默认选择全部
    private List<DocumentData> mDocRecordList = new ArrayList<>();
    private DocumentSourceDialogFragment sourceSelectDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = DocumentMainActivity_old.class.getSimpleName();
        setContentView(R.layout.activity_doc_main);
        mRefreshLayout = findViewById(R.id.refresh_layout);
        mMainBg = findViewById(R.id.bg_main);
        initScrollView();
        initHeaderView();
        initRecycleView();
        sourceSelectDialog = new DocumentSourceDialogFragment(this);
        setCustomHeaderLeftView(createCustomHeaderLeftView());
        if (mHeaderHandler != null) {
            mHeaderHandler.setBackgroundColor(Color.TRANSPARENT);
            mHeaderHandler.setTitleAlpha(0);
            mHelpBtn.setAlpha(0f);
            mHelpBtn.setClickable(false);
            mHeaderHandler.setDarkMode(false);
        }
        mRefreshLayout.setOnMultiListener(new SimpleMultiListener() {
            @Override
            public void onHeaderMoving(RefreshHeader header, boolean isDragging, float percent, int offset, int headerHeight, int maxDragHeight) {
                float scale = 1;
                if (percent > 0) {
                    scale = (float) (1 + percent * 0.2);
                }
                mMainBg.setScaleX(scale);
                mMainBg.setScaleY(scale);
            }
        });
        getPermission();
    }

    private View createCustomHeaderLeftView() {
        RelativeLayout customHeaderLeftView = new RelativeLayout(this);
        mHelpBlueBtn = new ImageView(this);
        mHelpBlueBtn.setBackgroundResource(R.drawable.doc_icon_help_btn_blue);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(DimensUtils.dp2Px(this, 46), DimensUtils.dp2Px(this, 18));
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        customHeaderLeftView.addView(mHelpBlueBtn, params);
        mHelpBlueBtn.setOnClickListener(onHelpBtnClickListener);

        mHelpBtn = new ImageView(this);
        mHelpBtn.setBackgroundResource(R.drawable.doc_icon_help_btn);
        params = new RelativeLayout.LayoutParams(DimensUtils.dp2Px(this, 44), DimensUtils.dp2Px(this, 44));
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        customHeaderLeftView.addView(mHelpBtn, params);
        mHelpBtn.setOnClickListener(onHelpBtnClickListener);
        return customHeaderLeftView;
    }

    private void initScrollView() {
        mScrollView = findViewById(R.id.scrollview);
        mScrollView.setBackgroundColor(Color.WHITE);
        mScrollView.getBackground().setAlpha(0);
        int h = DimensUtils.dp2Px(DocumentMainActivity_old.this, 200);
        mScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                Log.i(TAG, "onScrollChange: " + scrollY + "--" + oldScrollY);
                try {
                    if (scrollY > oldScrollY) {
                        if (426 < scrollY) {
                            mTopMenuLayout.setVisibility(View.VISIBLE);
                            mMenuLayout.setVisibility(View.INVISIBLE);
                        }
                    } else {
                        if (scrollY < 401) {
                            mTopMenuLayout.setVisibility(View.GONE);
                            mMenuLayout.setVisibility(View.VISIBLE);
                        }
                    }
                    mScrollY = Math.min(scrollY, h);
                    float alpha = 1f * mScrollY / h;
                    mHeaderHandler.setTitleAlpha(alpha);
                    mHelpBtn.setAlpha(alpha);
                    if (alpha == 0) {
                        mHelpBlueBtn.setVisibility(View.VISIBLE);
                        mHelpBtn.setClickable(false);
                    } else {
                        mHelpBlueBtn.setVisibility(View.GONE);
                        mHelpBtn.setClickable(true);
                    }
                    mScrollView.getBackground().setAlpha((int) (255 * alpha));
                    float fraction = scrollY >= h ? 1f : scrollY / (float) h;
                    mHeaderHandler.setBackgroundColor(Utils.changeAlpha(getResources().getColor(R.color.color_white), fraction));
//                    String hexStr = Integer.toHexString((int) alpha);
//                    if (hexStr.length() < 2) {
//                        hexStr = "0" + hexStr;
//                    }
//                    Log.i(TAG, "onScrollChange: color: #" + hexStr + "FFFFFF");
//                    int titleBarBgColor = Color.parseColor("#" + hexStr + "FFFFFF");
//                    mHeaderHandler.setBackgroundColor(titleBarBgColor);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
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
                            Intent intent = new Intent(DocumentMainActivity_old.this, DocumentPlayerActivity.class);
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
                new SDialog(DocumentMainActivity_old.this, getString(R.string.confirm_delete_doc), R.string.cancel, R.string.delete_str, new SDialog.SDialog2Listener() {
                    @Override
                    public void onClick(boolean left, View view) {
                        if (!left) {
                            Log.i(TAG, "delete: " + positon + "---" + data.tittle);
                            deleteData(data);
                            mAdapter.removeAt(positon);
                            DocLogSubmit.submit(DocLogSubmit.EVENTID_DELETE_DOC);
                        }
                    }
                }).show();
            }
        });
    }

    private View initHeaderView() {
        View headerView = findViewById(R.id.header_layout);
        mAddBtnLayout = headerView.findViewById(R.id.btn_add_doc);
        mAddBtnLayout.setOnClickListener(onAddDocBtnClickListener);
        initTopMenuLayout();
        initMenuLayout(headerView);
        return headerView;
    }

    private void initTopMenuLayout() {
        mTopMenuLayout = findViewById(R.id.ll_top_menu_layout);
        mTopMenuLayout.setBackgroundColor(Color.WHITE);
        mTopSpinner = mTopMenuLayout.findViewById(R.id.spinner_all_type);
        mTopMultiSelectBtn = mTopMenuLayout.findViewById(R.id.multi_select_btn);
        mTopMultiSelectBtn.setOnClickListener(onMultiSelectBtnClickListener);
        mTopSpinnerAdapter = new CustomSpinnerAdapter(this, DocumentConfig.SPINNER_FORMATS);
        spinnerSetting(mTopSpinner, mTopSpinnerAdapter, true);
    }

    private void initMenuLayout(View headerView) {
        mMenuLayout = headerView.findViewById(R.id.ll_menu_layout);
        mSpinner = mMenuLayout.findViewById(R.id.spinner_all_type);
        mMultiSelectBtn = mMenuLayout.findViewById(R.id.multi_select_btn);
        mMultiSelectBtn.setOnClickListener(onMultiSelectBtnClickListener);
        mSpinnerAdapter = new CustomSpinnerAdapter(this, DocumentConfig.SPINNER_FORMATS);
        spinnerSetting(mSpinner, mSpinnerAdapter, false);
    }

    private View.OnClickListener onHelpBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            startActivity(new Intent(DocumentMainActivity_old.this, DocumentHelpListActivity.class));
        }
    };

    private View.OnClickListener onAddDocBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
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
    };

    private View.OnClickListener onMultiSelectBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(DocumentMainActivity_old.this, DocumentDeleteActivity.class);
            intent.putExtra("data", (Serializable) mAdapter.getData());
            startActivity(intent);
        }
    };

    private void spinnerSetting(Spinner spinner, CustomSpinnerAdapter adapter, boolean isTopSpinner) {
        spinner.setDropDownHorizontalOffset(-DimensUtils.dp2Px(this, 5));
        spinner.setDropDownVerticalOffset(DimensUtils.dp2Px(this, 35));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String select = (String) parent.getItemAtPosition(position);
                Log.i(TAG, "onItemSelected: " + select);
                if (mCurFormat.equals(select)) {
                    return;
                }
                mTopSpinnerAdapter.setSelect(position);
                mSpinnerAdapter.setSelect(position);
                if (isTopSpinner) {
                    mSpinner.setSelection(position);
                } else {
                    mTopSpinner.setSelection(position);
                }
//                adapter.setArrowIcon(R.drawable.doc_icon_more_arrow_down);
                mCurFormat = select;
                filterData(select);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
//                adapter.setArrowIcon(R.drawable.doc_icon_more_arrow_down);
            }
        });
//        spinner.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
//                    adapter.setArrowIcon(R.drawable.doc_icon_more_arrow_up);
//                }
//                return false;
//            }
//        });
        spinner.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPermissionGranted) {
            loadData();
        }
    }

    private void getPermission() {
        PermissionsUtil.getInstance().requestPermission(this, new PermissionListener() {
            @Override
            public void permissionGranted(String[] permission) {
                Log.i(TAG, "permissionGranted: ");
                mPermissionGranted = true;
                //预初始化Tbs内核
                if (!DocumentBrowser.isInited()) {
                    DocumentBrowser.init(getApplicationContext(), "DocumentMainActivity_old onCreate.");
                }
                loadData();
                updateDocumentHelpVideo();
            }

            @Override
            public void permissionDenied(String[] permission) {
                Log.i(TAG, "permissionDenied: ");
                mAdapter.setList(new ArrayList<>());
                mMenuLayout.setVisibility(View.GONE);
                mAdapter.setEmptyView(R.layout.layout_doc_main_empty);
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
            mMenuLayout.setVisibility(View.GONE);
            mAdapter.setEmptyView(R.layout.layout_doc_main_empty);
        }
    }

    private void filterData(String filterFormat) {
        if (mTopMenuLayout.getVisibility() != View.VISIBLE) {
            mMenuLayout.setVisibility(View.VISIBLE);
        }
        if (FormatEnum.contains(filterFormat)) {
            List<DocumentData> filterList = new ArrayList<>();
            for (DocumentData filterData : mDocRecordList) {
                if (filterData.format.equals(filterFormat)) {
                    filterList.add(filterData);
                }
            }
            mAdapter.setList(filterList);
            if (filterList.size() == 0) {
                mAdapter.setEmptyView(R.layout.layout_doc_common_empty);
                mMultiSelectBtn.setVisibility(View.GONE);
            } else {
                mMultiSelectBtn.setVisibility(View.VISIBLE);
            }
        } else {
            mAdapter.setList(mDocRecordList);
            mMultiSelectBtn.setVisibility(View.VISIBLE);
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
            mMenuLayout.setVisibility(View.GONE);
            mAdapter.setEmptyView(R.layout.layout_doc_main_empty);
        }
    }

    @Override
    protected boolean isFloatHeader() {
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sourceSelectDialog != null && sourceSelectDialog.isAdded()) {
            sourceSelectDialog.dismissDialog();
        }
    }

    private void submitLocalPushUMData(int pos, DocumentData documentData) {
        LogParams params = LogParams.newParams().append("file_type", documentData.suffix)
                .append("file_size", FileCalculatorUtil.getFileSize(documentData.size));
        DocLogSubmit.submit(DocLogSubmit.EVENTID_CLICK_DOC, params.getParams());
    }
}
