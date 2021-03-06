package com.badtools.informer

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object UtilityMethods {

    fun toast(context: Context, msg: String, length: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, msg, length).show()
    }
    //check if this is a first start
    fun isFirstStart(context: Context) = getCache(context,"FirstStart").toInt() == 0
    //get android id
    @SuppressLint("HardwareIds")
    fun getAndroidID(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }
    //get current date
    @RequiresApi(Build.VERSION_CODES.O)
    fun currentDate(context: Context, pattern: String = "yyyy-MM-dd"): String {
        val dateRaw = LocalDateTime.now()
        val dateFormat = DateTimeFormatter.ofPattern(pattern)
        return dateRaw.format(dateFormat)
    }
    //string to date convert
    @RequiresApi(Build.VERSION_CODES.O)
    fun str2Date(context: Context, strDate: String): LocalDate {
        val simpleFormat = DateTimeFormatter.ISO_DATE
        return LocalDate.parse(strDate, simpleFormat)
    }
    //get and set cache
    fun setCache(context: Context, key: String, value: String) {
        val pref = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putString(key, value)
        editor.apply()
    }
    fun getCache(context: Context, key: String): String {
        val pref = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
        val defaultValue = "0"
        return pref.getString(key, defaultValue).toString()
    }

    //play sounds
    //normal key tap sound
    fun playClick(context: Context) {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        am.playSoundEffect(AudioManager.FX_KEY_CLICK, 1f)
    }
    //play beef tune when key press
    fun playBeep(context: Context, tune: Int) {
        try {
            val track: MediaPlayer? = MediaPlayer.create(context, tune)
            track?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    //disable any btn
    fun disable(context: Context, msg: String) {
        toast(context, msg)
    }
}
