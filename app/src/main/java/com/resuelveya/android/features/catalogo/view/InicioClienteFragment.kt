package com.resuelveya.android.features.catalogo.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.resuelveya.android.R
import com.resuelveya.android.core.session.SessionManager
import com.resuelveya.android.databinding.FragmentInicioClienteBinding
import com.resuelveya.android.features.catalogo.data.CatalogoRepository
import com.resuelveya.android.features.catalogo.model.Categoria
import com.resuelveya.android.features.catalogo.view.adapter.CategoriaAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InicioClienteFragment : Fragment() {

    private var _binding: FragmentInicioClienteBinding? = null
    private val binding get() = _binding!!

    private val repository = CatalogoRepository()
    private lateinit var adapter: CategoriaAdapter

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
        setupRecyclerView()
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

    private fun setupRecyclerView() {
        adapter = CategoriaAdapter { categoria ->
            navigateToTecnicos(categoria.id, categoria.nombre)
        }
        binding.rvCategorias.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvCategorias.adapter = adapter
    }

    private fun setupSearch() {
        binding.etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etBuscar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.etBuscar.text?.toString()?.trim().orEmpty()
                if (query.isNotEmpty()) {
                    navigateToTecnicos(null, null, query)
                }
                true
            } else {
                false
            }
        }
    }

    private fun setupNearbyCard() {
        binding.cardCerca.setOnClickListener {
            navigateToTecnicos(null, null, null)
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
                    val categorias = response.body().orEmpty()
                    if (categorias.isEmpty()) {
                        binding.tvVacio.visibility = View.VISIBLE
                        binding.rvCategorias.visibility = View.GONE
                    } else {
                        binding.tvVacio.visibility = View.GONE
                        binding.rvCategorias.visibility = View.VISIBLE
                        adapter.updateData(categorias)
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

    private fun showErrorState(message: String) {
        binding.tvVacio.text = message
        binding.tvVacio.visibility = View.VISIBLE
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToTecnicos(categoriaId: Long?, categoriaNombre: String?, searchQuery: String? = null) {
        val bundle = bundleOf(
            "categoriaId" to (categoriaId ?: -1L),
            "categoriaNombre" to (categoriaNombre ?: ""),
            "searchQuery" to (searchQuery ?: "")
        )
        try {
            findNavController().navigate(R.id.action_inicioClienteFragment_to_tecnicosFragment, bundle)
        } catch (e: Exception) {
            Log.e(TAG, "Error navegando a tecnicosFragment", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "InicioClienteFragment"
    }
}