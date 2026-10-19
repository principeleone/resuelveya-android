package com.resuelveya

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.resuelveya.databinding.ActivityMainClienteBinding
import com.resuelveya.ui.home.HomeFragment
// Ajusta estos imports a los paquetes reales de tus fragments:
// import com.resuelveya.ui.jobs.JobsFragment
// import com.resuelveya.ui.messages.MessagesFragment
// import com.resuelveya.ui.profile.ProfileFragment

class MainClienteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainClienteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainClienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        // Cambiar de pestaña
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> loadFragment(HomeFragment())
                //R.id.nav_jobs -> loadFragment(JobsFragment())
                //R.id.nav_messages -> loadFragment(MessagesFragment())
                //R.id.nav_profile -> loadFragment(ProfileFragment())
                else -> return@setOnItemSelectedListener false
            }
            true
        }

        // Tocar la pestaña que ya está seleccionada (tu caso: Inicio estando en el detalle)
        binding.bottomNav.setOnItemReselectedListener { item ->
            if (item.itemId == R.id.nav_home) {
                loadFragment(HomeFragment())
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        // Limpia las pantallas de detalle acumuladas antes de mostrar la pestaña
        supportFragmentManager.popBackStack(
            null, FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}