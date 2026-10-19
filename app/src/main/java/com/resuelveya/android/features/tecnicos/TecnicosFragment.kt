package com.resuelveya.android.features.tecnicos

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.resuelveya.android.R
import com.resuelveya.android.databinding.FragmentTecnicosBinding
import com.resuelveya.android.features.tecnicos.adapter.TecnicoAdapter
import com.resuelveya.android.features.tecnicos.network.RetrofitClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TecnicosFragment : Fragment() {

    private var _binding: FragmentTecnicosBinding? = null
    private val binding get() = _binding!!

    private var categoriaId: Long = 0L
    private var categoriaNombre: String = ""

    private lateinit var adapter: TecnicoAdapter
    private var searchJob: Job? = null

    companion object {
        fun newInstance(id: Long, nombre: String) = TecnicosFragment().apply {
            arguments = bundleOf(
                "categoriaId" to id,
                "categoriaNombre" to nombre
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        categoriaId = arguments?.getLong("categoriaId") ?: 0L
        categoriaNombre = arguments?.getString("categoriaNombre").orEmpty()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTecnicosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTitulo.text = categoriaNombre.ifBlank { "Técnicos Disponibles" }

        adapter = TecnicoAdapter { tecnico ->
            val bundle = bundleOf(
                "tecnicoId" to tecnico.id,
                "tecnicoNombre" to tecnico.nombre
            )
            try {
                findNavController().navigate(
                    R.id.action_tecnicosFragment_to_tecnicoDetalleFragment,
                    bundle
                )
            } catch (e: Exception) {
                Log.e("TecnicosFragment", "Error navegando a tecnicoDetalleFragment", e)
            }
        }
        binding.rvTecnicos.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTecnicos.adapter = adapter

        binding.etBuscar.doAfterTextChanged { texto ->
            searchJob?.cancel()
            searchJob = viewLifecycleOwner.lifecycleScope.launch {
                delay(400) // espera a que el usuario deje de escribir
                cargarTecnicos(texto?.toString())
            }
        }

        viewLifecycleOwner.lifecycleScope.launch { cargarTecnicos(null) }
    }

    private suspend fun cargarTecnicos(nombre: String?) {
        try {
            val idParam = if (categoriaId > 0L) categoriaId else null
            val respuesta = RetrofitClient.api.listarTecnicos(
                categoriaId = idParam,
                nombre = nombre?.takeIf { it.isNotBlank() }
            )
            val b = _binding ?: return

            val lista = respuesta.body()?.content.orEmpty()
            adapter.submitList(lista)
            b.tvVacio.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("TecnicosFragment", "Error al cargar técnicos", e)
            if (_binding != null) {
                Toast.makeText(
                    requireContext(),
                    "No se pudo cargar los técnicos",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchJob?.cancel()
        _binding = null
    }
}