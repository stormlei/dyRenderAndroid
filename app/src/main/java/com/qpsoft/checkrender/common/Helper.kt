package com.qpsoft.checkrender.common

import android.widget.ImageView
import coil.load
import coil.transform.CircleCropTransformation
import com.blankj.utilcode.util.CacheDiskStaticUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.TimeUtils
import com.qpsoft.checkrender.R
import com.qpsoft.checkrender.common.constants.Keys
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object Helper {
    //分转换元
    fun centToYuan(cent: Any): String {
        if (cent is Int) {
            return String.format("%.2f", cent/100f)
        } else if (cent is Float) {
            return String.format("%.2f", cent/100f)
        }
        return ""
    }

    fun long2Date(time: Long?): String {
        if (time == null) return ""
        val date = Date(time)
        val format = SimpleDateFormat("HH:mm")
        return format.format(date)
    }

    fun utc2Local(utcTime: String?): String {
        if (utcTime == null) return ""
        val utcFormat = if (utcTime.contains(".")) {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        } else {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
        }
        utcFormat.timeZone = TimeZone.getTimeZone("UTC")
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm")
        return format.format(utcFormat.parse(utcTime))
    }

    fun utc2LocalSimple(utcTime: String?): String {
        if (utcTime == null) return ""
        val utcFormat = if (utcTime.contains(".")) {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        } else {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
        }
        utcFormat.timeZone = TimeZone.getTimeZone("UTC")
        val format = SimpleDateFormat("HH:mm")
        return format.format(utcFormat.parse(utcTime))
    }

    @JvmStatic
    fun utc2LocalTwo(utcTime: String?): String {
        if (utcTime == null) return ""
        val utcFormat = if (utcTime.contains(".")) {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        } else {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
        }
        utcFormat.timeZone = TimeZone.getTimeZone("UTC")
        val format = SimpleDateFormat("yyyy-MM-dd")
        return format.format(utcFormat.parse(utcTime))
    }

    fun getTime(date: Date): String { //可根据需要自行截取数据显示
        val format = SimpleDateFormat("yyyy-MM-dd")
        return format.format(date)
    }

    fun birthdayToAge(dateStr: String?): Int { //可根据需要自行截取数据显示
        if (dateStr == null) return 0
        val format = SimpleDateFormat("yyyy")
        val birthdayYear = format.format(format.parse(dateStr)).toInt()
        val nowYear = format.format(Date()).toInt()
        return nowYear - birthdayYear
    }

    fun map2String(map: MutableMap<String, String>?): String {
        var s = ""
        if (map == null) {
            return s
        }
        for ((key, value ) in map) {
            s += "$key：$value，"
        }
        return s.substring(0, s.length - 1)
    }

    fun loadImage(imageView: ImageView, url: String?) {
        imageView.load(url) {
            crossfade(true)
            placeholder(R.drawable.default_pic)
            error(R.drawable.default_pic)
            transformations(CircleCropTransformation())
        }
    }

    fun loadFile(imageView: ImageView, picKey: String) {
        //val apiHost = CacheDiskStaticUtils.getString(Keys.HOST).replace("/api", "")
        val apiHost = ""
        val url = "$apiHost/storage/$picKey"
        val token = CacheDiskStaticUtils.getString(Keys.TOKEN)
        imageView.load(url) {
            crossfade(true)
            placeholder(R.drawable.pic_selector)
            error(R.drawable.pic_selector)
            addHeader("Authorization", "Bearer $token")
        }
    }
}