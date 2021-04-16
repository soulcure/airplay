package com.swaiotos.testdemo_tv;

import android.util.Log;

public class DataManager {
    public static DataManager Instance = new DataManager();
    private String data;
    private ReplayListener listener;

    private int replaySuccess = 0;
    private int receiverTimes = 0;
    private int replayFailed = 0;

    interface ReplayListener{
        void onRefresh(String str);
    }

    private DataManager(){}

    public void setListener(ReplayListener l){
        listener = l;
    }

    public void updateData(boolean repley){
        receiverTimes ++;
        if(repley){
            replaySuccess ++;
        }else{
            replayFailed ++;
        }
        int percent = 0;
        try {
            percent = (int) (((double)replaySuccess / receiverTimes) * 100);
        } catch (Exception e) {
        }
        StringBuffer sb = new StringBuffer();
        sb.append("接收：" + receiverTimes + "\n");
        sb.append("reply成功：" + replaySuccess + "\n");
        sb.append("reply失败：" + replayFailed + "\n");
        sb.append("reply成功率：" + percent + "%");
        Log.d("c-test", " updateData:" + (listener != null));
        if(listener != null) {
            listener.onRefresh(sb.toString());
        }
    }

    public void reset(){
         receiverTimes = 0;
        replaySuccess = 0;
         replayFailed = 0;
    }

}
