package swaiotos.channel.iot.common.http.net;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import swaiotos.channel.iot.common.utils.PublicParametersUtils;
import swaiotos.channel.iot.ss.SSChannelService;
import swaiotos.channel.iot.ss.server.http.HttpServiceConfig;
import swaiotos.channel.iot.ss.server.utils.Constants;


public class CooCaaRestApiFactory {

    private Context mContext;
    private int mVersionCode;
    private String mVersionName;


    public CooCaaRestApiFactory(Context context, int versionCode, String versionName) {
        mContext = context.getApplicationContext();
        mVersionCode = versionCode;
        mVersionName = versionName;

    }
    /**
     * MAC String N mac地址 bcec234e832e
     * cChip String N 机芯
     * cUDID String N 服务端分配给设备的唯一标识（激活ID）
     * cModel String N 机型
     * cSize String N 尺寸
     * cBrand String  品牌标识
     * cLicense String  牌照商
     *
     * */
    public CooCaaRestApi createGoLiveRestApi() {
        Map<String,String> DEFAULT_HEADERS = new HashMap<>();
        DEFAULT_HEADERS.put(Constants.COOCAA_MAC, PublicParametersUtils.getMac(mContext));
        DEFAULT_HEADERS.put(Constants.COOCAA_CCHIP, PublicParametersUtils.getcChip(mContext));
        DEFAULT_HEADERS.put(Constants.COOCAA_CUDID, PublicParametersUtils.getcUDID(mContext));
        DEFAULT_HEADERS.put(Constants.COOCAA_CMODEL, PublicParametersUtils.getcModel(mContext));
        DEFAULT_HEADERS.put(Constants.COOCAA_CSIZE, PublicParametersUtils.getcSize(mContext));
        DEFAULT_HEADERS.put(Constants.COOCAA_DEVICENAME, PublicParametersUtils.getdeviceName(mContext));
        DEFAULT_HEADERS.put(Constants.COOCAA_CVERSION,""+PublicParametersUtils.getVersionCode(mContext));
        
        return new CooCaaRestApiImpl(mContext, new RestApiFactory(mContext), DEFAULT_HEADERS);
    }
}
