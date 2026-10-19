package com.resuelveya.android.features.catalogo.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.resuelveya.android.R
import com.resuelveya.android.databinding.DialogSolicitarServicioBinding
import com.resuelveya.android.databinding.FragmentProveedorDetalleBinding
import com.resuelveya.android.features.catalogo.data.CatalogoRepository
import com.resuelveya.android.features.catalogo.model.PageResponse
import com.resuelveya.android.features.catalogo.model.ServicioPublico
import com.resuelveya.android.features.catalogo.model.ServicioTarifaItem
import com.resuelveya.android.features.catalogo.model.SolicitudRequest
import com.resuelveya.android.features.catalogo.model.SolicitudResponse
import com.resuelveya.android.features.catalogo.model.TecnicoDetalleResponse
import com.resuelveya.android.features.catalogo.view.adapter.ServicioTarifaAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ProveedorDetalleFragment : Fragment() {

    private var _binding: FragmentProveedorDetalleBinding? = null
    private val binding get() = _binding!!

    private val repository = CatalogoRepository()
    private lateinit var adapter: ServicioTarifaAdapter

    private var proveedorId: Long = -1L
    private var nombreTecnico: String? = null
    private var especialidadTecnico: String? = null
    private var distritoTecnico: String? = null
    private var calificacionTecnico: Double = 5.0
    private var totalReseniasTecnico: Int = 0
    private var presentacionTecnico: String? = null

    private var serviciosDisponibles: List<ServicioTarifaItem> = emptyList()
    private var servicioSeleccionado: ServicioTarifaItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            proveedorId = it.getLong("proveedorId", -1L)
            nombreTecnico = it.getString("nombre")
            especialidadTecnico = it.getString("especialidad")
            distritoTecnico = it.getString("distrito")
            calificacionTecnico = it.getDouble("calificacion", 5.0)
            totalReseniasTecnico = it.getInt("totalResenias", 0)
            presentacionTecnico = it.getString("presentacion")
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

        setupBackButton()
        renderInitialData()
        setupRecyclerView()
        setupListeners()
        loadDetalle()
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun renderInitialData() {
        if (!nombreTecnico.isNullOrBlank()) {
            binding.tvNombre.text = nombreTecnico
        }
        val especialidad = especialidadTecnico ?: "Técnico Especialista"
        val ubicacion = distritoTecnico ?: "Lima"
        binding.tvEspecialidad.text = "$especialidad · $ubicacion"

        binding.tvCalificacion.text = "${String.format("%.1f", calificacionTecnico)} · $totalReseniasTecnico reseñas"

        val presentacion = presentacionTecnico?.ifBlank { null }
            ?: "Profesional certificado y verificado en ResuelveYa con garantía de calidad y atención puntual."
        binding.tvAcerca.text = presentacion
    }

    private fun setupRecyclerView() {
        adapter = ServicioTarifaAdapter { servicio ->
            servicioSeleccionado = servicio
            navigateToNuevaSolicitud(servicio)
        }
        binding.rvServicios.layoutManager = LinearLayoutManager(requireContext())
        binding.rvServicios.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnSolicitar.setOnClickListener {
            val servicio = servicioSeleccionado
                ?: serviciosDisponibles.firstOrNull()
                ?: ServicioTarifaItem(
                    id = 1L,
                    titulo = "Servicio de $especialidadTecnico",
                    precioEstimado = 80.0,
                    tiempoEstimado = "2 horas"
                )
            navigateToNuevaSolicitud(servicio)
        }
    }

    private fun loadDetalle() {
        if (proveedorId <= 0) {
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
                        bindDetalleCompleto(detalle)
                    }
                    return
                }

                // Si da 401/403 o no carga servicios, buscar por el nombre o especialidad como fallback
                loadServiciosFallback()
            }

            override fun onFailure(call: Call<TecnicoDetalleResponse>, t: Throwable) {
                if (_binding == null) return
                binding.progress.visibility = View.GONE
                loadServiciosFallback()
                Log.e(TAG, "Fallo al obtener detalle completo, activando fallback", t)
            }
        })
    }

    private fun bindDetalleCompleto(detalle: TecnicoDetalleResponse) {
        if (!detalle.nombre.isNullOrBlank()) {
            binding.tvNombre.text = detalle.nombre
            nombreTecnico = detalle.nombre
        }
        val especialidad = detalle.especialidadNombre ?: especialidadTecnico ?: "Técnico Profesional"
        val ubicacion = detalle.distrito ?: detalle.ciudad ?: distritoTecnico ?: "Lima"
        binding.tvEspecialidad.text = "$especialidad · $ubicacion"

        val rating = detalle.calificacionPromedio ?: calificacionTecnico
        val reseniasCount = detalle.resenias?.size ?: totalReseniasTecnico
        binding.tvCalificacion.text = "${String.format("%.1f", rating)} · $reseniasCount reseñas"

        val presentacion = detalle.presentacion?.ifBlank { null }
            ?: presentacionTecnico
            ?: "Técnico certificado con amplia experiencia y garantía en cada servicio."
        binding.tvAcerca.text = presentacion

        val servicios = detalle.servicios.orEmpty()
        if (servicios.isNotEmpty()) {
            serviciosDisponibles = servicios
            adapter.updateData(servicios)
        } else {
            loadServiciosFallback()
        }
    }

    private fun loadServiciosFallback() {
        val busqueda = especialidadTecnico ?: nombreTecnico.orEmpty()
        repository.buscarServicios(busqueda, object : Callback<PageResponse<ServicioPublico>> {
            override fun onResponse(
                call: Call<PageResponse<ServicioPublico>>,
                response: Response<PageResponse<ServicioPublico>>
            ) {
                if (_binding == null) return
                val lista = response.body()?.content.orEmpty()
                val serviciosMapeados = if (lista.isNotEmpty()) {
                    lista.map { sp ->
                        ServicioTarifaItem(
                            id = sp.id,
                            titulo = sp.titulo,
                            descripcion = sp.descripcion,
                            precioEstimado = sp.precioEstimado,
                            tiempoEstimado = sp.tiempoEstimado,
                            categoriaId = sp.categoriaId,
                            categoriaNombre = sp.categoriaNombre
                        )
                    }
                } else {
                    listOf(
                        ServicioTarifaItem(
                            id = 1L,
                            titulo = "Atención técnica especializada",
                            descripcion = "Diagnóstico integral y solución en el domicilio.",
                            precioEstimado = 75.0,
                            tiempoEstimado = "1 - 2 horas"
                        )
                    )
                }
                serviciosDisponibles = serviciosMapeados
                adapter.updateData(serviciosMapeados)
            }

            override fun onFailure(call: Call<PageResponse<ServicioPublico>>, t: Throwable) {
                if (_binding == null) return
                val defaultList = listOf(
                    ServicioTarifaItem(
                        id = 1L,
                        titulo = "Atención técnica especializada",
                        descripcion = "Diagnóstico integral y solución en el domicilio.",
                        precioEstimado = 75.0,
                        tiempoEstimado = "1 - 2 horas"
                    )
                )
                serviciosDisponibles = defaultList
                adapter.updateData(defaultList)
            }
        })
    }

    private fun navigateToNuevaSolicitud(servicio: ServicioTarifaItem) {
        val bundle = Bundle().apply {
            putLong("tecnicoId", if (proveedorId > 0) proveedorId else 1L)
            putString("tecnicoNombre", nombreTecnico)
            putLong("servicioId", servicio.id ?: 1L)
            putString("servicioTitulo", servicio.titulo)
            putDouble("precioEstimado", servicio.precioEstimado ?: 80.0)
            putString("distrito", distritoTecnico)
        }
        try {
            findNavController().navigate(R.id.nuevaSolicitudFragment, bundle)
        } catch (e: Exception) {
            Log.e(TAG, "Error navegando a nuevaSolicitudFragment", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "ProveedorDetalleFragment"
    }
}