package com.skyworth.smarthome_tv.smarthomeplugininterface;

import android.view.View;

/**
 * @ClassName: IViewBorderListener
 * @Author: XuZeXiao
 * @CreateDate: 2020/6/22 10:42
 * @Description:
 */
public interface IViewBoundaryCallback {
    /**
     * 当插件View内部的焦点在顶部，按上键时回调给宿主。
     *
     * @param leaveView 当前落焦的View
     * @return
     */
    boolean onTopBoundary(View leaveView);

    /**
     * 当插件View内部的焦点在底部，按下键时回调给宿主。
     *
     * @param leaveView 当前落焦的View
     * @return
     */
    boolean onDownBoundary(View leaveView);

    /**
     * 当插件View内部的焦点在最左边，按左键时回调给宿主。
     *
     * @param leaveView 当前落焦的View
     * @return
     */
    boolean onLeftBoundary(View leaveView);

    /**
     * 当插件View内部的焦点在最右边，按右键时回调给宿主。
     *
     * @param leaveView 当前落焦的View
     * @return
     */
    boolean onRightBoundary(View leaveView);

    /**
     * 当插件View内部按下返回键时
     *
     * @param leaveView 当前落焦的View
     * @param leaveView
     * @return
     */
    boolean onBackKey(View leaveView);
}
