package com.coocaa.tvpi.module.openapi;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;

import java.util.Map;

import swaiotos.runtime.base.utils.ToastUtils;

public class ThirdLoginActivity extends AppCompatActivity {

    final static String TAG = "ThirdLogin";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String type = getIntent().getStringExtra("type");
        SHARE_MEDIA media = getMedia(type);
        Log.d(TAG, "start login : " + media);
        if(media == null) {
            finish();
        } else {
            if(UMShareAPI.get(this).isInstall(this, media)) {
                UMShareAPI.get(ThirdLoginActivity.this).getPlatformInfo(ThirdLoginActivity.this, media, umAuthListener);
            } else {
                ToastUtils.getInstance().showGlobalLong("应用未安装，无法登录");
            }
        }
    }

    private SHARE_MEDIA getMedia(String type) {
        if(type != null) {
            switch (type) {
                case "QQ":
                    return SHARE_MEDIA.QQ;
                case "WX":
                    return SHARE_MEDIA.WEIXIN;
            }
        }
        return SHARE_MEDIA.QQ;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult : " + requestCode + ", resultCode=" + resultCode + ", data=" + data);
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
    }

    UMAuthListener umAuthListener  = new UMAuthListener() {
        @Override
        public void onStart(SHARE_MEDIA share_media) {
            Log.d(TAG, "onStart media=" + share_media);
        }

        @Override
        public void onComplete(SHARE_MEDIA share_media, int i, Map<String, String> map) {
            Log.d(TAG, "onComplete map=" + map + ", media=" + share_media);
        }

        @Override
        public void onError(SHARE_MEDIA share_media, int i, Throwable throwable) {
            Log.d(TAG, "onError throwable=" + throwable + ", media=" + share_media);
            throwable.printStackTrace();
        }

        @Override
        public void onCancel(SHARE_MEDIA share_media, int i) {
            Log.d(TAG, "onCancel media=" + share_media);
        }
    };
}
