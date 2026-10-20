package com.resuelveya.android.features.proveedor.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.resuelveya.android.R
import com.resuelveya.android.core.network.ErrorResponse
import com.resuelveya.android.databinding.FragmentMisServiciosBinding
import com.resuelveya.android.features.proveedor.controller.ServicioProveedor
import com.resuelveya.android.features.proveedor.data.ProveedorRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MisServiciosFragment : Fragment() {

    private var _binding: FragmentMisServiciosBinding? = null
    private val binding get() = _binding!!
    private val repository = ProveedorRepository()
    private lateinit var adapter: ServiciosAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMisServiciosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        cargarServicios()
    }

    private fun setupRecyclerView() {
        adapter = ServiciosAdapter(
            onEliminarClick = { servicio ->
                confirmarEliminacion(servicio)
            },
            onEditarClick = { servicio ->
                val bundle = androidx.core.os.bundleOf(
                    "servicioId" to (servicio.id ?: -1L),
                    "titulo" to (servicio.titulo ?: ""),
                    "descripcion" to (servicio.descripcion ?: ""),
                    "precioEstimado" to (servicio.precioEstimado?.toFloat() ?: 0f),
                    "tiempoEstimado" to (servicio.tiempoEstimado ?: ""),
                    "categoriaId" to (servicio.categoriaId ?: -1L),
                    "categoriaNombre" to (servicio.categoriaNombre ?: "")
                )
                findNavController().navigate(R.id.action_misServiciosFragment_to_servicioFormFragment, bundle)
            }
        )
        binding.rvServicios.layoutManager = LinearLayoutManager(requireContext())
        binding.rvServicios.adapter = adapter
    }

    private fun setupListeners() {
        binding.fabAgregar.setOnClickListener {
            findNavController().navigate(R.id.action_misServiciosFragment_to_servicioFormFragment)
        }
    }

    private fun cargarServicios() {
        binding.progress.visibility = View.VISIBLE
        binding.tvVacio.visibility = View.GONE

        repository.getMisServicios(object : Callback<List<ServicioProveedor>> {
            override fun onResponse(
                call: Call<List<ServicioProveedor>>,
                response: Response<List<ServicioProveedor>>
            ) {
                if (_binding == null) return
                binding.progress.visibility = View.GONE

                if (response.isSuccessful) {
                    val servicios = response.body().orEmpty()
                    if (servicios.isEmpty()) {
                        binding.tvVacio.visibility = View.VISIBLE
                        binding.rvServicios.visibility = View.GONE
                    } else {
                        binding.tvVacio.visibility = View.GONE
                        binding.rvServicios.visibility = View.VISIBLE
                        adapter.updateList(servicios)
                    }
                    return
                }

                val errorBody = response.errorBody()?.string()
                val errorMsg = parseErrorMessage(errorBody) ?: "Error al obtener servicios (${response.code()})"
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
                binding.tvVacio.visibility = View.VISIBLE
            }

            override fun onFailure(call: Call<List<ServicioProveedor>>, t: Throwable) {
                if (_binding == null) return
                binding.progress.visibility = View.GONE
                binding.tvVacio.visibility = View.VISIBLE
                val errorMsg = "Error de conexión: ${t.localizedMessage ?: "No se pudo contactar al servidor"}"
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun confirmarEliminacion(servicio: ServicioProveedor) {
        val id = servicio.id ?: return
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Servicio")
            .setMessage("¿Estás seguro de que deseas eliminar '${servicio.titulo}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                ejecutarEliminacion(id)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun ejecutarEliminacion(id: Long) {
        binding.progress.visibility = View.VISIBLE
        repository.eliminarServicio(id, object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (_binding == null) return
                binding.progress.visibility = View.GONE
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Servicio eliminado", Toast.LENGTH_SHORT).show()
                    cargarServicios()
                } else {
                    Toast.makeText(requireContext(), "Error al eliminar servicio (${response.code()})", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                if (_binding == null) return
                binding.progress.visibility = View.GONE
                Toast.makeText(requireContext(), "Error de conexión al eliminar", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun parseErrorMessage(errorJson: String?): String? {
        if (errorJson.isNullOrBlank()) return null
        return try {
            val errorResponse = Gson().fromJson(errorJson, ErrorResponse::class.java)
            errorResponse.message ?: errorResponse.error
        } catch (_: Exception) {
            errorJson
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}