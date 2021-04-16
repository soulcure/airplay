package com.coocaa.statemanager.view.windowview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.support.annotation.RequiresApi;
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
public class QRCodeGlobalWindow {
    private static final String TAG = "QRCodeGlobalWindow";
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private LinearLayout mLayout;
    private ImageView mQRImageView;
    private TextView qrNumText;
    private Context mContext;

    public static final String QRCODE_BASE_URL = "http://tvpi.coocaa.com/swaiot/index.html?action=smart_screen&channel=dev&";
//    public static final String QRCODE_BASE_URL = "https://s.skysrt.com/IvEFz2?";


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public QRCodeGlobalWindow(Context context) {
        init(context);
        mContext = context;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void init(Context context) {
        mWindowManager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        mLayoutParams = new WindowManager.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mLayoutParams.gravity = Gravity.BOTTOM | Gravity.END;
        mLayoutParams.x = UiUtil.div(10);
        mLayoutParams.y = UiUtil.div(10);
        mLayoutParams.height = UiUtil.div(127);
        mLayoutParams.width = UiUtil.div(90);
        mLayoutParams.format = PixelFormat.RGBA_8888;
        mLayoutParams.type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;

        mLayout = new LinearLayout(context);
        mLayout.setOrientation(LinearLayout.VERTICAL);
        mLayout.setElevation(UiUtil.div(30));
        mLayout.setBackground(ThemeUtils.getDrawable(Color.parseColor("#CCFFFFFF"), UiUtil.div(10)));

        TextView topTextView = new TextView(context);
        topTextView.setIncludeFontPadding(false);
        topTextView.setTextSize(UiUtil.dpi(14));
        topTextView.setTextColor(Color.parseColor("#CC000000"));
        topTextView.setText("连接共享屏");
        LinearLayout.LayoutParams topParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        topParams.topMargin = UiUtil.div(4);
        topParams.gravity = Gravity.CENTER_HORIZONTAL;
        mLayout.addView(topTextView, topParams);


        mQRImageView = new ImageView(context);
        mQRImageView.setImageDrawable(ThemeUtils.getDrawable(Color.GRAY, 0));
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(UiUtil.div(80), UiUtil.div(80));
        imageParams.gravity = Gravity.CENTER_HORIZONTAL;
        imageParams.topMargin = UiUtil.div(4);
        mLayout.addView(mQRImageView, imageParams);

        qrNumText = new TextView(context);
        qrNumText.setTextSize(UiUtil.dpi(16));
        qrNumText.setTextColor(Color.parseColor("#cc000000"));
        qrNumText.setIncludeFontPadding(false);
        qrNumText.setLetterSpacing(0.035f);
        LinearLayout.LayoutParams qrNumParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        qrNumParams.gravity = Gravity.CENTER_HORIZONTAL;
        qrNumParams.topMargin = UiUtil.div(2);
        mLayout.addView(qrNumText, qrNumParams);
    }

    public void showWindow(String numStr,String qr) {
        Log.i(TAG, "showWindow: numStr = " + numStr+" qr:"+qr);
        Bitmap mBitmap = createQRImage(qr, UiUtil.div(80), UiUtil.div(80));
        mQRImageView.setImageBitmap(mBitmap);
        qrNumText.setText(numStr);
        try {
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
            Bitmap mBitmap = createQRImage(numStr, UiUtil.div(80), UiUtil.div(80));
            mQRImageView.setImageBitmap(mBitmap);
            qrNumText.setText(numStr);
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
