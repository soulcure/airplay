package com.coocaa.tvpi.base.mvp.proxy;


import com.coocaa.tvpi.base.mvp.IBaseView;

public class ProxyFragment<V extends IBaseView> extends ProxyImpl {
    public ProxyFragment(V view) {
        super(view);
    }
}
