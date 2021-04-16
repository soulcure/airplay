package com.coocaa.whiteboard.client;

import com.coocaa.whiteboard.server.ClientInServerCanvasInfo;
import com.coocaa.whiteboard.server.ServerCanvasInfo;
import com.coocaa.whiteboard.server.WhiteBoardServerCmdInfo;

public interface WhiteBoardClientListener {
    /**
     * 和server端连接成功
     */
    void onConnectSuccess();

    /**
     * 和server端连接失败，经过几次重连尝试后最终失败
     * @param reason
     */
    void onConnectFail(String reason);

    /**
     * 和server端连接断开
     * @param reason
     */
    void onConnectFailOnce(String reason);

    /**
     * 断开连接
     */
    void onConnectClose();

    /**
     * 收到server端消息，一般不需要处理
     * @param msg
     */
    void onReceiveMsg(String msg);

    void onReceiveCmdInfo(WhiteBoardServerCmdInfo cmdInfo);

    /**
     * path发生变化，别的手机触发了绘制事件
     */
    void onRenderChanged(String renderData);

    /**
     * 画布变化
     */
    void onCanvasChanged(ServerCanvasInfo sInfo);

    /**
     * dongle端白板中断
     * @param isInterrupt 是否是被其他应用启动打断
     */
    void onWhiteBoardAborted(boolean isInterrupt);

    /**
     * dongle白板被重新拉到前台
     */
    void onWhiteBoardResumeFront();
}
