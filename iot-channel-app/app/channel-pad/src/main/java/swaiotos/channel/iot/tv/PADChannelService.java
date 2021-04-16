package swaiotos.channel.iot.tv;


import android.content.Context;

import swaiotos.channel.iot.common.lsid.CommonSSChannelService;
import swaiotos.channel.iot.common.lsid.PadDeviceInfoManager;
import swaiotos.channel.iot.ss.device.PadDeviceInfo;

public class PADChannelService extends CommonSSChannelService<PadDeviceInfo> {

    @Override
    public PadDeviceInfo getTYPEDeviceInfo(Context context) {
        return PadDeviceInfoManager.getInstance(context).getDeviceInfo();
    }
}
