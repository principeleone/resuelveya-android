package com.resuelveya.android.features.tecnicos

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
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

        adapter = EspecialidadAdapter { especialidad ->
            Log.d("HomeFragment", "Click en: ${especialidad.nombre} (id=${especialidad.id})")

            val fragment = TecnicosFragment.newInstance(
                especialidad.id.toLong(),
                especialidad.nombre
            )
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit()
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
                Toast.makeText(
                    requireContext(),
                    "No se pudo conectar con el servidor",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}