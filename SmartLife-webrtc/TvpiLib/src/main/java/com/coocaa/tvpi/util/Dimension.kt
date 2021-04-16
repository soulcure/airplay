package com.coocaa.tvpi.util

import android.content.res.Resources
import android.util.TypedValue

/**
 * 正常编码中一般只会用到 [dp]/[sp] ;
 * 其中[dp]/[sp] 会根据系统分辨率将输入的dp/sp值转换为对应的px
 */
val Float.dp: Float
    get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics)

val Int.dp: Int
    get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics).toInt()


val Float.sp: Float                 // [xxhdpi](360 -> 1080)
    get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, this, Resources.getSystem().displayMetrics)

val Int.sp: Int
    get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, this.toFloat(), Resources.getSystem().displayMetrics).toInt()