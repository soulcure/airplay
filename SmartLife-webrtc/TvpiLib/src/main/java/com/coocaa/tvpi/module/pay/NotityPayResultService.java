package com.coocaa.tvpi.module.pay;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

import com.coocaa.tvpi.IIPay;
import com.coocaa.tvpi.IIPayResultCallback;
import com.coocaa.tvpi.module.pay.api.IPayApi;
import com.coocaa.tvpi.module.pay.api.IPayResultCallback;
import com.coocaa.tvpi.module.pay.bean.CCPayReq;
import com.coocaa.tvpi.module.pay.bean.CCPayResp;


public class NotityPayResultService extends IntentService implements IPayResultCallback {
    private PayBinder mPayBinder ;

    public NotityPayResultService() {
        super("pay-result-service");
        PayManager.getInstance().addCallback(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        PayUtil.checkNotNull(intent);
        if (mPayBinder == null)
            mPayBinder = new PayBinder(getApplicationContext());

        try {
            mPayBinder.pay((CCPayReq) intent.getParcelableExtra("req"));
        } catch (RemoteException e) {

        }
    }

    @Override
    public void paySuccessed() {
        mPayBinder.callbacks.beginBroadcast();
        try {
            for (int i = 0; i <  mPayBinder.callbacks.getRegisteredCallbackCount(); i++) {
                IIPayResultCallback mBroadcastItem = mPayBinder.callbacks.getBroadcastItem(i);
                if (mBroadcastItem != null) {
                    mBroadcastItem.notifyResult();
                }
            }
        } catch (RemoteException e) {

            e.printStackTrace();
        }
        mPayBinder.callbacks.finishBroadcast();
    }

    @Override
    public void payFailed(String reason) {

    }

    @Override
    public void payCancel() {

    }

    static class PayBinder extends IIPay.Stub {
        private Context mContext;
        private RemoteCallbackList<IIPayResultCallback>
                callbacks = new RemoteCallbackList<>();
        public PayBinder(Context context) {
            this.mContext = context;
        }

        @Override
        public CCPayResp pay(CCPayReq req) throws RemoteException {
            CCPayResp mResp = new CCPayResp();
            IPayApi mApi = PayApiFactory.createApi(mContext, req.type);

            mApi.pay(req);
            mResp.type = req.type + " zzzzz";
            return mResp;
        }




        @Override
        public void addCallback(IIPayResultCallback callback) throws RemoteException {
            if (callback != null){
                callbacks.register(callback);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mPayBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (mPayBinder == null)
            mPayBinder = new PayBinder(getApplicationContext());
    }
}
