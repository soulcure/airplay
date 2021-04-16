package com.coocaa.swaiotos.virtualinput.module.view.document;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.cocaa.swaiotos.virtualinput.R;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.swaiotos.virtualinput.iot.GlobalIOT;
import com.coocaa.swaiotos.virtualinput.module.view.decoration.CommonHorizontalItemDecoration;

import java.util.ArrayList;
import java.util.List;

import static com.coocaa.smartscreen.connect.SSConnectManager.TARGET_CLIENT_MEDIA_PLAYER;

/**
 * @Description: Excel表格控制器表名列表布局
 * @Author: wzh
 * @CreateDate: 3/15/21
 */
public class ExcelSheetsLayout {

    private final static String TAG = "DocumentExcelLayout";
    private Context mContext;
    private CenterLayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    private ExcelSheetsAdapter mAdapter;
    private String mCurSheetName;
    private String mOwner;
    private List<String> mDatas = new ArrayList<>();

    public ExcelSheetsLayout(Context context, RecyclerView recyclerView) {
        mContext = context;
        mRecyclerView = recyclerView;
        mLayoutManager = new CenterLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        CommonHorizontalItemDecoration decoration = new CommonHorizontalItemDecoration(DimensUtils.dp2Px(context, 18), DimensUtils.dp2Px(context, 6));
        mRecyclerView.addItemDecoration(decoration);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
//                Log.i(TAG, "onScrollStateChanged: newState:" + newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    Log.i(TAG, "onScrollStateChanged: 停止了");
                    refreshItemFocus();
                }
            }
        });
    }

    public void setOwner(String owner) {
        mOwner = owner;
    }

    public void setVisibility(int visibility) {
        mRecyclerView.setVisibility(visibility);
    }

    public void refreshData(List<String> datas, String curSheetName) {
        try {
            boolean needScroll = true;
            if (!TextUtils.isEmpty(mCurSheetName) && mCurSheetName.equals(curSheetName)) {
                needScroll = false;
            }
            mCurSheetName = curSheetName;
            mDatas.clear();
            mDatas.addAll(datas);
            refreshUI(needScroll);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshUI(boolean needScroll) {
        if (mAdapter == null) {
            mAdapter = new ExcelSheetsAdapter();
            mAdapter.setOnItemClickListener(new ExcelSheetsAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(String sheetName) {
                    mCurSheetName = sheetName;
                    String cmd = "sheet";
                    JSONObject paramJson = new JSONObject();
                    paramJson.put("sheetName", sheetName);
                    Log.i(TAG, "doc sendCmd: " + cmd + "--" + paramJson.toJSONString());
                    GlobalIOT.iot.sendCmd(cmd, "doc", paramJson.toJSONString(), TARGET_CLIENT_MEDIA_PLAYER, mOwner);
                    refreshItemFocus();
                }
            });
            mRecyclerView.setAdapter(mAdapter);
        }
        mAdapter.setCurSheetName(mCurSheetName);
        mAdapter.setData(mDatas);
        mRecyclerView.removeCallbacks(mScrollRunable);
        if (needScroll) {
            mRecyclerView.postDelayed(mScrollRunable, 60);
        }
    }

    private void refreshItemFocus() {
        try {
            if (mAdapter != null) {
                mAdapter.setCurSheetName(mCurSheetName);
            }
            if (mLayoutManager != null) {
                int count = mDatas.size();
                for (int i = 0; i < count; i++) {
                    View item = mLayoutManager.findViewByPosition(i);
                    if (item != null) {
                        boolean hasFocus = i == getCurPosition();
                        View view = item.findViewById(R.id.focus_view);
                        TextView sheetName = item.findViewById(R.id.sheet_name);
                        sheetName.setTextColor(hasFocus ? Color.parseColor("#F86239") : mContext.getResources().getColor(R.color.color_white_60));
                        sheetName.setBackgroundColor(hasFocus ? Color.parseColor("#1af86239") : mContext.getResources().getColor(R.color.color_white_10));
                        view.setBackgroundResource(hasFocus ? R.drawable.bg_doc_preview_focus : 0);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Runnable mScrollRunable = new Runnable() {
        @Override
        public void run() {
            try {
                mLayoutManager.smoothScrollToPosition(mRecyclerView, new RecyclerView.State(), getCurPosition());
            } catch (Exception e) {
                mRecyclerView.smoothScrollToPosition(getCurPosition());
            }
//            refreshItemFocus();
        }
    };

    private int getCurPosition() {
        if (TextUtils.isEmpty(mCurSheetName)) {
            return 0;
        }
        for (int i = 0; i < mDatas.size(); i++) {
            if (mDatas.get(i).equals(mCurSheetName)) {
                return i;
            }
        }
        return 0;
    }

    public void destroy() {
        mRecyclerView.removeCallbacks(mScrollRunable);
    }

}
