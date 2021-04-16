package swaiotos.channel.iot.ss.server.data.log;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import com.skyworth.framework.skysdk.properties.SkySystemProperties;

import swaiotos.channel.iot.ss.analysis.UserBehaviorAnalysis;
import swaiotos.channel.iot.ss.server.utils.MACUtils;
import swaiotos.sal.SAL;
import swaiotos.sal.SalModule;
import swaiotos.sal.platform.IDeviceInfo;
import swaiotos.sal.platform.ISystemInfo;
import swaiotos.sal.system.ISystem;


public class ReportDataUtils {
    private static ReportData.Header header = null;

    public static synchronized ReportData getReportData(Context c, String tag, ReportData.PayLoadData payLoadData) {
        ReportData data = new ReportData();
        data.header = updateHeader(c,tag);
        data.payload = payLoadData;
        return data;
    }

    private static ReportData.Header updateHeader(Context c,String tag){
        if(header == null){
            header = getReportHeader(c);
        }
        ReportData.Header mHeader = new ReportData.Header();
        mHeader.client = header.client;
        mHeader.timestamp = header.timestamp;
        mHeader.tag = tag;
        return mHeader;
    }

    private static ReportData.Header getReportHeader(Context c) {
        if (null == header) {
            header = new ReportData.Header();
            ReportData.ClientData clientData = new ReportData.ClientData();
            header.client = clientData;
            try {
                clientData.udid = getActiveID(c);
                clientData.chip = getcChip(c);
                clientData.mac = getMac(c);
                clientData.model = getcMode(c);
                clientData.appVersion = getAppVersionCode(c, c.getPackageName()) + "";
                clientData.sysVersion = getsysVersion(c);
                clientData.brand = Build.BRAND;
                clientData.skyform = getSkyform();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!TextUtils.isEmpty(UserBehaviorAnalysis.userId) &&  header.client != null) {
            header.client.udid = UserBehaviorAnalysis.userId;
        }

        header.timestamp = System.currentTimeMillis();
//        header.tag = tag;
        return header;
    }

    private static String MAC = "";

    public static String getMac(Context context) {
        if (!TextUtils.isEmpty(MAC))
            return MAC;
        try {
            try {
                IDeviceInfo deviceInfo = SAL.getModule(context, SalModule.DEVICE_INFO);
                MAC = deviceInfo.getMac();
            } catch (Exception e) {
                MAC = MACUtils.getMac(context);
            }
        } catch (Exception e) {
        }
        return MAC;
    }

    private static String activeID = "";

    public static String getActiveID(Context context) {
        if (!TextUtils.isEmpty(activeID))
            return activeID;
        try {
            ISystem iSystem = SAL.getModule(context, SalModule.SYSTEM);
            activeID = iSystem.getActiveId();
        } catch (Exception e) {
        }
        return activeID;
    }

    private static String cChip = "";

    public static String getcChip(Context context) {
        if (!TextUtils.isEmpty(cChip))
            return cChip;
        try {
            IDeviceInfo deviceInfo = SAL.getModule(context, SalModule.DEVICE_INFO);
            cChip = deviceInfo.getChip();
        } catch (Exception e) {
        }
        return cChip;
    }

    private static String cMode = "";

    public static String getcMode(Context context) {
        if (!TextUtils.isEmpty(cMode))
            return cMode;
        try {
            IDeviceInfo deviceInfo = SAL.getModule(context, SalModule.DEVICE_INFO);
            cMode = deviceInfo.getModel();
        } catch (Exception e) {
        }
        return cMode;
    }

    public static int getAppVersionCode(Context context, String pkgName) {
        int versionCode = -1;
        PackageManager pm = context.getPackageManager();
        PackageInfo info;
        try {
            if (pm != null) {
                info = pm.getPackageInfo(pkgName, 0);
                versionCode = info.versionCode;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    private static String sysversion = "";

    public static String getsysVersion(Context context) {

        if (!TextUtils.isEmpty(sysversion))
            return sysversion;
        try {
            ISystemInfo iSystemInfo = SAL.getModule(context, SalModule.SYSTEM_INFO);
            long versionCode = iSystemInfo.getVersionCode();
            sysversion = String.valueOf(versionCode);
        } catch (Exception e) {
        }
        return sysversion;
    }

    public static String getSkyform(){
        try {
            String skyform = SkySystemProperties.getProperty("ro.build.skyform");
//            Log.d("state","getSkyform skyform:"+skyform);
            return skyform;
        }catch (Exception e){
            return "";
        }
    }

}
