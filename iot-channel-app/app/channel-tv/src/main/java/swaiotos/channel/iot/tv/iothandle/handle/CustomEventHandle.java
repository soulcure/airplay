package swaiotos.channel.iot.tv.iothandle.handle;

import android.util.Log;

import swaiotos.channel.iot.tv.iothandle.handle.base.BaseChannelHandle;


public class CustomEventHandle extends BaseChannelHandle {

    @Override
    protected void onHandle() {
        Log.i(TAG, "CustomEventHandle onHandle: not found!!! return default.:" );
    }
}
