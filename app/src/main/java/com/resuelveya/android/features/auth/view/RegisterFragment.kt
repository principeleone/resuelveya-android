package com.resuelveya.android.features.auth.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.resuelveya.android.R
import com.resuelveya.android.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupListeners()
    }

    private fun setupViews() {
        // Inicializar vista con el rol Cliente por defecto
        updateRoleView(isProvider = false)
    }

    private fun setupListeners() {
        binding.toggleRol.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val isProvider = checkedId == R.id.btnRolProveedor
            updateRoleView(isProvider)
        }

        binding.btnIrLogin.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnRegistrar.setOnClickListener {
            val isProvider = binding.toggleRol.checkedButtonId == R.id.btnRolProveedor
            val roleName = if (isProvider) "Proveedor" else "Cliente"
            // Feedback temporal para validación visual de la pantalla
            Toast.makeText(
                requireContext(),
                "Formulario de registro ($roleName) listo para integración",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateRoleView(isProvider: Boolean) {
        if (isProvider) {
            binding.layoutCamposProveedor.visibility = View.VISIBLE
            binding.tvRolInfo.setText(R.string.auth_subtitulo_proveedor)
            binding.btnRegistrar.text = getString(R.string.auth_btn_registrar) + " (PROVEEDOR)"
            return
        }

        binding.layoutCamposProveedor.visibility = View.GONE
        binding.tvRolInfo.setText(R.string.auth_subtitulo_cliente)
        binding.btnRegistrar.text = getString(R.string.auth_btn_registrar) + " (CLIENTE)"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}