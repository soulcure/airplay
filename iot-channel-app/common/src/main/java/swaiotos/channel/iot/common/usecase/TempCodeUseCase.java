package swaiotos.channel.iot.common.usecase;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import swaiotos.channel.iot.common.http.Inject;
import swaiotos.channel.iot.common.http.exception.UniteThrowable;
import swaiotos.channel.iot.common.http.net.CooCaaRestApi;
import swaiotos.channel.iot.common.response.TempCodeResponse;
import swaiotos.channel.iot.common.response.ZxingCodeResponse;
import swaiotos.channel.iot.common.utils.Constants;
import swaiotos.channel.iot.common.utils.PublicParametersUtils;
import swaiotos.channel.iot.common.utils.SignCore;
import swaiotos.channel.iot.common.utils.TYPE;
import swaiotos.channel.iot.ss.SSChannelService;
import swaiotos.channel.iot.ss.server.utils.MD5;
import swaiotos.channel.iot.utils.AndroidLog;

/**
 * @ProjectName: iot-channel-app
 * @Package: swaiotos.channel.iot.common.usecase
 * @ClassName: TempCodeUseCase
 * @Description: java类作用描述
 * @Author: wangyuehui
 * @CreateDate: 2020/10/28 14:02
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/10/28 14:02
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class TempCodeUseCase extends UseCase<TempCodeUseCase.RequestValues, TempCodeUseCase.ResponseValue, TempCodeUseCase.TempCodeCallBackListener> {
    private static TempCodeUseCase mUseCase;
    private Context mContext;

    public static TempCodeUseCase getInstance(Context context) {
        if (mUseCase == null)
            synchronized (TempCodeUseCase.class) {
                if (mUseCase == null)
                    mUseCase = new TempCodeUseCase(context);
            }

        return mUseCase;
    }

    private TempCodeUseCase(Context context) {
        mContext = context;
    }

    private void queryTempCodeResponse(@NonNull String full_url, final TempCodeUseCase.TempCodeCallBackListener baseCallBackListener) {
        Inject.getGoLiveRestApi(mContext).queryAIotCooCaaResponse(
                Constants.IOT_COOCAA_TEMP_BASE_URL + Constants.IOT_COOCAA_TEMP_PATH,
                getTempCode(full_url), TempCodeResponse.class, new CooCaaRestApi.RequestListener<TempCodeResponse>() {
                    @Override
                    public void requestSuccess(TempCodeResponse tempCodeResponse) {
                        try {
                            if (baseCallBackListener != null) {
                                if (tempCodeResponse == null) {
                                    baseCallBackListener.onError(UniteThrowable.ErrorType.RESPONSE_ERROR ,"in " + TempCodeResponse.class.getSimpleName() +" reason:"+UniteThrowable.ErrorType.RESPONSE_ERROR_MESSAGE);
                                } else if (!tempCodeResponse.getCode().equals(Constants.COOCAA_SUCCESS)) {
                                    baseCallBackListener.onError(Integer.parseInt(tempCodeResponse.getCode()) ,"in " + TempCodeResponse.class.getSimpleName() +" reason:"+tempCodeResponse.getMessage());
                                } else {
                                    baseCallBackListener.onSuccess(tempCodeResponse.getData());
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void requestError(int errorType, int errorCode, String errorMessage) {
                        //异常处理
                        if (baseCallBackListener != null) {
                            baseCallBackListener.onError(errorType,"in QRCodeUseCase error reason:"+errorMessage);
                        }
                    }
                });

    }

    private Map<String, String> getTempCode(String full_url) {

        String appKey = swaiotos.channel.iot.ss.server.utils.Constants.getLogAppKey(SSChannelService.getContext());
        String mac = PublicParametersUtils.getMac(mContext);
        String time = String.valueOf(System.currentTimeMillis()/1000);
        String prestr = "appkey"+appKey+ "full_url"+full_url +"mac"+mac +"time"+time + swaiotos.channel.iot.ss.server.utils.Constants.LOG_SECRET;
        String sign = null;
        try {
            sign = MD5.getMd5(prestr);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        AndroidLog.androidLog("sign:"+sign);

        Map<String, String> qrCodeMap = new HashMap<>();
        qrCodeMap.put("appkey", appKey);
        qrCodeMap.put("full_url",full_url);
        qrCodeMap.put("mac", mac);
        qrCodeMap.put("time",time);
        qrCodeMap.put("sign",sign);

        return qrCodeMap;
    }

    @Override
    protected void executeUseCase(RequestValues requestValues, TempCodeCallBackListener baseCallBackListener) {
        queryTempCodeResponse(requestValues.getFull_url(),baseCallBackListener);
    }

    public static class RequestValues implements UseCase.RequestValues {

        private String full_url;
        public RequestValues(String full_url) {
            this.full_url = full_url;
        }

        public String getFull_url() {
            return full_url;
        }

        public void setFull_url(String full_url) {
            this.full_url = full_url;
        }
    }

    public static class ResponseValue implements UseCase.ResponseValue {
    }

    public interface TempCodeCallBackListener extends BaseCallBackListener {
        void onError(int errType,String msg);
        void onSuccess(String data);
    }
}
