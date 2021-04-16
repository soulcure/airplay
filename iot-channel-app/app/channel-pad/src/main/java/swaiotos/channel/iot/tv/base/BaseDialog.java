package swaiotos.channel.iot.tv.base;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

/**
 * @author wagnyuehui
 * @time 2020/3/27
 * @describe
 */
public class BaseDialog extends Dialog {
    public BaseDialog(Context context) {
        super(context);
    }

    public BaseDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    protected BaseDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }


    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }



}
