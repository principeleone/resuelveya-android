package com.resuelveya.android.features.home.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.resuelveya.android.core.session.SessionManager
import com.resuelveya.android.databinding.ActivityMainClienteBinding
import com.resuelveya.android.features.auth.view.AuthActivity

class MainClienteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainClienteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainClienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupViews()
        setupListeners()
    }

    private fun setupViews() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val email = currentUser?.email ?: "Sin correo"
        val nombre = currentUser?.displayName?.ifBlank { null } ?: email

        binding.tvBienvenidaCliente.text = "Hola, $nombre"
        binding.tvEmailCliente.text = email
        binding.chipRolCliente.text = "ROL: " + (SessionManager.getRol() ?: "CLIENTE")
    }

    private fun setupListeners() {
        binding.btnLogoutCliente.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            SessionManager.logout()

            val intent = Intent(this, AuthActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }
}