package com.coocaa.tvpi.module.openapi;

import android.os.Bundle;

import com.coocaa.publib.base.BaseActivity;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartscreen.constant.SmartConstans;
import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

public class JumpWxMPActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String id = getIntent().getStringExtra("id");
        String path = getIntent().getStringExtra("path");

        startMp(id, path);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void startMp(String id, String path) {
        IWXAPI api = WXAPIFactory.createWXAPI(this, SmartConstans.getBusinessInfo().APPID_WECHAT);
        if(api.isWXAppInstalled()) {
            WXLaunchMiniProgram.Req req = new WXLaunchMiniProgram.Req();
            req.userName = id; //"gh_d43f693ca31f"; // 小程序原始id
            req.path = path;   //拉起小程序页面的可带参路径，不填默认拉起小程序首页
            req.miniprogramType = WXLaunchMiniProgram.Req.MINIPTOGRAM_TYPE_RELEASE;
            api.sendReq(req);
        } else {
            ToastUtils.getInstance().showGlobalShort("请先安装微信APP");
        }
    }
}
