package com.coocaa.tvpi.module.pay;

import android.content.Context;
import android.text.TextUtils;
import android.util.ArrayMap;

import com.coocaa.tvpi.module.pay.api.CCPayApi;
import com.coocaa.tvpi.module.pay.api.IPayApi;
import com.coocaa.tvpi.module.pay.impl.ALiPayApi;
import com.coocaa.tvpi.module.pay.impl.WePayApi;

import static com.coocaa.tvpi.module.pay.bean.PayConstant.PAY_ALI;
import static com.coocaa.tvpi.module.pay.bean.PayConstant.PAY_WE;


public final class PayApiFactory {

    private static final ArrayMap<String, IPayApi> mPayApiSparseArray = new ArrayMap<>(3);
    private static final CCPayApi empty = new CCPayApi();

    public static IPayApi createApi(Context context, String pay_mode) {

        if (mPayApiSparseArray.containsKey(pay_mode)) {
            return mPayApiSparseArray.get(pay_mode);
        }
        if (TextUtils.isEmpty(pay_mode)) return empty;
        IPayApi api;
        switch (pay_mode) {
            case PAY_WE:
                api = new WePayApi(context);
                break;
            case PAY_ALI:
                api = new ALiPayApi(context);
                break;
            default:
                api = empty;
                break;
        }
        mPayApiSparseArray.put(pay_mode, api);
        return api;
    }

}
