package com.coocaa.smartscreen.repository.service.impl;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.coocaa.smartscreen.data.account.CoocaaUserInfo;
import com.coocaa.smartscreen.data.account.AccountLoginInfo;
import com.coocaa.smartscreen.data.account.TpTokenInfo;
import com.coocaa.smartscreen.data.account.UpdateAvatarBean;
import com.coocaa.smartscreen.data.account.YunXinUserInfo;
import com.coocaa.smartscreen.data.device.RegisterLogin;
import com.coocaa.smartscreen.data.device.RegisterLoginResp;
import com.coocaa.smartscreen.data.device.TVSourceModel;
import com.coocaa.smartscreen.data.device.UpdateDeviceInfoResp;
import com.coocaa.smartscreen.network.ObserverAdapter;
import com.coocaa.smartscreen.network.NetWorkManager;
import com.coocaa.smartscreen.network.ResponseTransformer;
import com.coocaa.smartscreen.network.common.Constants;
import com.coocaa.smartscreen.network.exception.ApiException;
import com.coocaa.smartscreen.network.util.ParamsUtil;
import com.coocaa.smartscreen.repository.callback.RepositoryCallback;
import com.coocaa.smartscreen.repository.future.InvocateFuture;
import com.coocaa.smartscreen.repository.service.LoginRepository;
import com.coocaa.smartscreen.repository.utils.Preferences;
import com.coocaa.smartscreen.utils.StringUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import swaiotos.channel.iot.ss.device.PhoneDeviceInfo;

import static com.coocaa.smartscreen.network.ResponseTransformer.SUCCESS_CODE_1;


public class LoginRepositoryImpl implements LoginRepository {
    private static final String TAG = LoginRepositoryImpl.class.getSimpleName();
    private String codeKey = "4a5684a62d40aab96b4ab8b18a57da2c"; //32位随机数


    @Override
    public InvocateFuture<Bitmap> getImageCaptcha(final int width, final int height) {
        return new InvocateFuture<Bitmap>() {
            @Override
            public void setCallback(final RepositoryCallback<Bitmap> callback) {
                callback.onStart();
                String temp = StringUtils.getRandomNumAndChacters(32);
                if (!TextUtils.isEmpty(temp)) {
                    codeKey = temp;
                }
                Map<String, Object> map = new HashMap<>();
                map.put("codeKey", codeKey);
                map.put("width", width);
                map.put("height", height);
                NetWorkManager.getInstance()
                        .getCoocaaAccountApiService()
                        .getImageCaptcha(ParamsUtil.getCoocaaAccountPublicQueryUrl(),map)
                        .compose(ResponseTransformer.<ResponseBody>handException())
                        .subscribe(new ObserverAdapter<ResponseBody>(){
                            @Override
                            public void onNext(ResponseBody responseBody) {
                                callback.onSuccess(BitmapFactory.decodeStream(responseBody.byteStream()));
                            }

                            @Override
                            public void onError(Throwable e) {
                                callback.onFailed(e);
                            }
                        });
            }
        };
    }

    @Override
    public InvocateFuture<Boolean> getSmsCaptcha(final String phoneNumber) {
        return new InvocateFuture<Boolean>() {
            @Override
            public void setCallback(final RepositoryCallback<Boolean> callback) {
                callback.onStart();
                Map<String, Object> map = new HashMap<>();
                map.put("mobile_email", phoneNumber);
                NetWorkManager.getInstance()
                        .getCoocaaAccountApiService()
                        .getSmsCaptcha(ParamsUtil.getCoocaaAccountPublicQueryUrl(),map)
                        .compose(ResponseTransformer.<Void>handleResult(SUCCESS_CODE_1))
                        .subscribe(new ObserverAdapter<Void>(){
                            @Override
                            public void onNext(Void aVoid) {
                                callback.onSuccess(true);
                            }

                            @Override
                            public void onError(Throwable e) {
                                //兼容后台返回，这个接口直接返回true或者false,导致这里发生解析异常
                                if(e instanceof ApiException
                                        && e.getMessage().contains("Expected BEGIN_OBJECT but was BOOLEAN at line 1 column 5 path $")) {
                                    callback.onSuccess(true);
                                }else {
                                    callback.onFailed(e);
                                }
                            }
                        });
            }
        };
    }

