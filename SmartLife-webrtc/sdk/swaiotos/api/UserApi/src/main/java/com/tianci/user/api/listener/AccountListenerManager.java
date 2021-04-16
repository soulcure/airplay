package com.tianci.user.api.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.tianci.user.api.Defines;
import com.tianci.user.api.utils.ULog;

import java.util.HashSet;
import java.util.Set;

public class AccountListenerManager {
    private static AccountListenerManager mInstance;
    private final Set<OnAccountChangedListener> listenerSet = new HashSet<>();

    public static synchronized AccountListenerManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new AccountListenerManager(context);
        }
        return mInstance;
    }

    private AccountListenerManager(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Defines.ACCOUNT_CHANGED);
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Defines.ACCOUNT_CHANGED.equals(intent.getAction())) {
                    ULog.i("onReceive ACCOUNT_CHANGED = " + Defines.ACCOUNT_CHANGED);
                    for (OnAccountChangedListener listener : listenerSet) {
                        listener.onAccountChanged();
                    }
                }
            }
        };
        context.registerReceiver(receiver, intentFilter);
    }

    public boolean register(OnAccountChangedListener listener) {
        synchronized (listenerSet) {
            listenerSet.add(listener);
        }
        return true;
    }

    public boolean unregister(OnAccountChangedListener listener) {
        synchronized (listenerSet) {
            listenerSet.remove(listener);
        }
        return true;
    }

}
