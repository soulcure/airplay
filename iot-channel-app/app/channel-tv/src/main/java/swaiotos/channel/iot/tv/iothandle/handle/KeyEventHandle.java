package swaiotos.channel.iot.tv.iothandle.handle;

import android.app.Instrumentation;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;

import java.util.HashSet;
import java.util.Set;

import swaiotos.channel.iot.tv.TVChannelApplication;
import swaiotos.channel.iot.tv.iothandle.data.Events;
import swaiotos.channel.iot.tv.iothandle.handle.base.BaseChannelHandle;
import swaiotos.channel.iot.utils.ThreadManager;
import swaiotos.sal.SAL;
import swaiotos.sal.SalModule;
import swaiotos.sal.system.ISystem;


public class KeyEventHandle extends BaseChannelHandle {


    public static Set<Integer> virtualInputKey = new HashSet<>();

    static {
        virtualInputKey.add(KeyEvent.KEYCODE_DPAD_UP);
        virtualInputKey.add(KeyEvent.KEYCODE_DPAD_DOWN);
        virtualInputKey.add(KeyEvent.KEYCODE_DPAD_LEFT);
        virtualInputKey.add(KeyEvent.KEYCODE_DPAD_RIGHT);
        virtualInputKey.add(KeyEvent.KEYCODE_DPAD_CENTER);
        virtualInputKey.add(KeyEvent.KEYCODE_BACK);
        virtualInputKey.add(KeyEvent.KEYCODE_TV_INPUT);
        virtualInputKey.add(KeyEvent.KEYCODE_VOLUME_DOWN);
        virtualInputKey.add(KeyEvent.KEYCODE_VOLUME_UP);
        virtualInputKey.add(KeyEvent.KEYCODE_VOLUME_MUTE);
        virtualInputKey.add(KeyEvent.KEYCODE_0);
        virtualInputKey.add(KeyEvent.KEYCODE_1);
        virtualInputKey.add(KeyEvent.KEYCODE_2);
        virtualInputKey.add(KeyEvent.KEYCODE_3);
        virtualInputKey.add(KeyEvent.KEYCODE_4);
        virtualInputKey.add(KeyEvent.KEYCODE_5);
        virtualInputKey.add(KeyEvent.KEYCODE_6);
        virtualInputKey.add(KeyEvent.KEYCODE_7);
        virtualInputKey.add(KeyEvent.KEYCODE_8);
        virtualInputKey.add(KeyEvent.KEYCODE_9);
        virtualInputKey.add(KeyEvent.KEYCODE_MENU);
        virtualInputKey.add(KeyEvent.KEYCODE_HOME);
        virtualInputKey.add(KeyEvent.KEYCODE_POWER);
        virtualInputKey.add(KeyEvent.KEYCODE_CHANNEL_UP);
        virtualInputKey.add(KeyEvent.KEYCODE_CHANNEL_DOWN);
        virtualInputKey.add(KeyEvent.KEYCODE_PROG_RED);
        virtualInputKey.add(KeyEvent.KEYCODE_PROG_GREEN);
        virtualInputKey.add(KeyEvent.KEYCODE_PROG_YELLOW);
        virtualInputKey.add(KeyEvent.KEYCODE_PROG_BLUE);
    }

    @Override
    protected void onHandle() {
        String key = mCmdData.cmd;
        Log.d(TAG, "KeyEventHandle  old  key:" + key);
        if (TextUtils.isEmpty(key)) {
            Events events = Events.decode(mCmdData.param);
            key = events.value;
            Log.d(TAG, "KeyEventHandle  new  key:" + key);
        }
        final String keys = key;
        ThreadManager.getInstance().ioThread(new Runnable() {
            @Override
            public void run() {
                int k = Integer.valueOf(keys);
                if (isVirtualInputSupport(k)) {
                    Log.d(TAG, "KeyEventHandle  virtualkey:" + k);
                    try {
                        ISystem iSystem = SAL.getModule(TVChannelApplication.getContext(), SalModule.SYSTEM);
                        iSystem.invokeCooCaaKey(k);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d(TAG, "KeyEventHandle  android  key:" + k);
                    getInst().sendKeyDownUpSync(k);   //android标准注入
                }
            }
        });
    }

    private boolean isVirtualInputSupport(int key) {
        if (virtualInputKey.contains(key)) {
            return true;
        } else {
            return false;
        }
    }

    private Instrumentation inst = null;

    private Instrumentation getInst() {
        if (inst == null) {
            inst = new Instrumentation();
        }
        return inst;
    }
}
