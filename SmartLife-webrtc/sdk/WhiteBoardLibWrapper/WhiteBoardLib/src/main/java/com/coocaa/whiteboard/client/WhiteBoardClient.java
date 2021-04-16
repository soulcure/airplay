package com.coocaa.whiteboard.client;

import android.content.Context;
import android.graphics.Matrix;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.FrameLayout;

import com.alibaba.fastjson.JSON;
import com.coocaa.define.CPath;
import com.coocaa.define.SvgConfig;
import com.coocaa.svg.ClientSurfaceViewRender;
import com.coocaa.svg.data.SvgData;
import com.coocaa.svg.data.SvgNode;
import com.coocaa.svg.render.badlogic.SvgViewRender;
import com.coocaa.whiteboard.IWhiteBoard;
import com.coocaa.whiteboard.server.WhiteBoardServerCmdInfo;
import com.coocaa.whiteboard.utils.ZipUtils;
import com.coocaa.whiteboard.view.WhiteBoard;

import java.lang.reflect.Field;

import swaiotos.sensor.client.data.ClientBusinessInfo;
import swaiotos.sensor.connect.IConnectCallback;
import swaiotos.sensor.data.AccountInfo;
import swaiotos.sensor.data.ClientCmdInfo;
import swaiotos.sensor.mgr.InfoManager;

public class WhiteBoardClient extends WhiteBoard {
    ClientBusinessInfo clientBusinessInfo;
    protected Context context;
    protected InfoManager infoManager;
    private float downX = 0;
    private float downY = 0;
    private float tx = 0;
    private float ty = 0;
    float preScale = 1f;
    ScaleGestureDetector mScaleGestureDetector;

    //todo 有时候开始缩放不流畅
    public WhiteBoardClient(Context context) {
        TAG = tag();
        useSurfaceView = false;
        SvgConfig.changeToClient();
        clientBusinessInfo = getBusinessInfo(context);
        onCreate(context);
        renderView.renderXml(new SvgData().toSvgString());
        infoManager = new InfoManager();
        infoManager.setBusinessInfo(clientBusinessInfo);
        mScaleGestureDetector = new ScaleGestureDetector(
                context,
                new ScaleGestureDetector.OnScaleGestureListener() {
                    @Override
                    public boolean onScale(ScaleGestureDetector detector) {
                        scale *= detector.getScaleFactor();
//                        scale*=(detector.getCurrentSpan()/detector.getPreviousSpan());
                        if (scale > 5 || scale < 0.1) {
                            scale = preScale;
                            return true;
                        }
                        getRoot().setScaleX(scale);
                        getRoot().setScaleY(scale);
                        preScale = scale;
                        return false;
                    }

                    @Override
                    public boolean onScaleBegin(ScaleGestureDetector detector) {
                        float newX = detector.getFocusX();
                        float newY = detector.getFocusY();
//                        float newX = midPntX;
//                        float newY = midPntY;
                        getRoot().setTranslationX(getRoot().getTranslationX() + (getRoot().getPivotX() - newX) * (1 - getRoot().getScaleX()));
                        getRoot().setTranslationY(getRoot().getTranslationY() + (getRoot().getPivotY() - newY) * (1 - getRoot().getScaleY()));
                        getRoot().setPivotX(newX);
                        getRoot().setPivotY(newY);
                        return true;
                    }

                    @Override
                    public void onScaleEnd(ScaleGestureDetector detector) {
//                        isScale = false;
                    }
                });
        fixMinSpan();
    }

    protected String tag() {
        return "WBClient";
    }

    protected ClientBusinessInfo getBusinessInfo(Context context) {
        return WhiteBoardClientSocket.INSTANCE.getBusinessInfo(context);
    }

