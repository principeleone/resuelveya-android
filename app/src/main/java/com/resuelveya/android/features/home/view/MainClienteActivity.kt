package com.resuelveya.android.features.home.view

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
import com.resuelveya.android.R
import com.resuelveya.android.databinding.ActivityMainClienteBinding

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
            setOf(
                R.id.inicioClienteFragment,
                R.id.misContratacionesFragment,
                R.id.perfilFragment
            )
        )

        binding.toolbarCliente.setupWithNavController(navController, appBarConfiguration)

        binding.bottomNavigationCliente.setOnItemSelectedListener { item ->
            if (item.itemId == navController.currentDestination?.id) {
                return@setOnItemSelectedListener true
            }

            when (item.itemId) {
                R.id.inicioClienteFragment -> {
                    if (!navController.popBackStack(R.id.inicioClienteFragment, false)) {
                        navController.navigate(R.id.inicioClienteFragment)
                    }
                    true
                }
                R.id.misContratacionesFragment -> {
                    navController.navigate(
                        R.id.misContratacionesFragment,
                        null,
                        androidx.navigation.NavOptions.Builder()
                            .setPopUpTo(R.id.inicioClienteFragment, false)
                            .setLaunchSingleTop(true)
                            .build()
                    )
                    true
                }
                R.id.perfilFragment -> {
                    navController.navigate(
                        R.id.perfilFragment,
                        null,
                        androidx.navigation.NavOptions.Builder()
                            .setPopUpTo(R.id.inicioClienteFragment, false)
                            .setLaunchSingleTop(true)
                            .build()
                    )
                    true
                }
                else -> false
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id in setOf(R.id.inicioClienteFragment, R.id.misContratacionesFragment, R.id.perfilFragment)) {
                binding.toolbarCliente.navigationIcon = null
                val menuItem = binding.bottomNavigationCliente.menu.findItem(destination.id)
                if (menuItem != null && !menuItem.isChecked) {
                    menuItem.isChecked = true
                }
            } else {
                binding.toolbarCliente.setNavigationIcon(R.drawable.ic_arrow_back)
                binding.toolbarCliente.setNavigationOnClickListener {
                    navController.navigateUp()
                }
            }
        }

        setupBackPressedHandler()
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

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}