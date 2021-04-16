package com.coocaa.tvpi.module.remote

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log

/**
 * @Author: yuzhan
 */
//class TvpiInstrumentation: Instrumentation {
object TvpiInstrumentation {
//    public interface IInstrumentation {
//        fun onActivityCreate(activity: Activity)
//        fun onActivityResume(activity: Activity)
//        fun onActivityPause(activity: Activity)
//        fun onActivityStop(activity: Activity)
//        fun onActivityDestroy(activity: Activity)
//    }

//    private var instrumentation: Instrumentation? = null


//    constructor(ins: Instrumentation) {
//        this.instrumentation = ins
//    }

    fun setCallback(i: IInstrumentation?) {
        callback = i
    }

    val TAG = "Tvpi"

    private var callback: IInstrumentation? = null

    fun init(context: Context?) {
        Log.d(TAG, "init .................................")
        try {
            var application: Application? = null
            if(context is Application) {
                application = context as Application
            }
            application?.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
                override fun onActivityPaused(activity: Activity) {
                    callback?.onActivityPause(activity)
                }

                override fun onActivityStarted(activity: Activity) {

                }

                override fun onActivityDestroyed(activity: Activity) {
                    callback?.onActivityDestroy(activity)
                }

                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                }

                override fun onActivityStopped(activity: Activity) {
                    callback?.onActivityStop(activity)
                }

                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                    callback?.onActivityCreate(activity)
                }

                override fun onActivityResumed(activity: Activity) {
                    callback?.onActivityResume(activity)
                }
            })
        } catch (e: Exception) {

        }
    }

//    companion object {
//        val TAG = "Tvpi"
//
//        private var callback: TvpiInstrumentation.IInstrumentation? = null
//
//        fun init(context: Context?): TvpiInstrumentation? {
//            try {
//                Log.d(TAG, "init tvpi instrumentation start")
//                val activityThreadClazz = Class.forName("android.app.ActivityThread")
//                val currentActivityField = activityThreadClazz.getDeclaredField("sCurrentActivityThread")
//                currentActivityField.isAccessible = true
//                val activityThreadObject = currentActivityField[null]
//                val mInstrumentation = activityThreadClazz.getDeclaredField("mInstrumentation")
//                mInstrumentation.isAccessible = true
//                val instrumentation = mInstrumentation[activityThreadObject] as Instrumentation
//                val tvpiInstrumentation = TvpiInstrumentation(instrumentation)
//                mInstrumentation[activityThreadObject] = tvpiInstrumentation
//                Log.d(TAG, "init tvpi instrumentation end")
//                return tvpiInstrumentation
//            } catch (e: Exception) {
//                Log.w(TAG, "init tvpi instrumentation fail : $e")
//                e.printStackTrace()
//            }
//            return null
//        }
//    }

//    override fun newActivity(cl: ClassLoader?, className: String?, intent: Intent?): Activity {
//        if(instrumentation == null)
//            return super.newActivity(cl, className, intent)
//        return instrumentation!!.newActivity(cl, className, intent)
//    }
//
//    override fun newActivity(clazz: Class<*>?, context: Context?, token: IBinder?, application: Application?, intent: Intent?, info: ActivityInfo?, title: CharSequence?, parent: Activity?, id: String?, lastNonConfigurationInstance: Any?): Activity {
//        if(instrumentation == null)
//            return super.newActivity(clazz, context, token, application, intent, info, title, parent, id, lastNonConfigurationInstance)
//        return instrumentation!!.newActivity(clazz, context, token, application, intent, info, title, parent, id, lastNonConfigurationInstance)
//    }
//
//    override fun callActivityOnCreate(activity: Activity, icicle: Bundle?, persistentState: PersistableBundle?) {
//        Log.d(TAG, "callActivityOnCreate ： " + activity.javaClass.name)
//        super.callActivityOnCreate(activity, icicle, persistentState)
//        if (callback != null) {
//            callback!!.onActivityCreate(activity)
//        }
//    }
//
//    override fun callActivityOnCreate(activity: Activity, icicle: Bundle?) {
//        Log.d(TAG, "callActivityOnCreate ： " + activity.javaClass.name)
//        super.callActivityOnCreate(activity, icicle)
//        if (callback != null) {
//            callback!!.onActivityCreate(activity)
//        }
//    }
//
//    override fun callActivityOnResume(activity: Activity) {
//        Log.d(TAG, "callActivityOnResume ： " + activity.javaClass.name)
//        super.callActivityOnResume(activity)
//        if (callback != null) {
//            callback!!.onActivityResume(activity)
//        }
//    }
//
//    override fun callActivityOnPause(activity: Activity) {
//        Log.d(TAG, "callActivityOnPause ： " + activity.javaClass.name)
//        super.callActivityOnPause(activity)
//        if (callback != null) {
//            callback!!.onActivityPause(activity)
//        }
//    }
//
//    override fun callActivityOnStop(activity: Activity) {
//        Log.d(TAG, "callActivityOnStop ： " + activity.javaClass.name)
//        super.callActivityOnStop(activity)
//        if (callback != null) {
//            callback!!.onActivityStop(activity)
//        }
//    }
//
//    override fun callActivityOnDestroy(activity: Activity) {
//        Log.d(TAG, "callActivityOnDestroy ： " + activity.javaClass.name)
//        super.callActivityOnDestroy(activity)
//        if (callback != null) {
//            callback!!.onActivityDestroy(activity)
//        }
//    }
}