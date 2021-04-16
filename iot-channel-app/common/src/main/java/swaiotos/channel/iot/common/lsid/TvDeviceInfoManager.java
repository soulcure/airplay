package swaiotos.channel.iot.common.lsid;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.SystemProperties;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;

import swaiotos.channel.iot.common.utils.PublicParametersUtils;
import swaiotos.channel.iot.ss.device.TVDeviceInfo;
import swaiotos.channel.iot.ss.server.ShareUtls;
import swaiotos.channel.iot.ss.server.utils.Constants;
import swaiotos.channel.iot.utils.AndroidLog;
import swaiotos.sal.SAL;
import swaiotos.sal.SalModule;
import swaiotos.sal.system.ISystem;

/**
 * @ProjectName: iot-channel-swaiotos
 * @Package: swaiotos.channel.iot.tv.lsid
 * @ClassName: DeviceInfoManager
 * @Description: java类作用描述
 * @Author: wangyuehui
 * @CreateDate: 2020/4/24 15:37
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/4/24 15:37
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class TvDeviceInfoManager {
    private static TvDeviceInfoManager mDeviceInfoManager;
    private Context mContext;
    private String mMovieSource;
    private String mChip, mModel, mSize, mDeviceName, mActiveId;
    private String cHomepageVersion;
    private String MAC;
    private String cFMode;
    private String cTcVersion;
    private String cPattern;
    private String Resolution;
    private String aSdk;
    private String cEmmcCID;
    private String cBrand;
    private String mNickName;
    private int blueSupport;

    public static TvDeviceInfoManager getInstance(Context context) {
        if (mDeviceInfoManager == null)
            synchronized (TvDeviceInfoManager.class) {
                if (mDeviceInfoManager == null)
                    mDeviceInfoManager = new TvDeviceInfoManager(context);
            }
        return mDeviceInfoManager;
    }

    private TvDeviceInfoManager(Context context) {
        this.mContext = context;
        updateMovieSource(context);
        init();
    }

    private void init() {
        mChip = PublicParametersUtils.getcChip(mContext);
        mModel = PublicParametersUtils.getcModel(mContext);
        mSize = PublicParametersUtils.getcSize(mContext);
        mActiveId = PublicParametersUtils.getcUDID(mContext);
        mDeviceName = PublicParametersUtils.getdeviceName(mContext);
        mMovieSource = getMovieSource();
        cHomepageVersion = PublicParametersUtils.getHomepageVersion(mContext) + "";
        MAC = PublicParametersUtils.getMac(mContext);
        cFMode = PublicParametersUtils.getcFMode();
        cTcVersion = PublicParametersUtils.getcTcVersion(mContext);
        cPattern = PublicParametersUtils.getcPattern();
        Resolution = PublicParametersUtils.getResolution(mContext);
        aSdk = PublicParametersUtils.getSDK();
        cEmmcCID = PublicParametersUtils.getcEmmcCID(mContext);
        cBrand = PublicParametersUtils.getcBrand(mContext);
        mNickName = "";

        try {
            blueSupport = 0;
            //G22需求：是否支持蓝牙，不支持就mac为空
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            AndroidLog.androidLog("---mBluetoothAdapter---isEnabled:"+mBluetoothAdapter.isEnabled());
            if (!mBluetoothAdapter.isEnabled()) {
                blueSupport = 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getMovieSource() {
        if (!TextUtils.isEmpty(mMovieSource)) {
            return mMovieSource;
        }
        try {
            mMovieSource = SystemProperties.get("third.get.movie.source");
            Log.d("source", "SystemProperties  source:" + mMovieSource);
            if (TextUtils.isEmpty(mMovieSource)) {
                mMovieSource = ShareUtls.getInstance(mContext).getString("movieSource", "");
                Log.d("source", "table source:" + mMovieSource);
            }
//            if (!TextUtils.isEmpty(mMovieSource)) {
//                if (mMovieSource.equals("tencent") || mMovieSource.equals("qq"))
//                    mMovieSource = "tencent";
//                else if (mMovieSource.equals("iqiyi") || mMovieSource.equals("yinhe"))
//                    mMovieSource = "iqiyi";
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mMovieSource;
    }

    private void updateMovieSource(Context mContext) {
        try {
            Log.d("source", " updateMovieSource");
            Context AContext = mContext.createPackageContext("com.tianci.movieplatform",
                    Context.CONTEXT_IGNORE_SECURITY);
            SharedPreferences sp = AContext.getSharedPreferences("movie_source",
                    Context.MODE_PRIVATE);
            String source = sp.getString("company", "default");
            Log.d("source", " movie source:" + source);
            ShareUtls.getInstance(mContext).putString("movieSource", source);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 默认获取设备信息
     */
    public TVDeviceInfo getDeviceInfo() {
        if (Constants.isDangle()) {
            try {
                ISystem iSystem = SAL.getModule(mContext, SalModule.SYSTEM);
                mNickName =  iSystem.getDeviceName();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (TextUtils.isEmpty(mNickName)) {
                mNickName = "酷开共享屏";
            }
        } else  {
            try {
                ISystem iSystem = SAL.getModule(mContext, SalModule.SYSTEM);
                mNickName =  iSystem.getDeviceName();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (TextUtils.isEmpty(mNickName)) {
                mNickName = "创维电视 " + mModel;
            }
        }

        return new TVDeviceInfo(mActiveId, mDeviceName, mMovieSource, mChip, mModel, mSize,
                cHomepageVersion,
                MAC,
                cFMode,
                cTcVersion,
                cPattern,
                Resolution,
                aSdk,
                cEmmcCID,
                cBrand, mNickName,blueSupport);
    }

//    /**
//     * 传入视频流
//     * */
//    public TVDeviceInfo getDeviceInfo(String movieSource) {
//        return new TVDeviceInfo(mActiveId,mDeviceName,movieSource,mChip,mModel,mSize);
//    }


}
