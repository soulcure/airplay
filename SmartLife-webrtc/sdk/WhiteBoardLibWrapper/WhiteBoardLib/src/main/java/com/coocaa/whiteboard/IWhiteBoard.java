package com.coocaa.whiteboard;

import android.view.MotionEvent;
import android.view.View;

import com.coocaa.svg.data.SvgData;
import com.coocaa.whiteboard.client.ClientCanvasInfo;
import com.coocaa.whiteboard.client.WhiteBoardClientListener;
import com.coocaa.whiteboard.conn.IConn;
import com.coocaa.whiteboard.data.CEraseInfo;
import com.coocaa.whiteboard.data.CPaintInfo;
import com.coocaa.whiteboard.gc.ILifecycle;

/**
 * @Author: yuzhan
 */
public interface IWhiteBoard extends ILifecycle, IConn {
    View getView();

    IWhiteBoard setPaintInfo(CPaintInfo paintInfo);

    IWhiteBoard setEraseInfo(CEraseInfo eraseInfo);

    /**
     * 全量绘制
     * @param renderData
     */
    IWhiteBoard render(String renderData);

    IWhiteBoard renderDiff(String diffRenderData);

    IWhiteBoard renderDiffXml(String diffXmlRenderData);

//    /**
//     * 全量数据
//     * @param data
//     * @return
//     */
//    IWhiteBoard setSvgData(SvgData data);

    /**
     * 差量绘制，目前先用MotionEvent代替，方便UI端调用，后续应该会调整
     * @param event
     */
    IWhiteBoard onMotionEvent(MotionEvent event);

    /**
     * 橡皮擦
     * @param event
     * @return
     */
    IWhiteBoard onEraseMotionEvent(MotionEvent event);

    IWhiteBoard registerListener(WhiteBoardClientListener listener);

    void test();

    /**
     * 清空所有画布，包括自己和别人的
     */
    void clearWhiteBoard(boolean exit);

    boolean undo();

    boolean redo();

    void offsetAndScale(float x, float y, float scale,float cx,float cy);
}
