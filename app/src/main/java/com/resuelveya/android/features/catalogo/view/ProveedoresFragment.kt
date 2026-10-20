package com.resuelveya.android.features.catalogo.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.resuelveya.android.R
import com.resuelveya.android.databinding.FragmentProveedoresBinding
import com.resuelveya.android.features.catalogo.data.CatalogoRepository
import com.resuelveya.android.features.catalogo.model.PageResponse
import com.resuelveya.android.features.catalogo.model.TecnicoPublico
import com.resuelveya.android.features.catalogo.view.adapter.ProveedorAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProveedoresFragment : Fragment() {

    private var _binding: FragmentProveedoresBinding? = null
    private val binding get() = _binding!!

    private val repository = CatalogoRepository()
    private lateinit var adapter: ProveedorAdapter

    private var categoriaId: Long = -1L
    private var categoriaNombre: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            categoriaId = it.getLong("categoriaId", -1L)
            categoriaNombre = it.getString("categoriaNombre")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProveedoresBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupHeader()
        setupRecyclerView()
        loadProveedores()
    }

    private fun setupHeader() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        val title = if (!categoriaNombre.isNullOrBlank()) {
            "Técnicos en $categoriaNombre"
        } else {
            "Técnicos Disponibles"
        }
        binding.tvTituloCategoria.text = title
    }

    private fun setupRecyclerView() {
        adapter = ProveedorAdapter { tecnico ->
            navigateToDetalle(tecnico)
        }
        binding.rvProveedores.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProveedores.adapter = adapter
    }

    private fun loadProveedores() {
        binding.progress.visibility = View.VISIBLE
        binding.tvVacio.visibility = View.GONE

        val targetCategoriaId = if (categoriaId > 0) categoriaId else 0L

        repository.getTecnicosPorCategoria(targetCategoriaId, object : Callback<PageResponse<TecnicoPublico>> {
            override fun onResponse(
                call: Call<PageResponse<TecnicoPublico>>,
                response: Response<PageResponse<TecnicoPublico>>
            ) {
                if (_binding == null) return
                binding.progress.visibility = View.GONE

                if (response.isSuccessful) {
                    val lista = response.body()?.content.orEmpty()
                    if (lista.isEmpty()) {
                        binding.tvVacio.visibility = View.VISIBLE
                        binding.rvProveedores.visibility = View.GONE
                    } else {
                        binding.tvVacio.visibility = View.GONE
                        binding.rvProveedores.visibility = View.VISIBLE
                        adapter.updateData(lista)
                    }
                    return
                }

                showErrorState("Error al obtener técnicos (${response.code()})")
            }

            override fun onFailure(call: Call<PageResponse<TecnicoPublico>>, t: Throwable) {
                if (_binding == null) return
                binding.progress.visibility = View.GONE
                showErrorState("No se pudo conectar con el servidor")
                Log.e(TAG, "Error de conexión al cargar proveedores", t)
            }
        })
    }

    private fun showErrorState(message: String) {
        binding.tvVacio.text = message
        binding.tvVacio.visibility = View.VISIBLE
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToDetalle(tecnico: TecnicoPublico) {
        val bundle = Bundle().apply {
            putLong("proveedorId", tecnico.id)
            putString("nombre", tecnico.nombre)
            putString("especialidad", tecnico.especialidadNombre)
            putString("distrito", tecnico.distrito ?: tecnico.ciudad)
            putDouble("calificacion", tecnico.calificacionPromedio ?: 5.0)
            putInt("totalResenias", tecnico.totalResenias ?: 0)
            putString("presentacion", tecnico.presentacion)
        }
        try {
            findNavController().navigate(R.id.proveedorDetalleFragment, bundle)
        } catch (e: Exception) {
            Log.e(TAG, "Error navegando a proveedorDetalleFragment", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "ProveedoresFragment"
    }
}