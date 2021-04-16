package com.coocaa.whiteboard.ui.toollayer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AlertDialog;

import com.coocaa.whiteboard.ui.R;
import com.coocaa.whiteboard.ui.base.BaseToolLayerView;
import com.coocaa.whiteboard.ui.base.IToolLayerView;
import com.coocaa.whiteboard.ui.callback.ToolLayerCallback;
import com.coocaa.whiteboard.ui.util.WhiteboardUIConfig;


/**
 * @ClassName IOverLayout
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 3/2/21
 * @Version TODO (write something)
 */
public class WBToolLayerView extends BaseToolLayerView implements IToolLayerView {

    private LinearLayout barLayout;
    private View btnPaint;  //画笔
    private View btnEraser; //橡皮
    private View btnMore;   //更多设置

    private PopupWindow paintPopupWindow;
    private PopupWindow erasePopupWindow;
    private PopupWindow morePopupWindow;
    private AlertDialog clearDialog;
    private ToolLayerCallback toolLayerCallback;

    private String currPaintColor;
    private int currPaintSize;
    private int currEraserSize;
    private @PaintMode int paintMode;


    public WBToolLayerView(Context context) {
        this(context, null);
    }

    public WBToolLayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        currPaintColor = getDefaultPaintColor();
        currPaintSize = WhiteboardUIConfig.DEFAULT_PAINT_SIZE;
        currEraserSize = WhiteboardUIConfig.DEFAULT_ERASER_SIZE;
        initView();
    }

    @Override
    public int getLayoutId() {
        return R.layout.whiteboard_tool_layer_layout;
    }

    @SuppressLint("RestrictedApi")
    private void initView() {
        barLayout = findViewById(R.id.bar_layout);
        btnPaint = findViewById(R.id.whiteboard_toolbar_paint);
        btnEraser = findViewById(R.id.whiteboard_toolbar_eraser);
        btnMore = findViewById(R.id.whiteboard_toolbar_more);
        ImageView ivExit = findViewById(R.id.whiteboard_exit);

        setToolbarSelectedTab(btnPaint);

        ivExit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toolLayerCallback != null) {
                    toolLayerCallback.onExitClick(false);
                }
            }
        });


        btnPaint.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!btnPaint.isSelected()) {
                    hideAllPopupWindow();
                    setToolbarSelectedTab(btnPaint);
                } else {
                    if(paintPopupWindow != null && paintPopupWindow.isShowing()) {
                        hidePaintPopupWindow();
                    }else {
                        showPaintPopupWindow();
                    }
                }
            }
        });

        btnEraser.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!btnEraser.isSelected()) {
                    hideAllPopupWindow();
                    setToolbarSelectedTab(btnEraser);
                } else {
                    if(erasePopupWindow != null && erasePopupWindow.isShowing()){
                        hideErasePopupWindow();
                    }else {
                        showEraserPopupWindow();
                    }
                }
            }
        });

        btnMore.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(morePopupWindow != null && morePopupWindow.isShowing()) {
                    hideMorePopupWindow();
                    setToolbarSelectedTab(btnPaint);
                }else {
                    hideAllPopupWindow();
                    setToolbarSelectedTab(btnMore);
                    showMorePopupWindow();
                }
            }
        });

        barLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideAllPopupWindow();
            }
        });
    }

    private void showPaintPopupWindow() {
        if (paintPopupWindow == null) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.popup_setting_paint, null);
            RadioGroup rgPaintColor = view.findViewById(R.id.whiteboard_toolbar_rg_paint_color);
            ((RadioButton)rgPaintColor.findViewById(getDefaultPaintColorRadioButtonId())).setChecked(true);
            RadioGroup rgPaintSize = view.findViewById(R.id.whiteboard_toolbar_rg_paint_size);

            rgPaintColor.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    int index = group.indexOfChild(group.findViewById(checkedId));
                    currPaintColor = WhiteboardUIConfig.PAINT_COLOR_MAP.get(index);
                }
            });

            rgPaintSize.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    int index = group.indexOfChild(group.findViewById(checkedId));
                    currPaintSize = WhiteboardUIConfig.PAINT_SIZE_MAP.get(index);
                }
            });

            paintPopupWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            paintPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    setToolbarSelectedTab(btnPaint);
                }
            });

        }

        if (paintPopupWindow != null && !paintPopupWindow.isShowing()) {
            int offsetX = (int) getResources().getDimension(R.dimen.toolbar_width) + dp2Px(getContext(), 25);
            paintPopupWindow.showAtLocation(barLayout, Gravity.CENTER_VERTICAL | Gravity.START, offsetX, 0);
        }
    }

    private void hidePaintPopupWindow(){
        if (paintPopupWindow != null && paintPopupWindow.isShowing()) {
            paintPopupWindow.dismiss();
        }
    }

    private void showEraserPopupWindow() {
        if (erasePopupWindow == null) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.popup_setting_eraser, null);
            RadioGroup rgEraserSize = view.findViewById(R.id.whiteboard_toolbar_rg_eraser_size);
            rgEraserSize.setVisibility(isShowEraserRadioGroup() ? VISIBLE : GONE);
            View btnClear = view.findViewById(R.id.whiteboard_toolbar_rb_eraser_clear);

            rgEraserSize.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    int index = group.indexOfChild(group.findViewById(checkedId));
                    currEraserSize = WhiteboardUIConfig.ERASER_SIZE_MAP.get(index);
                }
            });

            btnClear.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    erasePopupWindow.dismiss();
                    showClearDialog();
                }
            });

            erasePopupWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            erasePopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    setToolbarSelectedTab(btnEraser);
                }
            });
        }

        if (erasePopupWindow != null && !erasePopupWindow.isShowing()) {
            int offsetX = (int) getResources().getDimension(R.dimen.toolbar_width) + dp2Px(getContext(), 25);
            erasePopupWindow.showAtLocation(barLayout, Gravity.CENTER_VERTICAL | Gravity.START, offsetX, 0);
        }
    }

    private void hideErasePopupWindow(){
        if (erasePopupWindow != null && erasePopupWindow.isShowing()) {
            erasePopupWindow.dismiss();
        }
    }

    private void showMorePopupWindow() {
        if (morePopupWindow == null) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.popup_setting_more, null);
            View btnMoreSavePic = view.findViewById(R.id.whiteboard_toolbar_more_save);
            View btnMorePaging = view.findViewById(R.id.whiteboard_toolbar_more_paging);

            btnMoreSavePic.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    morePopupWindow.dismiss();
                    if (toolLayerCallback != null) {
                        toolLayerCallback.onMoreSavePicClick();
                    }
                }
            });

            btnMorePaging.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    morePopupWindow.dismiss();
                }
            });

            morePopupWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            morePopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    setToolbarSelectedTab(btnPaint);
                }
            });

        }

        if (morePopupWindow != null && !morePopupWindow.isShowing()) {
            int offsetX = (int) getResources().getDimension(R.dimen.toolbar_width) + dp2Px(getContext(), 25);
            int offsetY = dp2Px(getContext(), 113);
            morePopupWindow.showAtLocation(barLayout, Gravity.BOTTOM | Gravity.START, offsetX, offsetY);
        }
    }


    private void hideMorePopupWindow(){
        if (morePopupWindow != null && morePopupWindow.isShowing()) {
            morePopupWindow.dismiss();
        }
    }

    private void showClearDialog(){
        if(clearDialog == null) {
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_clear_canvas, null);
            clearDialog = new AlertDialog.Builder(getContext())
                    .setView(dialogView)
                    .create();
            View tvCancel = dialogView.findViewById(R.id.tv_cancel);
            View tvClear = dialogView.findViewById(R.id.tv_clear);
            tvCancel.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearDialog.dismiss();
                }
            });

            tvClear.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearDialog.dismiss();
                    setToolbarSelectedTab(btnPaint);
                    if (toolLayerCallback != null) {
                        toolLayerCallback.onEraserClearAllClick();
                    }
                }
            });

            Window window = clearDialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawable(new ColorDrawable(0x00000000));
            }
        }
        if (clearDialog != null && !clearDialog.isShowing()
                && getContext() instanceof Activity
                && !((Activity)getContext()).isFinishing()) {
            clearDialog.show();
        }
    }

    private void setToolbarSelectedTab(View selectedTab) {
        btnPaint.setSelected(false);
        btnEraser.setSelected(false);
        btnMore.setSelected(false);
        if (selectedTab != null) {
            selectedTab.setSelected(true);
            if (selectedTab == btnPaint) {
                paintMode = MODE_PAINT;
            } else if (selectedTab == btnEraser) {
                paintMode = MODE_ERASER;
            }else if(selectedTab == btnMore){
                paintMode = MODE_NONE;
            }
        }
    }


    public void setToolLayerCallback(ToolLayerCallback toolLayerCallback) {
        this.toolLayerCallback = toolLayerCallback;
    }

    @Override
    public View getContentView() {
        return this;
    }

    @Override
    public String getCurrPaintColor() {
        return currPaintColor;
    }

    @Override
    public int getCurrPaintSize() {
        return currPaintSize;
    }

    @Override
    public int getCurrEraserSize() {
        return currEraserSize;
    }

    @Override
    public @PaintMode int getCurrPaintMode() {
        return paintMode;
    }

    @Override
    public void hideAllPopupWindow(){
        hidePaintPopupWindow();
        hideErasePopupWindow();
        hideMorePopupWindow();
    }

    private int dp2Px(Context context, float dp) {
        if (context == null)
            return -1;
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    protected String getDefaultPaintColor(){
        return WhiteboardUIConfig.DEFAULT_WB_PAINT_COLOR;
    }

    protected int getDefaultPaintColorRadioButtonId(){
        return R.id.whiteboard_toolbar_rb_paint_black;
    }

    protected boolean isShowEraserRadioGroup(){
        return true;
    }
}
