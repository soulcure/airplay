package com.coocaa.tvpi.module.pay.impl;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
//
//import com.coocaa.tvpi.IIPay;
//import com.coocaa.tvpi.IIPayResultCallback;
import com.coocaa.tvpi.IIPay;
import com.coocaa.tvpi.IIPayResultCallback;
import com.coocaa.tvpi.module.pay.NotityPayResultService;
import com.coocaa.tvpi.module.pay.PayApiFactory;
import com.coocaa.tvpi.module.pay.PayManager;
import com.coocaa.tvpi.module.pay.PayUtil;
import com.coocaa.tvpi.module.pay.api.IPayApi;
import com.coocaa.tvpi.module.pay.api.IPayResultCallback;
import com.coocaa.tvpi.module.pay.api.IPayTask;
import com.coocaa.tvpi.module.pay.bean.CCPayReq;

import static android.content.Context.BIND_AUTO_CREATE;

public class CCPayTask implements IPayTask {
    protected Activity context;
    private Intent payService;
    private PayConnection mConnection;
    private IPayResultCallback callback;
    private IPayApi mApi;

    public CCPayTask(Activity context, IPayResultCallback callback, boolean bind) {
        this.context = context;
        payService = new Intent(context, NotityPayResultService.class);
        this.callback = callback;
        PayManager.getInstance().addCallback(callback);
        if (bind)
            bind();
    }

    @Override
    public final void pay(CCPayReq req) {
        PayUtil.checkNotNull(context);
        PayUtil.checkNotNull(req);
        synchronized (CCPayTask.class) {
            payService.putExtra("req", req);
            mApi = PayApiFactory.createApi(context, req.type);
            mApi  .pay(req);

        }
    }

    private void bind() {
        if (mConnection == null)
        mConnection = new PayConnection(context);
        context.bindService(payService, mConnection, BIND_AUTO_CREATE);
    }


    public static class PayConnection implements ServiceConnection {

        private Context context;

        public PayConnection(Activity context) {
            this.context =context;
        }


        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            IIPay mIPay = IIPay.Stub.asInterface(service);
            try {
                mIPay.addCallback(new IIPayResultCallback.Stub() {
                    @Override
                    public void notifyResult() {
                        PayManager.getInstance().notifyResult(true,"onServiceConnected");
                        context.unbindService(PayConnection.this);
                    }
                });
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

    private void unbind() {
        context.unbindService(mConnection);
    }

    @Override
    public void unRegister() {
        try {
            if (callback != null) {
                PayManager.getInstance().removeCallback(callback);
            }
            if (mApi instanceof WePayApi)
                ((WePayApi) mApi).unregisterReceiver();
            unbind();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
