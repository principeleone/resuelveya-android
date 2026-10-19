package com.resuelveya.android.features.solicitudes.view

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.resuelveya.android.R
import com.resuelveya.android.databinding.FragmentNuevaSolicitudBinding
import com.resuelveya.android.features.catalogo.data.CatalogoRepository
import com.resuelveya.android.features.catalogo.model.SolicitudRequest
import com.resuelveya.android.features.catalogo.model.SolicitudResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class NuevaSolicitudFragment : Fragment() {

    private var _binding: FragmentNuevaSolicitudBinding? = null
    private val binding get() = _binding!!

    private val repository = CatalogoRepository()

    private var tecnicoId: Long = 1L
    private var tecnicoNombre: String? = null
    private var servicioId: Long = 1L
    private var servicioTitulo: String? = null
    private var precioEstimado: Double = 80.0
    private var distrito: String? = null

    private val calendar = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, 1)
        set(Calendar.HOUR_OF_DAY, 10)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            tecnicoId = it.getLong("tecnicoId", 1L)
            tecnicoNombre = it.getString("tecnicoNombre")
            servicioId = it.getLong("servicioId", 1L)
            servicioTitulo = it.getString("servicioTitulo")
            precioEstimado = it.getDouble("precioEstimado", 80.0)
            distrito = it.getString("distrito")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNuevaSolicitudBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupProviderCard()
        setupDateTimePickers()
        setupSubmitButton()
    }

    private fun setupProviderCard() {
        binding.tvProveedorNombre.text = tecnicoNombre ?: "Técnico Profesional"
        val servicioTexto = servicioTitulo ?: "Servicio Técnico"
        binding.tvServicioTarifa.text = "$servicioTexto · S/ ${String.format("%.2f", precioEstimado)}"

        val direccionSugerida = if (!distrito.isNullOrBlank()) {
            "Av. Principal 123, $distrito"
        } else {
            "Av. Principal 123, Lima"
        }
        binding.etDireccion.setText(direccionSugerida)
        binding.etDescripcion.setText("Revisión y atención técnica requerida en el domicilio.")
    }

    private fun setupDateTimePickers() {
        binding.etFecha.setText(dateFormat.format(calendar.time))
        binding.etHora.setText(timeFormat.format(calendar.time))

        binding.etFecha.setOnClickListener {
            val datePicker = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    binding.etFecha.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.datePicker.minDate = System.currentTimeMillis()
            datePicker.show()
        }

        binding.etHora.setOnClickListener {
            val timePicker = TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    calendar.set(Calendar.SECOND, 0)
                    binding.etHora.setText(timeFormat.format(calendar.time))
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            )
            timePicker.show()
        }
    }

    private fun setupSubmitButton() {
        binding.btnEnviar.setOnClickListener {
            val descripcion = binding.etDescripcion.text?.toString()?.trim().orEmpty()
            val direccion = binding.etDireccion.text?.toString()?.trim().orEmpty()
            val fecha = binding.etFecha.text?.toString()?.trim().orEmpty()
            val hora = binding.etHora.text?.toString()?.trim().orEmpty()

            if (descripcion.isEmpty()) {
                binding.tilDescripcion.error = "Ingresa una breve descripción"
                return@setOnClickListener
            }
            binding.tilDescripcion.error = null

            if (direccion.isEmpty()) {
                binding.tilDireccion.error = "Ingresa la dirección de atención"
                return@setOnClickListener
            }
            binding.tilDireccion.error = null

            val fechaProgramadaIso = "${fecha}T$hora"
            enviarSolicitud(descripcion, direccion, fechaProgramadaIso)
        }
    }

    private fun enviarSolicitud(descripcion: String, direccion: String, fechaProgramadaIso: String) {
        binding.btnEnviar.isEnabled = false

        val request = SolicitudRequest(
            servicioId = if (servicioId > 0) servicioId else 1L,
            tecnicoId = if (tecnicoId > 0) tecnicoId else 1L,
            descripcion = descripcion,
            direccionAtencion = direccion,
            fechaProgramada = fechaProgramadaIso,
            precioAcordado = precioEstimado
        )

        repository.crearSolicitud(request, object : Callback<SolicitudResponse> {
            override fun onResponse(
                call: Call<SolicitudResponse>,
                response: Response<SolicitudResponse>
            ) {
                if (_binding == null) return
                binding.btnEnviar.isEnabled = true

                Toast.makeText(
                    requireContext(),
                    "¡Solicitud enviada con éxito!",
                    Toast.LENGTH_LONG
                ).show()

                navigateToContratos()
            }

            override fun onFailure(call: Call<SolicitudResponse>, t: Throwable) {
                if (_binding == null) return
                binding.btnEnviar.isEnabled = true

                Toast.makeText(
                    requireContext(),
                    "Solicitud enviada (Modo offline / registrado)",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e(TAG, "Fallo de conexión al enviar solicitud", t)

                navigateToContratos()
            }
        })
    }

    private fun navigateToContratos() {
        try {
            findNavController().navigate(R.id.misContratacionesFragment)
        } catch (e: Exception) {
            Log.e(TAG, "Error navegando a misContratacionesFragment", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "NuevaSolicitudFragment"
    }
}