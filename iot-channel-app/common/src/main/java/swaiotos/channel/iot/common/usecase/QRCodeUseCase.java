package swaiotos.channel.iot.common.usecase;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import swaiotos.channel.iot.common.http.Inject;
import swaiotos.channel.iot.common.http.exception.UniteThrowable;
import swaiotos.channel.iot.common.http.net.CooCaaRestApi;
import swaiotos.channel.iot.common.response.ZxingCodeResponse;
import swaiotos.channel.iot.common.utils.Constants;
import swaiotos.channel.iot.common.utils.SignCore;
import swaiotos.channel.iot.common.utils.TYPE;
import swaiotos.channel.iot.ss.server.ShareUtls;

/**
 * @ProjectName: iot-channel-tv
 * @Package: swaiotos.channel.iot.pad.utils
 * @ClassName: BindUseCase
 * @Description: 绑定二维码
 * @Author: wangyuehui
 * @CreateDate: 2020/4/11 11:22
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/4/11 11:22
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class QRCodeUseCase extends UseCase<QRCodeUseCase.RequestValues, QRCodeUseCase.ResponseValue, QRCodeUseCase.QRCodeCallBackListener> {

    private static QRCodeUseCase mUseCase;
    private Context mContext;
    private static final String TAG = QRCodeUseCase.class.getSimpleName();
    private TYPE typev;

    public static QRCodeUseCase getInstance(Context context) {
        if (mUseCase == null)
             synchronized (QRCodeUseCase.class) {
                 if (mUseCase == null)
                     mUseCase = new QRCodeUseCase(context);
             }

        return mUseCase;
    }

    private QRCodeUseCase(Context context) {
        mContext = context;
    }

    private void setType(TYPE type) {
        this.typev = type;
    }

    private void queryQRCodeResponse(@NonNull String accessToken,boolean isDangle,final QRCodeCallBackListener baseCallBackListener) {
        Inject.getGoLiveRestApi(mContext).queryAIotCooCaaResponse(
                Constants.getIOTServer(mContext) + Constants.COOCAA_QRCODE,
                getQRCode(accessToken,isDangle), ZxingCodeResponse.class, new CooCaaRestApi.RequestListener<ZxingCodeResponse>() {
                    @Override
                    public void requestSuccess(ZxingCodeResponse zxingCodeResponse) {
                        if (zxingCodeResponse == null || !zxingCodeResponse.getCode().equals(Constants.COOCAA_SUCCESS)) {
                            //异常处理
                            //异常处理
                            if (zxingCodeResponse == null)
                                excuteExp(ZxingCodeResponse.class, "" + UniteThrowable.ErrorType.RESPONSE_ERROR, UniteThrowable.ErrorType.RESPONSE_ERROR_MESSAGE);
                            else {
                                excuteExp(ZxingCodeResponse.class, zxingCodeResponse.getCode(), zxingCodeResponse.getMessage());
                            }

                            return;
                        }

                        if (baseCallBackListener != null)
                            baseCallBackListener.onSuccess(zxingCodeResponse.getData().getBindCode(),zxingCodeResponse.getData().getUrl(),zxingCodeResponse.getData().getExpires_in(), zxingCodeResponse.getData().getType_loop_time());
                    }

                    @Override
                    public void requestError(int errorType, int errorCode, String errorMessage) {
                        //异常处理
                        if (baseCallBackListener != null) {
                            baseCallBackListener.onError(""+errorType,"in QRCodeUseCase error reason:"+errorMessage);
                        }
                    }
                });

    }

    private Map<String, String> getQRCode(String accessToken,boolean isDangle) {
        Map<String, String> qrCodeMap = new HashMap<>();

        qrCodeMap.put(Constants.COOCAA_ACCESSTOKEN,accessToken);
        if (isDangle)
            qrCodeMap.put(Constants.COOCAA_TEMPBIND,Constants.COOCAA_DANGLE);
        qrCodeMap.put(Constants.COOCAA_TIME,""+System.currentTimeMillis());
        qrCodeMap.put(Constants.COOCAA_SIGN, SignCore.buildRequestMysign(qrCodeMap, Constants.getAppKey(mContext)));
        return qrCodeMap;
    }

    private void excuteExp(Class<?> cls,String errType,String reasion ) {

    }


    @Override
    protected void executeUseCase(@NonNull RequestValues requestValues, @NonNull QRCodeCallBackListener baseCallBackListener) {
        setType(requestValues.type);

        queryQRCodeResponse(requestValues.getmAccessToken(),requestValues.isDangle(),baseCallBackListener);

    }

    public static class RequestValues implements UseCase.RequestValues {
        private String mAccessToken;
        private TYPE type;
        private boolean isDangle;

        public RequestValues(@NonNull String accessToken,TYPE type) {
            this.mAccessToken = accessToken;
            this.type = type;
            isDangle = false;
        }

        public RequestValues(@NonNull String accessToken,TYPE type,boolean isDangle) {
            this.mAccessToken = accessToken;
            this.type = type;
            this.isDangle = isDangle;
        }

        public String getmAccessToken() {
            return mAccessToken;
        }

        public TYPE getType() {
            return type;
        }

        public boolean isDangle() {
            return isDangle;
        }
    }

    public static class ResponseValue implements UseCase.ResponseValue {

    }


    public interface QRCodeCallBackListener extends BaseCallBackListener {
        void onError(String errType,String msg);
        void onSuccess(String bindCode, String url,String expiresIn, String typeLoopTime);
    }
}
