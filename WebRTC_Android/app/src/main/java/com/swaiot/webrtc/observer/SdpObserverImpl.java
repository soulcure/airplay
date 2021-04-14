package com.swaiot.webrtc.observer;

import android.util.Log;

import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

public class SdpObserverImpl implements SdpObserver {
    private String TAG = "SdpObserver";

    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {
        Log.d(TAG, "CustomSdpObserver onCreateSuccess type="
                + sessionDescription.type.canonicalForm());
    }

    @Override
    public void onSetSuccess() {
        Log.d(TAG, "onSetSuccess");
    }

    @Override
    public void onCreateFailure(String s) {
        Log.e(TAG, "onCreateFailure s=" + s);
    }

    @Override
    public void onSetFailure(String s) {
        Log.e(TAG, "onSetFailure s=" + s);
    }
}
