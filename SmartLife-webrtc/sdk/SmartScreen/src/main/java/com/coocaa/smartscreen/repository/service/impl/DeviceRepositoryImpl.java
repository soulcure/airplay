package com.coocaa.smartscreen.repository.service.impl;

import com.coocaa.smartscreen.data.device.TvProperty;
import com.coocaa.smartscreen.network.NetWorkManager;
import com.coocaa.smartscreen.network.ObserverAdapter;
import com.coocaa.smartscreen.network.ResponseTransformer;
import com.coocaa.smartscreen.repository.callback.RepositoryCallback;
import com.coocaa.smartscreen.repository.future.InvocateFuture;
import com.coocaa.smartscreen.repository.service.DeviceRepository;

import java.util.HashMap;
import java.util.Map;

import static com.coocaa.smartscreen.network.ResponseTransformer.SUCCESS_CODE_0;

public class DeviceRepositoryImpl implements DeviceRepository{

    @Override
    public InvocateFuture<TvProperty> getTvProperty(final String chip, final String model) {
        return new InvocateFuture<TvProperty>() {
            @Override
            public void setCallback(final RepositoryCallback<TvProperty> callback) {
                callback.onStart();
                Map<String, String> map = new HashMap<>();
                map.put("chip", chip);
                map.put("model", model);
                NetWorkManager.getInstance()
                        .getCoocaaAccountApiService()
                        .getTvProperty(map)
                        .compose(ResponseTransformer.<TvProperty>handleResult(SUCCESS_CODE_0))
                        .subscribe(new ObserverAdapter<TvProperty>() {
                            @Override
                            public void onNext(TvProperty tvProperty) {
                                callback.onSuccess(tvProperty);
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
