package com.coocaa.tvpi.util

import java.sql.Date


object DateUtils {
    private val weekArray = arrayListOf("星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六")

    fun getWeek(date: Date?): String {
        return if (date != null) weekArray[date.day] else ""
    }
}