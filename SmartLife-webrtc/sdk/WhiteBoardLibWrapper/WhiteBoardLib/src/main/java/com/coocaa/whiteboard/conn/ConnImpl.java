package com.coocaa.whiteboard.conn;

/**
 * @Author: yuzhan
 */
public class ConnImpl implements IConn{

    String server;

    @Override
    public void setServerAddress(String server) {
        this.server = server;
    }

    @Override
    public void connect() {

    }

    @Override
    public void close() {

    }
}
