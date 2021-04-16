package com.skyworth.smarthome_tv.smarthomeplugininterface;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@IntDef({ShortcutState.SHORTCUT_EXPAND, ShortcutState.SHORTCUT_SHRINK})
public @interface ShortcutState {
    /**
     * 内容展开到全屏
     */
    int SHORTCUT_EXPAND = 1;
    /**
     * 内容收缩到底部初始位置
     */
    int SHORTCUT_SHRINK = 2;
}
