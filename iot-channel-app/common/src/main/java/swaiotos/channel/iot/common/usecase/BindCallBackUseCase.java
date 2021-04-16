package swaiotos.channel.iot.common.usecase;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import swaiotos.channel.iot.common.http.Inject;
import swaiotos.channel.iot.common.http.exception.UniteThrowable;
import swaiotos.channel.iot.common.http.net.CooCaaRestApi;
import swaiotos.channel.iot.common.response.QueryQrCodeResponse;
import swaiotos.channel.iot.common.utils.Constants;
import swaiotos.channel.iot.common.utils.StringUtils;
/**
 * @ProjectName: iot-channel-tv
 * @Package: swaiotos.channel.iot.pad.utils
 * @ClassName: BindUseCase
 * @Description: 绑定设备
 * @Author: wangyuehui
 * @CreateDate: 2020/4/11 11:22
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/4/11 11:22
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class BindCallBackUseCase extends UseCase<BindCallBackUseCase.RequestValues, BindCallBackUseCase.ResponseValue, BindCallBackUseCase.BindCallBackListener> {

    private static BindCallBackUseCase mUseCase;
    private Context mContext;
    private static final String TAG = BindCallBackUseCase.class.getSimpleName();
    private BindCallBackListener mBindCallBackListener;
    private String mBindCode,mOldBindCode;

    public static BindCallBackUseCase getInstance(Context context) {
        if (mUseCase == null)
             synchronized (BindCallBackUseCase.class) {
                 if (mUseCase == null)
                     mUseCase = new BindCallBackUseCase(context);
             }

        return mUseCase;
    }

    private BindCallBackUseCase(Context context) {
        mContext = context;
    }


    private void setBindCallBackListener(@NonNull BindCallBackListener bindCallBackListener) {
        mBindCallBackListener = bindCallBackListener;
    }

    private void setNewBindCode(@NonNull String bindCode) {
        mBindCode = bindCode;
    }

    private void setOldBindCode(@NonNull String bindCode) {
        mOldBindCode = bindCode;
    }



    private void queryPollResponse() {

        Inject.getGoLiveRestApi(mContext).queryAIotCooCaaResponse(
                Constants.getIOTServer(mContext) + Constants.COOCAA_QUERY_CODE,
                getQueryQrCodeMap(), QueryQrCodeResponse.class, new CooCaaRestApi.RequestListener<QueryQrCodeResponse>() {
                    @Override
                    public void requestSuccess(QueryQrCodeResponse queryQrCodeResponse) {
                        if (queryQrCodeResponse == null || !queryQrCodeResponse.getCode().equals(Constants.COOCAA_SUCCESS) ||
                                queryQrCodeResponse.getData() == null || StringUtils.isEmpty(queryQrCodeResponse.getData().getBindCodeType())) {
                            //异常处理

                            if (queryQrCodeResponse == null) {
                                excuteExp(QueryQrCodeResponse.class, "" + UniteThrowable.ErrorType.RESPONSE_ERROR, UniteThrowable.ErrorType.RESPONSE_ERROR_MESSAGE);
                            } else {
                                excuteExp(QueryQrCodeResponse.class, queryQrCodeResponse.getCode(), queryQrCodeResponse.getMessage());
                            }
                            return;
                        }

                        if (mBindCallBackListener != null)
                            mBindCallBackListener.onSuccess(queryQrCodeResponse.getData().getBindCodeType());
                    }

                    @Override
                    public void requestError(int errorType, int errorCode, String errorMessage) {
                        //异常处理
                        excuteExp(QueryQrCodeResponse.class, "" + errorCode, errorMessage);
                    }
                });
    }

    private Map<String, String> getQueryQrCodeMap() {
        Map<String, String> validCodeMap = new HashMap<>();

        validCodeMap.put(Constants.COOCAA_BINDCODE, mBindCode);
        validCodeMap.put(Constants.COOCAA_OLD_BINDCODE, mOldBindCode);
        return validCodeMap;
    }


    private void excuteExp(Class<?> cls,String errType,String reasion ) {
        if (mBindCallBackListener != null) {
            mBindCallBackListener.onError(cls.getSimpleName()+" reason:"+reasion,errType);
        }
    }

    @Override
    protected void executeUseCase(RequestValues requestValues, @NonNull BindCallBackListener baseCallBackListener) {
        setBindCallBackListener(baseCallBackListener);
        setNewBindCode(requestValues.getmBindCode());
        setOldBindCode(requestValues.getmOldBindCode());

        queryPollResponse();
    }

    public static class RequestValues implements UseCase.RequestValues {
        private String mBindCode;
        private String mOldBindCode;
        public RequestValues(String mBindCode,String oldBindCode) {
            this.mBindCode = mBindCode;
            this.mOldBindCode = oldBindCode;
        }

        public String getmBindCode() {
            return mBindCode;
        }

        public String getmOldBindCode() {
            return mOldBindCode;
        }
    }

    public static class ResponseValue implements UseCase.ResponseValue {

    }


    public interface BindCallBackListener extends BaseCallBackListener {
        void onError(String msg, String errorType);
        void onSuccess(String bindCodeType);
    }
}
