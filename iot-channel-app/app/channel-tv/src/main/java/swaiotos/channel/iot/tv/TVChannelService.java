package swaiotos.channel.iot.tv;

import android.content.Context;

import swaiotos.channel.iot.common.lsid.CommonSSChannelService;
import swaiotos.channel.iot.common.lsid.TvDeviceInfoManager;
import swaiotos.channel.iot.ss.device.TVDeviceInfo;

public class TVChannelService extends CommonSSChannelService<TVDeviceInfo> {

    @Override
    public TVDeviceInfo getTYPEDeviceInfo(Context context) {
        return TvDeviceInfoManager.getInstance(context).getDeviceInfo();
    }
}
