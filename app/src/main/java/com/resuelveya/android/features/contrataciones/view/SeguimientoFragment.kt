package com.resuelveya.android.features.contrataciones.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.resuelveya.android.R
import com.resuelveya.android.databinding.FragmentSeguimientoBinding
import com.resuelveya.android.databinding.ItemEstadoSeguimientoBinding

data class HitoSeguimiento(
    val titulo: String,
    val fecha: String,
    val completado: Boolean,
    val actual: Boolean
)

class SeguimientoFragment : Fragment() {

    private var _binding: FragmentSeguimientoBinding? = null
    private val binding get() = _binding!!

    private var solicitudId: Long = 0L
    private var servicioTitulo: String? = null
    private var tecnicoNombre: String? = null
    private var estado: String? = null
    private var fechaProgramada: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            solicitudId = it.getLong("solicitudId", 0L)
            servicioTitulo = it.getString("servicioTitulo")
            tecnicoNombre = it.getString("tecnicoNombre")
            estado = it.getString("estado")
            fechaProgramada = it.getString("fechaProgramada")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSeguimientoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cargarDatosSeguimiento()
    }

    private fun cargarDatosSeguimiento() {
        renderResumen()
        setupTimeline()
        setupListeners()

        if (solicitudId > 0) {
            java.util.concurrent.Executors.newSingleThreadExecutor().execute {
                try {
                    val entity = com.resuelveya.android.core.database.AppDatabase.getInstance(requireContext())
                        .contratoDao().getContratoById(solicitudId)
                    if (entity != null) {
                        servicioTitulo = entity.servicioTitulo ?: servicioTitulo
                        tecnicoNombre = entity.tecnicoNombre ?: tecnicoNombre
                        estado = entity.estado
                        fechaProgramada = entity.fechaProgramada ?: fechaProgramada

                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                            if (_binding != null) {
                                renderResumen()
                                setupTimeline()
                            }
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("SeguimientoFragment", "Error consultando contrato local", e)
                }
            }
        }
    }

    private fun renderResumen() {
        binding.tvServicio.text = servicioTitulo?.ifBlank { null } ?: "Servicio contratado"
        binding.tvProveedor.text = tecnicoNombre?.ifBlank { null } ?: "Técnico asignado"

        val fecha = fechaProgramada?.replace("T", " ") ?: "Por coordinar"
        binding.tvFecha.text = "Fecha: $fecha"

        val estadoActual = estado ?: "PENDIENTE"
        binding.tvEstado.text = estadoActual

        when (estadoActual.uppercase()) {
            "PENDIENTE" -> {
                binding.tvEstado.setBackgroundResource(R.drawable.bg_chip_pen)
                binding.tvEstado.setTextColor(ContextCompat.getColor(requireContext(), R.color.chip_pen_tx))
            }
            "EN_PROCESO", "ACEPTADA" -> {
                binding.tvEstado.setBackgroundResource(R.drawable.bg_chip_pro)
                binding.tvEstado.setTextColor(ContextCompat.getColor(requireContext(), R.color.chip_pro_tx))
            }
            "FINALIZADA", "COMPLETADA" -> {
                binding.tvEstado.setBackgroundResource(R.drawable.bg_chip_ok)
                binding.tvEstado.setTextColor(ContextCompat.getColor(requireContext(), R.color.chip_ok_tx))
            }
            else -> {
                binding.tvEstado.setBackgroundResource(R.drawable.bg_chip_pen)
                binding.tvEstado.setTextColor(ContextCompat.getColor(requireContext(), R.color.chip_pen_tx))
            }
        }
    }

    private fun setupTimeline() {
        val estadoActual = estado?.uppercase() ?: "PENDIENTE"
        val hitos = listOf(
            HitoSeguimiento(
                titulo = "Solicitud enviada",
                fecha = fechaProgramada?.replace("T", " ") ?: "Registrada",
                completado = true,
                actual = estadoActual == "PENDIENTE"
            ),
            HitoSeguimiento(
                titulo = "Técnico asignado y confirmado",
                fecha = if (estadoActual != "PENDIENTE") "Confirmado" else "En espera de confirmación",
                completado = estadoActual in listOf("ACEPTADA", "EN_PROCESO", "FINALIZADA", "COMPLETADA"),
                actual = estadoActual == "ACEPTADA"
            ),
            HitoSeguimiento(
                titulo = "Servicio en proceso",
                fecha = if (estadoActual in listOf("EN_PROCESO", "FINALIZADA", "COMPLETADA")) "En atención" else "Pendiente",
                completado = estadoActual in listOf("EN_PROCESO", "FINALIZADA", "COMPLETADA"),
                actual = estadoActual == "EN_PROCESO"
            ),
            HitoSeguimiento(
                titulo = "Servicio completado",
                fecha = if (estadoActual in listOf("FINALIZADA", "COMPLETADA")) "Finalizado con éxito" else "Por concluir",
                completado = estadoActual in listOf("FINALIZADA", "COMPLETADA"),
                actual = estadoActual in listOf("FINALIZADA", "COMPLETADA")
            )
        )

        binding.rvEstados.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEstados.adapter = TimelineAdapter(hitos)
    }

    private fun setupListeners() {
        binding.btnChat.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Iniciando chat con ${tecnicoNombre ?: "el técnico"}...",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class TimelineAdapter(private val lista: List<HitoSeguimiento>) :
        RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder>() {

        inner class TimelineViewHolder(val itemBinding: ItemEstadoSeguimientoBinding) :
            RecyclerView.ViewHolder(itemBinding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineViewHolder {
            val itemBinding = ItemEstadoSeguimientoBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return TimelineViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: TimelineViewHolder, position: Int) {
            val hito = lista[position]
            holder.itemBinding.tvTitulo.text = hito.titulo
            holder.itemBinding.tvFecha.text = hito.fecha

            if (hito.completado) {
                holder.itemBinding.ivDot.setBackgroundResource(R.drawable.bg_dot_done)
                holder.itemBinding.vLinea.setBackgroundResource(R.color.primary)
            } else if (hito.actual) {
                holder.itemBinding.ivDot.setBackgroundResource(R.drawable.bg_dot_actual)
                holder.itemBinding.vLinea.setBackgroundResource(R.color.border)
            } else {
                holder.itemBinding.ivDot.setBackgroundResource(R.drawable.bg_dot_pendiente)
                holder.itemBinding.vLinea.setBackgroundResource(R.color.border)
            }

            if (position == lista.size - 1) {
                holder.itemBinding.vLinea.visibility = View.GONE
            } else {
                holder.itemBinding.vLinea.visibility = View.VISIBLE
            }
        }

        override fun getItemCount(): Int = lista.size
    }
}