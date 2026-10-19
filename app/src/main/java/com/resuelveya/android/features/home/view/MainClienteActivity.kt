package com.resuelveya.android.features.home.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import com.resuelveya.android.R
import com.resuelveya.android.core.session.SessionManager
import com.resuelveya.android.databinding.ActivityMainClienteBinding
import com.resuelveya.android.features.auth.view.AuthActivity

class MainClienteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainClienteBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainClienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentContainerViewCliente) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.inicioClienteFragment)
        )

        binding.toolbarCliente.setupWithNavController(navController, appBarConfiguration)
        binding.bottomNavigationCliente.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.inicioClienteFragment) {
                binding.toolbarCliente.navigationIcon = null
            } else {
                binding.toolbarCliente.setNavigationIcon(R.drawable.ic_arrow_back)
                binding.toolbarCliente.setNavigationOnClickListener {
                    if (destination.id == R.id.misContratacionesFragment || destination.id == R.id.perfilFragment) {
                        binding.bottomNavigationCliente.selectedItemId = R.id.inicioClienteFragment
                    } else {
                        navController.navigateUp()
                    }
                }
            }
        }

        setupBackPressedHandler()
        setupToolbarMenu()
    }

    private fun setupBackPressedHandler() {
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentId = navController.currentDestination?.id
                if (currentId == R.id.misContratacionesFragment || currentId == R.id.perfilFragment) {
                    binding.bottomNavigationCliente.selectedItemId = R.id.inicioClienteFragment
                } else if (currentId != null && currentId != R.id.inicioClienteFragment) {
                    if (!navController.navigateUp()) {
                        binding.bottomNavigationCliente.selectedItemId = R.id.inicioClienteFragment
                    }
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        })
    }

    private fun setupToolbarMenu() {
        binding.toolbarCliente.inflateMenu(R.menu.menu_toolbar_cliente)
        binding.toolbarCliente.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_logout -> {
                    performLogout()
                    true
                }
                else -> false
            }
        }
    }

    private fun performLogout() {
        FirebaseAuth.getInstance().signOut()
        SessionManager.logout()

        val intent = Intent(this, AuthActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}