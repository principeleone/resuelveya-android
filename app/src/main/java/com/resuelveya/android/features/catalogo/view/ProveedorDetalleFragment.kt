package com.resuelveya.android.features.catalogo.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.resuelveya.android.databinding.FragmentProveedorDetalleBinding
import com.resuelveya.android.features.catalogo.data.CatalogoRepository
import com.resuelveya.android.features.catalogo.model.TecnicoDetalleResponse
import com.resuelveya.android.features.catalogo.view.adapter.ServicioTarifaAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProveedorDetalleFragment : Fragment() {

    private var _binding: FragmentProveedorDetalleBinding? = null
    private val binding get() = _binding!!

    private val repository = CatalogoRepository()
    private lateinit var adapter: ServicioTarifaAdapter
    private var proveedorId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            proveedorId = it.getLong("proveedorId", -1L)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProveedorDetalleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        loadDetalle()
    }

    private fun setupRecyclerView() {
        adapter = ServicioTarifaAdapter { servicio ->
            Toast.makeText(
                requireContext(),
                "Servicio seleccionado: ${servicio.titulo}",
                Toast.LENGTH_SHORT
            ).show()
        }
        binding.rvServicios.layoutManager = LinearLayoutManager(requireContext())
        binding.rvServicios.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnSolicitar.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Próximamente: Solicitar servicio con este técnico",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun loadDetalle() {
        if (proveedorId <= 0) {
            Toast.makeText(requireContext(), "ID de técnico no válido", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progress.visibility = View.VISIBLE

        repository.getTecnicoCompleto(proveedorId, object : Callback<TecnicoDetalleResponse> {
            override fun onResponse(
                call: Call<TecnicoDetalleResponse>,
                response: Response<TecnicoDetalleResponse>
            ) {
                if (_binding == null) return
                binding.progress.visibility = View.GONE

                if (response.isSuccessful) {
                    val detalle = response.body()
                    if (detalle != null) {
                        bindDetalle(detalle)
                    }
                    return
                }

                Toast.makeText(
                    requireContext(),
                    "No se pudo cargar el perfil del técnico (${response.code()})",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onFailure(call: Call<TecnicoDetalleResponse>, t: Throwable) {
                if (_binding == null) return
                binding.progress.visibility = View.GONE
                Toast.makeText(
                    requireContext(),
                    "Error de conexión al cargar perfil",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e(TAG, "Error al cargar detalle de técnico", t)
            }
        })
    }

    private fun bindDetalle(detalle: TecnicoDetalleResponse) {
        binding.tvNombre.text = detalle.nombre
        val especialidad = detalle.especialidadNombre ?: "Técnico Profesional"
        val ubicacion = detalle.distrito ?: detalle.ciudad ?: "Lima"
        binding.tvEspecialidad.text = "$especialidad · $ubicacion"

        val rating = detalle.calificacionPromedio ?: 5.0
        val reseniasCount = detalle.resenias.size
        binding.tvCalificacion.text = "${String.format("%.1f", rating)} · $reseniasCount reseñas"

        val presentacion = detalle.presentacion?.ifBlank { null }
            ?: "Profesional certificado y verificado en ResuelveYa con experiencia garantizada."
        binding.tvAcerca.text = presentacion

        adapter.updateData(detalle.servicios)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "ProveedorDetalleFragment"
    }
}