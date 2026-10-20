package com.resuelveya.android.features.proveedor.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.resuelveya.android.R
import com.resuelveya.android.core.database.ContratoSyncManager
import com.resuelveya.android.databinding.FragmentSolicitudDetalleBinding
import com.resuelveya.android.features.catalogo.model.SolicitudResponse
import com.resuelveya.android.features.proveedor.data.ProveedorRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SolicitudDetalleFragment : Fragment() {

    private var _binding: FragmentSolicitudDetalleBinding? = null
    private val binding get() = _binding!!

    private lateinit var syncManager: ContratoSyncManager
    private val repository = ProveedorRepository()

    // Datos recibidos por argumentos de navegación
    private var solicitudId: Long = -1L
    private var clienteNombre: String = ""
    private var servicioTitulo: String = ""
    private var descripcion: String = ""
    private var fechaProgramada: String = ""
    private var direccionAtencion: String = ""
    private var precioAcordado: Double = 0.0
    private var estado: String = "ACEPTADA"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            solicitudId = it.getLong(ARG_SOLICITUD_ID, -1L)
            clienteNombre = it.getString(ARG_CLIENTE_NOMBRE, "") ?: ""
            servicioTitulo = it.getString(ARG_SERVICIO_TITULO, "") ?: ""
            descripcion = it.getString(ARG_DESCRIPCION, "") ?: ""
            fechaProgramada = it.getString(ARG_FECHA_PROGRAMADA, "") ?: ""
            precioAcordado = if (it.containsKey(ARG_PRECIO_ACORDADO)) {
                when (val value = it.get(ARG_PRECIO_ACORDADO)) {
                    is Double -> value
                    is Float -> value.toDouble()
                    is Number -> value.toDouble()
                    else -> 0.0
                }
            } else {
                0.0
            }
            estado = it.getString(ARG_ESTADO, "ACEPTADA") ?: "ACEPTADA"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSolicitudDetalleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        syncManager = ContratoSyncManager(requireContext())
        populateData()
        configureActionButton()

        if (solicitudId > 0) {
            syncManager.obtenerContratoPorId(solicitudId) { local ->
                if (_binding == null) return@obtenerContratoPorId
                local?.let {
                    estado = it.estado
                    clienteNombre = it.clienteNombre ?: clienteNombre
                    servicioTitulo = it.servicioTitulo ?: servicioTitulo
                    descripcion = it.descripcion ?: descripcion
                    fechaProgramada = it.fechaProgramada ?: fechaProgramada
                    direccionAtencion = it.direccionAtencion ?: direccionAtencion
                    precioAcordado = it.precioAcordado ?: precioAcordado
                    populateData()
                    configureActionButton()
                }
            }
        }
    }

    private fun populateData() {
        binding.tvCliente.text = clienteNombre.ifBlank { getString(R.string.detalle_cliente_default) }
        binding.tvServicio.text = servicioTitulo.ifBlank { getString(R.string.detalle_servicio_default) }
        binding.tvDescripcion.text = descripcion.ifBlank { getString(R.string.detalle_sin_descripcion) }
        binding.tvFecha.text = fechaProgramada.replace("T", " ").ifBlank { getString(R.string.detalle_fecha_por_coordinar) }

        if (direccionAtencion.isNotBlank()) {
            binding.tvDireccion.text = direccionAtencion
            binding.tvDireccion.visibility = View.VISIBLE
            binding.icDireccion.visibility = View.VISIBLE
        } else {
            binding.tvDireccion.visibility = View.GONE
            binding.icDireccion.visibility = View.GONE
        }

        if (precioAcordado > 0) {
            binding.tvPrecio.text = getString(R.string.detalle_precio_formato, precioAcordado)
            binding.tvPrecio.visibility = View.VISIBLE
            binding.icPrecio.visibility = View.VISIBLE
        } else {
            binding.tvPrecio.visibility = View.GONE
            binding.icPrecio.visibility = View.GONE
        }

        updateEstadoChip()
    }

    private fun updateEstadoChip() {
        binding.tvEstadoLabel.text = estado.replace("_", " ")

        when (estado.uppercase()) {
            "PENDIENTE" -> {
                binding.tvEstadoLabel.setBackgroundResource(R.drawable.bg_chip_pen)
                binding.tvEstadoLabel.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.chip_pen_tx)
                )
                binding.tvInfoMessage.text = getString(R.string.detalle_info_pendiente)
            }
            "ACEPTADA" -> {
                binding.tvEstadoLabel.setBackgroundResource(R.drawable.bg_chip_pro)
                binding.tvEstadoLabel.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.chip_pro_tx)
                )
                binding.tvInfoMessage.text = getString(R.string.detalle_info_aceptada)
            }
            "EN_PROCESO" -> {
                binding.tvEstadoLabel.setBackgroundResource(R.drawable.bg_chip_pen)
                binding.tvEstadoLabel.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.chip_pen_tx)
                )
                binding.tvInfoMessage.text = getString(R.string.detalle_info_en_proceso)
            }
            "FINALIZADA", "COMPLETADA" -> {
                binding.tvEstadoLabel.setBackgroundResource(R.drawable.bg_chip_ok)
                binding.tvEstadoLabel.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.chip_ok_tx)
                )
                binding.tvInfoMessage.text = getString(R.string.detalle_info_finalizada)
            }
            "RECHAZADA", "CANCELADA" -> {
                binding.tvEstadoLabel.setBackgroundResource(R.drawable.bg_chip_gr)
                binding.tvEstadoLabel.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.chip_gr_tx)
                )
                binding.tvInfoMessage.text = getString(R.string.detalle_info_rechazada)
            }
            else -> {
                binding.tvEstadoLabel.setBackgroundResource(R.drawable.bg_chip_gr)
                binding.tvEstadoLabel.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.chip_gr_tx)
                )
                binding.tvInfoMessage.text = estado
            }
        }
    }

    private fun configureActionButton() {
        when (estado.uppercase()) {
            "PENDIENTE" -> {
                binding.llAccionesPendiente.visibility = View.VISIBLE
                binding.btnAccion.visibility = View.GONE
                binding.btnAceptarDetalle.setOnClickListener { aceptarSolicitud() }
                binding.btnRechazarDetalle.setOnClickListener { rechazarSolicitud() }
            }
            "ACEPTADA" -> {
                binding.llAccionesPendiente.visibility = View.GONE
                binding.btnAccion.visibility = View.VISIBLE
                binding.btnAccion.isEnabled = true
                binding.btnAccion.text = getString(R.string.detalle_btn_iniciar)
                binding.btnAccion.setOnClickListener { iniciarTrabajo() }
            }
            "EN_PROCESO" -> {
                binding.llAccionesPendiente.visibility = View.GONE
                binding.btnAccion.visibility = View.VISIBLE
                binding.btnAccion.isEnabled = true
                binding.btnAccion.text = getString(R.string.detalle_btn_finalizar)
                binding.btnAccion.setOnClickListener { finalizarTrabajo() }
            }
            else -> {
                // Sin acción para FINALIZADA, RECHAZADA u otros estados
                binding.llAccionesPendiente.visibility = View.GONE
                binding.btnAccion.visibility = View.GONE
            }
        }
    }

    private fun aceptarSolicitud() {
        if (solicitudId <= 0) return
        binding.progress.visibility = View.VISIBLE
        binding.btnAceptarDetalle.isEnabled = false
        binding.btnRechazarDetalle.isEnabled = false

        repository.aceptarSolicitud(solicitudId, object : Callback<SolicitudResponse> {
            override fun onResponse(call: Call<SolicitudResponse>, response: Response<SolicitudResponse>) {
                if (_binding == null) return
                binding.progress.visibility = View.GONE
                binding.btnAceptarDetalle.isEnabled = true
                binding.btnRechazarDetalle.isEnabled = true

                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), R.string.detalle_msg_aceptada_ok, Toast.LENGTH_SHORT).show()
                    estado = "ACEPTADA"
                    updateEstadoChip()
                    configureActionButton()

                    response.body()?.let { updated ->
                        syncManager.guardarContratosLocales(listOf(updated), esProveedor = true)
                    }
                } else {
                    Toast.makeText(requireContext(), "Error al aceptar solicitud (${response.code()})", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SolicitudResponse>, t: Throwable) {
                if (_binding == null) return
                binding.progress.visibility = View.GONE
                binding.btnAceptarDetalle.isEnabled = true
                binding.btnRechazarDetalle.isEnabled = true
                Toast.makeText(requireContext(), "Error de conexión: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun rechazarSolicitud() {
        if (solicitudId <= 0) return
        binding.progress.visibility = View.VISIBLE
        binding.btnAceptarDetalle.isEnabled = false
        binding.btnRechazarDetalle.isEnabled = false

        repository.rechazarSolicitud(solicitudId, object : Callback<SolicitudResponse> {
            override fun onResponse(call: Call<SolicitudResponse>, response: Response<SolicitudResponse>) {
                if (_binding == null) return
                binding.progress.visibility = View.GONE
                binding.btnAceptarDetalle.isEnabled = true
                binding.btnRechazarDetalle.isEnabled = true

                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), R.string.detalle_msg_rechazada_ok, Toast.LENGTH_SHORT).show()
                    estado = "RECHAZADA"
                    updateEstadoChip()
                    configureActionButton()
                    findNavController().popBackStack()
                } else {
                    Toast.makeText(requireContext(), "Error al rechazar solicitud (${response.code()})", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SolicitudResponse>, t: Throwable) {
                if (_binding == null) return
                binding.progress.visibility = View.GONE
                binding.btnAceptarDetalle.isEnabled = true
                binding.btnRechazarDetalle.isEnabled = true
                Toast.makeText(requireContext(), "Error de conexión: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun iniciarTrabajo() {
        if (solicitudId <= 0) return
        binding.progress.visibility = View.VISIBLE
        binding.btnAccion.isEnabled = false

        syncManager.iniciarContratoLocal(
            solicitudId = solicitudId,
            onSuccess = { _ ->
                if (_binding == null) return@iniciarContratoLocal
                binding.progress.visibility = View.GONE
                binding.btnAccion.isEnabled = true

                val mensaje = if (syncManager.isOnline()) {
                    getString(R.string.detalle_msg_iniciado_ok)
                } else {
                    getString(R.string.detalle_msg_iniciado_offline)
                }
                Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()

                // Actualizar la UI al nuevo estado
                estado = "EN_PROCESO"
                updateEstadoChip()
                configureActionButton()
            },
            onError = { errorMessage ->
                if (_binding == null) return@iniciarContratoLocal
                binding.progress.visibility = View.GONE
                binding.btnAccion.isEnabled = true
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun finalizarTrabajo() {
        if (solicitudId <= 0) return
        binding.progress.visibility = View.VISIBLE
        binding.btnAccion.isEnabled = false

        syncManager.finalizarContratoLocal(
            solicitudId = solicitudId,
            onSuccess = { _ ->
                if (_binding == null) return@finalizarContratoLocal
                binding.progress.visibility = View.GONE
                binding.btnAccion.isEnabled = true

                val mensaje = if (syncManager.isOnline()) {
                    getString(R.string.detalle_msg_finalizado_ok)
                } else {
                    getString(R.string.detalle_msg_finalizado_offline)
                }
                Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()

                // Actualizar la UI al nuevo estado
                estado = "FINALIZADA"
                updateEstadoChip()
                configureActionButton()
            },
            onError = { errorMessage ->
                if (_binding == null) return@finalizarContratoLocal
                binding.progress.visibility = View.GONE
                binding.btnAccion.isEnabled = true
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_SOLICITUD_ID = "solicitudId"
        private const val ARG_CLIENTE_NOMBRE = "clienteNombre"
        private const val ARG_SERVICIO_TITULO = "servicioTitulo"
        private const val ARG_DESCRIPCION = "descripcion"
        private const val ARG_FECHA_PROGRAMADA = "fechaProgramada"
        private const val ARG_DIRECCION_ATENCION = "direccionAtencion"
        private const val ARG_PRECIO_ACORDADO = "precioAcordado"
        private const val ARG_ESTADO = "estado"
    }
}