package com.coocaa.tvpi.module.pay.api;


import com.coocaa.tvpi.module.pay.bean.CCPayReq;

public interface IPayTask {
    void pay(CCPayReq req) ;
    void unRegister();
}
