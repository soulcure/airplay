Android端：

基本状态来源于通道的连接回调返回值，客户端定义为：

private int mConnectState;
其中四个返回值定义为常量

public final static int CONNECT_NOTHING = 0;//未连接
public final static int CONNECT_SSE = 1;//sse连接 广域网功能可用
public final static int CONNECT_LOCAL = 2;//本地连接 局域网功能可用
public final static int CONNECT_BOTH = 3;//都连接 广域网、局域网功能都可用
其中公用的常用判断方法

//局域网功能是否可用
public boolean isSameWifi() {
       return mConnectState == CONNECT_LOCAL || mConnectState == CONNECT_BOTH;
}

//是否连接广域网或者局域网设备
public boolean isConnected() {
       return mConnectState > CONNECT_NOTHING;
}

//获取保存的历史回连设备
public Device getHistoryDevice() {
    Device device = null;
    String jsonStr = SpUtil.getString(mContext, HISTORY_DEVICE);
    Log.d(TAG, "getHistoryDevice: " + jsonStr);

    if (!TextUtils.isEmpty(jsonStr)) {
        device = new Gson().fromJson(jsonStr, new TypeToken<Device<TVDeviceInfo>>() {
        }.getType());
    }

    if (null != device
            && device.getLastConnectTime() > 0
            && System.currentTimeMillis() - device.getLastConnectTime() > 60 * 60 * 1000) {
        Log.d(TAG, "getHistoryDevice: 超过1小时限制");
        clearHistoryDevice();
        return null;
    }

    return device;
}

首页、分享页状态UI刷新
enum class STATUS {
    NOT_CONNECTED,         //未连接(扫码连接设备状态)
    CONNECTING,            //连接中
    CONNECTED,             //已连接
    CONNECT_NOT_SAME_WIFI, //已连接不在同一wifi
    CONNECT_ERROR          //连接错误
}

STATUS status;
switch (SSConnectManager.getInstance().getConnectState()) {
    case SSConnectManager.CONNECT_BOTH:
        //UI连接状态，功能都能正常使用
        status = STATUS.CONNECTED;
        break;
    case SSConnectManager.CONNECT_LOCAL:
        //UI连接状态，功能都能正常使用
        status = STATUS.CONNECTED;
        break;
    case SSConnectManager.CONNECT_SSE:
        //UI感叹号状态，支持值广域网功能
        status = STATUS.CONNECT_NOT_SAME_WIFI;
        break;
    case SSConnectManager.CONNECT_NOTHING:
        Device device = SSConnectManager.getInstance().getHistoryDevice();
        if (device == null) {
            //UI未连接状态
            status = STATUS.NOT_CONNECTED;
        } else {
            //UI显示感叹号，连接异常
            status = STATUS.CONNECT_ERROR;
        }
        break;
    default:
        //UI未连接状态
        status = STATUS.NOT_CONNECTED;
        break;
}
