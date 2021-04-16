package swaiotos.channel.iot.tv.iothandle.handle.base;

import android.util.Log;


import com.coocaa.statemanager.common.bean.CmdData;

import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.channel.im.IMMessage;

public abstract class BaseChannelHandle implements MessageHandle {
    protected static final String TAG = "iotclient";
    protected IMMessage mIMMessage = null;
    protected SSChannel mChannel = null;
    protected CmdData mCmdData = null;

    @Override
    public void onHandle(IMMessage message, SSChannel channel, CmdData cmdData) {
        Log.i(TAG, "onHandle: " + message.getContent());
        Log.i(TAG, "onHandle: cmdData.type:" + cmdData.type);
        mIMMessage = message;
        mChannel = channel;
        mCmdData = cmdData;
        onHandle();
    }

    protected abstract void onHandle();

}
