package com.coocaa.tvpi.util

import android.content.Intent
import android.net.Uri
import java.lang.Exception

/**
 * @Author: yuzhan
 */
object IntentUtils {

    fun getStringExtra(intent: Intent, key: String) : String? {
        if(intent.hasExtra(key)) {
            return intent.getStringExtra(key)
        }
        return intent.data?.getQueryParameter(key)?: ""
    }

    fun getBooleanExtra(intent: Intent, key: String) : Boolean{
        if(intent.hasExtra(key)) {
            return intent.getBooleanExtra(key, false)
        }
        return intent.data?.getBooleanQueryParameter(key, false)?: false
    }

    fun getIntExtra(intent: Intent, key: String, defaultValue: Int) : Int{
        if(intent.hasExtra(key)) {
            return intent.getIntExtra(key, defaultValue)
        }
        val value = intent.data?.getQueryParameter(key)
        try {
            return value?.toInt()?: defaultValue
        } catch (e: Exception) {
            return defaultValue
        }
    }
}