    @Override
    public InvocateFuture<Boolean> getSmsCaptcha(final String phoneNumber, final String imageCaptcha) {
        return new InvocateFuture<Boolean>() {
            @Override
            public void setCallback(final RepositoryCallback<Boolean> callback) {
                callback.onStart();
                Map<String, Object> map = new HashMap<>();
                map.put("mobile_email", phoneNumber);
                map.put("codeKey", codeKey);
                map.put("capcha", imageCaptcha);
                NetWorkManager.getInstance()
                        .getCoocaaAccountApiService()
                        .getSmsCaptchaWithImageCaptcha(ParamsUtil.getCoocaaAccountPublicQueryUrl(),map)
                        .compose(ResponseTransformer.<Void>handleResult(SUCCESS_CODE_1))
                        .subscribe(new ObserverAdapter<Void>(){
                            @Override
                            public void onNext(Void aVoid) {
                                callback.onSuccess(true);
                            }

                            @Override
                            public void onError(Throwable e) {
                                //兼容后台返回，这个接口直接返回true或者false,导致这里发生解析异常
                                if(e instanceof ApiException
                                        && e.getMessage().contains("Expected BEGIN_OBJECT but was BOOLEAN at line 1 column 5 path $")) {
                                    callback.onSuccess(true);
                                }else {
                                    callback.onFailed(e);
                                }
                            }
                        });
            }
        };
    }



    @Override
    public InvocateFuture<AccountLoginInfo> smsCaptchaLogin(final String phoneNumber, final String smsCaptcha) {
        return new InvocateFuture<AccountLoginInfo>() {
            @Override
            public void setCallback(final RepositoryCallback<AccountLoginInfo> callback) {
                callback.onStart();
                Map<String, Object> map = new HashMap<>();
                map.put("mobile", phoneNumber);
                map.put("captcha", smsCaptcha);
                NetWorkManager.getInstance()
                        .getCoocaaAccountApiService()
                        .smsLoginServer(ParamsUtil.getCoocaaAccountPublicQueryUrl(),map)
                        .compose(ResponseTransformer.<AccountLoginInfo>handException())
                        .subscribe(new ObserverAdapter<AccountLoginInfo>(){
                            @Override
                            public void onNext(AccountLoginInfo loginInfo) {
                                callback.onSuccess(loginInfo);
                            }

                            @Override
                            public void onError(Throwable e) {
                                callback.onFailed(e);
                            }
                        });
            }
        };
    }

    @Override
    public InvocateFuture<AccountLoginInfo> oneKeyLogin(final String sanYanToken) {
        return new InvocateFuture<AccountLoginInfo>() {
            @Override
            public void setCallback(final RepositoryCallback<AccountLoginInfo> callback) {
                callback.onStart();
                HashMap<String, String> map = new HashMap<>();
                map.put("token", sanYanToken);
                map.put("systemType", "1");
                map.put("appId", "A7chmFPr");
                HashMap<String, String> pubMap = ParamsUtil.getCoocaaAccountPublicMap(map,null);
                NetWorkManager.getInstance()
                        .getCoocaaAccountApiService()
                        .oneClickLogin(pubMap)
                        .compose(ResponseTransformer.<AccountLoginInfo>handleResult(SUCCESS_CODE_1))
                        .subscribe(new ObserverAdapter<AccountLoginInfo>(){
                            @Override
                            public void onNext(AccountLoginInfo loginInfo) {
                                callback.onSuccess(loginInfo);
                            }

                            @Override
                            public void onError(Throwable e) {
                                callback.onFailed(e);
                            }
                        });
            }
        };
    }

