package swaiotos.channel.iot.common.usecase;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import swaiotos.channel.iot.common.http.Inject;
import swaiotos.channel.iot.common.http.exception.UniteThrowable;
import swaiotos.channel.iot.common.http.net.CooCaaRestApi;
import swaiotos.channel.iot.common.response.CooCaaResponse;
import swaiotos.channel.iot.common.response.ZxingCodeResponse;
import swaiotos.channel.iot.common.utils.Constants;
import swaiotos.channel.iot.common.utils.SignCore;
import swaiotos.channel.iot.common.utils.TYPE;

/**
 * @ProjectName: iot-channel-app
 * @Package: swaiotos.channel.iot.common.usecase
 * @ClassName: UpdateDeviceInfoUseCase
 * @Description: java类作用描述
 * @Author: wangyuehui
 * @CreateDate: 2020/5/14 20:08
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/5/14 20:08
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class UpdateDeviceInfoUseCase extends UseCase<UpdateDeviceInfoUseCase.RequestValues,UpdateDeviceInfoUseCase.ResponseValue,UpdateDeviceInfoUseCase.UpdateDeviceInfoCallBackListener> {

    private static UpdateDeviceInfoUseCase mUseCase;
    private Context mContext;
    private static final String TAG = UpdateDeviceInfoUseCase.class.getSimpleName();
    private UpdateDeviceInfoUseCase.UpdateDeviceInfoCallBackListener mUpdateDeviceInfoCallBackListener;
    private String mAccessToken;
    private String mDeviceInfo;

    public static UpdateDeviceInfoUseCase getInstance(Context context) {
        if (mUseCase == null)
            synchronized (QRCodeUseCase.class) {
                if (mUseCase == null)
                    mUseCase = new UpdateDeviceInfoUseCase(context);
            }

        return mUseCase;
    }

    private UpdateDeviceInfoUseCase(Context context) {
        mContext = context;
    }

    private void setAccessToken(String accessToken) {
        this.mAccessToken = accessToken;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.mDeviceInfo = deviceInfo;
    }

    private void setUpdateDeviceInfoCallBackListener(UpdateDeviceInfoCallBackListener updateDeviceInfoCallBackListener) {
        this.mUpdateDeviceInfoCallBackListener = updateDeviceInfoCallBackListener;
    }

    @Override
    protected void executeUseCase(UpdateDeviceInfoUseCase.RequestValues requestValues, UpdateDeviceInfoCallBackListener baseCallBackListener) {
        setUpdateDeviceInfoCallBackListener(baseCallBackListener);
        setAccessToken(requestValues.mAccessToken);
        setDeviceInfo(requestValues.deviceInfo);

        queryUpdateDeviceInfoResponse();
    }

    private void queryUpdateDeviceInfoResponse() {
        Inject.getGoLiveRestApi(mContext).queryAIotCooCaaResponse(
                Constants.getIOTServer(mContext) + Constants.COOCAA_UPDATE_DEVICEINFO,
                getDeviceDeviceInfo(), CooCaaResponse.class, new CooCaaRestApi.RequestListener<CooCaaResponse>() {
                    @Override
                    public void requestSuccess(CooCaaResponse cooCaaResponse) {
                        if (cooCaaResponse == null || !cooCaaResponse.getCode().equals(Constants.COOCAA_SUCCESS)) {
                            //异常处理
                            //异常处理
                            if (cooCaaResponse == null)
                                excuteExp(CooCaaResponse.class, "" + UniteThrowable.ErrorType.RESPONSE_ERROR, UniteThrowable.ErrorType.RESPONSE_ERROR_MESSAGE);
                            else {
                                excuteExp(CooCaaResponse.class, cooCaaResponse.getCode(), cooCaaResponse.getMessage());
                            }
                            return;
                        }

                        if (mUpdateDeviceInfoCallBackListener != null)
                            mUpdateDeviceInfoCallBackListener.onSuccess();
                    }

                    @Override
                    public void requestError(int errorType, int errorCode, String errorMessage) {
                        //异常处理
                        excuteExp(ZxingCodeResponse.class, "" + errorCode, errorMessage);
                    }
                });
    }

    private void excuteExp(Class<?> cls,String errType,String errorMessage ) {
        if (mUpdateDeviceInfoCallBackListener != null) {
            mUpdateDeviceInfoCallBackListener.onError(errType ," errorMessage:"+errorMessage);
        }
    }

    private Map<String, String> getDeviceDeviceInfo() {
        Map<String, String> qrCodeMap = new HashMap<>();

        qrCodeMap.put(Constants.COOCAA_ACCESSTOKEN,mAccessToken);
        qrCodeMap.put(Constants.COOCAA_DEVICE_INFO,mDeviceInfo);
        return qrCodeMap;
    }

    public static class RequestValues implements UseCase.RequestValues {
        private String mAccessToken;
        private String deviceInfo;

        public RequestValues(@NonNull String accessToken, String deviceInfo) {
            this.mAccessToken = accessToken;
            this.deviceInfo = deviceInfo;
        }

        public String getmAccessToken() {
            return mAccessToken;
        }

        public String getDeviceInfo() {
            return deviceInfo;
        }
    }

    public static class ResponseValue implements UseCase.ResponseValue {

    }


    public interface UpdateDeviceInfoCallBackListener extends BaseCallBackListener {
        void onError(String errorType,String msg);
        void onSuccess();
    }
}
