package com.coocaa.smartscreen.repository.service.impl;

import android.util.Log;

import com.coocaa.smartscreen.data.account.YunXinUserInfo;
import com.coocaa.smartscreen.data.videocall.ContactsResp;
import com.coocaa.smartscreen.network.NetWorkManager;
import com.coocaa.smartscreen.network.ObserverAdapter;
import com.coocaa.smartscreen.network.ResponseTransformer;
import com.coocaa.smartscreen.network.common.Constants;
import com.coocaa.smartscreen.network.util.ParamsUtil;
import com.coocaa.smartscreen.push.bean.TvpiMessage;
import com.coocaa.smartscreen.repository.callback.RepositoryCallback;
import com.coocaa.smartscreen.repository.future.InvocateFuture;
import com.coocaa.smartscreen.repository.service.VideoCallRepository;
import com.coocaa.smartscreen.repository.utils.Preferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.coocaa.smartscreen.network.ResponseTransformer.SUCCESS_CODE_1;


public class VideoCallRepositoryImpl implements VideoCallRepository {
    private static final String TAG = VideoCallRepositoryImpl.class.getSimpleName();

    @Override
    public InvocateFuture<YunXinUserInfo> addContacts(final String accessToken, final boolean isForceAdd,
                                                      final String yxRegisterCode, final String friendRemark) {
        return new InvocateFuture<YunXinUserInfo>() {
            @Override
            public void setCallback(final RepositoryCallback<YunXinUserInfo> callback) {
                callback.onStart();
                Map<String, Object> formMap = new HashMap<>();
                formMap.put("accessToken", accessToken);
                formMap.put("yxRegisterCode", String.valueOf(yxRegisterCode));
                formMap.put("isForceAdd", isForceAdd ? "1" : "0");//是否强制添加，1是，0否
                formMap.put("yxNickName", String.valueOf(friendRemark));
                NetWorkManager.getInstance()
                        .getVideoCallApiService()
                        .addFriends(formMap)
                        .compose(ResponseTransformer.<YunXinUserInfo>handleResult(SUCCESS_CODE_1))
                        .subscribe(new ObserverAdapter<YunXinUserInfo>() {
                            @Override
                            public void onNext(YunXinUserInfo userInfo) {
                                callback.onSuccess(userInfo);

                                //添加好友增加推送
                                pushToTv(yxRegisterCode);
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
    public InvocateFuture<Void> deleteContacts(final String accessToken,
                                               final String friendYxAccountId) {
        return new InvocateFuture<Void>() {
            @Override
            public void setCallback(final RepositoryCallback<Void> callback) {
                callback.onStart();
                Map<String, Object> formMap = new HashMap<>();
                formMap.put("accessToken", accessToken);
                formMap.put("friendAccountId", friendYxAccountId);
                NetWorkManager.getInstance()
                        .getVideoCallApiService()
                        .deleteFriends(formMap)
                        .compose(ResponseTransformer.<Void>handleResult(SUCCESS_CODE_1))
                        .subscribe(new ObserverAdapter<Void>() {
                            @Override
                            public void onNext(Void aVoid) {
                                callback.onSuccess(null);
                            }

                            @Override
                            public void onError(Throwable e) {
                                if (e.getMessage().contains("item is null")) {
                                    callback.onSuccess(null);
                                } else {
                                    callback.onFailed(e);
                                }
                            }
                        });
            }
        };
    }

    @Override
    public InvocateFuture<Void> updateContacts(final String accessToken, final String friendYxAccountId,
                                               final String friendRemark) {
        return new InvocateFuture<Void>() {
            @Override
            public void setCallback(final RepositoryCallback<Void> callback) {
                callback.onStart();
                Map<String, Object> formMap = new HashMap<>();
                formMap.put("accessToken", accessToken);
                formMap.put("friendAccountId", friendYxAccountId);
                formMap.put("yxNickName", friendRemark);
                NetWorkManager.getInstance()
                        .getVideoCallApiService()
                        .updateFriendsNickname(formMap)
                        .compose(ResponseTransformer.<Void>handleResult(SUCCESS_CODE_1))
                        .subscribe(new ObserverAdapter<Void>() {
                            @Override
                            public void onNext(Void aVoid) {
                                callback.onSuccess(aVoid);
                            }

                            @Override
                            public void onError(Throwable e) {
                                if (e.getMessage().contains("item is null")) {
                                    callback.onSuccess(null);
                                } else {
                                    callback.onFailed(e);
                                }
                            }
                        });
            }
        };
    }

    @Override
    public InvocateFuture<Void> agreeOrRefundAddContacts(final String accessToken, final boolean isAgree, final String friendYxAccountId) {
        return new InvocateFuture<Void>() {
            @Override
            public void setCallback(final RepositoryCallback<Void> callback) {
                callback.onStart();
                Map<String, Object> formMap = new HashMap<>();
                formMap.put("accessToken", accessToken);
                formMap.put("friendAccountId", friendYxAccountId);
                formMap.put("isAgree", isAgree ? "1" : "0");
                NetWorkManager.getInstance()
                        .getVideoCallApiService()
                        .agreeRefundAddFriends(formMap)
                        .compose(ResponseTransformer.<Void>handleResult(SUCCESS_CODE_1))
                        .subscribe(new ObserverAdapter<Void>() {
                            @Override
                            public void onNext(Void aVoid) {
                                callback.onSuccess(null);
                            }

                            @Override
                            public void onError(Throwable e) {
                                if (e.getMessage().contains("item is null")) {
                                    callback.onSuccess(null);
                                } else {
                                    callback.onFailed(e);
                                }
                            }
                        });
            }
        };
    }

    @Override
    public InvocateFuture<Boolean> isFriend(final String accessToken, final String friendYxAccountId) {
        return new InvocateFuture<Boolean>() {
            @Override
            public void setCallback(final RepositoryCallback<Boolean> callback) {
                callback.onStart();
                Map<String, Object> formMap = new HashMap<>();
                formMap.put("accessToken", accessToken);
                formMap.put("friendAccountId", friendYxAccountId);
                NetWorkManager.getInstance()
                        .getVideoCallApiService()
                        .isFriends(formMap)
                        .compose(ResponseTransformer.<Void>handleResult(SUCCESS_CODE_1))
                        .subscribe(new ObserverAdapter<Void>() {
                            @Override
                            public void onNext(Void aVoid) {
                                callback.onSuccess(true);
                            }

                            @Override
                            public void onError(Throwable e) {
                                if (e.getMessage().contains("item is null")) {
                                    callback.onSuccess(true);
                                } else if(e.getMessage().contains("非好友关系")
                                        ||e.getMessage().contains("非联系人关系") ){
                                    callback.onSuccess(false);
                                }else {
                                    callback.onFailed(e);
                                }
                            }
                        });
            }
        };
    }

    @Override
    public InvocateFuture<List<ContactsResp>> getContactsList(final String accessToken) {
        return new InvocateFuture<List<ContactsResp>>() {
            @Override
            public void setCallback(final RepositoryCallback<List<ContactsResp>> callback) {
                callback.onStart();
                Map<String, Object> formMap = new HashMap<>();
                formMap.put("accessToken", accessToken);
                NetWorkManager.getInstance()
                        .getVideoCallApiService()
                        .getFriendList(formMap)
                        .compose(ResponseTransformer.<List<ContactsResp>>handleResult(SUCCESS_CODE_1))
                        .subscribe(new ObserverAdapter<List<ContactsResp>>() {
                            @Override
                            public void onNext(List<ContactsResp> contactsResps) {
                                callback.onSuccess(contactsResps);
                            }

                            @Override
                            public void onError(Throwable e) {
                                if (e.getMessage().contains("沒有查询到数据")) {
                                    callback.onSuccess(Collections.<ContactsResp>emptyList());
                                } else {
                                    callback.onFailed(e);
                                }
                            }
                        });
            }
        };
    }

    @Override
    public List<String> getLocalNicknameList() {
        String[] nickname = new String[]{"爸爸", "妈妈", "爷爷", "奶奶", "外公", "外婆", "姑姑", "姑父", "舅舅", "舅妈"};
        return Arrays.asList(nickname);
    }

    @Override
    public InvocateFuture<Boolean> pushToTv(final String registerId) {
        return new InvocateFuture<Boolean>() {
            @Override
            public void setCallback(final RepositoryCallback<Boolean> callback) {
                callback.onStart();
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("deviceId", String.valueOf(registerId));
                hashMap.put("clientId", Constants.VIDEO_CALL_CLIENT_ID);
                hashMap.put("paramName", new Gson().toJson(new TvpiMessage("1","1")));
                String time = String.valueOf(System.currentTimeMillis() / 1000);
                hashMap.put("time", time);
                hashMap.put("sign", ParamsUtil.getSignByQueryAndBodyParams(hashMap));
                NetWorkManager.getInstance()
                        .getVideoCallApiService()
                        .pushToTv(hashMap)
                        .compose(ResponseTransformer.<Void>handleResult(SUCCESS_CODE_1))
                        .subscribe(new ObserverAdapter<Void>() {
                            @Override
                            public void onNext(Void aVoid) {
                                callback.onSuccess(true);
                            }

                            @Override
                            public void onError(Throwable e) {
                                if (e.getMessage().contains("item is null")) {
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
    public void saveContactsList(List<ContactsResp> contactsRespList) {
        Gson gson = new Gson();
        Preferences.VideoCall.saveContactsList(gson.toJson(contactsRespList));
    }

    @Override
    public List<ContactsResp> queryContactsList() {
        String contactsList = Preferences.VideoCall.getContactsList();
        Gson gson = new Gson();
        return gson.fromJson(contactsList,new TypeToken<List<ContactsResp>>() {}.getType());
    }
}
