package swaiotos.channel.iot.tv.iothandle.handle;

import android.util.Log;

import swaiotos.channel.iot.tv.iothandle.handle.base.BaseChannelHandle;


public class DefaultHandle extends BaseChannelHandle {

    @Override
    protected void onHandle() {
        Log.i(TAG, "DefaultHandle onHandle: not found!!! return default. mCmdData.type:" + mCmdData.type);
    }
}
