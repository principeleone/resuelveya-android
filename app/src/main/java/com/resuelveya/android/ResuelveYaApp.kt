package com.resuelveya.android

import android.app.Application
import com.google.android.gms.maps.GoogleMap
import com.resuelveya.android.core.database.AppDatabase
import com.resuelveya.android.core.session.SessionManager

class ResuelveYaApp : Application() {

    private lateinit var map: GoogleMap

    override fun onCreate() {
        super.onCreate()
        SessionManager.init(this)
        AppDatabase.getInstance(this)
    }
}
