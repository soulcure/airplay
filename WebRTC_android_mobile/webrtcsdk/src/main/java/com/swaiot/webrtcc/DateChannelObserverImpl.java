package com.swaiot.webrtcc;

import android.util.Log;

import org.webrtc.DataChannel;

public class DateChannelObserverImpl implements DataChannel.Observer {

    private static final String TAG = "DateChannelObserver";

    @Override
    public void onBufferedAmountChange(long l) {
        Log.d(TAG, "onBufferedAmountChange : " + l);
    }

    @Override
    public void onStateChange() {
        Log.d(TAG, "onStateChange");
    }

    @Override
    public void onMessage(DataChannel.Buffer buffer) {
        Log.d(TAG, "onMessage DataChannel : " + buffer.data.toString());
    }
}
