package swaiotos.runtime.h5.core.os.exts.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import swaiotos.runtime.h5.H5CoreExt;
import swaiotos.runtime.h5.core.os.exts.utils.ExtLog;

public class StorageExt extends H5CoreExt implements IStorageExt {

    public static final String NAME = "storage";
    public static final String TAG = "StorageExt";

    @Override
    @JavascriptInterface
    public void setStorage(String id, String json) {
        ExtLog.d(TAG, "setStorage id=" + id + ", json=" + json);
        boolean ret = false;
        try {
            JSONObject jsonObject = JSON.parseObject(json);
            String key = jsonObject.getString("key");
            String value = jsonObject.getString("data");
            ret = saveSp(key, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        native2js(id, ret ? RET_SUCCESS : RET_FAIL, new JSONObject().toJSONString());
    }

    @Override
    @JavascriptInterface
    public void getStorage(String id, String json) {
        ExtLog.d(TAG, "getStorage id=" + id + ", json=" + json);
        boolean ret = false;
        try {
            JSONObject jsonObject = JSON.parseObject(json);
            String key = jsonObject.getString("key");
            String value = getSp(key);
            ret = value != null;
            if(ret) {
                JSONObject params = new JSONObject();
                params.put("data", value);
                native2js(id, RET_SUCCESS, params.toJSONString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(!ret) {
            native2js(id, RET_FAIL, new JSONObject().toJSONString());
        }
    }

    @Override
    @JavascriptInterface
    public void removeStorage(String id, String json) {
        ExtLog.d(TAG, "removeStorage id=" + id + ", json=" + json);
        boolean ret = false;
        try {
            JSONObject jsonObject = JSON.parseObject(json);
            String key = jsonObject.getString("key");
            ret = removeSp(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        native2js(id, ret ? RET_SUCCESS : RET_FAIL, new JSONObject().toJSONString());
    }

    @Override
    @JavascriptInterface
    public void clearStorage(String id) {
        ExtLog.d(TAG, "clearStorage id=" + id);
        clearSp();
        native2js(id, RET_SUCCESS, new JSONObject().toJSONString());
    }

    @Override
    @JavascriptInterface
    public void getStorageInfo(String id) {
        ExtLog.d(TAG, "getStorageInfo id=" + id);
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("keys", getAllKey());
            StorageInfo info = getStorageInfoFromSDCard();
            jsonObject.put("currentSize", info.totalSpace - info.avaliableSpace);
            jsonObject.put("limitSize", info.avaliableSpace);
            native2js(id, RET_SUCCESS, jsonObject.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
            native2js(id, RET_FAIL, new JSONObject().toJSONString());
        }
    }

    private boolean saveSp(String key, String value) {
        ExtLog.d(TAG, "save key=" + key + ", value=" + value);
        if(TextUtils.isEmpty(key) || value == null)
            return false;
        SharedPreferences sp = context.getSharedPreferences(SP_SPACE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.commit();
        return true;
    }

    private String getSp(String key) {
        SharedPreferences sp = context.getSharedPreferences(SP_SPACE, Context.MODE_PRIVATE);
        String value = sp.getString(key, null);
        ExtLog.d(TAG, "get key=" + key + ", value=" + value);

        return value;
    }

    private boolean hasSp(String key) {
        SharedPreferences sp = context.getSharedPreferences(SP_SPACE, Context.MODE_PRIVATE);
        return sp.contains(key);
    }

    private List<String> getAllKey() {
        SharedPreferences sp = context.getSharedPreferences(SP_SPACE, Context.MODE_PRIVATE);
        Map<String, ?> all = sp.getAll();
        List<String> keyArray = new LinkedList<>();
        Set<String> keySet = all.keySet();
        if(keySet != null && !keySet.isEmpty()) {
            keyArray.addAll(keySet);
        }
        return keyArray;
    }

    private boolean removeSp(String key) {
        ExtLog.d(TAG, "remove key=" + key);
        if(TextUtils.isEmpty(key))
            return false;
        SharedPreferences sp = context.getSharedPreferences(SP_SPACE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(key);
        editor.commit();
        return true;
    }

    private void clearSp() {
        ExtLog.d(TAG, "clear...");
        SharedPreferences sp = context.getSharedPreferences(SP_SPACE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.commit();
    }

    private final static String SP_SPACE = "smart_js_api";

    static class StorageInfo {
        public long avaliableSpace;//kb
        public long totalSpace; //kb
    }

    private StorageInfo getStorageInfoFromSDCard() {
        StorageInfo info = new StorageInfo();
        try {
            String state = Environment.getExternalStorageState();
            if(Environment.MEDIA_MOUNTED.equals(state)) {
                File sdcardDir = Environment.getExternalStorageDirectory();
                StatFs sf = new StatFs(sdcardDir.getPath());
                long blockSize = sf.getBlockSize();
                long blockCount = sf.getBlockCount();
                long availCount = sf.getAvailableBlocks();

                info.totalSpace = blockSize*blockCount/1024;
                info.avaliableSpace = availCount*blockSize/1024;

                ExtLog.d("", "block大小:" + blockSize+", block数目:" + blockCount+", 总大小:" + info.totalSpace + "KB");
                ExtLog.d("", "可用的block数目：:" + availCount + ", 剩余空间:" + info.avaliableSpace + "KB");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return info;
    }
}
