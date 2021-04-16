package com.coocaa.tvpi.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.widget.AppCompatEditText;

import com.coocaa.tvpi.util.TextWatchAdapter;
import com.coocaa.tvpilib.R;

/**
 * 简单添加清除功能的EdiText
 * created by songxing on 2019/12/1
 */
public class DeletableEditText extends AppCompatEditText implements View.OnFocusChangeListener {

    private boolean hasFocus = true;
    private boolean disableClean;
    private float rightDrawableSize;
    private Drawable clearDrawable;

    private static final int DRAWABLE_LEFT = 0;
    private static final int DRAWABLE_TOP = 1;
    private static final int DRAWABLE_RIGHT = 2;
    private static final int DRAWABLE_BOTTOM = 3;

    public DeletableEditText(Context context) {
        this(context, null);
    }

    public DeletableEditText(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public DeletableEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initCustomAttrs(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setGravity(Gravity.CENTER_VERTICAL);
        setDrawable();
        this.setOnFocusChangeListener(this);
        this.addTextChangedListener(new SimpleTextWatch());
        updateDrawable(true);
    }


    private void initCustomAttrs(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.DeletableEditText, defStyleAttr, 0);
        disableClean = ta.getBoolean(R.styleable.DeletableEditText_mClearEditText_disableClear, true);
        rightDrawableSize = ta.getDimension(R.styleable.DeletableEditText_mClearEditText_rightDrawableSize, dip2px(context, 14));
        ta.recycle();
    }


    private void setDrawable() {
        //获取EditText的DrawableRight,假如没有设置我们就使用默认的图片:左上右下（0123）
        clearDrawable = getCompoundDrawables()[DRAWABLE_RIGHT];
        if (clearDrawable == null) {
            //获取默认图标
            clearDrawable = getResources().getDrawable(R.drawable.login_delete_pwd);
        }
        clearDrawable.setBounds(0, 0, (int) rightDrawableSize, (int) rightDrawableSize);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //可以获得上下左右四个drawable，右侧排第二。图标没有设置则为空。
        Drawable rightIcon = getCompoundDrawables()[DRAWABLE_RIGHT];
        if (rightIcon != null && event.getAction() == MotionEvent.ACTION_UP) {
            //检查点击的位置是否是右侧的删除图标
            //注意，使用getRwwX()是获取相对屏幕的位置，getX()可能获取相对父组件的位置
            int leftEdgeOfRightDrawable = getRight() - getPaddingRight()
                    - rightIcon.getBounds().width();
            if (event.getRawX() >= leftEdgeOfRightDrawable) {
                setText("");
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        this.hasFocus = hasFocus;
        updateDrawable(this.hasFocus);
    }

    // 更新删除图片状态: 当内容不为空，而且获得焦点，才显示右侧删除按钮
    private void updateDrawable(boolean hasFocus) {
        if (length() > 0 && hasFocus) {
            if (disableClean) {
                setCompoundDrawables( getCompoundDrawables()[DRAWABLE_LEFT], getCompoundDrawables()[DRAWABLE_TOP], clearDrawable, getCompoundDrawables()[DRAWABLE_BOTTOM]);
            } else {
                setCompoundDrawables(getCompoundDrawables()[DRAWABLE_LEFT], getCompoundDrawables()[DRAWABLE_TOP], null, getCompoundDrawables()[DRAWABLE_BOTTOM]);
            }
        } else {
            setCompoundDrawables(getCompoundDrawables()[DRAWABLE_LEFT], getCompoundDrawables()[DRAWABLE_TOP], null, getCompoundDrawables()[DRAWABLE_BOTTOM]);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        clearDrawable = null;
        super.finalize();
    }


    public class SimpleTextWatch extends TextWatchAdapter {
        @Override
        public void afterTextChanged(Editable editable) {
            updateDrawable(hasFocus);
        }
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
