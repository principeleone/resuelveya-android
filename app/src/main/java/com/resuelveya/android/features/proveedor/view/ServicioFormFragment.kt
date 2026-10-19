package com.resuelveya.android.features.proveedor.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.resuelveya.android.core.network.ErrorResponse
import com.resuelveya.android.databinding.FragmentServicioFormBinding
import com.resuelveya.android.features.proveedor.controller.CategoriaResponse
import com.resuelveya.android.features.proveedor.controller.ServicioProveedor
import com.resuelveya.android.features.proveedor.controller.ServicioRequest
import com.resuelveya.android.features.proveedor.data.ProveedorRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ServicioFormFragment : Fragment() {

    private var _binding: FragmentServicioFormBinding? = null
    private val binding get() = _binding!!
    private val repository = ProveedorRepository()

    private var categorias: List<CategoriaResponse> = emptyList()
    private var selectedCategoriaId: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentServicioFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        cargarCategorias()
    }

    private fun setupListeners() {
        binding.btnGuardarServicio.setOnClickListener {
            guardarServicio()
        }

        binding.btnCancelarServicio.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun cargarCategorias() {
        repository.getCategorias(object : Callback<List<CategoriaResponse>> {
            override fun onResponse(
                call: Call<List<CategoriaResponse>>,
                response: Response<List<CategoriaResponse>>
            ) {
                if (_binding == null) return
                if (response.isSuccessful) {
                    categorias = response.body().orEmpty()
                    val nombresCategorias = categorias.map { it.nombre }
                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        nombresCategorias
                    )
                    binding.actEspecialidad.setAdapter(adapter)

                    binding.actEspecialidad.setOnItemClickListener { _, _, position, _ ->
                        selectedCategoriaId = categorias[position].id
                    }
                }
            }

            override fun onFailure(call: Call<List<CategoriaResponse>>, t: Throwable) {
                // Silently fallback si falla categorías
            }
        })
    }

    private fun guardarServicio() {
        val titulo = binding.etNombreServicio.text?.toString()?.trim().orEmpty()
        val descripcion = binding.etDescripcionServicio.text?.toString()?.trim().orEmpty()
        val tarifaStr = binding.etTarifa.text?.toString()?.trim().orEmpty()
        val duracionStr = binding.etDuracion.text?.toString()?.trim().orEmpty()

        if (titulo.isEmpty()) {
            binding.tilNombreServicio.error = "Ingresa el nombre del servicio"
            return
        }
        binding.tilNombreServicio.error = null

        val categoriaId = selectedCategoriaId ?: categorias.firstOrNull()?.id
        if (categoriaId == null) {
            binding.tilEspecialidad.error = "Selecciona una categoría"
            return
        }
        binding.tilEspecialidad.error = null

        if (descripcion.isEmpty()) {
            binding.tilDescripcionServicio.error = "Ingresa una descripción"
            return
        }
        binding.tilDescripcionServicio.error = null

        val tarifa = tarifaStr.toDoubleOrNull()
        if (tarifa == null || tarifa <= 0.0) {
            binding.tilTarifa.error = "Ingresa un precio válido"
            return
        }
        binding.tilTarifa.error = null

        if (duracionStr.isEmpty()) {
            binding.tilDuracion.error = "Ingresa el tiempo estimado"
            return
        }
        binding.tilDuracion.error = null

        val tiempoEstimado = if (duracionStr.contains("hora", ignoreCase = true)) {
            duracionStr
        } else {
            "$duracionStr horas"
        }

        val request = ServicioRequest(
            titulo = titulo,
            descripcion = descripcion,
            precioEstimado = tarifa,
            tiempoEstimado = tiempoEstimado,
            categoriaId = categoriaId,
            activo = true
        )

        binding.btnGuardarServicio.isEnabled = false

        repository.crearServicio(request, object : Callback<ServicioProveedor> {
            override fun onResponse(
                call: Call<ServicioProveedor>,
                response: Response<ServicioProveedor>
            ) {
                if (_binding == null) return
                binding.btnGuardarServicio.isEnabled = true

                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Servicio creado exitosamente", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                    return
                }

                val errorBody = response.errorBody()?.string()
                val errorMsg = parseErrorMessage(errorBody) ?: "Error al guardar servicio (${response.code()})"
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
            }

            override fun onFailure(call: Call<ServicioProveedor>, t: Throwable) {
                if (_binding == null) return
                binding.btnGuardarServicio.isEnabled = true
                Toast.makeText(requireContext(), "Error de conexión: ${t.localizedMessage}", Toast.LENGTH_LONG).show()
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