    @Override
    public InvocateFuture<CoocaaUserInfo> getCoocaaUserInfo(final String accessToken) {
        return new InvocateFuture<CoocaaUserInfo>() {
            @Override
            public void setCallback(final RepositoryCallback<CoocaaUserInfo> callback) {
                callback.onStart();
                Map<String, Object> map = new HashMap<>();
                map.put("access_token", accessToken);
                NetWorkManager.getInstance()
                        .getCoocaaAccountApiService()
                        .getCoocaaUserInfo(ParamsUtil.getCoocaaAccountPublicQueryUrl(), map)
                        .compose(ResponseTransformer.<CoocaaUserInfo>handException())
                        .subscribe(new ObserverAdapter<CoocaaUserInfo>(){
                            @Override
                            public void onNext(CoocaaUserInfo coocaaUserInfo) {
                                callback.onSuccess(coocaaUserInfo);
                            }

                            @Override
                            public void onError(Throwable e) {
                                callback.onFailed(e);
                            }
                        });
            }
        };
    }

    @Override
    public InvocateFuture<String> updateCoocaaUserInfo(final String accessToken,
                                                        final String nickName,
                                                        final String gender,
                                                        final String birthday) {
        return new InvocateFuture<String>() {
            @Override
            public void setCallback(final RepositoryCallback<String> callback) {
                callback.onStart();
                Map<String, Object> queryMap = new HashMap<>();
                queryMap.put("access_token", accessToken);
                Map<String, Object> fieldMap = new HashMap<>();
                if(!TextUtils.isEmpty(nickName)) {
                    fieldMap.put("nick_name", nickName);
                }
                if(!TextUtils.isEmpty(gender)) {
                    fieldMap.put("gender", gender);
                }
                if(!TextUtils.isEmpty(birthday)) {
                    fieldMap.put("birthday", birthday);
                }
                NetWorkManager.getInstance()
                        .getCoocaaAccountApiService()
                        .updateCoocaaUserInfo(ParamsUtil.getCoocaaAccountPublicQueryUrl(queryMap), fieldMap)
                        .compose(ResponseTransformer.<String>handException())
                        .subscribe(new ObserverAdapter<String>(){
                            @Override
                            public void onNext(String response) {
                                callback.onSuccess(response);
                            }

                            @Override
                            public void onError(Throwable e) {
                                callback.onFailed(e);
                            }
                        });
            }
        };
    }

