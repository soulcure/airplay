package com.coocaa.tvpi.base.mvp.proxy;


import com.coocaa.tvpi.base.mvp.BasePresenter;
import com.coocaa.tvpi.base.mvp.IBaseView;
import com.coocaa.tvpi.base.mvp.inject.InjectPresenter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ProxyImpl implements IProxy {

    private IBaseView mView;
    private List<BasePresenter> mInjectPresenters;

    public ProxyImpl(IBaseView view) {
        this.mView = view;
        mInjectPresenters = new ArrayList<>();
    }

    @SuppressWarnings({"unchecked", "TryWithIdenticalCatches"})
    @Override
    public void bindPresenter() {
        //获得已经申明的变量，包括私有的
        Field[] fields = mView.getClass().getDeclaredFields();
        for (Field field : fields) {
            //获取变量上面的注解类型
            InjectPresenter injectPresenter = field.getAnnotation(InjectPresenter.class);
            if (injectPresenter != null) {
                try {
                    Class<? extends BasePresenter> type = (Class<? extends BasePresenter>) field.getType();
                    BasePresenter mInjectPresenter = type.newInstance();
                    mInjectPresenter.attach(mView);
                    field.setAccessible(true);
                    field.set(mView, mInjectPresenter);
                    if(mInjectPresenters != null) {
                        mInjectPresenters.add(mInjectPresenter);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (ClassCastException e) {
                    e.printStackTrace();
                    throw new RuntimeException("SubClass must extends Class:BasePresenter");
                }
            }
        }
    }

    @Override
    public void unbindPresenter() {
        /**
         * 解绑，避免内存泄漏
         */
        if(mInjectPresenters != null && !mInjectPresenters.isEmpty()) {
            for (BasePresenter presenter : mInjectPresenters) {
                presenter.detach();
            }
            mInjectPresenters.clear();
            mInjectPresenters = null;
        }
    }
}
