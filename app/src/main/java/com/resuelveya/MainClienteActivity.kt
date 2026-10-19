package com.resuelveya

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.resuelveya.databinding.ActivityMainClienteBinding
import com.resuelveya.ui.home.HomeFragment

class MainClienteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainClienteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainClienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, HomeFragment())
                .commit()
        }
    }
}