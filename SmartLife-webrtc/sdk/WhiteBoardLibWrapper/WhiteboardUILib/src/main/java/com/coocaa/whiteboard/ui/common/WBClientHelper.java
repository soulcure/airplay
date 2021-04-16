package com.coocaa.whiteboard.ui.common;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.coocaa.define.SvgConfig;
import com.coocaa.define.SvgPaintDef;
import com.coocaa.whiteboard.client.WhiteBoardClient;
import com.coocaa.whiteboard.client.WhiteBoardClientListener;
import com.coocaa.whiteboard.data.CEraseInfo;
import com.coocaa.whiteboard.data.CPaintInfo;
import com.coocaa.whiteboard.server.WhiteBoardServerCmdInfo;
import com.coocaa.whiteboard.ui.R;
import com.coocaa.whiteboard.ui.base.IToolLayerView;
import com.coocaa.whiteboard.ui.callback.ToolLayerCallback;
import com.coocaa.whiteboard.ui.gesturelayer.NewGestureLayerView;
import com.coocaa.whiteboard.ui.gesturelayer.detector.CPathGestureDetector;
import com.coocaa.whiteboard.ui.toollayer.WBToolLayerView;
import com.coocaa.whiteboard.ui.util.WhiteboardSpUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import swaiotos.sensor.data.AccountInfo;

import static com.coocaa.whiteboard.ui.toollayer.WBToolLayerView.*;


/**
 * 手机、pad复用逻辑
 */
public abstract class WBClientHelper {

    protected Activity activity;
    FrameLayout frameLayout;
    View tipLayout;
    protected FrameLayout containerLayout;
    protected WhiteBoardClient whiteBoardView;
    private NewGestureLayerView overlayView;
    IToolLayerView toolLayerView;
    CPaintInfo paintInfo = new CPaintInfo();
    CEraseInfo eraseInfo = new CEraseInfo();

    private String TAG = tag();


    public WBClientHelper(Activity activity) {
        this(activity, null);
    }

    public WBClientHelper(Activity activity, String clientSSID) {
        this.activity = activity;
        frameLayout = new FrameLayout(activity);
        frameLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        frameLayout.setBackgroundColor(Color.parseColor("#F4F4F4"));

        containerLayout = new FrameLayout(activity);
        int w = getWidth();
        int h = getHeight();
        Log.d(TAG, "container w=" + w + ", h=" + h);
        FrameLayout.LayoutParams wbParams = new FrameLayout.LayoutParams(w, h);
        wbParams.leftMargin = containerLeftMargin();
        wbParams.topMargin = containerTopMargin();
        containerLayout.setLayoutParams(wbParams);
        frameLayout.addView(containerLayout);

        overlayView = new NewGestureLayerView(activity);
        initWhiteBoardClient(clientSSID);
        initOverlayView();
        initToolView();
        initTipView();
        fixScale();
    }

    protected String tag() {
        return "WBClient";
    }

    protected int containerLeftMargin() {
        return (int) activity.getResources().getDimension(R.dimen.toolbar_width);//默认是toolBarWidth
    }

    protected int containerTopMargin() {
        return 0;
    }

    public void setBackground(Drawable drawable) {
        containerLayout.setBackground(drawable);
    }

    public View onCreate(Bundle savedInstanceState, WhiteBoardServerCmdInfo syncInitData) {
        if (syncInitData != null) {
            whiteBoardView.setInitData(syncInitData);
        }
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        return frameLayout;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUIEvent(WhiteBoardUIEvent event) {
        Log.d(TAG, "onUIEvent : " + event);
        if (event == null)
            return;
        if (WhiteBoardUIEvent.DO_WHAT_EXIT.equals(event.doWhat)) {
            activity.finish();
        }
    }

    public void setConnectCallback(WhiteBoardClientListener callback) {
        whiteBoardView.registerListener(callback);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initTipView() {
        if (isShowTipView() ) {
            tipLayout = LayoutInflater.from(activity).inflate(R.layout.whiteboard_tip_layout, null);
            View btnOk = tipLayout.findViewById(R.id.btn_ok);

            btnOk.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isShowTipView() && tipLayout != null && tipLayout.getVisibility() == View.VISIBLE) {
                        tipLayout.setVisibility(View.GONE);
                        WhiteboardSpUtil.putBoolean(activity,WhiteboardSpUtil.Keys.IS_FIRST_ENTER_WHITEBOARD,false);
                    }
                }
            });

            tipLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });

            FrameLayout.LayoutParams tipParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            frameLayout.addView(tipLayout, tipParams);
        }
    }

    private void initWhiteBoardClient(String clientSSID) {
        whiteBoardView = newClient(activity);

        whiteBoardView.setAccountInfo(getAccountInfo());
        whiteBoardView.onCreate(activity);
        FrameLayout.LayoutParams wbParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        whiteBoardView.getView().setLayoutParams(wbParams);
        containerLayout.addView(whiteBoardView.getView());

        containerLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                overlayView.handleTouchEvent(event);
                return pathDetector.onTouchEvent(event);
            }
        });
    }

    protected WhiteBoardClient newClient(Context context) {
        return new WhiteBoardClient(context);
    }

    private void initOverlayView() {
        overlayView = new NewGestureLayerView(activity);
        containerLayout.addView(overlayView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void initToolView() {
        toolLayerView = createToolLayerView(activity);
        frameLayout.addView(toolLayerView.getContentView());
        toolLayerView.setToolLayerCallback(new ToolLayerCallback() {
            @Override
            public void onExitClick(boolean isClearCanvas) {
                Log.d(TAG, "onExitClick : isClearCanvas=" + isClearCanvas);
                if (isClearCanvas) {
                    whiteBoardView.clearWhiteBoard(true);
                }
                activity.finish();
            }

            @Override
            public void onMoreSavePicClick() {
                Log.d(TAG, "onMoreSavePicClick");
                onSavePicClick();
            }

            @Override
            public void onEraserClearAllClick() {
                Log.d(TAG, "onMoreClearCanvasClick");
                whiteBoardView.clearWhiteBoard(false);
            }

            @Override
            public void onTvCanvasChange(int width, int height, float scale, int posX, int posY) {

            }
        });
    }

    protected IToolLayerView createToolLayerView(Activity activity) {
        return new WBToolLayerView(activity);
    }

    /**
     * BufferLayer(  638): dimensions too large 5760 x 3240
     *
     * @return
     */

    protected int getWidth() {
        return 1920 * 3;
    }

    protected int getHeight() {
        return 1080 * 3;
    }

    protected boolean isShowTipView() {
        return WhiteboardSpUtil.getBoolean(activity,WhiteboardSpUtil.Keys.IS_FIRST_ENTER_WHITEBOARD,true);
    }


    protected void fixScale() {
        DisplayMetrics outMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getRealMetrics(outMetrics);
        float scaleX = (outMetrics.widthPixels - containerLeftMargin()) * 1f / 1920f;
        float scaleY = outMetrics.heightPixels * 1f / 1080;
        Log.d(TAG, "scaleX = " + scaleX + " , scaleY = " + scaleY + "  " + containerLeftMargin());
//        Matrix mMatrix = new Matrix();
//        mMatrix.setScale(scaleX,scaleY);
//        whiteBoardView.setMatrix(mMatrix);

        containerLayout.setPivotX(0);
        containerLayout.setPivotY(0);
        containerLayout.setScaleX(scaleX);
        containerLayout.setScaleY(scaleY);

    }

    public void finish() {
        whiteBoardView.stop();
    }

    public void onResume() {
        whiteBoardView.start();
    }

    public void onNewIntent(Intent intent) {

    }

    public void onPause() {

    }

    public void onStop() {
        whiteBoardView.onStop();
    }

    public void onDestroy() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        whiteBoardView.onDestroy();
    }

    public ViewGroup getRootView() {
        return frameLayout;
    }

    public boolean savePicture() {
        return whiteBoardView.savePicture();
    }

    protected abstract void onSavePicClick();

    protected abstract AccountInfo getAccountInfo();

    private boolean isPaint() {
        return toolLayerView.getCurrPaintMode() == MODE_PAINT;
    }

    private boolean isEraser() {
        return toolLayerView.getCurrPaintMode() == MODE_ERASER;
    }

    CPathGestureDetector pathDetector = new CPathGestureDetector(new CPathGestureDetector.SimpleGestureDetectorListener() {

        @Override
        public void onGestureStarted(MotionEvent event) {
            Log.d("BBB", "onGestureStarted, MODE_PAINT=" + MODE_PAINT);
            Log.d("BBB", "onGestureStarted, MODE_PAINT=" + toolLayerView.getCurrPaintColor());
            if (isPaint()) {
                paintInfo.setEffect(null);
                paintInfo.setStrokeColor(toolLayerView.getCurrPaintColor());
                paintInfo.setStrokeWidth(String.valueOf(toolLayerView.getCurrPaintSize()));
                whiteBoardView.setPaintInfo(paintInfo);

                overlayView.setGestureColor(Color.parseColor(toolLayerView.getCurrPaintColor()));
                overlayView.setGestureStrokeWidth(toolLayerView.getCurrPaintSize());
                overlayView.isDrawPath(true);
            } else if (isEraser()) {
                paintInfo.setEffect(SvgPaintDef.EFF_ERASE);
                eraseInfo.setWidth(toolLayerView.getCurrEraserSize());
                whiteBoardView.setEraseInfo(eraseInfo);

                overlayView.setGestureColor(SvgConfig.BG_COLOR);
                overlayView.setGestureStrokeWidth(toolLayerView.getCurrEraserSize());
                overlayView.isDrawPath(true);
            } else {
                paintInfo.setEffect(SvgPaintDef.EFF_ERASE);
                eraseInfo.setWidth(0);
                whiteBoardView.setEraseInfo(eraseInfo);
                overlayView.isDrawPath(false);
            }

            if (isPaint())
                whiteBoardView.onMotionEvent(event);
            else if (isEraser())
                whiteBoardView.onEraseMotionEvent(event);

            //手指按下的时候就消失
            if (toolLayerView != null) {
                toolLayerView.hideAllPopupWindow();
            }
        }

        @Override
        public void onGesture(MotionEvent event) {
            Log.d("BBB", "onGesture, isPaint=" + isPaint());
            if (isPaint())
                whiteBoardView.onMotionEvent(event);
            else if (isEraser())
                whiteBoardView.onEraseMotionEvent(event);
        }

        @Override
        public void onGestureEnded(MotionEvent event) {
            Log.d("BBB", "onGestureEnded, isPaint=" + isPaint());
            if (isPaint())
                whiteBoardView.onMotionEvent(event);
            else if (isEraser())
                whiteBoardView.onEraseMotionEvent(event);
        }

        @Override
        public void onGestureCancelled(MotionEvent event) {
            Log.d("BBB", "onGestureCancelled, action=" + event.getAction());
            if (isPaint())
                whiteBoardView.onMotionEvent(event);
            else if (isEraser())
                whiteBoardView.onEraseMotionEvent(event);
        }
    });
}
