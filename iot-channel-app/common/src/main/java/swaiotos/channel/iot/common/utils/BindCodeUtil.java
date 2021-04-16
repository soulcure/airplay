package swaiotos.channel.iot.common.utils;

import android.content.Context;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import swaiotos.channel.iot.common.lsid.LSID;
import swaiotos.channel.iot.common.usecase.LSIDUseCase;
import swaiotos.channel.iot.common.usecase.QRCodeUseCase;
import swaiotos.channel.iot.common.usecase.RegisterUseCase;
import swaiotos.channel.iot.ss.server.ShareUtls;
import swaiotos.channel.iot.utils.NetUtils;


/**
 * @author chenaojun
 */
public class BindCodeUtil {

    private final int FIRST_BEAT = 0;
    private final int HEART_BEAT_INTERVAL = 60;
    private String TAG = BindCodeUtil.class.getSimpleName();
    private ScheduledExecutorService heartBeatScheduled;

    public interface BindCodeCall {

        /**
         * 展示二维码
         *
         * @param bindCode
         * @param url
         * @param expiresIn
         */
        void onBindBitmapShow(String bindCode, String url, String expiresIn) throws RemoteException;
    }

    public interface TokenCodeCall {

        /**
         *
         */
        void getToken(String token);

        void getTokenError(String error,String msg) ;
    }

    private static class BindCodeUtilHolder {
        private static final BindCodeUtil INSTANCE = new BindCodeUtil();
    }

    public static final BindCodeUtil getInstance() {
        return BindCodeUtilHolder.INSTANCE;
    }

    private void loadBindCode(final Context context, TYPE type, String token, final BindCodeCall codeCall) {

        QRCodeUseCase.getInstance(context).run(new QRCodeUseCase.RequestValues(token, type), new QRCodeUseCase.QRCodeCallBackListener() {
            @Override
            public void onError(String errType, String msg) {
                Log.d(TAG, "QRCodeUseCase:" + msg);
            }

            @Override
            public void onSuccess(String bindCode, String url, String expiresIn, String typeLoopTime) {
                try {
                    codeCall.onBindBitmapShow(bindCode, url, expiresIn);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

        });
    }

    public void getTempBindCode(final Context context, final TYPE type, final TokenCodeCall tokenCodeCall) {
        Log.d(TAG, "getBindCode: ");
        String accessToken = null;
        try {
            if (new File(context.getFilesDir(), Constants.COOCAA_FILE_ACCESSTOKEN_NAME).exists()) {
                //首先读取文件中的accessToken
                accessToken = FileAccessTokenUtils.getDataFromFile(context, Constants.COOCAA_FILE_ACCESSTOKEN_NAME);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //为空情况读取SharedPreferences
        if (TextUtils.isEmpty(accessToken)) {
            accessToken = ShareUtls.getInstance(context).getString(Constants.COOCAA_PREF_ACCESSTOKEN, "");
        }
        if (TextUtils.isEmpty(accessToken)) {
            RegisterUseCase.getInstance(context).run(
                    new RegisterUseCase.RequestValues(type),
                    new RegisterUseCase.RegisterCallBackListener() {
                        @Override
                        public void onError(String errType, String msg) {
                            Log.d(TAG, "Register/login query error msg:" + msg);
                            tokenCodeCall.getTokenError(errType,msg);
                        }

                        @Override
                        public void onSuccess(final String token) {
                            ShareUtls.getInstance(context).putString(Constants.COOCAA_PREF_ACCESSTOKEN, token);
                            FileAccessTokenUtils.saveDataToFile(context, Constants.COOCAA_FILE_ACCESSTOKEN_NAME, token);

                            tokenCodeCall.getToken(token);
                        }
                    }
            );
        } else {
            tokenCodeCall.getToken(accessToken);
        }
    }

    public void getBindCode(final Context context, final TYPE type, final BindCodeCall codeCall) {
        Log.d(TAG, "getBindCode: ");
        String accessToken = null;
        try {
            if (new File(context.getFilesDir(), Constants.COOCAA_FILE_ACCESSTOKEN_NAME).exists()) {
                //首先读取文件中的accessToken
                accessToken = FileAccessTokenUtils.getDataFromFile(context, Constants.COOCAA_FILE_ACCESSTOKEN_NAME);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //为空情况读取SharedPreferences
        if (TextUtils.isEmpty(accessToken)) {
            accessToken = ShareUtls.getInstance(context).getString(Constants.COOCAA_PREF_ACCESSTOKEN, "");
        }
        if (accessToken.equals("")) {
            RegisterUseCase.getInstance(context).run(
                    new RegisterUseCase.RequestValues(type),
                    new RegisterUseCase.RegisterCallBackListener() {
                        @Override
                        public void onError(String errType, String msg) {
                            Log.d(TAG, "Register/login query error msg:" + msg);
                        }

                        @Override
                        public void onSuccess(final String token) {
                            ShareUtls.getInstance(context).putString(Constants.COOCAA_PREF_ACCESSTOKEN, token);
                            FileAccessTokenUtils.saveDataToFile(context, Constants.COOCAA_FILE_ACCESSTOKEN_NAME, token);
                            loadBindCode(context, type, token, codeCall);
                        }
                    }
            );
        } else {
            loadBindCode(context, type, accessToken, codeCall);
        }
    }

    /**
     * 开始心跳
     */
    public void startHeartBeat(final Context context, final TYPE type, final BindCodeCall codeCall) {
        Log.d(TAG, "startHeartBeat: 6");
        if (heartBeatScheduled == null) {
            heartBeatScheduled = Executors.newScheduledThreadPool(1);
        }

        Log.d(TAG, "startHeartBeat: 7");
        if (!heartBeatScheduled.isShutdown()) {
            heartBeatScheduled.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "run: ");
                    getBindCode(context, type, codeCall);
                }
            }, FIRST_BEAT, HEART_BEAT_INTERVAL, TimeUnit.SECONDS);
        }
    }

    /**
     * 停止心跳
     */
    public void stopHeartBeat() {
        Log.d(TAG, "socket client stopHeartBeat---");
        if (heartBeatScheduled != null) {
            heartBeatScheduled.shutdown();
            heartBeatScheduled = null;
        }
    }


}
