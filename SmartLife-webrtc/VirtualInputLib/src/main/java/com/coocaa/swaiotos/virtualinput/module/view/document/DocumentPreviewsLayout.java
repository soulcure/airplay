package com.coocaa.swaiotos.virtualinput.module.view.document;

import android.content.Context;
import android.util.Log;
import android.view.View;

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
 * 文档预览图列表
 */
public class DocumentPreviewsLayout {

    private String TAG = DocumentPreviewsLayout.class.getSimpleName();
    private CenterLayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    private DocumentPreviewsAdapter mAdapter;
    private List<String> mDatas = new ArrayList<>();
    private String mOwner;
    private int mCurPosition;
    private int mCurPageDirection;
    private View mCurFocusView;//当前选中的item view

    public DocumentPreviewsLayout(Context context, RecyclerView recyclerView) {
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

    public void setCurPageDirection(int direction) {
        mCurPageDirection = direction;
    }

    public void refreshData(List<String> datas, int currentPage) {
        if (datas != null && datas.size() > 0) {
            if (isNeedUpdate(datas)) {
                mCurPosition = currentPage;
                boolean needScroll = true;
                if (mDatas.size() > 0) {
                    needScroll = false;
                }
                mDatas.clear();
                mDatas.addAll(datas);
                refreshUI(needScroll);
            } else {
                if (mCurPosition != currentPage) {
                    Log.i(TAG, "refreshData: scrollTo :" + currentPage);
                    int lastPos = mCurPosition;
                    mCurPosition = currentPage;
                    delayScroll(100);
                }
            }
        } else {
            clearData();
        }
    }

    private void clearData() {
        mCurFocusView = null;
        mCurPosition = 0;
        mDatas.clear();
        if (mAdapter != null) {
            mAdapter.setCurPageDirection(mCurPageDirection);
            mAdapter.setCurPosition(mCurPosition);
            mAdapter.setData(mDatas);
        }
    }

    private boolean isNeedUpdate(List<String> datas) {
        if (mDatas.size() == 0) {
            return true;
        }
        for (String imgUrl : datas) {
            if (!mDatas.contains(imgUrl)) {
                return true;
            }
        }
        return false;
    }

    private void refreshUI(boolean needScroll) {
        if (mAdapter == null) {
            mAdapter = new DocumentPreviewsAdapter();
            mAdapter.setOnItemClickListener(new DocumentPreviewsAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    if (mCurFocusView != null) {
                        //此动作是解决选择相隔4个以上位置的item会出现两个焦点的问题
                        refreshItem(mCurFocusView, false);
                    }
                    mCurPosition = position;
                    delayScroll(0);
                    JSONObject paramJson = new JSONObject();
                    paramJson.put("pageIndex", position + 1);
                    String cmd = "jumpPage";
                    Log.i(TAG, "doc sendCmd: " + cmd + "--" + paramJson.toJSONString());
                    GlobalIOT.iot.sendCmd(cmd, "doc", paramJson.toJSONString(), TARGET_CLIENT_MEDIA_PLAYER, mOwner);
//                    refreshItemFocus();
                }
            });
            mRecyclerView.setAdapter(mAdapter);
        }
        mAdapter.setCurPageDirection(mCurPageDirection);
        mAdapter.setCurPosition(mCurPosition);
        mAdapter.setData(mDatas);
        if (needScroll) {
            delayScroll(60);
        }
    }

    private void refreshItemFocus() {
        try {
            if (mAdapter != null) {
                mAdapter.setCurPosition(mCurPosition);
            }
            if (mLayoutManager != null) {
                int count = mDatas.size();
                for (int i = 0; i < count; i++) {
                    View item = mLayoutManager.findViewByPosition(i);
                    if (item != null) {
                        boolean hasFocus = i == mCurPosition;
                        refreshItem(item, hasFocus);
                        if (hasFocus) {
                            mCurFocusView = item;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshItem(View item, boolean hasFocus) {
        View focus = item.findViewById(R.id.preview_focus_iv);
        View pageNum = item.findViewById(R.id.corner_pagenum);
        if (focus != null) {
            focus.setVisibility(hasFocus ? View.VISIBLE : View.INVISIBLE);
        }
        if (pageNum != null) {
            pageNum.setBackgroundResource(hasFocus ? R.drawable.doc_icon_corner_orange : R.drawable.doc_icon_corner_gray);
        }
    }

    private Runnable mScrollRunable = new Runnable() {
        @Override
        public void run() {
            try {
                mLayoutManager.smoothScrollToPosition(mRecyclerView, new RecyclerView.State(), mCurPosition);
            } catch (Exception e) {
                mRecyclerView.smoothScrollToPosition(mCurPosition);
            }
//            refreshItemFocus();
        }
    };

    private void delayScroll(long delay) {
        mRecyclerView.removeCallbacks(mScrollRunable);
        mRecyclerView.postDelayed(mScrollRunable, delay);
    }

    public void destroy() {
        mRecyclerView.removeCallbacks(mScrollRunable);
    }

}