    @Override
    public InvocateFuture<List<UpdateAvatarBean>> updateAvatar(final String accessToken,final String base64Avatar,final String type) {
        return new InvocateFuture<List<UpdateAvatarBean>>() {
            @Override
            public void setCallback(final RepositoryCallback<List<UpdateAvatarBean>> callback) {
                callback.onStart();
                Map<String, Object> queryMap = new HashMap<>();
                queryMap.put("access_token", accessToken);
                Map<String, Object> fieldMap = new HashMap<>();
                fieldMap.put("avatar", base64Avatar);
                fieldMap.put("type", type);
                NetWorkManager.getInstance()
                        .getCoocaaAccountApiService()
                        .updateCoocaaAvatar(ParamsUtil.getCoocaaAccountPublicQueryUrl(queryMap), fieldMap)
                        .compose(ResponseTransformer.<ResponseBody>handException())
                        .subscribe(new ObserverAdapter<ResponseBody>(){
                            @Override
                            public void onNext(ResponseBody responseBody) {
                                try {
                                    String responseStr = responseBody.string();
                                    List<UpdateAvatarBean> list = new ArrayList<>();
                                    JSONObject jsonObject = new JSONObject(responseStr);
                                    String url50 = jsonObject.optString("50");
                                    if(!TextUtils.isEmpty(url50)) {
                                        list.add(new UpdateAvatarBean("50", url50));
                                    }

                                    String url70 = jsonObject.optString("70");
                                    if(!TextUtils.isEmpty(url70)) {
                                        list.add(new UpdateAvatarBean("70", url70));
                                    }

                                    String url200 = jsonObject.optString("200");
                                    if(!TextUtils.isEmpty(url200)) {
                                        list.add(new UpdateAvatarBean("200", url200));
                                    }

                                    String url250 = jsonObject.optString("250");
                                    if(!TextUtils.isEmpty(url250)) {
                                        list.add(new UpdateAvatarBean("250", url250));
                                    }

                                    String url370 = jsonObject.optString("370");
                                    if(!TextUtils.isEmpty(url370)) {
                                        list.add(new UpdateAvatarBean("370", url370));
                                    }

                                    String url500 = jsonObject.optString("500");
                                    if(!TextUtils.isEmpty(url500)) {
                                        list.add(new UpdateAvatarBean("500", url500));
                                    }

                                    String url800 = jsonObject.optString("800");
                                    if(!TextUtils.isEmpty(url800)) {
                                        list.add(new UpdateAvatarBean("800", url800));
                                    }
                                    callback.onSuccess(list);
                                } catch (JSONException | IOException e) {
                                    e.printStackTrace();
                                    callback.onFailed(e);
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                callback.onFailed(e);
                            }
                        });
            }
        };
    }

    @Override
    public InvocateFuture<TpTokenInfo> getTpToken(final String accessToken) {
        return new InvocateFuture<TpTokenInfo>() {
            @Override
            public void setCallback(final RepositoryCallback<TpTokenInfo> callback) {
                callback.onStart();
                Map<String, Object> map = new HashMap<>();
                map.put("access_token", accessToken);
                NetWorkManager.getInstance()
                        .getCoocaaAccountApiService()
                        .getTpToken(map)
                        .compose(ResponseTransformer.<TpTokenInfo>handException())
                        .subscribe(new ObserverAdapter<TpTokenInfo>(){
                            @Override
                            public void onNext(TpTokenInfo tpTokenInfo) {
                                callback.onSuccess(tpTokenInfo);
                                saveTpToken(tpTokenInfo);
                            }

                            @Override
                            public void onError(Throwable e) {
                                callback.onFailed(e);
                            }
                        });
            }
        };
    }

    public InvocateFuture<YunXinUserInfo> getYunXinUserInfo(final String accessToken) {
        return new InvocateFuture<YunXinUserInfo>() {
            @Override
            public void setCallback(final RepositoryCallback<YunXinUserInfo> callback) {
                callback.onStart();
                Map<String, Object> map = new HashMap<>();
                map.put("accessToken", accessToken);
                map.put("yxRegisterType", "mobile");
                map.put("clientId", Constants.VIDEO_CALL_CLIENT_ID);
                NetWorkManager.getInstance()
                        .getCoocaaAccountApiService()
                        .getYXUserInfo(map)
                        .compose(ResponseTransformer.<YunXinUserInfo>handleResult(SUCCESS_CODE_1))
                        .subscribe(new ObserverAdapter<YunXinUserInfo>() {
                            @Override
                            public void onNext(YunXinUserInfo userInfo) {
                                callback.onSuccess(userInfo);
                            }

                            @Override
                            public void onError(Throwable e) {
                                callback.onFailed(e);
                            }
                        });
            }
        };
    }


    @Override
    public InvocateFuture<RegisterLogin> registerDevice(final String accessToken,final String nickname,final String openId) {
        return new InvocateFuture<RegisterLogin>() {
            @Override
            public void setCallback(final RepositoryCallback<RegisterLogin> callback) {
                callback.onStart();
                Map<String, String> map = new HashMap<>();
                map.put("ccAccessToken", accessToken);
                map.put("zpRegisterType", "openid");
                map.put("zpNickName", nickname);
                PhoneDeviceInfo deviceInfo = new PhoneDeviceInfo("12345",
                        openId, nickname, Build.MODEL, "", "");
                map.put("deviceInfo", new Gson().toJson(deviceInfo));
                NetWorkManager.getInstance()
                        .getCoocaaAccountApiService()
                        .registerDevice(map)
                        .compose(ResponseTransformer.<RegisterLoginResp>handException())
                        .subscribe(new ObserverAdapter<RegisterLoginResp>() {
                            @Override
                            public void onNext(RegisterLoginResp registerLoginResp) {
                                if(registerLoginResp.code.equals("0")) {
                                    callback.onSuccess(registerLoginResp.data);
                                }else {
                                    callback.onFailed(new Exception(registerLoginResp.msg));
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                callback.onFailed(e);
                            }
                        });
            }
        };
    }

    @Override
    public InvocateFuture<Integer> updateDeviceInfo(final String accessToken,final String nickname,final String openId) {
        return new InvocateFuture<Integer>() {
            @Override
            public void setCallback(final RepositoryCallback<Integer> callback) {
                callback.onStart();
                Map<String, String> map = new HashMap<>();
                map.put("accessToken", accessToken);
                PhoneDeviceInfo deviceInfo = new PhoneDeviceInfo("12345",
                        openId, nickname, Build.MODEL, "", "");
                map.put("deviceInfo", new Gson().toJson(deviceInfo));
                NetWorkManager.getInstance()
                        .getCoocaaAccountApiService()
                        .updateDeviceInfo(map)
                        .compose(ResponseTransformer.<UpdateDeviceInfoResp>handException())
                        .subscribe(new ObserverAdapter<UpdateDeviceInfoResp>() {
                            @Override
                            public void onNext(UpdateDeviceInfoResp updateDeviceInfoResp) {
                                if(updateDeviceInfoResp.code.equals("0")) {
                                    callback.onSuccess(new Integer(updateDeviceInfoResp.data));
                                }else {
                                    callback.onFailed(new Exception(updateDeviceInfoResp.msg));
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                callback.onFailed(e);
                            }
                        });
            }
        };
    }


    @Override
    public InvocateFuture<TVSourceModel> getTVSource(String activeId) {
        return null;
    }

    @Override
    public InvocateFuture<TVSourceModel> getTVSource(String mac, String model, String version, String tv_name) {
        return null;
    }

    @Override
    public void saveToken(String token) {
        Preferences.Login.saveAccessToken(token);
    }

    @Override
    public void saveDeviceRegisterLoginInfo(RegisterLogin registerLogin) {
        Gson gson = new Gson();
        Preferences.Login.saveDeviceRegisterLoginInfo(gson.toJson(registerLogin));
    }

    @Override
    public void saveCoocaaUserInfo(CoocaaUserInfo info) {
        Gson gson = new Gson();
        Preferences.Login.saveCoocaaUserInfo(gson.toJson(info));
    }

    @Override
    public void saveYunXinUserInfo(YunXinUserInfo info) {
        Gson gson = new Gson();
        Preferences.Login.saveYunXinUserInfo(gson.toJson(info));
    }

    @Override
    public void saveTpToken(TpTokenInfo info) {
        Gson gson = new Gson();
        Preferences.Login.saveTpToken(gson.toJson(info));
    }

    @Override
    public String queryToken() {
        return Preferences.Login.getAccessToken();
    }

    @Override
    public RegisterLogin queryDeviceRegisterLoginInfo() {
        String deviceRegisterLoginInfoJson = Preferences.Login.getDeviceRegisterLoginInfo();
        Gson gson = new Gson();
        return gson.fromJson(deviceRegisterLoginInfoJson, new TypeToken<RegisterLogin>() {}.getType());
    }

    @Override
    public CoocaaUserInfo queryCoocaaUserInfo() {
        String coocaaUserInfoJson = Preferences.Login.getCoocaaUserInfoJson();
        Gson gson = new Gson();
        return gson.fromJson(coocaaUserInfoJson,new TypeToken<CoocaaUserInfo>() {}.getType());
    }

    @Override
    public YunXinUserInfo queryYunXinUserInfo() {
        String yunXinUserInfoJson = Preferences.Login.getYunXinUserInfoJson();
        Gson gson = new Gson();
        return gson.fromJson(yunXinUserInfoJson,new TypeToken<YunXinUserInfo>() {}.getType());
    }

    @Override
    public TpTokenInfo queryTpTokenInfo() {
        String tpTokenInfoJson = Preferences.Login.getTpTokenInfoJson();
        Gson gson = new Gson();
        return gson.fromJson(tpTokenInfoJson, TpTokenInfo.class);
    }
}
