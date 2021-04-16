package com.coocaa.statemanager.view.windowview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.text.TextPaint;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.coocaa.statemanager.R;
import com.coocaa.statemanager.StateManager;
import com.coocaa.statemanager.view.ThemeUtils;
import com.coocaa.statemanager.view.UiUtil;
import com.coocaa.statemanager.view.widget.ConnectCodeView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Describe:大的二维码弹框
 * Created by AwenZeng on 2021/01/20
 */
public class BigQRCodeGlobalWindow {
    private static final String TAG = "BigQRCodeGlobalWindow";
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private RelativeLayout mBgLayout;
    private LinearLayout mLayout;
    private ImageView mQRImageView;
    private ConnectCodeView mQRcodeLayout;
    private TextView mCountDownTv;
    private Context mContext;
    private int mShowTime = WINDOW_SHOW_TIME;
    private DissmisWindowHandler mDissmisWindowHandler;
    private static final int WINDOW_SHOW_TIME = 15;//15秒关闭

    private class DissmisWindowHandler extends android.os.Handler {

        public DissmisWindowHandler(@NonNull Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            mShowTime--;
            if (mShowTime <= 0) {
                dismissWindow();
            } else {
                mCountDownTv.setText(String.format("%s 秒后消失", mShowTime));
                mDissmisWindowHandler.sendEmptyMessageDelayed(0, 1000);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public BigQRCodeGlobalWindow(Context context) {
        init(context);
        mContext = context;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void init(Context context) {
        mWindowManager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        mLayoutParams = new WindowManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mLayoutParams.format = PixelFormat.RGBA_8888;
        mLayoutParams.type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;

        mDissmisWindowHandler = new DissmisWindowHandler(context.getMainLooper());


        mLayout = new LinearLayout(context);
        mLayout.setOrientation(LinearLayout.VERTICAL);
        mLayout.setElevation(UiUtil.div(30));
        mLayout.setBackground(ThemeUtils.getDrawable(Color.parseColor("#FFFFFF"), UiUtil.div(32)));


        ImageView titleImg = new ImageView(context);
        titleImg.setBackgroundResource(R.drawable.big_qrcode_title);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(UiUtil.div(336), UiUtil.div(48));
        titleParams.topMargin = UiUtil.div(60);
        titleParams.gravity = Gravity.CENTER_HORIZONTAL;
        mLayout.addView(titleImg, titleParams);


        mQRImageView = new ImageView(context);
        mQRImageView.setImageDrawable(ThemeUtils.getDrawable(Color.GRAY, 0));
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(UiUtil.div(550), UiUtil.div(550));
        imageParams.gravity = Gravity.CENTER_HORIZONTAL;
        imageParams.topMargin = UiUtil.div(60);
        mLayout.addView(mQRImageView, imageParams);

        mQRcodeLayout = new ConnectCodeView(context);
        LinearLayout.LayoutParams qrCodeParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        qrCodeParams.gravity = Gravity.CENTER_HORIZONTAL;
        qrCodeParams.topMargin = UiUtil.div(30);
        mLayout.addView(mQRcodeLayout, qrCodeParams);


        mCountDownTv = new TextView(context);
        mCountDownTv.setTextSize(UiUtil.dpi(40));
        mCountDownTv.setTextColor(Color.parseColor("#66000000"));
        mCountDownTv.setIncludeFontPadding(false);
        LinearLayout.LayoutParams countDownParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        countDownParams.gravity = Gravity.CENTER_HORIZONTAL;
        countDownParams.topMargin = UiUtil.div(52);
        mLayout.addView(mCountDownTv, countDownParams);


        mBgLayout = new RelativeLayout(context);
        mBgLayout.setBackgroundColor(Color.parseColor("#66000000"));
        RelativeLayout.LayoutParams bgLayoutParams = new RelativeLayout.LayoutParams(UiUtil.div(696), UiUtil.div(960));
        bgLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        mBgLayout.addView(mLayout, bgLayoutParams);
    }

    public void showWindow(String numStr, String qr) {
        Log.i(TAG, "showWindow: numStr = " + numStr + " qr:" + qr);
        try {
            Bitmap mBitmap = createQRImage(qr, UiUtil.div(550), UiUtil.div(550));
            mQRImageView.setImageBitmap(mBitmap);
            mQRcodeLayout.showQRCode(numStr);
            mShowTime = WINDOW_SHOW_TIME;
            mWindowManager.addView(mBgLayout, mLayoutParams);
            mCountDownTv.setText(String.format("%s 秒后消失", mShowTime));
            mDissmisWindowHandler.sendEmptyMessageDelayed(0, 1000);
        } catch (Exception e) {
            Log.e(TAG, "showWindow: error = " + e.getMessage());
        }
    }

    public void dismissWindow() {
        try {
            StateManager.INSTANCE.setShowBigQrCodeWindowStatus(false);
            mDissmisWindowHandler.removeCallbacksAndMessages(null);
            mWindowManager.removeView(mBgLayout);
        } catch (Exception e) {
            Log.e(TAG, "dismissWindow: error = ");
        }
    }

    public void restartShowWindow(String numStr, String qr) {
        try {
            mDissmisWindowHandler.removeCallbacksAndMessages(null);
            Log.i(TAG, "showWindow: numStr = " + numStr + " qr:" + qr);
            Bitmap mBitmap = createQRImage(qr, UiUtil.div(550), UiUtil.div(550));
            mQRImageView.setImageBitmap(mBitmap);
            mQRcodeLayout.showQRCode(numStr);
            mShowTime = WINDOW_SHOW_TIME;
            mCountDownTv.setText(String.format("%s 秒后消失", mShowTime));
            mDissmisWindowHandler.sendEmptyMessageDelayed(0, 1000);
        } catch (Exception e) {
            Log.e(TAG, "dismissWindow: error = ");
        }
    }

    public void updateWinodow(String numStr) {
        Log.i(TAG, "updateWinodow: numStr = " + numStr);
        try {
            Bitmap mBitmap = createQRImage(numStr, UiUtil.div(550), UiUtil.div(550));
            mQRImageView.setImageBitmap(mBitmap);
            mQRcodeLayout.showQRCode(numStr);
        } catch (Exception e) {
            Log.i(TAG, "updateWinodow: error = " + e.getMessage());
        }
    }


    /**
     * 生成二维码Bitmap
     *
     * @param content   内容
     * @param widthPix  图片宽度
     * @param heightPix 图片高度
     */
    public Bitmap createQRImage(String content, int widthPix, int heightPix) {
        try {
            if (content == null || "".equals(content)) {
                return null;
            }

            //配置参数
            Map<EncodeHintType, Object> hints = new HashMap();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            //容错级别
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            //设置空白边距的宽度
            //    hints.put(EncodeHintType.MARGIN, 1); //default is 4

            // 图像数据转换，使用了矩阵转换
            BitMatrix bitMatrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, widthPix, heightPix, hints);
            bitMatrix = deleteWhite(bitMatrix);//删除白边

            widthPix = bitMatrix.getWidth();
            heightPix = bitMatrix.getHeight();
            int[] pixels = new int[widthPix * heightPix];
            // 下面这里按照二维码的算法，逐个生成二维码的图片，
            // 两个for循环是图片横列扫描的结果
            for (int y = 0; y < heightPix; y++) {
                for (int x = 0; x < widthPix; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * widthPix + x] = 0xff000000;
                    } else {
                        pixels[y * widthPix + x] = 0x00000000;
                    }
                }
            }

            // 生成二维码图片的格式，使用ARGB_8888
            Bitmap bitmap = Bitmap.createBitmap(widthPix, heightPix, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, widthPix, 0, 0, widthPix, heightPix);

            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 删除白边
     *
     * @param matrix
     * @return
     */
    private BitMatrix deleteWhite(BitMatrix matrix) {
        int[] rec = matrix.getEnclosingRectangle();
        int resWidth = rec[2] + 1;
        int resHeight = rec[3] + 1;

        BitMatrix resMatrix = new BitMatrix(resWidth, resHeight);
        resMatrix.clear();
        for (int i = 0; i < resWidth; i++) {
            for (int j = 0; j < resHeight; j++) {
                if (matrix.get(i + rec[0], j + rec[1]))
                    resMatrix.set(i, j);
            }
        }
        return resMatrix;
    }
}
