package com.coocaa.tvpi.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

import com.coocaa.tvpi.util.FilterUtils;

/**
 * <p>
 * 自定义TextView，该TextView只需要一张默认背景， 按下效果自动运算
 * </p>
 * <p/>
 * 提供5种颜色滤镜以及可设置自定义滤镜
 * <p/>
 * <p>
 * demos:
 * </p>
 * <ul>
 * <li>常规使用：</br> btnOK = (BlossomTextView) view.findViewById(R.id.btnOK);</br>
 * btnOK.setPressColorType(Type.green);//按下效果，可选</br>
 * btnOK.setSelfFilter(array);//自定义滤镜，可选</br> btnOK.setEnable(true);</br></li>
 * </ul>
 */
public class STextView extends TextView {
    /**
     * 自定义滤镜
     */
    float arraySelf[];
    boolean idle = true;
    /**
     * 按下背景bitmap
     */
    private Bitmap bgBitmap;
    /**
     * 默认背景
     */
    private Drawable bgDefault;
    /**
     * 背景变换类型
     */
    private FilterUtils.Type bgType = FilterUtils.Type.light;
    private int paddingLeft;
    private int paddingTop;
    private int paddingRight;
    private int paddingBottom;
    private boolean b_can_click = true;

    public STextView(Context context) {
        super(context);
        init();
    }

    private void init() {
        paddingLeft = getPaddingLeft();
        paddingTop = getPaddingTop();
        paddingRight = getPaddingRight();
        paddingBottom = getPaddingBottom();
    }

    public STextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public STextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * 是否可点击，默认可点
     *
     * @param b
     */
    public void setCanClick(boolean b) {
        setEnabled(b);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        String text = getText().toString();
        if (idle && text.length() > 0) {
            setMinWidth(0);
            setMinHeight(0);
            setMaxWidth(Integer.MAX_VALUE);
            setMaxHeight(Integer.MAX_VALUE);
            setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        // 文字变更需要重置背景图
        bgDefault = null;
        if (null != bgBitmap && !bgBitmap.isRecycled()) {
            bgBitmap.recycle();
        }
        bgBitmap = null;

        super.onTextChanged(text, start, lengthBefore, lengthAfter);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled())
            return false;
        if (bgDefault == null) {
            bgDefault = getBackground();
        }
        if (null == bgBitmap) {
            int w = getWidth();
            int h = getHeight();
            Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bm);
            bgDefault.draw(canvas);

            float array[] = (null != arraySelf) ? arraySelf : FilterUtils.getFilter(bgType);

            setWidth(w);
            setHeight(h);
            bgBitmap = getTransformBg(bm, w, h, array);
        }
        idle = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setBackgroundDrawable(new BitmapDrawable(bgBitmap));
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                idle = true;
                if (bgType != FilterUtils.Type.gray)
                    setBackgroundDrawable(bgDefault);
                // 1秒内不允许重复点击
                if (!b_can_click) {
                    // 此处屏蔽onTouchEvent
                    return false;
                }
                b_can_click = false;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        b_can_click = true;
                    }
                }, 1000);
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            default:
                idle = true;
                this.setBackgroundDrawable(bgDefault);
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 计算变换效果图
     *
     * @param w
     * @param h
     * @return
     */
    private Bitmap getTransformBg(Bitmap src, int w, int h, float array[]) {
        Bitmap thisBg = Bitmap.createBitmap(src, 0, 0, w, h);
        Bitmap newBitmap = null;
        newBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColorFilter(null);
        ColorMatrix cm = new ColorMatrix();
        // 设置颜色矩阵
        cm.set(array);
        // 设置滤镜
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        // 绘图
        canvas.drawBitmap(thisBg, 0, 0, paint);
        thisBg.recycle();
        thisBg = null;
        return newBitmap;
    }

    /**
     * 设置新背景
     *
     * @param resid
     */
    public void setBgResource(int resid) {
        bgDefault = null;
        if (null != bgBitmap && !bgBitmap.isRecycled()) {
            bgBitmap.recycle();
        }
        bgBitmap = null;
        setBackgroundResource(resid);
    }

    /**
     * 设置背景颜色变换方式，不设置则使用默认滤镜
     *
     * @param bgType
     */
    public void setPressColorType(FilterUtils.Type bgType) {
        this.bgType = bgType;
        this.arraySelf = null;
    }

    /**
     * 设置自定义滤镜，20个数字的数组
     *
     * @param arraySelf
     */
    public void setSelfFilter(float[] arraySelf) {
        this.bgType = FilterUtils.Type.self;
        this.arraySelf = arraySelf;
    }
}
