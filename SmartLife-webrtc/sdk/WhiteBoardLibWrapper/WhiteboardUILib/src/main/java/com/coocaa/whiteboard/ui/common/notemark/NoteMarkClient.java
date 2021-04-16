package com.coocaa.whiteboard.ui.common.notemark;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;

import com.coocaa.define.SvgConfig;
import com.coocaa.svg.note.NoteMarkClientSurfaceViewRender;
import com.coocaa.svg.note.NoteMarkClientSvgViewRender;
import com.coocaa.svg.render.badlogic.SvgViewRender;
import com.coocaa.whiteboard.IWhiteBoard;
import com.coocaa.whiteboard.client.WhiteBoardClient;
import com.coocaa.whiteboard.notemark.NoteMarkClientSocket;

import swaiotos.sensor.client.data.ClientBusinessInfo;
import swaiotos.sensor.data.AccountInfo;

public class NoteMarkClient extends WhiteBoardClient {

    public NoteMarkClient(Context context) {
        super(context);
        SvgConfig.changeColor("#00000000");
    }

    protected String tag() {
        return "NMClient";
    }

    protected ClientBusinessInfo getBusinessInfo(Context context) {
        return NoteMarkClientSocket.INSTANCE.getBusinessInfo(context);
    }

    @Override
    public void start() {
        NoteMarkClientSocket.INSTANCE.setCallback(callback);
        NoteMarkClientSocket.INSTANCE.start();
        NoteMarkClientSocket.INSTANCE.connect();
//        client.connect(serverUrl);
    }

    @Override
    public void stop() {
        NoteMarkClientSocket.INSTANCE.stop();
//        client.stop();
        NoteMarkClientSocket.INSTANCE.setCallback(null);
    }

    @Override
    public void setAccountInfo(AccountInfo accountInfo, String clientSSID) {
        NoteMarkClientSocket.INSTANCE.init(context, accountInfo, clientSSID);
//        client = new SensorClient(context, clientBusinessInfo, accountInfo);
        infoManager.setAccountInfo(accountInfo);
        infoManager.setId(accountInfo.mobile);
        NoteMarkClientSocket.INSTANCE.setCallback(callback);
//        client.setCallback(callback);
    }

    @Override
    public void onCreate(Context context) {
        this.context = context;
        if (renderView == null) {
            renderView = useSurfaceView ? new NoteMarkClientSurfaceViewRender(context) : new NoteMarkClientSvgViewRender(context);
        }
    }

    @Override
    public void onStop() {
        NoteMarkClientSocket.INSTANCE.stop();
//        CacheUtil.saveCache(context, getCurrentSvgString());
//        client.stop();
    }

    @Override
    protected void send(String text) {
        NoteMarkClientSocket.INSTANCE.send(text);
    }

}