    private void fixMinSpan() {
        try {
            Field mField = mScaleGestureDetector.getClass().getDeclaredField("mMinSpan");
            mField.setAccessible(true);
            mField.set(mScaleGestureDetector, 20);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    protected View getRoot() {
        return (View) getView().getParent();
    }

    public void start() {
        WhiteBoardClientSocket.INSTANCE.setCallback(callback);
        WhiteBoardClientSocket.INSTANCE.start();
        WhiteBoardClientSocket.INSTANCE.connect();
//        client.connect(serverUrl);
    }

    public void stop() {
        WhiteBoardClientSocket.INSTANCE.stop();
//        client.stop();
        WhiteBoardClientSocket.INSTANCE.setCallback(null);
    }

    public void setAccountInfo(AccountInfo accountInfo) {
        setAccountInfo(accountInfo, null);
    }

    public void setAccountInfo(AccountInfo accountInfo, String clientSSID) {
        WhiteBoardClientSocket.INSTANCE.init(context, accountInfo, clientSSID);
//        client = new SensorClient(context, clientBusinessInfo, accountInfo);
        infoManager.setAccountInfo(accountInfo);
        infoManager.setId(accountInfo.mobile);
        WhiteBoardClientSocket.INSTANCE.setCallback(callback);
//        client.setCallback(callback);
    }

    @Override
    public void onCreate(Context context) {
        this.context = context;
        if (renderView == null) {
            renderView = useSurfaceView ? new ClientSurfaceViewRender(context) : new SvgViewRender(context);
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        WhiteBoardClientSocket.INSTANCE.stop();
//        CacheUtil.saveCache(context, getCurrentSvgString());
//        client.stop();
    }

    @Override
    public void test() {
        Log.d(TAG, "print svg data....");
        String svgString = getCurrentSvgString();
        Log.d(TAG, svgString);
        log(svgString);

//        ClientCmdInfo info = ClientCmdInfo.build(infoManager, WhiteBoardClientCmdData.CMD_CLIENT_REQUEST_USE_CLIENT_DATA);
//        info.zip = true;
//        info.content = ZipUtils.zipString(svgString);
//
//        Log.d(TAG, "send svg data : " + info);
//        try {
//            WhiteBoardClientSocket.INSTANCE.send(JSON.toJSONString(info));
////            client.send(JSON.toJSONString(info));
//        } catch (Exception e) {
//            Log.d(TAG, "test prase send svg data fail." + e.toString());
//            e.printStackTrace();
//        }
    }


    private void onClientCanvasMove() {
        ClientCmdInfo info = ClientCmdInfo.build(infoManager, WhiteBoardClientCmdData.CMD_CLIENT_SEND_CANVAS_MOVE);
        String moveString = JSON.toJSONString(svgCanvasMoveInfo);
        info.content = moveString;
        Log.d(TAG, "start send canvas move data : " + moveString);
        try {
            send(JSON.toJSONString(info));
        } catch (Exception e) {
            Log.d(TAG, "send canvas move data fail:" + e.toString());
            e.printStackTrace();
        }
    }

    protected void send(String text) {
        WhiteBoardClientSocket.INSTANCE.send(text);
    }

    @Override
    protected void onPathFinish(CPath path, boolean isErase) {
        //true 是兼容就方案
        SvgNode node = renderView.renderDiff(pathWriter.pathString(path), true);
        SvgNode serverNode = null;
        if (!serverPath.isEmpty()){
            serverNode  = renderView.parseServerNode(pathWriter.pathString(serverPath));
        }

        node.owner = infoManager.getId();

//        String data = JSON.toJSONString(node);
//        SvgPathNode clone  = JSON.parseObject(data, SvgPathNode.class);


//        if (clone instanceof SvgPathNode){
//            ((SvgPathNode) clone).canvasInfo.x = x;
//            ((SvgPathNode) clone).canvasInfo.y =y;
//            ((SvgPathNode) clone).canvasInfo.scale =scale;
//        }
//
//        String nodeSvgString = clone.toSvgString();
        String nodeSvgString = serverNode != null ?
                serverNode.toSvgString():node.toSvgString();


        Log.d(TAG, "start send diff path data : " + nodeSvgString);
//        test();
        ClientCmdInfo info = ClientCmdInfo.build(infoManager, WhiteBoardClientCmdData.CMD_CLIENT_SEND_DIFF_PATH);
        info.content = ZipUtils.zipString(nodeSvgString);

        info.zip = true;
        info.addExtra("format", "xml");
        info.addExtra("cCanvas", JSON.toJSONString(clientBusinessInfo));
        try {
            send(JSON.toJSONString(info));
//            client.send(JSON.toJSONString(info));
        } catch (Exception e) {
            Log.d(TAG, "send diff path data fail." + e.toString());
            e.printStackTrace();
        }
    }


    @Override
    public void clearWhiteBoard(boolean exit) {
        Log.d(TAG, "clearWhiteBoard, with exit=" + exit);
        renderView.clear();
        ClientCmdInfo info = ClientCmdInfo.build(infoManager, exit ? WhiteBoardClientCmdData.CMD_CLIENT_SEND_CLEAR_WHITEBOARD_ALL_AND_EXIT : WhiteBoardClientCmdData.CMD_CLIENT_SEND_CLEAR_WHITEBOARD_ALL);
        try {
            send(JSON.toJSONString(info));
        } catch (Exception e) {
            Log.d(TAG, "send clearWhiteBoard fail : " + e.toString());
            e.printStackTrace();
        }
    }

    public void log(String msg) {
        int max_str_length = 2001 - TAG.length();
        //大于4000时
        while (msg.length() > max_str_length) {
            Log.i(TAG, msg.substring(0, max_str_length));
            msg = msg.substring(max_str_length);

        }

        Log.d(TAG, msg);
    }

    private void onDiffPath(String svgString) {
        Log.d(TAG, "onDiffPath");
        renderView.renderDiff(svgString);
    }

    private void onDiffXmlPath(String svgString) {
        Log.d(TAG, "onDiffXmlPath");
        renderView.renderDiffXml(svgString);
    }

    private float midPntX, midPntY;
    private boolean allowGestrue = true;

    public void allowGesture(boolean allow) {
        allowGestrue = allow;
    }

    private Matrix matrix;

    public void setMatrix(Matrix matrix) {
        this.matrix = matrix;
    }

    protected IWhiteBoard onMotionEvent(MotionEvent event, boolean isErase) {
        MotionEvent serverEvent = null;
        if (matrix != null) {
            serverEvent = MotionEvent.obtain(event);
            serverEvent.transform(matrix);
        }
        if (allowGestrue)
            mScaleGestureDetector.onTouchEvent(event);
        if (event.getPointerCount() > 1) {
            midPntX = (event.getX(0) + event.getX(1)) / 2;
            midPntY = (event.getY(0) + event.getY(1)) / 2;
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                moveType = 1;
                downX = event.getRawX();
                downY = event.getRawY();
                clientPath.reset();
                serverPath.reset();
                float x = event.getX();
                float y = event.getY();
                clientPath.start(x, y);

                if (serverEvent != null) {
                    serverPath.start(serverEvent.getX(), serverEvent.getY());
                }
                resetCanvasMoveInfo();
                break;
            case MotionEvent.ACTION_CANCEL:
                moveType = 1;
                downX = event.getRawX();
                downY = event.getRawY();
                clientPath.reset();
                serverPath.reset();
                clientPath.start(event.getX(), event.getY());
                if (serverEvent != null) {
                    serverPath.start(serverEvent.getX(), serverEvent.getY());
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                moveType = 2;
                break;
            case MotionEvent.ACTION_MOVE:
                if (moveType == 2 && event.getPointerCount() > 1 && allowGestrue) {
                    ty = event.getRawY() - downY;
                    tx = event.getRawX() - downX;
                    //下
//                    getRoot().offsetTopAndBottom((int) ty);
//                    getRoot().offsetLeftAndRight((int) tx);
                    calcBound(tx, ty);
                    downX = event.getRawX();
                    downY = event.getRawY();
                } else if (event.getPointerCount() == 1) {
                    x = event.getX();
                    y = event.getY();
                    clientPath.moveTo(x, y);
                    if (serverEvent != null)
                        serverPath.moveTo(serverEvent.getX(), serverEvent.getY());
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
                if (moveType == 1) {
                    clientPath.end(event.getX(), event.getY());
                    if (serverEvent != null) {
                        serverPath.end(serverEvent.getX(), serverEvent.getY());
                    }
                    onPathFinish(clientPath, isErase);
                }
                break;
        }
        return this;
    }

    private void calcBound(float tx, float ty) {
        FrameLayout.LayoutParams mParams = (FrameLayout.LayoutParams) getRoot().getLayoutParams();
        mParams.leftMargin += tx;
        mParams.topMargin += ty;
        getRoot().setLayoutParams(mParams);
    }

    public void setInitData(WhiteBoardServerCmdInfo info) {
        String svgString = info.content;
        if (!TextUtils.isEmpty(info.content) && info.zip) {
            svgString = ZipUtils.unzipString(svgString);
        }
        if (WhiteBoardServerCmdInfo.CMD_SERVER_NOTIFY_SYNC_INIT_DATA.equals(info.cmd)) {
            if (!TextUtils.isEmpty(svgString)) {
                renderView.renderXml(svgString);
            }
            listener.onCanvasChanged(info.sCanvas);
        }
    }

    protected IConnectCallback callback = new IConnectCallback() {
        @Override
        public void onSuccess() {
            Log.d(TAG, "onSuccess");
            if (listener != null) {
                listener.onConnectSuccess();
            }
        }

        @Override
        public void onFail(String reason) {
            Log.d(TAG, "onFail : " + reason);
            if (listener != null) {
                listener.onConnectFail(reason);
            }
        }

        @Override
        public void onFailOnce(String reason) {
            Log.d(TAG, "onFailOnce : " + reason);
            if (listener != null) {
                listener.onConnectFailOnce(reason);
            }
        }

        @Override
        public void onClose() {
            Log.d(TAG, "onClose : ");
            if (listener != null) {
                listener.onConnectClose();
            }
        }

        @Override
        public void onMessage(String msg) {
            Log.d(TAG, "收到Server消息 : " + msg);
            try {
                WhiteBoardServerCmdInfo info = JSON.parseObject(msg, WhiteBoardServerCmdInfo.class);
//                JSONObject jsonObject = JSON.parseObject(msg);
//                String cmd = jsonObject.getString("cmd");
                String cmd = info.cmd;
                String svgString = info.content;
                if (!TextUtils.isEmpty(info.content) && info.zip) {
                    svgString = ZipUtils.unzipString(svgString);
                }
                if (WhiteBoardServerCmdInfo.CMD_SERVER_NOTIFY_SYNC_INIT_DATA.equals(cmd)) {
                    Log.d(TAG, "receive server init svgData : " + svgString);
                    if (!TextUtils.isEmpty(svgString)) {
                        renderView.renderXml(svgString);
                    }
                    listener.onCanvasChanged(info.sCanvas);
                } else if (WhiteBoardServerCmdInfo.CMD_SERVER_NOTIFY_DIFF_PATH.equals(cmd)) {
                    Log.d(TAG, "this client cid=" + infoManager.getId());
                    if ("xml".equals(info.getExtra("format"))) {
                        onDiffXmlPath(svgString);
                    } else {
                        onDiffPath(svgString);
                    }
                } else if (WhiteBoardServerCmdInfo.CMD_SERVER_NOTIFY_ABORT_BY_OTHERAPPS.equals(cmd)) {
                    Log.d(TAG, "receive server aborted by other apps");
                    if (listener != null) {
                        listener.onWhiteBoardAborted(true);
                    }
                } else if(WhiteBoardServerCmdInfo.CMD_SERVER_NOTIFY_CLEAR_CANVAS.equals(cmd)) {
                    Log.d(TAG, "receive server clear canvas.");
                    renderView.clear();
                } else if(WhiteBoardServerCmdInfo.CMD_SERVER_NOTIFY_RESUME_TO_FRONT.equals(cmd)) {
                    Log.d(TAG, "receive server resume bring to front.");
                    if (listener != null) {
                        listener.onWhiteBoardResumeFront();
                    }
                } else {
                    if (listener != null) {
                        listener.onReceiveCmdInfo(info);
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "parse cmd fail : " + e.toString());
                e.printStackTrace();
            }
            if (listener != null) {
                listener.onReceiveMsg(msg);
            }
        }
    };
}
