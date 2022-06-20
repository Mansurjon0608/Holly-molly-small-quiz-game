package com.pstorli.wackymole.util

import android.app.Application
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.mstar004.holymoly.logError
import com.pstorli.wackymole.util.Consts.TAG
import com.pstorli.wackymole.util.Consts.ZERO

/**
 * Secure / Encrypted shared prefs.
 */
class MolePrefs (application: Application) {

    // *********************************************************************************************
    // Shared Prefs Keys
    // *********************************************************************************************
    val LEVEL         = "LEVEL"
    val SCORE         = "SCORE"

    // *********************************************************************************************
    // Vars
    // *********************************************************************************************
    lateinit var sharedPreferences: SharedPreferences

    init {
        loadSharedPreference(application)
    }

    /**
     * Get the level.
     */
    fun getlevel (): Int {
        return getPref(LEVEL, ZERO)
    }

    /**
     * Get the score.
     */
    fun getScore (): Int {
        return getPref(SCORE, ZERO)
    }

    /**
     * Save the level.
     */
    fun saveLevel (level: Int) {
        setPref(LEVEL, level)
    }

    /**
     * Save the score.
     */
    fun saveScore (score: Int) {
        setPref(SCORE, score)
    }

    /**
     * Set up shared prefs.
     */
    private fun loadSharedPreference(application: Application): SharedPreferences {
        try {
            sharedPreferences = EncryptedSharedPreferences.create(
                application,
                TAG,
                getMasterKey(application),
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
        catch (ex: Exception) {
            ex.logError()
        }

        return sharedPreferences
    }

    /**
     * Save a preference
     */
    fun setPref (key:String, value:String)
    {
        sharedPreferences.edit()
            .putString(key, value)
            .apply()
    }

    /**
     * Save a preference
     */
    fun setPref (key:String, value:Int)
    {
        sharedPreferences.edit()
            .putInt(key, value)
            .apply()
    }

    /**
     * Save a preference
     */
    fun setPref (key:String, value:Boolean)
    {
        sharedPreferences.edit()
            .putBoolean(key, value)
            .apply()
    }

    /**
     * Get a string
     */
    fun getPref (key:String, defValue: String = ""): String
    {
        var result = defValue
        if (sharedPreferences.contains(key)) {
            sharedPreferences.getString(key, defValue).let {
                result = it!!
            }
        }
        return result
    }

    /**
     * Get an int pref.
     */
    fun getPref (key:String, defValue: Int=0): Int
    {
        var result = defValue
        if (sharedPreferences.contains(key)) {
            result = sharedPreferences.getInt(key, defValue)
        }
        return result
    }

    /**
     * Get an int pref.
     */
    fun getPref (key:String, defValue: Boolean=false): Boolean
    {
        var result = defValue
        if (sharedPreferences.contains(key)) {
            result = sharedPreferences.getBoolean(key, defValue)
        }
        return result
    }

    /**
     * Get the master key.
     */
    private fun getMasterKey(application: Application): MasterKey {
        return MasterKey.Builder(application)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }
}