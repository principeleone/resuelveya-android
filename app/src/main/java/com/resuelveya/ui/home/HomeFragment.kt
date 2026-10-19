package com.resuelveya.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.resuelveya.R
import com.resuelveya.adapter.EspecialidadAdapter
import com.resuelveya.databinding.FragmentHomeBinding
import com.resuelveya.ui.tecnicos.TecnicosFragment

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // Vive en la Activity: sobrevive a los replace() del menú inferior
    private val viewModel: HomeViewModel by activityViewModels()

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

        // Mostrar los datos cuando lleguen (o al instante si ya estaban guardados)
        viewModel.especialidades.observe(viewLifecycleOwner) { lista ->
            adapter.submitList(lista)
        }

        // Mostrar el error una sola vez
        viewModel.error.observe(viewLifecycleOwner) { mensaje ->
            if (mensaje != null) {
                Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show()
                viewModel.errorMostrado()
            }
        }

        // Solo llama al servidor si todavía no hay datos
        viewModel.cargarEspecialidades()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}