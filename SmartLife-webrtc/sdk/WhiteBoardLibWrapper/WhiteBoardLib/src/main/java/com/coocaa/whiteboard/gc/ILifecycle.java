package com.coocaa.whiteboard.gc;

import android.content.Context;

/**
 * @Author: yuzhan
 */
public interface ILifecycle {
    void onCreate(Context context);
    void onPause();
    void onStart();
    void onStop();
    void onResume();
    void onDestroy();
    void onNewIntent();
}
