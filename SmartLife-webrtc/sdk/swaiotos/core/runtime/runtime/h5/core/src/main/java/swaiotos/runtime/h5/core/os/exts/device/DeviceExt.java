package swaiotos.runtime.h5.core.os.exts.device;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import swaiotos.channel.iot.ccenter.CCenterManger;
import swaiotos.channel.iot.ccenter.CCenterMangerImpl;
import swaiotos.runtime.h5.H5CoreExt;
import swaiotos.runtime.h5.common.event.OnQrCodeCBData;
import swaiotos.runtime.h5.common.util.EmptyUtils;
import swaiotos.runtime.h5.common.util.LogUtil;
import swaiotos.runtime.h5.core.os.exts.utils.ExtLog;

/**
 * @ClassName: DeviceExt
 * @Author: AwenZeng
 * @CreateDate: 1/7/21
 * @Description:
 */
public class DeviceExt extends H5CoreExt implements IDeviceJsExt {
    public static final String NAME = "device";

    private static final String TAG = "DeviceExt";

    private static H5CoreExt ext = null;
    private Context mContext;
    private final Set<String> listenerIds = new TreeSet<>();
    protected static Vibrator vibrator;

    public static synchronized H5CoreExt get(Context context) {
        if (ext == null) {
            ext = new DeviceExt(context);
        }
        return ext;
    }

    public DeviceExt(Context context) {
        mContext = context;
        Log.d(TAG, "new DeviceExt : " + context);
        EventBus.getDefault().register(this);
    }

    String scanQrcodeId = null;

    @JavascriptInterface
    @Override
    public void scanQrcode(String id) {
        ExtLog.d(TAG, "scanQrcode id=" + id);
        scanQrcodeId = id;
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        EventBus.getDefault().post(new RequestScanQrCodeEvent());
    }

    /**
     * 是否显示dangleTV的二维码
     *
     * @param id     js请求id
     * @param isShow 是否显示，"true":显示  "false":隐藏
     */
    @JavascriptInterface
    @Override
    public void enableDeviceQrcode(String id, String isShow) {
        Log.d(TAG, "enableDeviceQrcode:id=" + id + " isShow=" + isShow);
        if (EmptyUtils.isEmpty(isShow)) {
            return;
        }
        try {
            Intent qrBroadCastIntent = new Intent();
            qrBroadCastIntent.setAction("swaiotos.channel.iot.action.qrshow");
            qrBroadCastIntent.setPackage("swaiotos.channel.iot");
            qrBroadCastIntent.putExtra("show", isShow.equals("true") ? true : false);//是否显示全局二维码
            mContext.sendBroadcast(qrBroadCastIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取当前dangle设备二维码字符串
     *
     * @param id  js请求id
     * @param url 加载网页地址
     * @return
     */
    @JavascriptInterface
    @Override
    public void getDeviceQrcodeString(String id, String url) {
        Log.d(TAG, "getDeviceQrcodeString:id=" + id + " url=" + url);
        Map<String, String> stringStringMap = new HashMap<>();
        stringStringMap.put("applet", url);
        CCenterMangerImpl.getCCenterManger(mContext.getApplicationContext()).getCCodeString(stringStringMap, new CCenterManger.CCenterListener() {
            @Override
            public void ccodeCallback(String code) {
                Log.d(TAG, "ccodeCallback code:" + code);
                JSONObject data = JSON.parseObject(code);
                String bind_code = data.getString("showCode");
                String qr = data.getString("qrCode");
                try {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("bindCode", bind_code);
                    map.put("qrCode", qr);
                    String qrCodeInfo = JSON.toJSONString(map);
                    native2js(id, RET_SUCCESS, qrCodeInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @JavascriptInterface
    @Override
    public void addQrcodeChangedListener(String id) {
        synchronized (listenerIds) {
            Log.d(TAG, "addQrcodeChangedListener(), id: " + id);
            listenerIds.add(id);
        }
    }

    @JavascriptInterface
    @Override
    public void removeQrcodeChangedListener(String id, String listenerId) {
        synchronized (listenerIds) {
            Log.d(TAG, "removeQrcodeChangedListener(), id: " + id);
            listenerIds.remove(id);
        }
    }

    @JavascriptInterface
    public void vibrateDevice(String id, String json) {
        Log.d(TAG, "vibrateDevice, id: " + id + ", json=" + json);
        if(vibrator == null) {
            vibrator = (Vibrator) context.getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                JSONObject jsonObject = JSON.parseObject(json);
                long ms = 100;
                if(jsonObject.containsKey("ms")) {
                    ms = Long.parseLong(jsonObject.getString("ms"));
                }
                int swing = VibrationEffect.DEFAULT_AMPLITUDE;
                if(jsonObject.containsKey("swing")) {
                    swing = Integer.parseInt(jsonObject.getString("swing"));
                }
                vibrator.vibrate(VibrationEffect.createOneShot(ms, swing));
            } catch (Exception e) {
                vibrator.vibrate(100L);
            }
        } else {
            vibrator.vibrate(100L);
        }

        native2js(id, RET_SUCCESS, new JSONObject().toString());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onScanQrCodeResult(ScanQrCodeEvent event) {
        ExtLog.d("onScanQrCodeResult : " + event);
        if (!TextUtils.isEmpty(scanQrcodeId)) {
            JSONObject object = new JSONObject();
            object.put("url", event.getResult());
            native2js(scanQrcodeId, RET_SUCCESS, JSON.toJSONString(object));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnGetQrCodeCallBack(OnQrCodeCBData qrCode) {
        Log.d(TAG, "OnGetQrCodeCallBack: " + qrCode.toString());
        if (qrCode == null) {
            LogUtil.androidLog("OnGetQrCodeCallBack event == null");
            return;
        }
        if (EmptyUtils.isEmpty(listenerIds)) {
            return;
        }
        try {
            HashMap<String, Object> map = new HashMap<>();
            map.put("bindCode", qrCode.bindCode);
            map.put("qrCode", qrCode.qrCode);
            String qrCodeInfo = JSON.toJSONString(map);
            for (String id : listenerIds) {
                native2js(id, ON_RECEIVE, qrCodeInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void detach(Context context) {
        Log.d(TAG, "DeviceExt detach : " + context);
        try {
            throw new RuntimeException("print detach");
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.detach(context);
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }
}
