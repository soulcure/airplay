package com.coocaa.whiteboard.client;

import swaiotos.sensor.data.ClientCmdInfo;

public class WhiteBoardClientCmdData extends ClientCmdInfo {

    public boolean zip = false;//数据是否压缩
    public boolean isXml = false;

    public final static String CMD_CLIENT_REQUEST_STATUS = "request-status";
    public final static String CMD_CLIENT_REQUEST_SERVER_DATA = "request-server-data";
    public final static String CMD_CLIENT_REQUEST_USE_CLIENT_DATA = "request-use-client-data";
    public final static String CMD_CLIENT_SEND_DIFF_PATH = "c-send-diff-path";
    public final static String CMD_CLIENT_SEND_CANVAS_MOVE = "c-send-canvas-move";
    public final static String CMD_CLIENT_SEND_CLEAR_WHITEBOARD_ALL = "c-clear-canvas-all";
    public final static String CMD_CLIENT_SEND_CLEAR_WHITEBOARD_ALL_AND_EXIT = "c-clear-canvas-all-and-exit";
    public final static String CMD_CLIENT_REQUEST_SCREENSHOT = "c-request-screenshot";
}
