package swaiotos.channel.iot.common.usecase;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import swaiotos.channel.iot.common.entity.PushValidCode;
import swaiotos.channel.iot.common.http.Inject;
import swaiotos.channel.iot.common.http.exception.UniteThrowable;
import swaiotos.channel.iot.common.http.net.CooCaaRestApi;
import swaiotos.channel.iot.common.lsid.PadDeviceInfoManager;
import swaiotos.channel.iot.common.lsid.TvDeviceInfoManager;
import swaiotos.channel.iot.common.response.AuthCodeResponse;
import swaiotos.channel.iot.common.response.TokenResponse;
import swaiotos.channel.iot.common.utils.Constants;
import swaiotos.channel.iot.common.utils.PublicParametersUtils;
import swaiotos.channel.iot.common.utils.SignCore;
import swaiotos.channel.iot.common.utils.StringUtils;
import swaiotos.channel.iot.common.utils.TYPE;
import swaiotos.channel.iot.ss.device.PadDeviceInfo;
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

public class RegisterUseCase extends UseCase<RegisterUseCase.RequestValues, RegisterUseCase.ResponseValue, RegisterUseCase.RegisterCallBackListener> {

    private static RegisterUseCase mUseCase;
    private Context mContext;
    private static final String TAG = RegisterUseCase.class.getSimpleName();
    private RegisterCallBackListener mRegisterCallBackListener;
    private ScheduledExecutorService mScheduledExecutorService;
    private ValidBroadcastReceiver mValidBroadcastReceiver;
    private AtomicBoolean mBlockAtomicBoolean = new AtomicBoolean(true);
    private TYPE typev;

    public static RegisterUseCase getInstance(Context context) {
        if (mUseCase == null)
             synchronized (RegisterUseCase.class) {
                 if (mUseCase == null)
                     mUseCase = new RegisterUseCase(context);
             }

        return mUseCase;
    }

    private RegisterUseCase(Context context) {
        mContext = context;
        init();
    }

