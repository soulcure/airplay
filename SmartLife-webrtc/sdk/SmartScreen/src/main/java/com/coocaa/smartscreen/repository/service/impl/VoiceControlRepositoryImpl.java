package com.coocaa.smartscreen.repository.service.impl;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.coocaa.smartscreen.data.account.YunXinUserInfo;
import com.coocaa.smartscreen.data.movie.LongVideoListResp;
import com.coocaa.smartscreen.data.voice.VoiceAdviceInfo;
import com.coocaa.smartscreen.network.NetWorkManager;
import com.coocaa.smartscreen.network.ObserverAdapter;
import com.coocaa.smartscreen.network.ResponseTransformer;
import com.coocaa.smartscreen.repository.callback.RepositoryCallback;
import com.coocaa.smartscreen.repository.future.InvocateFuture;
import com.coocaa.smartscreen.repository.service.VoiceControlRepository;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DefaultObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

import static com.coocaa.smartscreen.network.ResponseTransformer.SUCCESS_CODE_1;

/**
 * @author chenaojun
 */
public class VoiceControlRepositoryImpl implements VoiceControlRepository {

    private String TAG = VoiceControlRepositoryImpl.class.getSimpleName();

    @Override
    public InvocateFuture<VoiceAdviceInfo> getAdvice() {

        return new InvocateFuture<VoiceAdviceInfo>() {
            @Override
            public void setCallback(final RepositoryCallback<VoiceAdviceInfo> callback) {
                callback.onStart();
                Map<String, Object> formMap = new HashMap<>();
                formMap.put("key", "mobile_ai_prompt");

                NetWorkManager.getInstance()
                        .getVoiceControlApiService()
                        .getAdvice(formMap)
                        .compose(ResponseTransformer.<ResponseBody>handException())
                        .subscribe(new DefaultObserver<ResponseBody>() {
                            @Override
                            public void onNext(ResponseBody responseBody) {
                                String response = "";
                                try {
                                    response = responseBody.string();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                VoiceAdviceInfo voiceAdviceInfo = new Gson().fromJson(response,VoiceAdviceInfo.class);
                                callback.onSuccess(voiceAdviceInfo);
                            }

                            @Override
                            public void onError(Throwable e) {
                                callback.onFailed(e);
                            }

                            @Override
                            public void onComplete() {
                            }
                        });
            }
        };
    }

}
