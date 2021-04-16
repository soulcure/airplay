package com.coocaa.tvpi.module.reversescreen.view;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coocaa.tvpilib.R;

/**
 * 同屏退出按钮
 * Created by songxing on 2020/4/21
 */
public class ExitView extends LinearLayout {
    private Context context;
    private LinearLayout root;
    private TextView tvExit;
    private ExitListener exitListener;
    private CountDownTimer timer;
    private boolean isNeedConfirm = true;


    public ExitView(Context context) {
        this(context, null, 0);
    }

    public ExitView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExitView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView();
    }

    private void initView() {
        LayoutInflater.from(context).inflate(R.layout.layout_reverse_exit, this, true);
        root = findViewById(R.id.root);
        tvExit = findViewById(R.id.tv_reverse_exit);
        tvExit.setVisibility(GONE);

        root.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNeedConfirm) {
                    isNeedConfirm = false;
                    updateUI();
                    startCounter();
                } else {
                    stopCounter();
                    if (exitListener != null) {
                        exitListener.onExitClick();
                    }
                }
            }
        });
    }

    private void updateUI() {
        tvExit.setVisibility(isNeedConfirm ? GONE : VISIBLE);
    }


    private void startCounter() {
        if (timer == null) {
            timer = new CountDownTimer(3000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    Log.d("exit", "onTick: ");
                }

                @Override
                public void onFinish() {
                    Log.d("exit", "onFinish: ");
                    isNeedConfirm = true;
                    updateUI();
                }
            };
        }
        timer.start();
    }

    private void stopCounter() {
        if (timer != null) {
            timer.cancel();
        }
    }


    public void setOnExitClickListener(ExitListener exitListener) {
        this.exitListener = exitListener;
    }

    public interface ExitListener {
        void onExitClick();
    }
}
