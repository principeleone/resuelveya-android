package com.resuelveya.android.features.auth.view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.resuelveya.android.R
import com.resuelveya.android.core.session.SessionManager

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            val nextIntent = if (SessionManager.isLoggedIn()) {
                val rol = SessionManager.getRol().orEmpty()
                if (rol.equals("TECNICO", ignoreCase = true)) {
                    Intent(this, com.resuelveya.android.features.home.view.MainProveedorActivity::class.java)
                } else {
                    Intent(this, com.resuelveya.android.features.home.view.MainClienteActivity::class.java)
                }
            } else {
                Intent(this, AuthActivity::class.java)
            }

            startActivity(nextIntent)
            finish()
        }, 2000)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}