    private void init() {
        //监听push消息
        IntentFilter intentFilter = new IntentFilter(Constants.COOCAA_QRCODE_ACTION);
        intentFilter.addAction(Constants.COOCAA_PUSH_ACTION);
        mValidBroadcastReceiver = new ValidBroadcastReceiver();
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mValidBroadcastReceiver,intentFilter);
    }

    private void setRegisterCallBackListener(@NonNull RegisterCallBackListener registerCallBackListener) {
        mRegisterCallBackListener = registerCallBackListener;
    }

    private void setType(TYPE type) {
        this.typev = type;
    }

    private void queryAuthCodeResponse() {
        Inject.getGoLiveRestApi(mContext).queryAIotCooCaaResponse(
                Constants.getIOTServer(mContext) + Constants.COOCAA_VALID_CODE,
                getValidCode(), AuthCodeResponse.class, new CooCaaRestApi.RequestListener<AuthCodeResponse>() {
                    @Override
                    public void requestSuccess(AuthCodeResponse authCodeResponse) {
                        if (authCodeResponse == null || !authCodeResponse.getCode().equals(Constants.COOCAA_SUCCESS) ||
                                authCodeResponse.getData() == null) {
                            //异常处理
                            if (authCodeResponse == null)
                                excuteExp(AuthCodeResponse.class, "" + UniteThrowable.ErrorType.RESPONSE_ERROR, UniteThrowable.ErrorType.RESPONSE_ERROR_MESSAGE);
                            else
                                excuteExp(AuthCodeResponse.class, authCodeResponse.getCode(), authCodeResponse.getMessage());
                            return;
                        }
                        if (StringUtils.isEmpty(authCodeResponse.getData().getVerificationCode())) {
                            //走push消息
                            queryPushMsgOrRetry(authCodeResponse);
                        } else {
                            queryRegisterResponse(authCodeResponse.getData().getVerificationCode());
                        }
                    }

                    @Override
                    public void requestError(int errorType, int errorCode, String errorMessage) {
                        //异常处理
                        excuteExp(AuthCodeResponse.class, "" + errorCode, errorMessage);
                    }
                });

    }
    /**
     *
     * 重试、或接收push数据
     *
     * */
    private void queryPushMsgOrRetry(final AuthCodeResponse authCodeResponse) {
        if (!mBlockAtomicBoolean.get())
            return;
        mBlockAtomicBoolean.compareAndSet(true,false);
        if (StringUtils.isEmpty(authCodeResponse.getData().getRetryNumber()) ||
                StringUtils.isEmpty(authCodeResponse.getData().getRetryIntervalTime())) {
            return;
        }

        final AtomicInteger atomicInteger = new AtomicInteger(Integer.parseInt(authCodeResponse.getData().getRetryNumber()));
        //重试机制
        mScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        mScheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (atomicInteger.getAndDecrement() <= 0){
                    mScheduledExecutorService.shutdownNow();
                } else {
                    queryAuthCodeResponse();
                }

            }
        },Integer.parseInt(authCodeResponse.getData().getRetryIntervalTime()),
                Integer.parseInt(authCodeResponse.getData().getRetryIntervalTime()), TimeUnit.SECONDS);

    }

    @SuppressLint("NewApi")
    private Map<String, String> getValidCode() {
        Map<String, String> validCodeMap = new HashMap<>();

        validCodeMap.put(Constants.COOCAA_VALIDE_CODE_DEVICEID, PublicParametersUtils.getcUDID(mContext));
        validCodeMap.put(Constants.COOCAA_TIME,""+System.currentTimeMillis());
        validCodeMap.put(Constants.COOCAA_SIGN, SignCore.buildRequestMysign(validCodeMap, Constants.getAppKey(mContext)));
        return validCodeMap;
    }

    private void queryRegisterResponse(final String authCode) {
        Inject.getGoLiveRestApi(mContext).queryAIotCooCaaResponse(
                Constants.getIOTServer(mContext) + Constants.COOCAA_REGISTER_LOGIN,
                getRegisterLogin(authCode), TokenResponse.class, new CooCaaRestApi.RequestListener<TokenResponse>() {
                    @Override
                    public void requestSuccess(TokenResponse tokenResponse) {
                        if (tokenResponse == null || !tokenResponse.getCode().equals(Constants.COOCAA_SUCCESS)
                                || tokenResponse.getData() == null) {
                            //异常处理
                            if (tokenResponse == null)
                                excuteExp(TokenResponse.class, "" + UniteThrowable.ErrorType.RESPONSE_ERROR, UniteThrowable.ErrorType.RESPONSE_ERROR_MESSAGE);
                            else
                                excuteExp(TokenResponse.class, tokenResponse.getCode(), tokenResponse.getMessage());
                            return;
                        }
                        mRegisterCallBackListener.onSuccess(tokenResponse.getData().getAccessToken());
                    }

                    @Override
                    public void requestError(int errorType, int errorCode, String errorMessage) {
                        //异常处理
                        excuteExp(TokenResponse.class, "" + errorCode, errorMessage);
                    }
                }
        );
    }

    private Map<String, String> getRegisterLogin(String authCode) {
        Map<String, String> registerLoginMap = new HashMap<>();
        if (typev == TYPE.TV){
            if (swaiotos.channel.iot.ss.server.utils.Constants.isDangle()) {
                registerLoginMap.put(Constants.COOCAA_REGISTER_LOGIN_TYPE,"dongle");
            } else {
                registerLoginMap.put(Constants.COOCAA_REGISTER_LOGIN_TYPE,"tv");
            }
            //tv和dongle都返回创建房间
            registerLoginMap.put(Constants.COOCAA_CREATE_ROOM,"1");

            registerLoginMap.put(Constants.COOCAA_DEVICE_INFO,""+ JSONObject.toJSONString(TvDeviceInfoManager.getInstance(mContext).getDeviceInfo()));
            Log.d(TAG,JSONObject.toJSONString(TvDeviceInfoManager.getInstance(mContext).getDeviceInfo()));
        } else if (typev == TYPE.PAD) {
            registerLoginMap.put(Constants.COOCAA_REGISTER_LOGIN_TYPE,"pad");
            PadDeviceInfo padDeviceInfo = PadDeviceInfoManager.getInstance(mContext).getDeviceInfo();
            registerLoginMap.put(Constants.COOCAA_DEVICE_INFO,""+ JSONObject.toJSONString(padDeviceInfo));
            Log.d(TAG,JSONObject.toJSONString(padDeviceInfo));
        } else {
            registerLoginMap.put(Constants.COOCAA_REGISTER_LOGIN_TYPE,"tv");
            registerLoginMap.put(Constants.COOCAA_DEVICE_INFO,""+ JSONObject.toJSONString(TvDeviceInfoManager.getInstance(mContext).getDeviceInfo()));
        }
        registerLoginMap.put(Constants.COOCAA_REGISTER_LOGIN_NAME,"");
        registerLoginMap.put(Constants.COOCAA_REGISTER_LOGIN_CODE, authCode);
        registerLoginMap.put(Constants.COOCAA_TIME,""+System.currentTimeMillis());
        registerLoginMap.put(Constants.COOCAA_SIGN, SignCore.buildRequestMysign(registerLoginMap, Constants.getAppKey(mContext)));
        return registerLoginMap;
    }

    private void excuteExp(Class<?> cls,String errType,String reasion ) {
        if (mRegisterCallBackListener != null) {
            mRegisterCallBackListener.onError(errType,"in " + cls.getSimpleName()+" "+reasion);
        }
    }

    @Override
    protected void executeUseCase(RequestValues requestValues, @NonNull RegisterCallBackListener baseCallBackListener) {
        setRegisterCallBackListener(baseCallBackListener);
        setType(requestValues.type);

        queryAuthCodeResponse();
    }

    public static class RequestValues implements UseCase.RequestValues {

        private TYPE type;
        public RequestValues(TYPE type) {
            this.type = type;
        }
        public TYPE getType() {
            return type;
        }
    }

    public static class ResponseValue implements UseCase.ResponseValue {
    }

    public interface RegisterCallBackListener extends BaseCallBackListener {
        void onError(String errType,String msg);
        void onSuccess(String token);
    }

    private class ValidBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.COOCAA_PUSH_ACTION)) {
                String pushMsg = intent.getStringExtra(Constants.COOCAA_PUSH_MSG);
                PushValidCode pushValidCode = JSON.parseObject(pushMsg, PushValidCode.class);
                Log.d(TAG,"pushMsg:"+pushMsg);
                if (pushValidCode != null && pushValidCode.getIot_chanel() != null &&
                    pushValidCode.getIot_chanel().getCmd() != null && pushValidCode.getIot_chanel().getCmd().equals("IOT_CHANNEL_VALID_CODE")) {
                    if (pushValidCode.getIot_chanel().getData() != null &&
                            !StringUtils.isEmpty(pushValidCode.getIot_chanel().getData().getValidCode())) {
                        queryRegisterResponse(pushValidCode.getIot_chanel().getData().getValidCode());
                        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mValidBroadcastReceiver);
                        mValidBroadcastReceiver = null;
                    }
                }
            }
        }
    }
}
