package com.ciscowebex.androidsdk.kitchensink.utils

import android.content.Context
import com.ciscowebex.androidsdk.kitchensink.auth.LoginActivity

object SharedPrefUtils {
    fun saveLoginTypePref(context:Context, type: LoginActivity.LoginType) {
        val pref = context.getSharedPreferences(Constants.Keys.KitchenSinkSharedPref, Context.MODE_PRIVATE)

        pref?.let {
            it.edit().putString(Constants.Keys.LoginType, type.value).apply()
        }
    }

    fun getLoginTypePref(context:Context): String? {
        val pref = context.getSharedPreferences(Constants.Keys.KitchenSinkSharedPref, Context.MODE_PRIVATE)

        pref?.let {
            return pref.getString(Constants.Keys.LoginType, null)
        }

        return null
    }

    fun saveEmailPref(context:Context, email: String) {
        val pref = context.getSharedPreferences(Constants.Keys.KitchenSinkSharedPref, Context.MODE_PRIVATE)
        pref?.edit()?.putString(Constants.Keys.Email, email)?.apply()
    }

    fun clearEmailPref(context:Context) {
        val pref = context.getSharedPreferences(Constants.Keys.KitchenSinkSharedPref, Context.MODE_PRIVATE)
        pref?.edit()?.remove(Constants.Keys.Email)?.apply()
    }

    fun getEmailPref(context:Context): String? {
        val pref = context.getSharedPreferences(Constants.Keys.KitchenSinkSharedPref, Context.MODE_PRIVATE)

        pref?.let {
            return pref.getString(Constants.Keys.Email, null)
        }

        return null
    }

    fun setVirtualBgAdded(context:Context, isAdded: Boolean) {
        val pref = context.getSharedPreferences(Constants.Keys.KitchenSinkSharedPref, Context.MODE_PRIVATE)
        pref?.edit()?.putBoolean(Constants.Keys.IsVirtualBgAdded, isAdded)?.apply()
    }

    fun isVirtualBgAdded(context: Context): Boolean {
        val pref = context.getSharedPreferences(Constants.Keys.KitchenSinkSharedPref, Context.MODE_PRIVATE)

        pref?.let {
            return pref.getBoolean(Constants.Keys.IsVirtualBgAdded, false)
        }

        return false
    }

    fun setAppBackgroundRunningPreferred(context:Context, isPreferred: Boolean) {
        val pref = context.getSharedPreferences(Constants.Keys.KitchenSinkSharedPref, Context.MODE_PRIVATE)
        pref?.edit()?.putBoolean(Constants.Keys.IsBackgroundRunningEnabled, isPreferred)?.apply()
    }

    fun isAppBackgroundRunningPreferred(context: Context) : Boolean  {
        val pref = context.getSharedPreferences(Constants.Keys.KitchenSinkSharedPref, Context.MODE_PRIVATE)

        pref?.let {
            return pref.getBoolean(Constants.Keys.IsBackgroundRunningEnabled, false)
        }

        return false
    }

    fun getFedrampPref(context:Context): Boolean {
        val pref = context.getSharedPreferences(Constants.Keys.KitchenSinkSharedPref, Context.MODE_PRIVATE)

        pref?.let {
            return pref.getBoolean(Constants.Keys.Fedramp, false)
        }

        return false
    }

    fun saveFedrampPref(context:Context, enabled: Boolean) {
        val pref = context.getSharedPreferences(Constants.Keys.KitchenSinkSharedPref, Context.MODE_PRIVATE)
        pref?.edit()?.putBoolean(Constants.Keys.Fedramp, enabled)?.apply()
    }
}