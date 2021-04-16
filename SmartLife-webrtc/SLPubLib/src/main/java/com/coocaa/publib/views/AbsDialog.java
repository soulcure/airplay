package com.coocaa.publib.views;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * 共通dialog
 *
 * @author wangyajun
 */
public class AbsDialog extends Dialog {
    /**
     * 点击外部关闭
     */
    boolean cancel = true;
    /**
     * 显示范围
     */
    Rect rect = new Rect();
    /**
     * decor节点
     */
    private ViewGroup decor;
    /**
     * decor子节点
     */
    private ViewGroup child;
    /**
     * 根节点
     */
    private View root;

    public AbsDialog(Context context, int theme) {
        super(context, theme);
    }

    public void init() {
        decor = (ViewGroup) getWindow().getDecorView();
        child = (ViewGroup) decor.findViewById(android.R.id.content);
        root = child.getChildAt(0);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        rect.set(root.getLeft(), root.getTop(), root.getRight(), root.getBottom());
        int top = (int) (ev.getY() - child.getTop());
        if (cancel && !rect.contains((int) ev.getX(), top)) {
            dismiss();
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void setCanceledOnTouchOutside(boolean cancel) {
        super.setCanceledOnTouchOutside(cancel);
        this.cancel = cancel;
    }
}
