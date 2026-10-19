package com.resuelveya.android

import android.app.Application
import com.resuelveya.android.core.session.SessionManager

class ResuelveYaApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Inicializa el gestor de sesión global con el contexto de la aplicación
        SessionManager.init(this)
    }
}
