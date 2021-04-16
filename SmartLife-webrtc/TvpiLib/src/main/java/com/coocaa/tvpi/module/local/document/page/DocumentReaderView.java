package com.coocaa.tvpi.module.local.document.page;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.coocaa.tvpi.module.local.document.DocumentUtil;
import com.tencent.smtt.sdk.TbsReaderView;

import java.io.File;

/**
 * @Description: 文件浏览View
 * @Author: wzh
 * @CreateDate: 2020/10/20
 */
public class DocumentReaderView extends FrameLayout {

    private final static String TAG = DocumentReaderView.class.getSimpleName();
    private TbsReaderView mTbsReaderView;
    private View mTbsContentView = null;
    private LinearLayout mTbsMenuParentLayout = null;//放映、最近文件 按钮的父布局
    private int mInitCount = 0;

    public DocumentReaderView(Context context) {
        this(context, null);
    }

    public DocumentReaderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DocumentReaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void createTbsReaderView() {
        if (mTbsReaderView != null && mTbsReaderView.getParent() != null) {
            removeView(mTbsReaderView);
            mTbsReaderView.onStop();
            mTbsReaderView = null;
        }
        mTbsReaderView = new TbsReaderView(getContext(), new TbsReaderView.ReaderCallback() {
            @Override
            public void onCallBackAction(Integer integer, Object o, Object o1) {
                hideMenuView();
            }
        });
        addView(mTbsReaderView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public void openFile(String url) {
        if (TextUtils.isEmpty(url)) {
            Log.e(TAG, "openFile: url is null!!!");
            return;
        }
        Log.i(TAG, "openFile: url:" + url);
        mInitCount = 0;
        createTbsReaderView();
        dispaly(url);
    }

    private void dispaly(String filePath) {
        try {
            String bsReaderTemp = DocumentUtil.READER_TEMP_PATH;
            File bsReaderTempFile = new File(bsReaderTemp);
            if (!bsReaderTempFile.exists()) {
                Log.d(TAG, "start create " + DocumentUtil.READER_TEMP_PATH + "!!!");
                boolean mkdir = bsReaderTempFile.mkdir();
                if (!mkdir) {
                    Log.e(TAG, "create " + DocumentUtil.READER_TEMP_PATH + " failed!!!");
                }
            }
            //加载文件
            Bundle localBundle = new Bundle();
            localBundle.putString("filePath", filePath);
            localBundle.putString("tempPath", Environment.getExternalStorageDirectory() + "/" + "TbsReaderTemp");
            boolean bool = this.mTbsReaderView.preOpen(DocumentUtil.getFileType(filePath), false);
            if (bool) {
                this.mTbsReaderView.openFile(localBundle);
                postDelayed(mInitReaderViewRunnable, 100);
            } else {
                Log.e(TAG, "not supported by:" + filePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Runnable mInitReaderViewRunnable = new Runnable() {
        @Override
        public void run() {
            if (mInitCount == 5) {
                return;
            }
            boolean isHide = hideMenuView();
            if (isHide) {
                return;
            } else {
                postDelayed(this, 60);
            }
            mInitCount++;
            Log.i(TAG, "InitReaderView count:" + mInitCount);
        }
    };

    private View getTbsContentView() {
        try {
            //获取真正显示文档内容的View
            if (mTbsContentView == null) {
                FrameLayout tbsParentView = ((FrameLayout) mTbsReaderView.getChildAt(0));
                if (tbsParentView != null) {
                    //查找FileReaderContentView
                    for (int i = 0; i < tbsParentView.getChildCount(); i++) {
                        ViewGroup view = (ViewGroup) tbsParentView.getChildAt(i);
                        if (view.getClass().getSimpleName().contains("FileReaderContentView")) {
                            mTbsContentView = view.getChildAt(0);
//                            TextView pageSize = (TextView) view.getChildAt(1);//页码
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
        return mTbsContentView;
    }

    private boolean hideMenuView() {
        try {
            //获取菜单View
            if (mTbsMenuParentLayout == null) {
                FrameLayout tbsParentView = ((FrameLayout) mTbsReaderView.getChildAt(0));
                if (tbsParentView != null) {
                    //查找菜单父布局MenuView
                    for (int i = 0; i < tbsParentView.getChildCount(); i++) {
                        ViewGroup view = (ViewGroup) tbsParentView.getChildAt(i);
                        if (view.getClass().getSimpleName().contains("MenuView")) {
                            RelativeLayout rl = (RelativeLayout) view.getChildAt(0);
                            mTbsMenuParentLayout = (LinearLayout) rl.getChildAt(0);
                            break;
                        }
                    }
                }
            }
            if (mTbsMenuParentLayout != null) {
                boolean isHide = false;
                //隐藏放映、最近文件按钮
                for (int i = 0; i < mTbsMenuParentLayout.getChildCount(); i++) {
                    ViewGroup menuButtonView = (ViewGroup) mTbsMenuParentLayout.getChildAt(i);
                    TextView menuButtonTv = (TextView) menuButtonView.getChildAt(0);
                    if (menuButtonTv != null) {
                        if (!TextUtils.isEmpty(menuButtonTv.getText())) {
                            Log.i(TAG, "hideMenuView: " + menuButtonTv.getText().toString());
                        }
                        menuButtonTv.setText("");
                        menuButtonTv.setBackground(null);
                        isHide = true;
                    }
                }
                return isHide;
            }
        } catch (Exception e) {
        }
        return false;
    }

//    public void scrollUp() {
//        int scrollY = (int) (getHeight() * 0.2);
//        Log.i(TAG, "scrollUp: getHeight():" + getHeight() + "----getHeight() * 0.2):" + scrollY);
//        //View tbsMenuView = (((FrameLayout) mTbsReaderView.getChildAt(0)).getChildAt(1));
//        //tbsMenuView.setVisibility(GONE);//菜单隐藏
//        try {
//            //获取真正显示文档内容的View
//            View view = getTbsContentView();
//            view.scrollBy(getScrollX(), -scrollY);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void scrollDown() {
//        int scrollY = (int) (getHeight() * 0.2);
//        Log.i(TAG, "scrollDown: getHeight():" + getHeight() + "----getHeight() * 0.2):" + scrollY);
//        //View tbsMenuView = (((FrameLayout) mTbsReaderView.getChildAt(0)).getChildAt(1));
//        //tbsMenuView.setVisibility(GONE);//菜单隐藏
//        try {
//            //获取真正显示文档内容的View
//            View view = getTbsContentView();
//            view.scrollBy(getScrollX(), scrollY);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public void destroy() {
        removeCallbacks(mInitReaderViewRunnable);
        removeAllViews();
        if (mTbsReaderView != null) {
            mTbsReaderView.onStop();
            mTbsReaderView = null;
        }
    }
}
