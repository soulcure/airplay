package swaiotos.channel.iot.common.lsid;

import android.content.Context;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;

import swaiotos.channel.iot.ss.manager.lsid.LSIDInfo;
import swaiotos.channel.iot.ss.manager.lsid.LSIDManager;
import swaiotos.channel.iot.ss.server.utils.Constants;
import swaiotos.channel.iot.utils.AndroidLog;
import swaiotos.channel.iot.utils.NetUtils;

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
    private ILSIDManagerService mLSIDManagerService;
    private LSID mLSID;
    private Context mContext;
    private Callback mCallback;
    private LSIDStuts lsidStuts = LSIDStuts.INIT;
    private int mCount;
    private AtomicBoolean atomicBoolean = new AtomicBoolean(true);

    private LSIDInfo lsidInfo;

    public LSIDInfoManager(Context context, ILSIDManagerService lSIDManagerService) {
        mContext = context;
        mLSIDManagerService = lSIDManagerService;
        lsidInfo = new LSIDInfo();
        mCount = 0;
    }


    public void queryLSID() {
        AndroidLog.androidLog("---queryLSID--------------------------------------");
        //死循环获取accessToken+sid
        while (mLSID == null
                || TextUtils.isEmpty(mLSID.lsid)
                || TextUtils.isEmpty(mLSID.token)) {
            lsidStuts = LSIDStuts.WATING;
            //判断网络
            while (!NetUtils.isConnected(mContext)) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            lsidStuts = LSIDStuts.QUERYING;

            while (mCount < 3) {
                try {
                    mLSID = mLSIDManagerService.getLSID();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                if (!TextUtils.isEmpty(mLSID.lsid) && !TextUtils.isEmpty(mLSID.token)) {
                    lsidStuts = LSIDStuts.LSIDSUCCESS;
                    break;
                }
                //等待5s以后重新获取
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                AndroidLog.androidLog("-----while--mCount--:" + mCount);
                mCount++;
            }
            mCount = 0;
            if (TextUtils.isEmpty(mLSID.lsid) || TextUtils.isEmpty(mLSID.token)) {
                lsidStuts = LSIDStuts.WATING;
                atomicBoolean.compareAndSet(false, true);
                while (atomicBoolean.get()) {
                    AndroidLog.androidLog("-----WATING----");
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                if (Constants.isDangle()) {
                    lsidInfo.setLsid(mLSID.lsid);
                    lsidInfo.setAccessToken(mLSID.token);
                    lsidInfo.setTempCode(mLSID.tempCode);
                    lsidInfo.setRoomId(mLSID.roomId);
                } else {
                    lsidInfo.setLsid(mLSID.lsid);
                    lsidInfo.setAccessToken(mLSID.token);
                }

                lsidStuts = LSIDStuts.LSIDSUCCESS;
            }
        }
    }

    public void checkLSID() {
        if (lsidStuts == LSIDStuts.LSIDSUCCESS)
            return;
        if (lsidStuts == LSIDStuts.WATING) {
            atomicBoolean.compareAndSet(true, false);
        } else {
            mCount = 0;
        }
    }

    @Override
    public void addCallback(Callback callback) {
        mCallback = callback;
    }

    @Override
    public void removeCallback(Callback callback) {
        mCallback = null;
    }

    @Override
    public LSIDInfo refreshLSIDInfo() {
        AndroidLog.androidLog("-----refreshLSIDInfo----");
        if (lsidStuts == LSIDStuts.WATING) {
            AndroidLog.androidLog("-refreshLSIDInfo----WATING----");
            atomicBoolean.compareAndSet(true, false);
        }
        if (mLSID == null) {
            return null;
        }
        if (Constants.isDangle()) {
            lsidInfo.setLsid(mLSID.lsid);
            lsidInfo.setAccessToken(mLSID.token);
            lsidInfo.setTempCode(mLSID.tempCode);
            lsidInfo.setRoomId(mLSID.roomId);
        } else {
            lsidInfo.setLsid(mLSID.lsid);
            lsidInfo.setAccessToken(mLSID.token);
        }

        return lsidInfo;
    }

    @Override
    public LSIDInfo getLSIDInfo() {

        if (mLSID == null) {
            return null;
        }

        if (Constants.isDangle()) {
            lsidInfo.setLsid(mLSID.lsid);
            lsidInfo.setAccessToken(mLSID.token);
            lsidInfo.setTempCode(mLSID.tempCode);
            lsidInfo.setRoomId(mLSID.roomId);
        } else {
            lsidInfo.setLsid(mLSID.lsid);
            lsidInfo.setAccessToken(mLSID.token);
        }

        return lsidInfo;
    }

    @Override
    public LSIDInfo reset() {
        Log.d("yao", "mLSID.lsid:" + mLSID.lsid + " mLSID.token:" + mLSID.token);
        if (mCallback != null && mLSID != null && !TextUtils.isEmpty(mLSID.lsid))
            mCallback.onLSIDUpdate();

        if (mLSID == null) {
            return null;
        }

        if (Constants.isDangle()) {
            lsidInfo.setLsid(mLSID.lsid);
            lsidInfo.setAccessToken(mLSID.token);
            lsidInfo.setTempCode(mLSID.tempCode);
            lsidInfo.setRoomId(mLSID.roomId);
        } else {
            lsidInfo.setLsid(mLSID.lsid);
            lsidInfo.setAccessToken(mLSID.token);
        }

        return lsidInfo;
    }

    @Override
    public void setSid(String sid, String token) {
        mLSID = new LSID(sid, token);
    }

    @Override
    public void setSidAndUserId(String sid, String token, String userId) {

    }


    enum LSIDStuts {
        INIT,
        WATING,
        QUERYING,
        LSIDSUCCESS
    }


}
