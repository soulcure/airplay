package com.coocaa.tvpi.util;

import android.os.SystemClock;

/**
 * @Author: yuzhan
 */
public class FastClick {
    private long interval = 300; //300ms间隔保护
    private long lastTime = 0;

    public void setInterval(long i) {
        this.interval = i;
    }

    public boolean isFaskClick() {
        if(lastTime > 0 && (SystemClock.uptimeMillis() - lastTime < interval)) {
            return true;
        } else {
            lastTime = SystemClock.uptimeMillis();
            return false;
        }
    }
}
