package com.coocaa.swaiotos.virtualinput.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.cocaa.swaiotos.virtualinput.R;
import com.coocaa.swaiotos.virtualinput.utils.DimensUtils;

import java.util.ArrayList;
import java.util.List;

public class RippleView extends View {

    private String TAG = "RippleView";

    private Context mContext;

    private float mWidth;

    private float mHeight;

    private Paint mPaint;

    private List<Circle> mCircles;

    private int mColor;

    private int mStrokeWidth;

    /**
     * 开始园半径
     */
    private float mStartRadius;

    /**
     * 结束园半径
     */
    private float mEndRadius;

    /**
     * 开始透明度
     */
    private float mStartAlpha;

    /**
     * 结束透明度
     */
    private float mEndAlpha;

    /**
     * 圆圈之间的间隔时间
     */
    private int mIntervalTime;

    /**
     * 圆圈扩散时间
     */
    private long mDiffusionTime;


    private long mPreDrawTime;


    public RippleView(Context context) {
        this(context, null);
    }

    public RippleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RippleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray tya = context.obtainStyledAttributes(attrs, R.styleable.RippleView);
        mColor = tya.getColor(R.styleable.RippleView_cColor, Color.BLACK);
        mIntervalTime = tya.getInt(R.styleable.RippleView_cIntervalTime, 1000);
        mDiffusionTime = tya.getInt(R.styleable.RippleView_cDiffusionTime, 3000);
        mStartRadius = tya.getInt(R.styleable.RippleView_cStartRadius, 0);
        mEndRadius = tya.getInt(R.styleable.RippleView_cEndRadius, 0);
        mStrokeWidth = tya.getInt(R.styleable.RippleView_cStrokeWidth, 2);
        mStartAlpha = tya.getFloat(R.styleable.RippleView_cStartAlpha, 1);
        mEndAlpha = tya.getFloat(R.styleable.RippleView_cEndAlpha, 1);
        mPreDrawTime = System.currentTimeMillis();
        Log.d(TAG, "RippleView: ");
        tya.recycle();
        init();
    }

    private void init() {
        mContext = getContext();
        //画笔样式
        mPaint = new Paint();
        mPaint.setColor(mColor);
        mPaint.setStrokeWidth(DimensUtils.dp2Px(mContext, mStrokeWidth));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setAntiAlias(true);

        mCircles = new ArrayList<>();
        Circle c = new Circle(mStartRadius, (int) (mStartAlpha * 255), System.currentTimeMillis());
        mCircles.add(c);

        setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawCircle(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthSpecMode == MeasureSpec.EXACTLY) {
            mWidth = widthSpecSize;
        } else {
            mWidth = DimensUtils.dp2Px(mContext, 120);
        }

        if (heightSpecMode == MeasureSpec.EXACTLY) {
            mHeight = heightSpecSize;
        } else {
            mHeight = DimensUtils.dp2Px(mContext, 120);
        }

        setMeasuredDimension((int) mWidth, (int) mHeight);
    }

    private void drawCircle(Canvas canvas) {
        canvas.save();
        for (int i = 0; i < mCircles.size(); i++) {
            Circle c = mCircles.get(i);
            mPaint.setAlpha(c.alpha);
            canvas.drawCircle(mWidth / 2, mHeight / 2, DimensUtils.dp2Px(mContext, c.radius) - mPaint.getStrokeWidth(), mPaint);
            double alpha = mStartAlpha * 255 + ((c.radius - mStartRadius) / (mEndRadius - mStartRadius) * ((mEndAlpha - mStartAlpha)) * 255);
            c.alpha = (int) alpha;
            c.radius = mStartRadius + ((float)(System.currentTimeMillis() - c.circleAddTime) / (float)mDiffusionTime) * (mEndRadius - mStartRadius);
            // c.radius += mSpeed * 0.1;
        }

        //另外起一个for循环去掉无用的circle避免闪烁
        for (int i = 0; i < mCircles.size(); i++) {
            Circle c = mCircles.get(i);
            if (c.radius > mWidth || c.radius > mEndRadius) {
                Log.d(TAG, "drawCircle: remove " + c.radius);
                mCircles.remove(i);
            }
        }

        if (System.currentTimeMillis() - mPreDrawTime > mIntervalTime) {
            mPreDrawTime = System.currentTimeMillis();
            Circle c = new Circle(mStartRadius, (int) (mStartAlpha * 255), System.currentTimeMillis());
            mCircles.add(c);
        }
        postInvalidateDelayed(5);
        canvas.restore();
    }

    private void stopAnimal() {

    }

    private void startAnimal() {

    }

    class Circle {

        Circle(float radius, int alpha, long circleAddTime) {
            this.radius = radius;
            this.alpha = alpha;
            this.circleAddTime = circleAddTime;
        }

        private float radius;

        private int alpha;

        private long circleAddTime;
    }

}
