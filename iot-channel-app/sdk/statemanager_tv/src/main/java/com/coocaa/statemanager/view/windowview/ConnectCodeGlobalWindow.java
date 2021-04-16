package com.coocaa.statemanager.view.windowview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextPaint;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coocaa.statemanager.view.ThemeUtils;
import com.coocaa.statemanager.view.UiUtil;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.HashMap;
import java.util.Map;

/**
 * @ Created on: 2020/10/22
 * @Author: LEGION XiaoLuo
 * @ Description:
 */
public class ConnectCodeGlobalWindow {
    private static final String TAG = "ConnectCodeGlobalWindow";
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private LinearLayout mLayout;
    private TextView qrNumText;
    private Context mContext;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ConnectCodeGlobalWindow(Context context) {
        init(context);
        mContext = context;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void init(Context context) {
        mWindowManager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        mLayoutParams = new WindowManager.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        mLayoutParams.width = UiUtil.div(253);
        mLayoutParams.height = UiUtil.div(50);
        mLayoutParams.format = PixelFormat.RGBA_8888;
        mLayoutParams.type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;

        mLayout = new LinearLayout(context);
        mLayout.setOrientation(LinearLayout.HORIZONTAL);
        mLayout.setElevation(UiUtil.div(30));
        mLayout.setBackground(ThemeUtils.getDrawable(Color.parseColor("#66000000"),0,0,UiUtil.div(10),0));

        qrNumText = new TextView(context);
        qrNumText.setTextSize(UiUtil.dpi(28));
        qrNumText.setTextColor(Color.parseColor("#ccFFFFFF"));
        qrNumText.setIncludeFontPadding(false);
        qrNumText.setGravity(Gravity.CENTER_VERTICAL);
        TextPaint paint = qrNumText.getPaint();
        paint.setFakeBoldText(true);
        qrNumText.setLetterSpacing(0.035f);
        LinearLayout.LayoutParams qrNumParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        qrNumParams.leftMargin = UiUtil.div(10);
        mLayout.addView(qrNumText, qrNumParams);
    }

    public void showWindow(String numStr,String qr) {
        Log.i(TAG, "showWindow: numStr = " + numStr+" qr:"+qr);
        try {
            updateWinodow(numStr);
            mWindowManager.addView(mLayout, mLayoutParams);
        } catch (Exception e) {
            Log.e(TAG, "showWindow: error = " + e.getMessage());
        }
    }

    public void dismissWindow() {
        try {
            mWindowManager.removeView(mLayout);
        } catch (Exception e) {
            Log.e(TAG, "dismissWindow: error = ");
        }
    }

    public void updateWinodow(String numStr) {
        Log.i(TAG, "updateWinodow: numStr = " + numStr);
        try {
            qrNumText.setText("连屏码 " + numStr);
        } catch (Exception e) {
            Log.i(TAG, "updateWinodow: error = " + e.getMessage());
        }
    }
}
