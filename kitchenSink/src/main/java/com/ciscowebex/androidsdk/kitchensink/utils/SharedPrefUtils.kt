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

    fun clearLoginTypePref(context:Context) {
        val pref = context.getSharedPreferences(Constants.Keys.KitchenSinkSharedPref, Context.MODE_PRIVATE)

        pref?.let {
            it.edit().remove(Constants.Keys.LoginType).apply()
        }
    }

    fun getLoginTypePref(context:Context): String? {
        val pref = context.getSharedPreferences(Constants.Keys.KitchenSinkSharedPref, Context.MODE_PRIVATE)

        pref?.let {
            return pref.getString(Constants.Keys.LoginType, null)
        }

        return null
    }
}