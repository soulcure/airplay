package swaiotos.runtime.np;

import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import swaiotos.runtime.Applet;

/**
 * @ClassName: NPAppletActivityStackManager
 * @Author: lu
 * @CreateDate: 2020/11/1 11:09 AM
 * @Description:
 */
class NPAppletActivityStackManager {
    private static final String TAG = "np-stack";
    public static NPAppletActivityStackManager manager = new NPAppletActivityStackManager();

    private final Map<String, List<NPAppletActivity>> activities = new ConcurrentHashMap<>();
    private final Map<String, Applet> rootAppletMap = new ConcurrentHashMap<>();

    private NPAppletActivityStackManager() {

    }

    public void push(String applet, NPAppletActivity activity) {
        synchronized (activities) {
            List<NPAppletActivity> nps = activities.get(applet);
            if (nps == null) {
                nps = new Stack<>();
                activities.put(applet, nps);
            }
            if (!nps.contains(activity)) {
                nps.add(activity);
                Log.d(TAG, "push " + activity + "@" + applet);
            }
            if(rootAppletMap.get(applet) == null && activity.getApplet() != null) {
                rootAppletMap.put(applet, activity.getApplet());
            }
        }
    }

    public void pop(String applet, NPAppletActivity activity) {
        synchronized (activities) {
            List<NPAppletActivity> nps = activities.get(applet);
            if (nps != null) {
                if (nps.contains(activity)) {
                    nps.remove(activity);
                }
                if (nps.isEmpty()) {
                    activities.remove(applet);
                }
                Log.d(TAG, "pop " + activity + "@" + applet);
            }
        }
    }

    public boolean remove(String applet, NPAppletActivity activity) {
        synchronized (activities) {
            List<NPAppletActivity> nps = activities.get(applet);
            if (nps != null) {
                boolean r = nps.remove(activity);
                if (nps.isEmpty()) {
                    activities.remove(applet);
                }
                Log.d(TAG, "remove " + r + "@" + applet);
                if(nps.isEmpty()) {
                    rootAppletMap.remove(applet);
                }
                return r;
            }
        }
        return false;
    }

    public boolean exit(String applet) {
        synchronized (activities) {
            List<NPAppletActivity> nps = activities.get(applet);
            if (nps != null) {
                for (NPAppletActivity activity : nps) {
                    activity.finish();
                    activity.overridePendingTransition(0, swaiotos.runtime.base.R.anim.applet_exit);
                    Log.d(TAG, "exit " + activity + "@" + applet);
                }
                activities.remove(applet);
                if(nps.isEmpty()) {
                    rootAppletMap.remove(applet);
                }
                return true;
            }
        }
        return false;
    }

    public void exitAll() {
        synchronized (activities) {
            Set<String> applets = activities.keySet();
            for (String applet : applets) {
                exit(applet);
            }
            activities.clear();
        }
    }

    public int size(String applet) {
        return activities.get(applet) == null ? 0 : activities.get(applet).size();
    }

    public Applet getRootApplet(String applet) {
        return rootAppletMap.get(applet);
    }
}
