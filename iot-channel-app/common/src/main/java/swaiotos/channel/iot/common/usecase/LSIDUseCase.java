package swaiotos.channel.iot.common.usecase;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import swaiotos.channel.iot.common.http.Inject;
import swaiotos.channel.iot.common.http.exception.UniteThrowable;
import swaiotos.channel.iot.common.http.net.CooCaaRestApi;
import swaiotos.channel.iot.common.response.AuthCodeResponse;
import swaiotos.channel.iot.common.response.UserLSIDResponse;
import swaiotos.channel.iot.common.utils.Constants;
import swaiotos.channel.iot.common.utils.StringUtils;
import swaiotos.channel.iot.common.utils.TYPE;
import swaiotos.channel.iot.ss.server.ShareUtls;


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

public class LSIDUseCase extends UseCase<LSIDUseCase.RequestValues, LSIDUseCase.ResponseValue, LSIDUseCase.QuerylSIDCallBackListener> {

    private static LSIDUseCase mUseCase;
    private Context mContext;
    private static final String TAG = LSIDUseCase.class.getSimpleName();
    private QuerylSIDCallBackListener mQuerylSIDCallBackListener;
    private String mAccessToken;
    private TYPE type;

    public static LSIDUseCase getInstance(Context context) {
        if (mUseCase == null)
             synchronized (LSIDUseCase.class) {
                 if (mUseCase == null)
                     mUseCase = new LSIDUseCase(context);
             }

        return mUseCase;
    }

    private LSIDUseCase(Context context) {
        mContext = context;
    }


    private LSIDUseCase setBindSubmitCallBackListener(@NonNull QuerylSIDCallBackListener querylSIDCallBackListener) {
        mQuerylSIDCallBackListener = querylSIDCallBackListener;
        return this;
    }

    private LSIDUseCase setAccessToken(@NonNull String token) {
        mAccessToken = token;
        return this;
    }

    private void setTYPE(TYPE type) {
        this.type = type;
    }

    private void queryUserLSIDResponse() {
        Inject.getGoLiveRestApi(mContext).queryAIotCooCaaResponse(
                Constants.getIOTServer(mContext) + Constants.COOCAA_USERINFO,
                getUserLSID(), UserLSIDResponse.class, new CooCaaRestApi.RequestListener<UserLSIDResponse>() {
                    @Override
                    public void requestSuccess(UserLSIDResponse userLSIDResponse) {
                        if (userLSIDResponse == null || !userLSIDResponse.getCode().equals(Constants.COOCAA_SUCCESS) ||
                                userLSIDResponse.getData() == null) {
                            //异常处理
                            if (userLSIDResponse == null)
                                excuteExp(AuthCodeResponse.class, ""+ UniteThrowable.ErrorType.RESPONSE_ERROR, UniteThrowable.ErrorType.RESPONSE_ERROR_MESSAGE);
                            else {
                                excuteExp(AuthCodeResponse.class, userLSIDResponse.getCode(),userLSIDResponse.getMessage());
                            }
                            return;
                        }

                        if (mQuerylSIDCallBackListener != null) {
                            if (swaiotos.channel.iot.ss.server.utils.Constants.isDangle()) {
                                mQuerylSIDCallBackListener.onSuccess(userLSIDResponse.getData().getZpLsid(),userLSIDResponse.getData().getDeviceInfo(),userLSIDResponse.getData().getBindCode(),
                                        userLSIDResponse.getData().getRoomId());
                            } else {
                                mQuerylSIDCallBackListener.onSuccess(userLSIDResponse.getData().getZpLsid(),userLSIDResponse.getData().getDeviceInfo());
                            }
                        }

                    }

                    @Override
                    public void requestError(int errorType, int errorCode, String errorMessage) {
                        //异常处理
                        excuteExp(UserLSIDResponse.class, ""+errorCode,errorMessage);
                    }
                });
    }

    private Map<String, String> getUserLSID() {
        Map<String, String> registerLoginMap = new HashMap<>();

        registerLoginMap.put(Constants.COOCAA_ACCESSTOKEN,mAccessToken);
        //tv和dongle都返回创建房间
        registerLoginMap.put(Constants.COOCAA_ACC_CREATEROOM,Constants.COOCAA_DANGLE);
        if (swaiotos.channel.iot.ss.server.utils.Constants.isDangle())
            registerLoginMap.put(Constants.CC_REGISTER_TYPE,Constants.CC_DONGLE);

        return registerLoginMap;
    }

    private void excuteExp(Class<?> cls,String errType,String reasion ) {
        if (mQuerylSIDCallBackListener != null) {
            mQuerylSIDCallBackListener.onError(errType ,"in " + cls.getSimpleName() +" reason:"+reasion);
        }
    }

    @Override
    protected void executeUseCase(RequestValues requestValues, @NonNull QuerylSIDCallBackListener baseCallBackListener) {
        setBindSubmitCallBackListener(baseCallBackListener);
        setAccessToken(requestValues.getmAccessToken());
        setTYPE(requestValues.getType());

        queryUserLSIDResponse();
    }

    public static class RequestValues implements UseCase.RequestValues {

        private String mAccessToken;
        private TYPE type;

        public RequestValues(String accessTokenPref, TYPE type) {
            this.mAccessToken = accessTokenPref;
            this.type = type;
        }

        public String getmAccessToken() {
            return mAccessToken;
        }

        public TYPE getType() {
            return type;
        }
    }

    public static class ResponseValue implements UseCase.ResponseValue {
    }

    public interface QuerylSIDCallBackListener extends BaseCallBackListener {
        void onError(String errorType,String msg);
        //sid ,isQuery true获取LSID服务的信息
        void onSuccess(String sid,String deviceInfo);

        void onSuccess(String sid,String deviceInfo,String tempCode,String roomId);
    }

}
