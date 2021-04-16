package com.coocaa.tvpi.module.local.document.page;

import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.publib.base.BaseActivity;
import com.coocaa.publib.data.local.DocumentData;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.publib.views.SDialog;
import com.coocaa.smartscreen.utils.SpUtil;
import com.coocaa.tvpi.module.base.UnVirtualInputable;
import com.coocaa.tvpi.module.local.adapter.DocumentMultiSelectAdapter;
import com.coocaa.tvpi.module.local.document.DocLogSubmit;
import com.coocaa.tvpi.module.local.document.DocumentUtil;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpi.view.decoration.CommonVerticalItemDecoration;
import com.coocaa.tvpilib.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @Description: 文档清除页面
 * @Author: wzh
 * @CreateDate: 12/31/20
 */
public class DocumentDeleteActivity extends BaseActivity implements UnVirtualInputable {

    private RecyclerView mRecyclerView;
    private DocumentMultiSelectAdapter mAdapter;
    private View mClearBtnLayout, mClearBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = DocumentDeleteActivity.class.getSimpleName();
        setContentView(R.layout.activity_document_delete);
        StatusBarHelper.translucent(this);
        StatusBarHelper.setStatusBarLightMode(this);
        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        initClearButton();
        initRecycleView();
        List<DocumentData> dataList = (List<DocumentData>) getIntent().getSerializableExtra("data");
        if (dataList != null && dataList.size() > 0) {
            mAdapter.setList(dataList);
        }
    }

    private void initClearButton() {
        mClearBtnLayout = findViewById(R.id.clear_btn_layout);
        mClearBtn = findViewById(R.id.clear_btn);
        mClearBtn.setAlpha(0.5f);
        mClearBtnLayout.setClickable(false);
        mClearBtnLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAdapter.getSelectList().size() > 0) {
                    new SDialog(DocumentDeleteActivity.this, getString(R.string.confirm_delete_doc), R.string.cancel, R.string.delete_str, new SDialog.SDialog2Listener() {
                        @Override
                        public void onClick(boolean left, View view) {
                            if (!left) {
                                clearDoc();
                                DocLogSubmit.submit(DocLogSubmit.EVENTID_DELETE_DOC);
                                if (mAdapter.getData().size() == 0) {
                                    finish();
                                }
                            }
                        }
                    }).show();
                }
            }
        });
    }

    private void initRecycleView() {
        mRecyclerView = findViewById(R.id.activity_recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.addItemDecoration(new CommonVerticalItemDecoration(DimensUtils.dp2Px(this, 10), DimensUtils.dp2Px(this, 25f), DimensUtils.dp2Px(this, 35f)));
        mAdapter = new DocumentMultiSelectAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnSelectListener(new DocumentMultiSelectAdapter.OnSelectListener() {
            @Override
            public void onSelectChange(LinkedHashMap<String, DocumentMultiSelectAdapter.DocSelectData> selectDatas) {
                if (selectDatas.size() > 0) {
                    mClearBtnLayout.setClickable(true);
                    mClearBtn.setAlpha(1f);
                } else {
                    mClearBtnLayout.setClickable(false);
                    mClearBtn.setAlpha(0.5f);
                }
            }
        });
//        View view = new View(this);
//        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DimensUtils.dp2Px(this, 70)));
//        mAdapter.addFooterView(view);
    }

    private void clearDoc() {
        try {
            List<DocumentData> docRecordList = SpUtil.getList(this, DocumentUtil.SP_KEY_RECORD);
            List<DocumentData> remainList = new ArrayList<>();
            for (DocumentData data : docRecordList) {
                DocumentMultiSelectAdapter.DocSelectData selectData = mAdapter.getSelectList().get(data.url);
                if (selectData != null) {
                    mAdapter.remove(selectData.data);
                } else {
                    remainList.add(data);
                }
            }
            SpUtil.putList(this, DocumentUtil.SP_KEY_RECORD, remainList);
            if (remainList.size() == 0) {
                mAdapter.setList(remainList);
            }
            mAdapter.clearSelectList();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
