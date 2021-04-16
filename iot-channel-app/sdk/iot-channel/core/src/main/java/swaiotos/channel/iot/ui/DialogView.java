package swaiotos.channel.iot.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


/**
 * @ Created on: 2020/4/3
 * @Author: LEGION XiaoLuo
 * @ Description:
 */
public class DialogView extends LinearLayout {

    TextView cancelBtn;
    TextView confirmButton;
    TextView textView;

    public DialogView(Context context) {
        super(context);
        initView();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        //边框圆角效果
        GradientDrawable parentDrawable = new GradientDrawable();
        parentDrawable.setShape(GradientDrawable.RECTANGLE);
        parentDrawable.setCornerRadius(dip2px(18));
        parentDrawable.setColor(Color.parseColor("#F4F4F4"));
        setBackground(parentDrawable);

        LayoutParams params = new LayoutParams(dip2px(300), dip2px(150));
        setPadding(dip2px(20), dip2px(20), dip2px(20), dip2px(20));
        setOrientation(VERTICAL);
        params.gravity = Gravity.CENTER;
        setLayoutParams(params);

        textView = new TextView(getContext());
        LayoutParams textParams = new LayoutParams(dip2px(260), dip2px(44));
        textParams.gravity = Gravity.CENTER;
        textView.setText("不支持，请下载");
        textView.setTextSize(16);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(Color.parseColor("#000000"));
        textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        addView(textView, textParams);

        LinearLayout layout = new LinearLayout(getContext());
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dip2px(45));
        layoutParams.topMargin = dip2px(20);
        layout.setOrientation(HORIZONTAL);
        addView(layout, layoutParams);

        cancelBtn = new TextView(getContext());
        LayoutParams cancelParams = new LayoutParams(dip2px(125), ViewGroup.LayoutParams.MATCH_PARENT);
        cancelParams.gravity = Gravity.CENTER;
        cancelBtn.setText("取消");
        cancelBtn.setClickable(true);
        cancelBtn.setFocusable(true);
        cancelBtn.setTextSize(16);
        cancelBtn.setGravity(Gravity.CENTER);
        cancelBtn.setTextColor(Color.parseColor("#000000"));
        layout.addView(cancelBtn, cancelParams);

        confirmButton = new TextView(getContext());
        final LayoutParams confirmParams = new LayoutParams(dip2px(125), ViewGroup.LayoutParams.MATCH_PARENT);
        confirmParams.leftMargin = dip2px(10);
        confirmParams.gravity = Gravity.CENTER;
        confirmButton.setClickable(true);
        confirmButton.setFocusable(true);
        confirmButton.setText("确定");
        confirmButton.setTextSize(16);
        confirmButton.setGravity(Gravity.CENTER);
        confirmButton.setTextColor(Color.parseColor("#000000"));
        layout.addView(confirmButton, confirmParams);

        int[] colors = {Color.parseColor("#FF7700"),Color.parseColor("#FF9000")};
        GradientDrawable FocusedDrawable = new GradientDrawable();
        FocusedDrawable.setShape(GradientDrawable.RECTANGLE);
        FocusedDrawable.setColors(colors); //添加颜色组
        FocusedDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);//设置线性渐变
        FocusedDrawable.setOrientation(GradientDrawable.Orientation.RIGHT_LEFT);//设置渐变方向
        FocusedDrawable.setCornerRadius(dip2px(8));

        GradientDrawable nonFocusedDrawable = new GradientDrawable();
        nonFocusedDrawable.setShape(GradientDrawable.RECTANGLE);
        nonFocusedDrawable.setColor(Color.parseColor("#ffffff"));
        nonFocusedDrawable.setCornerRadius(dip2px(8));

        cancelBtn.setBackground(nonFocusedDrawable);
        confirmButton.setBackground(FocusedDrawable);
        confirmButton.setTextColor(Color.parseColor("#FFFFFF"));

//        confirmButton.setOnFocusChangeListener(new OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (hasFocus) {
//                    confirmButton.setTextColor(Color.parseColor("#000000"));
//                } else {
//                    confirmButton.setTextColor(Color.parseColor("#FFFFFF"));
//                }
//            }
//        });
//
//        cancelBtn.setOnFocusChangeListener(new OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (hasFocus) {
//                    cancelBtn.setTextColor(Color.parseColor("#000000"));
//                } else {
//                    cancelBtn.setTextColor(Color.parseColor("#FFFFFF"));
//                }
//            }
//        });
//
//        confirmButton.setOnTouchListener(new OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_DOWN ) {
//                    confirmButton.setTextColor(Color.parseColor("#FFFFFF"));
//                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
//                    confirmButton.setTextColor(Color.parseColor("#000000"));
//                }
//                return false;
//            }
//        });
//
//
//        cancelBtn.setOnTouchListener(new OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_DOWN ) {
//                    cancelBtn.setTextColor(Color.parseColor("#FFFFFF"));
//                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
//                    cancelBtn.setTextColor(Color.parseColor("#000000"));
//                }
//                return false;
//            }
//        });


    }

    public int dip2px(int dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    //边框圆角效果
    private StateListDrawable getBackGroundColor() {
        GradientDrawable nonFocusedDrawable = new GradientDrawable();
        nonFocusedDrawable.setShape(GradientDrawable.RECTANGLE);
        nonFocusedDrawable.setColor(Color.parseColor("#ffffff"));
        nonFocusedDrawable.setCornerRadius(8);

        int[] colors = {Color.parseColor("#FF7700"),Color.parseColor("#FF9000")};
        GradientDrawable FocusedDrawable = new GradientDrawable();
        FocusedDrawable.setShape(GradientDrawable.RECTANGLE);
        FocusedDrawable.setColors(colors); //添加颜色组
        FocusedDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);//设置线性渐变
        FocusedDrawable.setOrientation(GradientDrawable.Orientation.RIGHT_LEFT);//设置渐变方向
        FocusedDrawable.setCornerRadius(8);

        StateListDrawable stateListDrawable = new StateListDrawable();
        //Non focused states
        stateListDrawable.addState(new int[]{-android.R.attr.state_focused, -android.R.attr.state_selected, -android.R.attr.state_pressed},
                nonFocusedDrawable);
        stateListDrawable.addState(new int[]{-android.R.attr.state_focused, android.R.attr.state_selected, -android.R.attr.state_pressed},
                nonFocusedDrawable);
        //Focused states
        stateListDrawable.addState(new int[]{android.R.attr.state_focused, -android.R.attr.state_selected, -android.R.attr.state_pressed},
                FocusedDrawable);
        stateListDrawable.addState(new int[]{android.R.attr.state_focused, android.R.attr.state_selected, -android.R.attr.state_pressed},
                FocusedDrawable);
        //Pressed
        stateListDrawable.addState(new int[]{android.R.attr.state_selected, android.R.attr.state_pressed},
                FocusedDrawable);
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed},
                FocusedDrawable);

        return stateListDrawable;
    }


}
