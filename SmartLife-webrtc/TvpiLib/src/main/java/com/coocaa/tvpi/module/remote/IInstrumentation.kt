package com.coocaa.tvpi.module.remote

import android.app.Activity

/**
 * @Author: yuzhan
 */
interface IInstrumentation {
    fun onActivityCreate(activity: Activity)
    fun onActivityResume(activity: Activity)
    fun onActivityPause(activity: Activity)
    fun onActivityStop(activity: Activity)
    fun onActivityDestroy(activity: Activity)
}