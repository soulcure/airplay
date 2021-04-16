package swaiotos.channel.iot.common.lsid;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import swaiotos.channel.iot.common.account.AccountInfo;
import swaiotos.channel.iot.common.account.AccountManager;
import swaiotos.channel.iot.common.utils.PublicParametersUtils;
import swaiotos.channel.iot.ss.device.PadDeviceInfo;

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
public class PadDeviceInfoManager {
    private static PadDeviceInfoManager mDeviceInfoManager;
    private Context mContext;
    public String activeId;
    public String deviceName;
    public String mChip, mModel, mSize;
    public String mAccount;
    public String mNickName;

    public static PadDeviceInfoManager getInstance(Context context) {
        if (mDeviceInfoManager == null)
            synchronized (PadDeviceInfoManager.class) {
                if (mDeviceInfoManager == null)
                    mDeviceInfoManager = new PadDeviceInfoManager(context);
            }
        return mDeviceInfoManager;
    }

    private PadDeviceInfoManager(Context context) {
        this.mContext = context;
        init();
    }

    private void init() {
        mChip = PublicParametersUtils.getcChip(mContext);
        mModel = PublicParametersUtils.getcModel(mContext);
        mSize = PublicParametersUtils.getcSize(mContext);
        deviceName = PublicParametersUtils.getdeviceName(mContext);
        activeId = PublicParametersUtils.getcUDID(mContext);
        mAccount = "";
        mNickName = "";
    }

    /**updateMovieSource
     *
     * 默认获取设备信息
     *
     * */
    public PadDeviceInfo getDeviceInfo() {

        if(AccountManager.getManager(mContext).hasLogin()) {
            AccountInfo accountInfo = AccountManager.getManager(mContext).getAccountInfo();
            mAccount = accountInfo.user_id;
            if (TextUtils.isEmpty(mAccount))
                mAccount = "";
            mNickName = accountInfo.nick_name;
            if (TextUtils.isEmpty(mNickName))
                mNickName = "";
        } else {
            mAccount = "";
            mNickName = "";
        }

        return new PadDeviceInfo(activeId,
                deviceName,
                mChip,
                mModel,
                mSize,
                mAccount,
                mNickName);
    }


}
