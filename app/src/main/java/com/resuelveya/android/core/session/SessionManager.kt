package com.resuelveya.android.core.session

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

    init {
        // Asegura que la última instancia creada quede registrada como singleton
        if (instance == null) {
            instance = this
        }
    }

    fun saveToken(token: String) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .apply()
    }

    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun saveRol(rol: String) {
        prefs.edit()
            .putString(KEY_ROL, rol)
            .apply()
    }

    fun getRol(): String? {
        return prefs.getString(KEY_ROL, null)
    }

    fun isLoggedIn(): Boolean {
        return !getToken().isNullOrBlank()
    }

    fun logout() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "resuelveya_session"
        private const val KEY_TOKEN = "TOKEN"
        private const val KEY_ROL = "ROL"

        @Volatile
        private var instance: SessionManager? = null

        fun init(context: Context): SessionManager {
            return instance ?: synchronized(this) {
                instance ?: SessionManager(context.applicationContext).also { instance = it }
            }
        }

        fun getInstance(context: Context? = null): SessionManager {
            if (instance == null && context != null) {
                init(context)
            }
            return checkNotNull(instance) {
                "SessionManager must be initialized before use. Call SessionManager.init(context) or pass a Context to getInstance."
            }
        }

        fun getToken(): String? {
            return instance?.getToken()
        }

        fun getRol(): String? {
            return instance?.getRol()
        }

        fun isLoggedIn(): Boolean {
            return instance?.isLoggedIn() ?: false
        }

        fun logout() {
            instance?.logout()
        }
    }
}