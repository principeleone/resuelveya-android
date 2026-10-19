package com.resuelveya.android.features.proveedor.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.resuelveya.android.R
import com.resuelveya.android.core.session.SessionManager
import com.resuelveya.android.databinding.FragmentInicioProveedorBinding

class InicioProveedorFragment : Fragment() {

    private var _binding: FragmentInicioProveedorBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInicioProveedorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    private fun setupViews() {
        val rol = SessionManager.getRol() ?: "TECNICO"
        binding.tvSaludo.text = "Hola, Técnico"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}