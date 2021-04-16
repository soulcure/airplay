package swaiotos.channel.iot.ccenter;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.text.TextUtils;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @ProjectName: iot-channel-app
 * @Package: swaiotos.channel.iot.ccenter
 * @ClassName: CCenterManger
 * @Description: 二维码中心：处理获取二维码相关数据
 * @Author: wangyuehui
 * @CreateDate: 2020/12/18 10:32
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/12/18 10:32
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class CCenterMangerImpl implements CCenterManger{
    public static String CCENTER_INTENT_BUNDLE = "SYSTEM_INTENT_BUNDLE";
    public static String CCENTER_START_MESSENGER = "SYSTEM_START_MESSENGER";
    public static String CCENTER_EXTRA_MESSASGE = "CCENTER_EXTRA_MESSASGE";
    public static String CCENTER_CODE = "CCENTER_CODE";

    private static CCenterMangerImpl C_CENTER_MANGER;
    private final Context mContext;

    public static CCenterMangerImpl getCCenterManger(Context context) {
        if (C_CENTER_MANGER == null) {
            synchronized (CCenterMangerImpl.class) {
                if (C_CENTER_MANGER == null) {
                    C_CENTER_MANGER = new CCenterMangerImpl(context.getApplicationContext());
                }
            }
        }
        return C_CENTER_MANGER;
    }

    private CCenterMangerImpl (Context context) {
        this.mContext = context;
    }

    @Override
    public void getCCodeString(CCenterListener cCenterListener) {
        startCCenterServer(null,cCenterListener);

    }

    @Override
    public void getCCodeString(Map<String, String> urlMap, CCenterListener cCenterListener) {
        startCCenterServer(urlMap,cCenterListener);
    }

    /**
     *
     * 启动 SendLetterServiceImpl服务
     *
     * */
    private void startCCenterServer(Map<String,String> urlMap, CCenterListener cCenterListener) {
        //启动
        Intent intent = new Intent("swaiotos.channel.iot.common.SendLetterServiceImpl.action");
        intent.setPackage("swaiotos.channel.iot");

        Messenger messenger = new Messenger(new CCenterHandler(cCenterListener));
        Bundle bundle = new Bundle();

        intent.putExtra(CCENTER_INTENT_BUNDLE,bundle);
        bundle.putParcelable(CCENTER_START_MESSENGER, messenger);
        if (urlMap != null && urlMap.size() > 0) {
            bundle.putString(CCENTER_EXTRA_MESSASGE, getUrlParamsByMap(urlMap));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mContext.startForegroundService(intent);
        } else {
            mContext.startService(intent);
        }
    }

    /**
     * 将map转换成url
     *
     * @param map
     * @return
     */
    public String getUrlParamsByMap(Map<String, String> map) {
        if (map == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            try {
                sb.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(),"UTF-8")).append("&");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        String s = sb.toString();
        if (s.endsWith("&")) {
            s = s.substring(0,s.length()-1);
        }
        return s;
    }

    public static class CCenterHandler extends Handler {

        private final CCenterListener cCenterListener;

        public CCenterHandler (CCenterListener cCenterListener) {
            this.cCenterListener = cCenterListener;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            if (bundle != null) {
                String qrCodeJson = bundle.getString(CCENTER_CODE);
                if (cCenterListener != null && !TextUtils.isEmpty(qrCodeJson)) {
                    cCenterListener.ccodeCallback(qrCodeJson);
                }
            }
        }
    }
}
