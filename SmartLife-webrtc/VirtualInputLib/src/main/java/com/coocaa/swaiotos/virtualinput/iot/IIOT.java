package com.coocaa.swaiotos.virtualinput.iot;

import android.view.MotionEvent;

/**
 * @Author: yuzhan
 */
public interface IIOT {
    void sendCmd(String cmd, String type, String paramsJson, String clientId);
    /**
     * reason:由于沟通问题导致使用该方法的业务CMD的param嵌套CMD
     * @param cmd
     * @param type
     * @param paramsJson
     * @param clientId
     * @param owner  业务拥有者
     */
    void sendCmd(String cmd, String type, String paramsJson, String clientId,String owner);

    /**
     * reason:通用的方法
     * @param cmd
     * @param clientId
     * @param owner
     */
    void sendCmdCommon(String clientId, String cmd, String owner);
    void sendCmdCommon(String clientId, String cmd, String owner, int protoVersion );

    void sendKeyEvent(int keyCode, int action);
    void sendTouchEvent(int action, float x, float y);

    void sendTouchEvent(MotionEvent event);
}
