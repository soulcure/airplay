package swaiotos.channel.iot.tv.iothandle.handle;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;

import swaiotos.channel.iot.tv.iothandle.data.LocalMediaParams;
import swaiotos.channel.iot.tv.iothandle.handle.base.BaseChannelHandle;

public class LocalMediaHandle extends BaseChannelHandle {

    @Override
    protected void onHandle() {
        String url = mIMMessage.getContent();
        if (TextUtils.isEmpty(url)) {
            Log.i(TAG, "LocalMediaHandle onHandle: url is null !!!");
            return;
        }
        String name = "";
        if (!TextUtils.isEmpty(mCmdData.param)) {
            LocalMediaParams params = JSONObject.parseObject(mCmdData.param, LocalMediaParams.class);
            if (params != null) {
                name = params.name;
            }
        }
        String mediaType = "NULL";
        switch (mIMMessage.getType()) {
            case IMAGE:
                mediaType = "IMAGE";
                break;
            case AUDIO:
                mediaType = "MUSIC";
                break;
            case VIDEO:
                mediaType = "MOVIE";
                break;
            case DOC:
                mediaType = "DOC";
                break;
        }
        try {
//            XServiceManager.getLiteCCC().startMedia(url, name, mediaType);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
