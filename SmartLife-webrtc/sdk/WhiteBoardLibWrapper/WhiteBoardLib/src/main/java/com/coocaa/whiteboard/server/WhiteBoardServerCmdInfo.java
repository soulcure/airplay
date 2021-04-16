package com.coocaa.whiteboard.server;

import com.alibaba.fastjson.JSON;

import java.util.HashMap;
import java.util.Map;

import swaiotos.sensor.data.ServerCmdInfo;

public class WhiteBoardServerCmdInfo extends ServerCmdInfo {

    public String cId;
    public String sId;
    public String content;
    public String cmd;
    public ServerCanvasInfo sCanvas;
    public ClientInServerCanvasInfo cCanvas;
    public boolean zip = false;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public final static String CMD_SERVER_NOTIFY_SYNC_INIT_DATA = "server-notify-sync-init";

    public final static String CMD_SERVER_NOTIFY_DIFF_PATH = "server-notify-diff-path";

    public final static String CMD_SERVER_NOTIFY_CLEAR_CANVAS = "server-notify-clear-canvas";

    public final static String CMD_SERVER_NOTIFY_ABORT_BY_OTHERAPPS = "server-notify-abort-by-other";

    //白板被中断退出后，重新恢复到前台
    public final static String CMD_SERVER_NOTIFY_RESUME_TO_FRONT = "server-notify-resume-front";

    public final static String CMD_SERVER_RESPONSE_STATUS = "response-status";
    public final static String CMD_SERVER_RESPONSE_SERVER_WHITEBOARD_DATA = "response-server-data";
    public final static String CMD_SERVER_RESPONSE_USE_CLIENT_DATA = "response-use-client-data";
    public final static String CMD_SERVER_NOTIFY_WHITEBOARD_DATA_CHANGED = "server-notify-data-changed";
}
