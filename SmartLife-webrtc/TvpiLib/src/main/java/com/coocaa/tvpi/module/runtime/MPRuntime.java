package com.coocaa.tvpi.module.runtime;

import android.content.Context;
import android.os.Bundle;

import com.coocaa.smartscreen.constant.SmartConstans;
import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import swaiotos.runtime.Applet;
import swaiotos.runtime.base.AppletRunner;

import static swaiotos.runtime.Applet.APPLET_MP;

/**
 * @Author: yuzhan
 */
public class MPRuntime implements AppletRunner {
    @Override
    public void start(Context context, Applet applet) throws Exception {
        start(context, applet, null);
    }

    @Override
    public void start(Context context, Applet applet, Bundle bundle) throws Exception {
        if(applet == null || context == null)
            return ;
        if (APPLET_MP.equals(applet.getType())) {
            IWXAPI api = WXAPIFactory.createWXAPI(context, SmartConstans.getBusinessInfo().APPID_WECHAT);
            WXLaunchMiniProgram.Req req = new WXLaunchMiniProgram.Req();
            req.userName = applet.getId(); //eg"gh_d43f693ca31f"; // 小程序原始id
            req.path = applet.getTarget(); //拉起小程序页面的可带参路径，不填默认拉起小程序首页
            req.miniprogramType = WXLaunchMiniProgram.Req.MINIPTOGRAM_TYPE_RELEASE;
            api.sendReq(req);
        }
    }
}
