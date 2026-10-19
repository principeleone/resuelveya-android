package com.resuelveya.android.features.catalogo.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.resuelveya.android.R
import com.resuelveya.android.databinding.FragmentServicioDetalleBinding

class ServicioDetalleFragment : Fragment() {

    private var _binding: FragmentServicioDetalleBinding? = null
    private val binding get() = _binding!!

    private var servicioId: Long = 0L
    private var servicioTitulo: String? = null
    private var servicioDescripcion: String? = null
    private var precioEstimado: Double = 0.0
    private var tiempoEstimado: String? = null
    private var tecnicoId: Long = 0L
    private var tecnicoNombre: String? = null
    private var tecnicoEspecialidad: String? = null
    private var tecnicoDistrito: String? = null
    private var tecnicoCalificacion: Double = 5.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            servicioId = it.getLong("servicioId", 0L)
            servicioTitulo = it.getString("servicioTitulo")
            servicioDescripcion = it.getString("servicioDescripcion")
            precioEstimado = it.getDouble("precioEstimado", 0.0)
            tiempoEstimado = it.getString("tiempoEstimado")
            tecnicoId = it.getLong("tecnicoId", 0L)
            tecnicoNombre = it.getString("tecnicoNombre")
            tecnicoEspecialidad = it.getString("tecnicoEspecialidad")
            tecnicoDistrito = it.getString("tecnicoDistrito")
            tecnicoCalificacion = it.getDouble("tecnicoCalificacion", 5.0)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentServicioDetalleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBackButton()
        renderData()
        setupSubmitButton()
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun renderData() {
        binding.tvNombreServicio.text = servicioTitulo?.ifBlank { null } ?: "Servicio Técnico"
        binding.tvTarifaEstimada.text = if (precioEstimado > 0) "S/ ${String.format("%.2f", precioEstimado)}" else "A coordinar"
        binding.tvTiempoEstimado.text = if (!tiempoEstimado.isNullOrBlank()) "· $tiempoEstimado" else ""

        val desc = servicioDescripcion?.ifBlank { null }
            ?: "Diagnóstico, inspección y solución técnica especializada garantizada por el profesional en tu domicilio."
        binding.tvDescripcionServicio.text = desc

        binding.tvTecnicoNombre.text = tecnicoNombre?.ifBlank { null } ?: "Técnico Profesional"
        val especialidad = tecnicoEspecialidad ?: "Especialista"
        val ubicacion = tecnicoDistrito ?: "Lima"
        binding.tvTecnicoEspecialidad.text = "$especialidad · $ubicacion"
        binding.tvTecnicoCalificacion.text = "${String.format("%.1f", tecnicoCalificacion)} · Verificado"
    }

    private fun setupSubmitButton() {
        binding.btnSolicitarServicio.setOnClickListener {
            val bundle = Bundle().apply {
                putLong("tecnicoId", if (tecnicoId > 0) tecnicoId else 1L)
                putString("tecnicoNombre", tecnicoNombre)
                putLong("servicioId", if (servicioId > 0) servicioId else 1L)
                putString("servicioTitulo", servicioTitulo)
                putDouble("precioEstimado", if (precioEstimado > 0) precioEstimado else 80.0)
                putString("distrito", tecnicoDistrito)
            }
            try {
                findNavController().navigate(R.id.action_servicioDetalleFragment_to_nuevaSolicitudFragment, bundle)
            } catch (e: Exception) {
                Log.e(TAG, "Error navegando a nuevaSolicitudFragment", e)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "ServicioDetalleFragment"
    }
}
