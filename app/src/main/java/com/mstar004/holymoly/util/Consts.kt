package com.pstorli.wackymole.util

import android.util.Log
import com.airbnb.lottie.BuildConfig.DEBUG

object Consts {

    const val LEVEL_TIME    = 60L
    const val NEGATIVE      = -1
    const val SECOND        = 1000L // 1000 Milli seconds == 1 Second
    const val SQUARE_SIZE   = 96
    const val ZERO          = 0

    val GAME_SPEED          = 500L
    val GAME_SPEED_FASTEST  = 100L
    val GAME_SPEED_DEC      = 50L

    const val TAG           = "WackyMole"


    fun logError (ex: Exception)
    {
        logError (TAG, ex.toString())
    }

    fun logError (msg:String, t: Throwable? = null)
    {
        Log.e (TAG, msg, t)
    }

    fun logError (msg:String)
    {
        logError (TAG, msg)
    }

    fun logError (tag: String, msg:String)
    {
        Log.e (tag, msg)
    }

    fun logWarning (msg:String)
    {
        Log.w (TAG, msg)
    }

    fun logWarning (tag: String, msg:String)
    {
        Log.w (tag, msg)
    }

    fun logInfo (msg:String)
    {
        Log.i (TAG, msg)
    }

    fun logInfo (tag: String, msg:String)
    {
        Log.i (tag, msg)
    }

    fun debug (msg:String)
    {
        debug (TAG, msg)
    }

    fun debug (tag: String, msg:String)
    {
        if (DEBUG)  Log.d(tag, msg)
    }
}