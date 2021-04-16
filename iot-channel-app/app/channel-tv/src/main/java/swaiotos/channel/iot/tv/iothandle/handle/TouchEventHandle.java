package swaiotos.channel.iot.tv.iothandle.handle;

import android.app.Instrumentation;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;

import com.google.gson.Gson;
import com.swaiotos.skymirror.sdk.data.TouchData;
import com.swaiotos.skymirror.sdk.reverse.MotionEventUtil;

import swaiotos.channel.iot.tv.iothandle.data.Events;
import swaiotos.channel.iot.tv.iothandle.handle.base.BaseChannelHandle;
import swaiotos.channel.iot.utils.ThreadManager;


public class TouchEventHandle extends BaseChannelHandle {

    @Override
    protected void onHandle() {
        String touchKey = "";
        try {
            Events events = Events.decode(mCmdData.param);
            touchKey = events.value;
            Log.d(TAG, "KeyEventHandle  new  touchKey:" + touchKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(touchKey)) {
            return;
        }
        final String eventJson = touchKey;
        ThreadManager.getInstance().ioThread(new Runnable() {
            @Override
            public void run() {
                try {
                    MotionEvent event = MotionEventUtil.formatMotionEvent(new Gson().fromJson(eventJson, TouchData.class));
                    Log.d(TAG, "TouchEventHandle onCommand:event --- " + event.toString());
                    getInst().sendPointerSync(event);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "TouchEventHandle onCommand event error--- " + e.toString());
                }
            }
        });
    }

    private Instrumentation inst = null;

    private Instrumentation getInst() {
        if (inst == null) {
            inst = new Instrumentation();
        }
        return inst;
    }

}
