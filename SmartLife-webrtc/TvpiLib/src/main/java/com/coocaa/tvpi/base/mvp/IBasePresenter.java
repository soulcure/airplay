package com.coocaa.tvpi.base.mvp;

public interface IBasePresenter {

    void attach(IBaseView view);

    void detach();
}
