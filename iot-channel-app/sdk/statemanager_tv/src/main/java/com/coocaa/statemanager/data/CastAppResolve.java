package com.coocaa.statemanager.data;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;

import swaiotos.channel.iot.ss.server.data.log.CastData;
import swaiotos.channel.iot.ss.server.data.log.ReportData;
import swaiotos.channel.iot.ss.server.data.log.ReportDataUtils;
import swaiotos.channel.iot.ss.server.http.SessionHttpService;
import swaiotos.channel.iot.ss.server.http.api.HttpApi;
import swaiotos.channel.iot.ss.server.http.api.HttpResult;
import swaiotos.channel.iot.ss.server.http.api.HttpSubscribe;
import swaiotos.channel.iot.ss.server.http.api.HttpThrowable;
import swaiotos.channel.iot.utils.ThreadManager;

public class CastAppResolve {
    public static CastAppResolve Resolver = new CastAppResolve();
    private static CastData cData = null;
    private static CastData tmpData = null;

    public CastData getCastData() {
        return tmpData;
    }

    public void updateCastData(String uid, String pkg, String className, String type, String content, String respons, String appScreenURI, String sourceClient) {
        Log.d("log", "updateCastData uid:" + uid + " className:" + className + " type:" + type);
//        Log.d("log", "updateCastData content:" + content);
        tmpData = new CastData();
        tmpData.uid = uid;
        tmpData.castPkg = pkg;
        tmpData.castClassName = className;
        tmpData.castType = type;
        tmpData.content = content;
        tmpData.respont = respons;
        tmpData.startTime = System.currentTimeMillis();
        tmpData.appScreenURI = appScreenURI;
        if(!TextUtils.isEmpty(sourceClient) && sourceClient.equals("ss-clientID-mobile-iphone")) {
            tmpData.mobileType = "IOS";
        }else{
            tmpData.mobileType = "Android";
        }
    }

    public void startCast(CastData data) {
        Log.d("log", " startCast ");
        cData = data;
    }

    public void endCast(final Context c, final String pkg, final String className, final boolean isSame) {
        Log.d("log", " endCast ");
        if (cData == null || TextUtils.isEmpty(pkg) || TextUtils.isEmpty(className)) {
            return;
        }
        final CastData cCastData = copyData(cData);
        cData = null;
        Log.d("log", "endCast pkg:" + pkg + " className:" + className);
        Log.d("log", "endCast isSame:" + isSame + " !(pkg.equals(cData.castPkg) && className.equals(cData.castClassName)):" + !(pkg.equals(cCastData.castPkg) && className.equals(cCastData.castClassName)));
        if (isSame || !(pkg.equals(cCastData.castPkg) && className.equals(cCastData.castClassName))) {
            cCastData.duration = System.currentTimeMillis() - cCastData.startTime;
            submitAppCast(c, cCastData);
        }
    }

    public void submitAppCast(final Context mcontext, final CastData data) {
        ThreadManager.getInstance().ioThread(new Runnable() {
            @Override
            public void run() {
                ReportData.PayLoadData<CastData> pData = new ReportData.PayLoadData<>();
                ReportData.EventData<CastData> eData = new ReportData.EventData();
                eData.data = data;
                eData.eventName = "swaiotos_appcast_end";
                eData.eventTime = System.currentTimeMillis();
                pData.events = new ArrayList<ReportData.EventData<CastData>>();
                pData.events.add(eData);
                ReportData reportData = ReportDataUtils.getReportData(mcontext, "dongle.push", pData);
                Log.d("log", "submitAppCast reportData:" + new Gson().toJson(reportData));

                HttpApi.getInstance().request(SessionHttpService.SERVICE.reportLog(reportData), new HttpSubscribe<HttpResult<Void>>() {
                    @Override
                    public void onSuccess(HttpResult<Void> result) {
                        Log.d("log", " log submit success");
                    }

                    @Override
                    public void onError(HttpThrowable error) {
                        Log.d("log", " HttpApi error:" + error.getErrMsg());
                    }
                });
            }
        });
    }

    public void submitReportData(final ReportData reportData) {
        ThreadManager.getInstance().ioThread(new Runnable() {
            @Override
            public void run() {
                HttpApi.getInstance().request(SessionHttpService.SERVICE.reportLog(reportData), new HttpSubscribe<HttpResult<Void>>() {
                    @Override
                    public void onSuccess(HttpResult<Void> result) {
//                        Log.d("log", "11 log submit success");
                    }

                    @Override
                    public void onError(HttpThrowable error) {
                        Log.d("log", "11 HttpApi error:" + error.getErrMsg());
                    }
                });
            }
        });
    }

    public void submitDanma(final Context c, final String uid, final String pkg, final String className, final String type, final String content, final String respons,String sourceClient) {
        Log.d("log", "submit danma :");
        CastData danData = new CastData();
        danData.uid = uid;
        danData.castPkg = pkg;
        danData.castClassName = className;
        danData.castType = type;
        danData.content = content;
        danData.respont = respons;
        danData.startTime = System.currentTimeMillis();
        danData.duration = 3 * 1000;
        if(!TextUtils.isEmpty(sourceClient) && sourceClient.equals("ss-clientID-mobile-iphone")) {
            danData.mobileType = "IOS";
        }else{
            danData.mobileType = "Android";
        }
        submitAppCast(c, danData);
    }

    private CastData copyData(CastData data) {
        CastData cData = new CastData();
        cData.uid = data.uid;
        cData.castPkg = data.castPkg;
        cData.castClassName = data.castClassName;
        cData.castType = data.castType;
        cData.content = data.content;
        cData.respont = data.respont;
        cData.startTime = data.startTime;
        cData.appScreenURI = data.appScreenURI;
        cData.mobileType = data.mobileType;
        return cData;
    }

}
