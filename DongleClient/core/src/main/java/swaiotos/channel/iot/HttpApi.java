package swaiotos.channel.iot;

import android.content.Context;
import android.util.Log;

import com.coocaa.sdk.entity.IMMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observer;
import swaiotos.channel.iot.callback.BindResult;
import swaiotos.channel.iot.callback.ConnectStatusListener;
import swaiotos.channel.iot.callback.DeviceCallback;
import swaiotos.channel.iot.callback.ResultListener;
import swaiotos.channel.iot.callback.UnBindResult;
import swaiotos.channel.iot.db.bean.Device;
import swaiotos.channel.iot.db.helper.DeviceHelper;
import swaiotos.channel.iot.entity.MessageData;
import swaiotos.channel.iot.http.HttpEngine;
import swaiotos.channel.iot.http.SignUtils;
import swaiotos.channel.iot.request.ReqDevices;
import swaiotos.channel.iot.request.ResRoomMsg;
import swaiotos.channel.iot.response.BindDeviceResp;
import swaiotos.channel.iot.response.DeviceDataResp;
import swaiotos.channel.iot.response.DeviceStatusResp;
import swaiotos.channel.iot.response.RoomOnlineResp;
import swaiotos.channel.iot.response.UnbindResp;

public class HttpApi {

    private static final String TAG = "HttpApi";

    private SkyServer skyServer;
    private Context mContext;
    private ConnectStatusListener mConnectListener;

    public HttpApi(SkyServer skyServer) {
        this.mContext = skyServer;
        this.skyServer = skyServer;
    }

    public void setConnectListener(ConnectStatusListener listener) {
        this.mConnectListener = listener;
    }

    public void reqBindDevice(final DeviceCallback callback) {
        Map<String, String> map = new HashMap<>();
        String accessToken = skyServer.getAccessToken();
        map.put("accessToken", accessToken);
        map.put("queryType", "3");
        HttpEngine.getBindDevice(SignUtils.signMap(map),
                new Observer<BindDeviceResp>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "reqBindDevice onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (callback != null) {
                            callback.onFail(-1, e.getMessage());
                        }
                    }

                    @Override
                    public void onNext(BindDeviceResp result) {
                        if (result.isSuccess()) {
                            List<Device> list = result.getData().getUserBindDeviceList();
                            skyServer.post(new Runnable() {
                                @Override
                                public void run() {
                                    DeviceHelper.instance().delAllDevice(mContext);
                                    if (list != null && list.size() > 0) {
                                        DeviceHelper.instance().insertOrUpdate(mContext, list);
                                    }
                                }
                            });

                            if (callback != null) {
                                callback.onSuccess(list);
                            }

                            if (mConnectListener != null) {
                                mConnectListener.onBindDevice(list);
                            }
                        } else {
                            if (callback != null) {
                                callback.onFail(result.getCode(), result.getMsg());
                            }
                        }
                    }
                });
    }


    public void bindDevice(String bindCode, BindResult callback) {
        String accessToken = skyServer.getAccessToken();
        HttpEngine.bindDevice(accessToken, bindCode, new Observer<DeviceDataResp>() {

            @Override
            public void onCompleted() {
                Log.d(TAG, "bindDevice onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                if (callback != null) {
                    callback.onFail(-1, e.getMessage());
                }
            }

            @Override
            public void onNext(DeviceDataResp result) {
                if (result.isSuccess()) {
                    final DeviceDataResp.DataBean dataBean = result.getData();
                    final Device bean = new Device(dataBean);

                    skyServer.post(new Runnable() {
                        @Override
                        public void run() {

                            DeviceHelper.instance().insertOrUpdate(mContext, bean);
                        }
                    });

                    if (callback != null) {
                        callback.onSuccess(bean);
                    }
                } else {
                    if (callback != null) {
                        callback.onFail(result.getCode(), result.getMsg());
                    }
                }


            }
        });
    }


    public void unbindDevice(final String targetSid, int deleteType, UnBindResult callback) {
        String accessToken = skyServer.getAccessToken();
        HttpEngine.unbindDevice(accessToken, targetSid, deleteType, new Observer<UnbindResp>() {

            @Override
            public void onCompleted() {
                Log.d(TAG, "unbindDevice onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                if (callback != null) {
                    callback.onFail(-1, e.getMessage());
                }
            }

            @Override
            public void onNext(UnbindResp result) {
                if (result.isSuccess()) {
                    skyServer.post(new Runnable() {
                        @Override
                        public void run() {
                            DeviceHelper.instance().delDeviceBySid(mContext, targetSid);
                        }
                    });

                    if (callback != null) {
                        callback.onSuccess(result.getData());
                    }
                } else {
                    if (callback != null) {
                        callback.onFail(result.getCode(), result.getMsg());
                    }
                }
            }
        });
    }


    public void refreshOnlineStatus(final DeviceCallback callback) {
        final List<Device> list = DeviceHelper.instance().toQueryDeviceList(mContext);
        if (list != null && list.size() > 0) {
            String[] sids = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                sids[i] = list.get(i).getZpLsid();
            }
            ReqDevices param = new ReqDevices();
            param.setSids(sids);

            HttpEngine.refreshOnlineStatus(param, new Observer<DeviceStatusResp>() {

                @Override
                public void onCompleted() {
                    Log.d(TAG, "refreshOnlineStatus onCompleted");
                }

                @Override
                public void onError(Throwable e) {
                    if (callback != null) {
                        callback.onFail(-1, e.getMessage());
                    }
                }

                @Override
                public void onNext(DeviceStatusResp result) {
                    if (result.isSuccess()) {
                        List<DeviceStatusResp.DataBean> tempList = result.getData();
                        for (Device item : list) {
                            for (DeviceStatusResp.DataBean data : tempList) {
                                if (item.getZpLsid().equals(data.getSid())) {
                                    item.setZpStatus(data.getOnlineStatus());
                                    break;
                                }
                            }
                        }
                        if (callback != null) {
                            callback.onSuccess(list);
                        }

                    } else {
                        if (callback != null) {
                            callback.onFail(result.getCode(), result.getMsg());
                        }
                    }
                }
            });
        } else {
            if (callback != null) {
                callback.onFail(-2, "设备列表为空");
            }
        }
    }


    public void sendBroadCastByHttp(IMMessage message, ResultListener callback) {
        String accessToken = skyServer.getAccessToken();
        String msgId = message.getId();

        MessageData msgData = new MessageData();
        msgData.setId(message.getId());
        msgData.setClient_source(message.getClientSource());
        msgData.setClient_target(message.getClientTarget());
        msgData.setExtra(message.getExtra());
        msgData.setReply(false);
        msgData.setContent(message.getContent());
        msgData.setType(message.getType().name());


        ResRoomMsg resRoomMsg = new ResRoomMsg(msgId, msgData);


        HttpEngine.sendBroadCastRoomMessage(accessToken, resRoomMsg, new Observer<RoomOnlineResp>() {
            @Override
            public void onCompleted() {
                Log.d(TAG, "sendBroadCastRoomMessage onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                if (callback != null) {
                    callback.onResult(-1, e.getMessage());
                }
            }

            @Override
            public void onNext(RoomOnlineResp result) {
                if (result.isSuccess()) {
                    RoomOnlineResp.DataBean data = result.getHostOnline();
                    int code = data.getHostOnline();

                    if (code == 1) {
                        callback.onResult(code, "host online");
                    } else {
                        callback.onResult(code, "host offline");
                    }
                } else {
                    if (callback != null) {
                        callback.onResult(result.getCode(), result.getMsg());
                    }
                }
            }
        });
    }

}
