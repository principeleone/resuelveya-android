package com.resuelveya.android.features.catalogo.view

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.resuelveya.android.R
import com.resuelveya.android.databinding.FragmentInicioClienteBinding
import com.resuelveya.android.features.catalogo.data.CatalogoRepository
import com.resuelveya.android.features.catalogo.model.Categoria
import com.resuelveya.android.features.catalogo.model.PageResponse
import com.resuelveya.android.features.catalogo.model.ServicioPublico
import com.resuelveya.android.features.catalogo.view.adapter.CategoriaAdapter
import com.resuelveya.android.features.catalogo.view.adapter.ServicioPublicoAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InicioClienteFragment : Fragment() {

    private var _binding: FragmentInicioClienteBinding? = null
    private val binding get() = _binding!!

    private val repository = CatalogoRepository()
    private lateinit var categoriaAdapter: CategoriaAdapter
    private lateinit var servicioAdapter: ServicioPublicoAdapter

    private var cachedCategorias: List<Categoria> = emptyList()
    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInicioClienteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupGreeting()
        setupAdapters()
        setupSearch()
        setupNearbyCard()
        loadCategorias()
    }

    private fun setupGreeting() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val displayName = currentUser?.displayName?.ifBlank { null }
            ?: currentUser?.email?.substringBefore("@")
            ?: "Cliente"
        binding.tvSaludo.text = "Hola, $displayName 👋"
    }

    private fun setupAdapters() {
        categoriaAdapter = CategoriaAdapter { categoria ->
            navigateToProveedores(categoria.id, categoria.nombre)
        }

        servicioAdapter = ServicioPublicoAdapter { servicio ->
            val tecnicoId = servicio.tecnicoId
            if (tecnicoId != null && tecnicoId > 0) {
                navigateToProveedorDetalle(
                    tecnicoId = tecnicoId,
                    nombre = servicio.tecnicoNombre,
                    especialidad = servicio.categoriaNombre,
                    distrito = servicio.tecnicoDistrito ?: servicio.tecnicoCiudad,
                    calificacion = servicio.tecnicoCalificacionPromedio ?: 5.0
                )
            } else {
                Toast.makeText(requireContext(), "Técnico no disponible actualmente", Toast.LENGTH_SHORT).show()
            }
        }

        showCategoriasView(cachedCategorias)
    }

    private fun showCategoriasView(categorias: List<Categoria>) {
        binding.tvCategorias.text = getString(R.string.catalogo_categorias)
        binding.cardCerca.visibility = View.VISIBLE
        binding.rvCategorias.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvCategorias.adapter = categoriaAdapter
        categoriaAdapter.updateData(categorias)

        if (categorias.isEmpty()) {
            binding.tvVacio.visibility = View.VISIBLE
            binding.tvVacio.text = getString(R.string.catalogo_vacio)
        } else {
            binding.tvVacio.visibility = View.GONE
        }
    }

    private fun showServiciosView(servicios: List<ServicioPublico>) {
        binding.tvCategorias.text = getString(R.string.catalogo_servicios_encontrados)
        binding.cardCerca.visibility = View.GONE
        binding.rvCategorias.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCategorias.adapter = servicioAdapter
        servicioAdapter.updateData(servicios)

        if (servicios.isEmpty()) {
            binding.tvVacio.visibility = View.VISIBLE
            binding.tvVacio.text = getString(R.string.catalogo_sin_servicios)
        } else {
            binding.tvVacio.visibility = View.GONE
        }
    }

    private fun setupSearch() {
        binding.etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchRunnable?.let { searchHandler.removeCallbacks(it) }
                val query = s?.toString()?.trim().orEmpty()

                if (query.isEmpty()) {
                    showCategoriasView(cachedCategorias)
                    return
                }

                searchRunnable = Runnable {
                    buscarServicios(query)
                }
                searchHandler.postDelayed(searchRunnable!!, 350)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etBuscar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.etBuscar.text?.toString()?.trim().orEmpty()
                if (query.isNotEmpty()) {
                    searchRunnable?.let { searchHandler.removeCallbacks(it) }
                    buscarServicios(query)
                }
                true
            } else {
                false
            }
        }
    }

    private fun setupNearbyCard() {
        binding.cardCerca.setOnClickListener {
            navigateToProveedores(-1L, "Todos los Técnicos")
        }
    }

    private fun loadCategorias() {
        binding.progress.visibility = View.VISIBLE
        binding.tvVacio.visibility = View.GONE

        repository.getCategorias(object : Callback<List<Categoria>> {
            override fun onResponse(
                call: Call<List<Categoria>>,
                response: Response<List<Categoria>>
            ) {
                if (_binding == null) return
                binding.progress.visibility = View.GONE

                if (response.isSuccessful) {
                    cachedCategorias = response.body().orEmpty()
                    if (binding.etBuscar.text.isNullOrBlank()) {
                        showCategoriasView(cachedCategorias)
                    }
                    return
                }

                showErrorState("Error al obtener categorías (${response.code()})")
            }

            override fun onFailure(call: Call<List<Categoria>>, t: Throwable) {
                if (_binding == null) return
                binding.progress.visibility = View.GONE
                showErrorState("No se pudo conectar con el servidor")
                Log.e(TAG, "Error de red al cargar categorías", t)
            }
        })
    }

    private fun buscarServicios(query: String) {
        binding.progress.visibility = View.VISIBLE
        binding.tvVacio.visibility = View.GONE

        repository.buscarServicios(query, object : Callback<PageResponse<ServicioPublico>> {
            override fun onResponse(
                call: Call<PageResponse<ServicioPublico>>,
                response: Response<PageResponse<ServicioPublico>>
            ) {
                if (_binding == null) return
                binding.progress.visibility = View.GONE

                if (response.isSuccessful) {
                    val lista = response.body()?.content.orEmpty()
                    showServiciosView(lista)
                    return
                }

                showServiciosView(emptyList())
            }

            override fun onFailure(call: Call<PageResponse<ServicioPublico>>, t: Throwable) {
                if (_binding == null) return
                binding.progress.visibility = View.GONE
                showServiciosView(emptyList())
                Log.e(TAG, "Error al buscar servicios", t)
            }
        })
    }

    private fun showErrorState(message: String) {
        binding.tvVacio.text = message
        binding.tvVacio.visibility = View.VISIBLE
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToProveedores(categoriaId: Long, categoriaNombre: String) {
        val bundle = Bundle().apply {
            putLong("categoriaId", categoriaId)
            putString("categoriaNombre", categoriaNombre)
        }
        try {
            findNavController().navigate(R.id.proveedoresFragment, bundle)
        } catch (e: Exception) {
            Log.e(TAG, "Error navegando a proveedoresFragment", e)
        }
    }

    private fun navigateToProveedorDetalle(
        tecnicoId: Long,
        nombre: String? = null,
        especialidad: String? = null,
        distrito: String? = null,
        calificacion: Double = 5.0
    ) {
        val bundle = Bundle().apply {
            putLong("proveedorId", tecnicoId)
            putString("nombre", nombre)
            putString("especialidad", especialidad)
            putString("distrito", distrito)
            putDouble("calificacion", calificacion)
        }
        try {
            findNavController().navigate(R.id.action_inicioClienteFragment_to_proveedorDetalleFragment, bundle)
        } catch (e: Exception) {
            Log.e(TAG, "Error navegando a proveedorDetalleFragment", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchRunnable?.let { searchHandler.removeCallbacks(it) }
        _binding = null
    }

    companion object {
        private const val TAG = "InicioClienteFragment"
    }
}