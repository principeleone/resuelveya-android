package com.resuelveya.android.features.tecnicos

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.resuelveya.android.R
import com.resuelveya.android.databinding.FragmentHomeBinding
import com.resuelveya.android.features.tecnicos.adapter.EspecialidadAdapter
import com.resuelveya.android.features.tecnicos.network.RetrofitClient
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: EspecialidadAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUser = FirebaseAuth.getInstance().currentUser
        val displayName = currentUser?.displayName?.ifBlank { null }
            ?: currentUser?.email?.substringBefore("@")
            ?: "Cliente"
        binding.tvGreeting.text = "Hola, $displayName 👋"

        adapter = EspecialidadAdapter { especialidad ->
            Log.d("HomeFragment", "Click en: ${especialidad.nombre} (id=${especialidad.id})")

            val bundle = bundleOf(
                "categoriaId" to especialidad.id.toLong(),
                "categoriaNombre" to especialidad.nombre
            )
            try {
                findNavController().navigate(R.id.action_homeFragment_to_tecnicosFragment, bundle)
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error navegando a tecnicosFragment", e)
            }
        }

        binding.rvEspecialidades.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvEspecialidades.adapter = adapter

        cargarEspecialidades()
    }

    private fun cargarEspecialidades() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val lista = RetrofitClient.api.getEspecialidades()
                adapter.submitList(lista)
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error al cargar especialidades", e)
                if (_binding != null) {
                    Toast.makeText(
                        requireContext(),
                        "No se pudo conectar con el servidor",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}