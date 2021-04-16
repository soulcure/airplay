package com.coocaa.tvpi.base.mvp.proxy;


import com.coocaa.tvpi.base.mvp.IBaseView;

public class ProxyActivity<V extends IBaseView> extends ProxyImpl {
    public ProxyActivity(V view) {
        super(view);
    }
}
