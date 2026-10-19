package com.resuelveya.android.features.perfil.view

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.resuelveya.android.core.session.SessionManager
import com.resuelveya.android.databinding.FragmentPerfilBinding
import com.resuelveya.android.features.auth.view.AuthActivity

class PerfilFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupListeners()
    }

    private fun setupViews() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val rol = SessionManager.getRol() ?: "TECNICO"

        binding.tvNombrePerfil.text = currentUser?.displayName?.ifBlank { currentUser.email } ?: currentUser?.email ?: "Usuario"
        binding.chipRol.text = rol.uppercase()
    }

    private fun setupListeners() {
        binding.btnLogout.setOnClickListener {
            // Cerrar sesión en Firebase y SessionManager
            FirebaseAuth.getInstance().signOut()
            SessionManager.logout()

            val intent = Intent(requireContext(), AuthActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}