package com.coocaa.publib.connect.service;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.coocaa.smartscreen.repository.Repository;
import com.coocaa.smartscreen.repository.service.LoginRepository;

import java.util.LinkedHashSet;

import swaiotos.channel.iot.ss.manager.lsid.LSIDInfo;
import swaiotos.channel.iot.ss.manager.lsid.LSIDManager;
import swaiotos.channel.iot.utils.AndroidLog;

/**
 * @ProjectName: iot-channel-swaiotos
 * @Package: swaiotos.channel.iot.tv.lsid
 * @ClassName: LSIDInfo
 * @Description: 获取sid、accessToken，并且刷新注册登录
 * @Author: wangyuehui
 * @CreateDate: 2020/4/26 11:50
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/4/26 11:50
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class LSIDInfoManager implements LSIDManager {

    private static final String TAG = LSIDManager.class.getSimpleName();

    private Context mContext;

    private LinkedHashSet<Callback> mCallbacks;

    private LSIDInfo mLSIDInfo;

    public LSIDInfoManager(Context context) {
        mContext = context;
        mCallbacks = new LinkedHashSet<>();
    }


    @Override
    public void addCallback(Callback callback) {
        mCallbacks.add(callback);
    }

    @Override
    public void removeCallback(Callback callback) {
        mCallbacks.remove(callback);
    }

    @Override
    public LSIDInfo refreshLSIDInfo() {
        Log.d(TAG, "refreshLSIDInfo: ");
        String token = null;
        String lsid = null;
        String userId = null;
        try {
            token = Repository.get(LoginRepository.class).queryDeviceRegisterLoginInfo().access_token;
            lsid = Repository.get(LoginRepository.class).queryDeviceRegisterLoginInfo().zpLsid;
            userId = Repository.get(LoginRepository.class).queryCoocaaUserInfo().mobile;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(lsid) && !TextUtils.isEmpty(token) && !TextUtils.isEmpty(userId)) {
            mLSIDInfo =  new LSIDInfo(lsid, token,userId);
        } else if (null == mLSIDInfo) {
            mLSIDInfo = new LSIDInfo("lsid,", "token", "userId");
        }
        Log.d(TAG, "refreshLSIDInfo: lsid  = " + mLSIDInfo.lsid + "  token = " + mLSIDInfo.accessToken + " userId:"+userId);
        return mLSIDInfo;
    }

    @Override
    public LSIDInfo getLSIDInfo() {
        Log.d(TAG, "getLSIDInfo: ");
        String token = null;
        String lsid = null;
        String userId = null;
        try {
            token = Repository.get(LoginRepository.class).queryDeviceRegisterLoginInfo().access_token;
            lsid = Repository.get(LoginRepository.class).queryDeviceRegisterLoginInfo().zpLsid;
            userId = Repository.get(LoginRepository.class).queryCoocaaUserInfo().mobile;
        } catch (Exception e) {
            e.printStackTrace();
        }
        AndroidLog.androidLog("----getLSIDInfo--token:"+token + "  lsid:"+lsid);
        if (!TextUtils.isEmpty(lsid) && !TextUtils.isEmpty(token) && !TextUtils.isEmpty(userId)) {
            mLSIDInfo =  new LSIDInfo(lsid, token,userId);
        } else if (null == mLSIDInfo) {
            mLSIDInfo = new LSIDInfo("lsid,", "token", "userId");
        }
        Log.d(TAG, "getLSIDInfo: lsid  = " + mLSIDInfo.lsid + "  token = " + mLSIDInfo.accessToken+ " userId:"+userId);
        return mLSIDInfo;
    }

    @Override
    public LSIDInfo reset() {
        return null;
    }

    @Override
    public void setSid(String sid, String token) {
        Log.d(TAG, "setSid: " + sid + "  token: " + token);
        mLSIDInfo = new LSIDInfo(sid, token);
        Log.d(TAG, "setSid: lsid  = " + mLSIDInfo.lsid + "  token = " + mLSIDInfo.accessToken);
    }

    @Override
    public void setSidAndUserId(String sid, String token, String userId) {
        Log.d(TAG, "setSid: " + sid + "  token: " + token);
        mLSIDInfo = new LSIDInfo(sid, token,userId);
        Log.d(TAG, "setSid: lsid  = " + mLSIDInfo.lsid + "  token = " + mLSIDInfo.accessToken);
    }
}
