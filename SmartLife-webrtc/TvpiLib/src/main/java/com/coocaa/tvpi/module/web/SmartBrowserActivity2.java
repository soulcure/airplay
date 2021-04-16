package com.coocaa.tvpi.module.web;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.tvpi.module.io.HomeIOThread;
import com.coocaa.tvpi.module.io.HomeUIThread;
import com.coocaa.tvpi.module.web.adapter.BrowserRecordAdapter;
import com.coocaa.tvpi.module.web.dialog.SmartBrowserBottomDeleteDialog;
import com.coocaa.tvpi.module.web.dialog.SmartBrowserDeleteDialog;
import com.coocaa.tvpi.util.ClipboardUtil;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpilib.R;
import com.tencent.smtt.sdk.WebIconDatabase;
import com.tencent.smtt.utils.Md5Utils;

import java.util.List;


/**
 * 拉起dongle端/TV端web页面
 *
 * @Author: yuzhan
 */
public class SmartBrowserActivity2 extends AppCompatActivity implements View.OnClickListener {

    private SmartBrowserBottomDeleteDialog bottomDeleteDialog;
    private String clipboardContent;
    private TextView tvAdd;
    private ImageView ivAdd;
    private ImageView ivBack;
    private ConstraintLayout clNone;
    private RecyclerView recyclerView;
    private BrowserRecordAdapter adapter;
    static String TAG = "SmartBrowser";
    public static String localClipboardHistory = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WebIconDatabase.getInstance().open(getDir("icons", MODE_PRIVATE).getPath());
        setContentView(R.layout.activity_smart_browser);
        StatusBarHelper.translucent(this);
        StatusBarHelper.setStatusBarLightMode(this);
        initView();

        HomeUIThread.execute(100, new Runnable() {
            @Override
            public void run() {
                parseClipboard();
            }
        });
    }

    private void initView() {
        tvAdd = findViewById(R.id.tv_none_add);
        ivAdd = findViewById(R.id.iv_add);
        ivBack = findViewById(R.id.iv_back);
        clNone = findViewById(R.id.cl_none);
        recyclerView = findViewById(R.id.recycler_view);
        adapter = new BrowserRecordAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        adapter.setHandleItemEventListener(new BrowserRecordAdapter.handleItemEventListener() {
            @Override
            public void onItemClick(int position, WebRecordBean data) {
                Intent intent = new Intent(SmartBrowserActivity2.this, SmartBrowserSearchActivity.class);
                intent.putExtra("webUrl", data.getWebUrl());
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(int position, WebRecordBean data) {
                Log.d(TAG, "onDeleteClick,position ====" + position);
                showDeleteDialog(data);
            }

            @Override
            public void onSelectClick(int position, WebRecordBean data) {

            }
        });

        tvAdd.setOnClickListener(this);
        ivAdd.setOnClickListener(this);
        ivBack.setOnClickListener(this);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        parseClipboard();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        HomeIOThread.execute(new Runnable() {
            @Override
            public void run() {
                final List<WebRecordBean> recordList = BrowserRecordUtils.getRecord(SmartBrowserActivity2.this);
                HomeUIThread.execute(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "onResume()      recordList========" + recordList);
                        adapter.setList(recordList);
                        if (recordList != null && recordList.size() > 0) {
                            clNone.setVisibility(View.GONE);
                            ivAdd.setVisibility(View.VISIBLE);
                        } else {
                            ivAdd.setVisibility(View.GONE);
                            clNone.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        });
    }

    private void parseClipboard() {
        String clipboard = getClipboardContent();
        if (!TextUtils.isEmpty(clipboard)) {
            clipboardContent = ClipboardUtil.conversionKeywordLoadOrSearch(getClipboardContent());
            Log.d(TAG, "clipboardContent=" + clipboardContent);
            if (validUrl(clipboardContent) && (TextUtils.isEmpty(localClipboardHistory) ||
                    (!TextUtils.isEmpty(localClipboardHistory)
                            && !localClipboardHistory.equalsIgnoreCase(clipboardContent)))) {
                SmartBrowserClipboardDialogActivity.start(this, clipboardContent);
                setShowDialogUrl(SmartBrowserActivity2.this, clipboardContent);
            }
        }
    }

    public static void setShowDialogUrl(Context context, String url) {
        localClipboardHistory = url;
        if(context != null && url != null) {
            saveSp(context, Md5Utils.getMD5(url), "1");
        }
    }

    public static boolean isDuplicateUrl(Context context, String url) {
        if(context == null || url == null)
            return true;
        boolean isDuplicateUrl = hasSp(context, Md5Utils.getMD5(url));
        Log.d(TAG, "isDuplicateUrl, ret=" + isDuplicateUrl + ", url=" + url);
        return isDuplicateUrl;
    }

    final static String sp_space = "web_sp";
    private static void saveSp(Context context, String key, String value) {
        SharedPreferences sp = context.getSharedPreferences(sp_space, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.apply();
        editor.commit();
    }

    private static String getSp(Context context, String key, String defValue) {
        SharedPreferences sp = context.getSharedPreferences(sp_space, Context.MODE_PRIVATE);
        return sp.getString(key, defValue);
    }

    private static boolean hasSp(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(sp_space, Context.MODE_PRIVATE);
        return sp.contains(key);
    }


    private void showDeleteDialog(WebRecordBean data) {
        SmartBrowserDeleteDialog deleteDialog = new SmartBrowserDeleteDialog(this, data);
        deleteDialog.setOnDeleteClickListener(new SmartBrowserDeleteDialog.onOptionClickListener() {
            @Override
            public void onCancelClick() {

            }

            @Override
            public void onDeleteClick(WebRecordBean data) {
                BrowserRecordUtils.removeWebRecord(SmartBrowserActivity2.this, data);
                List<WebRecordBean> recordList = BrowserRecordUtils.getRecord(SmartBrowserActivity2.this);
                adapter.setList(recordList);
                if (recordList == null || recordList.isEmpty()) {
                    refresh(); //清空后刷新UI
                }
            }
        });
        deleteDialog.show();
    }

    private void showBottomDeleteDialog() {
        if (bottomDeleteDialog == null) {
            bottomDeleteDialog = new SmartBrowserBottomDeleteDialog(this);
            bottomDeleteDialog.setOnDeleteClickListener(new SmartBrowserBottomDeleteDialog.onDeleteClickListener() {
                @Override
                public void onDeleteClick() {

                }
            });
        }
        bottomDeleteDialog.show();
    }

    private void hideBottomDeleteDialog() {
        if (bottomDeleteDialog != null) {
            bottomDeleteDialog.dismiss();
        }
    }

    private String getClipboardContent() {
        ClipboardManager cm = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            ClipData data = cm.getPrimaryClip();
            if (data != null && data.getItemCount() > 0) {
                ClipData.Item item = data.getItemAt(0);
                if (item != null) {
                    CharSequence sequence = item.coerceToText(this);
                    if (sequence != null) {
                        return sequence.toString();
                    }
                }
            }
        }
        return null;
    }

    private boolean validUrl(String content) {
        if (content == null)
            return false;
        String lowerContent = content.toLowerCase();
        return lowerContent.startsWith("http://") || lowerContent.startsWith("https://");
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_back) {
            finish();
        } else if (id == R.id.tv_none_add || id == R.id.iv_add) {
            startActivity(new Intent(this, SmartBrowserSearchActivity.class));
        }
    }

}
