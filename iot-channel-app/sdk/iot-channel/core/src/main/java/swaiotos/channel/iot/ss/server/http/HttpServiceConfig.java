package swaiotos.channel.iot.ss.server.http;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

import swaiotos.channel.iot.ss.device.RobotDeviceInfo;
import swaiotos.channel.iot.ss.server.utils.Constants;
import swaiotos.channel.iot.ss.server.utils.MACUtils;
import swaiotos.channel.iot.ss.SSChannelService;
import swaiotos.channel.iot.ss.SSContext;
import swaiotos.channel.iot.ss.device.Device;
import swaiotos.channel.iot.ss.device.DeviceInfo;
import swaiotos.channel.iot.ss.device.PadDeviceInfo;
import swaiotos.channel.iot.ss.device.PhoneDeviceInfo;
import swaiotos.channel.iot.ss.device.TVDeviceInfo;


public class HttpServiceConfig {

    private static SSContext mSSContext = null;

    public static void init(SSContext connect) {
        mSSContext = connect;
    }

    public static final SessionHeaderLoader HEADER_LOADER = new SessionHeaderLoader();

    public static class SessionHeaderLoader {
        Map<String, String> DEFAULT_HEADERS = null;

        private Map<String, String> loadHeader() {
            Log.d("TAG","XXXXXXXXXXXXX1111");
            DEFAULT_HEADERS = new HashMap<>();
            DEFAULT_HEADERS.put(Constants.COOCAA_MAC, getMac(SSChannelService.getContext()));
            DEFAULT_HEADERS.put(Constants.COOCAA_CVERSION,""+Constants.getVersionCode(SSChannelService.getContext()));
            DeviceInfo.TYPE type = mSSContext.getDeviceInfo().type();
            Device d = null;
            switch (type) {
                case TV:
                    TVDeviceInfo info = (TVDeviceInfo) mSSContext.getDeviceInfo();
                    DEFAULT_HEADERS.put(Constants.COOCAA_CCHIP, info.mChip);
                    DEFAULT_HEADERS.put(Constants.COOCAA_CUDID, info.activeId);
                    DEFAULT_HEADERS.put(Constants.COOCAA_CMODEL, info.mModel);
                    DEFAULT_HEADERS.put(Constants.COOCAA_CSIZE, info.mSize);
                    DEFAULT_HEADERS.put(Constants.COOCAA_DEVICENAME, info.deviceName);
                    break;
                case PAD:
                    PadDeviceInfo padDeviceInfo = (PadDeviceInfo) mSSContext.getDeviceInfo();
                    DEFAULT_HEADERS.put(Constants.COOCAA_CCHIP, padDeviceInfo.mChip);
                    DEFAULT_HEADERS.put(Constants.COOCAA_CUDID, padDeviceInfo.activeId);
                    DEFAULT_HEADERS.put(Constants.COOCAA_CMODEL, padDeviceInfo.mModel);
                    DEFAULT_HEADERS.put(Constants.COOCAA_CSIZE, padDeviceInfo.mSize);
                    DEFAULT_HEADERS.put(Constants.COOCAA_DEVICENAME, padDeviceInfo.deviceName);
                    Log.d("TAG","XXXXXXXXXXXXX2222");
                    break;
                case PHONE:
                    PhoneDeviceInfo phoneDeviceInfo = (PhoneDeviceInfo) mSSContext.getDeviceInfo();
                    DEFAULT_HEADERS.put(Constants.COOCAA_IMEI, phoneDeviceInfo.imei);
                    break;
                case THIRD:
                    RobotDeviceInfo robotDeviceInfo = (RobotDeviceInfo) mSSContext.getDeviceInfo();
                    DEFAULT_HEADERS.put(Constants.COOCAA_CMODEL, robotDeviceInfo.getModel());
                    DEFAULT_HEADERS.put(Constants.COOCAA_CUDID, robotDeviceInfo.getDeviceId());
                    break;
            }

//            DEFAULT_HEADERS.put(Constants.COOCAA_CCHIP, PublicParametersUtils.getcChip(SSChannelService.getContext()));
//            DEFAULT_HEADERS.put(Constants.COOCAA_CUDID, PublicParametersUtils.getcUDID(SSChannelService.getContext()));
//            DEFAULT_HEADERS.put(Constants.COOCAA_CMODEL, PublicParametersUtils.getcModel(SSChannelService.getContext()));
//            DEFAULT_HEADERS.put(Constants.COOCAA_CSIZE, PublicParametersUtils.getcSize(SSChannelService.getContext()));
//            DEFAULT_HEADERS.put(Constants.COOCAA_DEVICENAME, PublicParametersUtils.getdeviceName());

            DEFAULT_HEADERS.put(Constants.COOCAA_CBRAND, getcBrand());

            Log.d(HttpServiceConfig.class.getSimpleName(), JSONObject.toJSONString(DEFAULT_HEADERS));
            return DEFAULT_HEADERS;
        }

        public synchronized Map<String, String> getHeader() {
            if (DEFAULT_HEADERS == null || DEFAULT_HEADERS.size() < 1)
                return loadHeader();
            return DEFAULT_HEADERS;
        }

        public synchronized void updateHeader() {
            DEFAULT_HEADERS = null;
            Log.i("header", "updateHeader");
            getHeader();
        }
    }

    private static String getcBrand() {
        return Build.BRAND;
    }

    /**
     * //     * 获取mac地址
     * //
     */
    private static String MAC = "";

    private static String getMac(Context context) {
        if (!TextUtils.isEmpty(MAC))
            return MAC;
        try {
            String channel = Constants.getIOTChannel(context);
            if (channel.equals("TV")) {
                MAC = MACUtils.getMac(context);
            } else {
                MAC = Settings.Global.getString(context.getContentResolver(), "swaiot_mac_key");
                if (TextUtils.isEmpty(MAC)) {
                    MAC = MACUtils.getMac(context);
                    if (TextUtils.isEmpty(MAC)) {
                        MAC = "";
                    }
                }
            }
        } catch (Exception e) {
            MAC = "AAAAAAAAAAAA";
        }
        return MAC;
    }

}
