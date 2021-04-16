package com.coocaa.smartscreen.repository.service.impl;

import android.text.TextUtils;

import com.coocaa.smartscreen.data.device.BindCodeMsg;
import com.coocaa.smartscreen.data.device.BindCodeMsgResp;
import com.coocaa.smartscreen.network.NetWorkManager;
import com.coocaa.smartscreen.network.ObserverAdapter;
import com.coocaa.smartscreen.network.ResponseTransformer;
import com.coocaa.smartscreen.repository.callback.RepositoryCallback;
import com.coocaa.smartscreen.repository.future.InvocateFuture;
import com.coocaa.smartscreen.repository.service.BindCodeRepository;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 陈澳军
 */
public class BindCodeRepositoryImpl implements BindCodeRepository {

    @Override
    public InvocateFuture<BindCodeMsg> getBindCode(final String accessToken, final String activationId, final String spaceId) {
        return new InvocateFuture<BindCodeMsg>() {
            @Override
            public void setCallback(final RepositoryCallback<BindCodeMsg> callback) {
                callback.onStart();
                Map<String, String> map = new HashMap<>();
                map.put("accessToken", accessToken);
                if (!TextUtils.isEmpty(activationId)) {
                    map.put("activationId", activationId);
                } else {
                    if (!TextUtils.isEmpty(spaceId)) {
                        map.put("spaceId", spaceId);
                    } else {
                        callback.onFailed(new Exception("数据为空"));
                        return;
                    }
                }

                NetWorkManager.getInstance()
                        .getCoocaaAccountApiService()
                        .getBindCode(map)
                        .compose(ResponseTransformer.<BindCodeMsgResp>handException())
                        .subscribe(new ObserverAdapter<BindCodeMsgResp>() {
                            @Override
                            public void onNext(BindCodeMsgResp bindCodeMsgResp) {
                                if(bindCodeMsgResp.getCode().equals("0")) {
                                    callback.onSuccess(bindCodeMsgResp.getData());
                                }else {
                                    callback.onFailed(new Exception(bindCodeMsgResp.getMsg()));
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
}
