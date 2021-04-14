package com.threesoft.webrtc.webrtcroom.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.util.Log;

import com.threesoft.webrtc.webrtcroom.webrtcmodule.WebRtcClient2;

public class NetWorkStateReceiver extends BroadcastReceiver {
    private static final String TAG = "NetWorkStateReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG,"onReceive");

        //检测API是不是小于23，因为到了API23之后getNetworkInfo(int networkType)方法被弃用
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            //获得ConnectivityManager对象
            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if(networkInfo != null){
                if(networkInfo.isAvailable()){
                    Log.d(TAG,"net is ok, socketio is disconnected ,reconnect now...");
                    WebRtcClient2.getInstance().reConnect();


                }
            }

//API大于23时使用下面的方式进行网络监听
        }else {

            //获得ConnectivityManager对象
            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            //获取所有网络连接的信息
            Network[] networks = connMgr.getAllNetworks();
            //用于存放网络连接信息
            StringBuilder sb = new StringBuilder();
            if(networks != null){
                //通过循环将网络信息逐个取出来
                for (int i=0; i < networks.length; i++){
                    //获取ConnectivityManager对象对应的NetworkInfo对象
                    NetworkInfo networkInfo = connMgr.getNetworkInfo(networks[i]);
                    if(networkInfo.isAvailable()){
                        Log.d(TAG,"net is ok, socketio is disconnected ,reconnect now...");
                        WebRtcClient2.getInstance().reConnect();
                    }
                }
            }

        }
    }
}