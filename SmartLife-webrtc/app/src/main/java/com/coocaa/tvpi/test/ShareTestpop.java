package com.coocaa.tvpi.test;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.coocaa.smartscreen.R;


public class ShareTestpop extends PopupWindow {

    private View mPopView;
    private TextView btn_cancel, btn_confirm;
    private Context mContext;

    private OnOKClickListener onClickListener;

    public interface OnOKClickListener {
        /**
         * Called when a view has been clicked.
         */
        void onClick();

    }

    public ShareTestpop(Context context, OnOKClickListener mOnClick) {
        super(context);
        mContext = context;
        onClickListener = mOnClick;
        init(context);
        setPopupWindow();
    }

    private void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        mPopView = inflater.inflate(R.layout.pop_share_test, null);
        btn_cancel = mPopView.findViewById(R.id.btn_cancel);
        btn_confirm = mPopView.findViewById(R.id.btn_confirm);

        btn_cancel.setOnClickListener(view -> setDismiss((Activity) mContext));

        btn_confirm.setOnClickListener(view -> {
            setDismiss((Activity) mContext);
            onClickListener.onClick();

        });
    }

    /**
     * 设置窗口的相关属性
     */
    @SuppressLint("InlinedApi")
    private void setPopupWindow() {
        this.setContentView(mPopView);// 设置View
        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);// 设置弹出窗口的宽
        this.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);// 设置弹出窗口的高
        this.setFocusable(true);// 设置弹出窗口可
        this.setBackgroundDrawable(new ColorDrawable(0x00000000));// 设置背景透明
        this.setOutsideTouchable(true);

    }

    public void setPopupShow(View view, Activity activity, float bgAlpha) {
        showAtLocation(view, Gravity.CENTER, 0, 0);
        WindowManager.LayoutParams lp = activity.getWindow()
                .getAttributes();
        lp.alpha = bgAlpha;
        (activity).getWindow().setAttributes(lp);

    }

    private void setDismiss(Activity activity) {
        this.dismiss();
        WindowManager.LayoutParams lp = activity.getWindow()
                .getAttributes();
        lp.alpha = (float) 1.0;
        (activity).getWindow().setAttributes(lp);

    }

}
