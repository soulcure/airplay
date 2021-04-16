package com.coocaa.whiteboard.conn;

/**
 * @Author: yuzhan
 */
public interface IConn {
    void setServerAddress(String server);
    void connect();
    void close();
}
