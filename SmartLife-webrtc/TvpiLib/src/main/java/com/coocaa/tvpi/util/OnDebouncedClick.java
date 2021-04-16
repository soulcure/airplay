package com.coocaa.tvpi.util;

import android.view.View;

import java.util.concurrent.TimeUnit;

public class OnDebouncedClick implements View.OnClickListener {
    private View.OnClickListener onClickListener;
    private long debounceIntervalInMillis;
    private long previousClickTimestamp;

    public OnDebouncedClick(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
        this.debounceIntervalInMillis = 300;
    }

    @Override
    public void onClick(View view) {
        long currentClickTimestamp = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
        if (previousClickTimestamp == 0
                || currentClickTimestamp - previousClickTimestamp >= debounceIntervalInMillis) {
            //update click timestamp
            previousClickTimestamp = currentClickTimestamp;
            onClickListener.onClick(view);
        }
    }
}
