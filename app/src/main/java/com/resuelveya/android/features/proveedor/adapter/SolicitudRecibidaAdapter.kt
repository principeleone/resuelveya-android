package com.resuelveya.android.features.proveedor.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.resuelveya.android.R
import com.resuelveya.android.databinding.ItemSolicitudRecibidaBinding
import com.resuelveya.android.features.catalogo.model.SolicitudResponse

class SolicitudRecibidaAdapter(
    private var solicitudes: List<SolicitudResponse> = emptyList(),
    private val onItemClick: ((SolicitudResponse) -> Unit)? = null,
    private val onAceptarClick: ((SolicitudResponse) -> Unit)? = null,
    private val onRechazarClick: ((SolicitudResponse) -> Unit)? = null,
    private val onIniciarClick: ((SolicitudResponse) -> Unit)? = null,
    private val onFinalizarClick: ((SolicitudResponse) -> Unit)? = null
) : RecyclerView.Adapter<SolicitudRecibidaAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemSolicitudRecibidaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(solicitud: SolicitudResponse) {
            binding.tvCliente.text = solicitud.clienteNombre?.ifBlank { null } ?: "Cliente"
            binding.tvServicio.text = solicitud.servicioTitulo?.ifBlank { null }
                ?: solicitud.descripcion?.ifBlank { null }
                ?: "Servicio solicitado"

            val fecha = solicitud.fechaProgramada?.replace("T", " ") ?: "Fecha por coordinar"
            binding.tvFechaHora.text = fecha

            val direccion = solicitud.direccionAtencion?.ifBlank { null }
            if (!direccion.isNullOrBlank()) {
                binding.tvDireccionAtencion.visibility = View.VISIBLE
                binding.tvDireccionAtencion.text = direccion
            } else {
                binding.tvDireccionAtencion.visibility = View.GONE
            }

            val estado = solicitud.estado ?: "PENDIENTE"
            binding.tvEstado.text = estado.replace("_", " ")

            when (estado.uppercase()) {
                "PENDIENTE" -> {
                    binding.tvEstado.setBackgroundResource(R.drawable.bg_chip_pen)
                    binding.tvEstado.setTextColor(
                        ContextCompat.getColor(binding.root.context, R.color.chip_pen_tx)
                    )
                    if (onAceptarClick != null || onRechazarClick != null) {
                        binding.llAcciones.visibility = View.VISIBLE
                        binding.btnRechazar.visibility = View.VISIBLE
                        binding.btnRechazar.text = binding.root.context.getString(R.string.proveedor_btn_rechazar)
                        binding.btnAceptar.visibility = View.VISIBLE
                        binding.btnAceptar.text = binding.root.context.getString(R.string.proveedor_btn_aceptar)
                        binding.btnAceptar.setOnClickListener { onAceptarClick?.invoke(solicitud) }
                        binding.btnRechazar.setOnClickListener { onRechazarClick?.invoke(solicitud) }
                    } else {
                        binding.llAcciones.visibility = View.GONE
                    }
                }
                "ACEPTADA" -> {
                    binding.tvEstado.setBackgroundResource(R.drawable.bg_chip_pro)
                    binding.tvEstado.setTextColor(
                        ContextCompat.getColor(binding.root.context, R.color.chip_pro_tx)
                    )
                    if (onIniciarClick != null) {
                        binding.llAcciones.visibility = View.VISIBLE
                        binding.btnRechazar.visibility = View.GONE
                        binding.btnAceptar.visibility = View.VISIBLE
                        binding.btnAceptar.text = "INICIAR CONTRATO"
                        binding.btnAceptar.setOnClickListener { onIniciarClick.invoke(solicitud) }
                    } else {
                        binding.llAcciones.visibility = View.GONE
                    }
                }
                "EN_PROCESO" -> {
                    binding.tvEstado.setBackgroundResource(R.drawable.bg_chip_pro)
                    binding.tvEstado.setTextColor(
                        ContextCompat.getColor(binding.root.context, R.color.chip_pro_tx)
                    )
                    if (onFinalizarClick != null) {
                        binding.llAcciones.visibility = View.VISIBLE
                        binding.btnRechazar.visibility = View.GONE
                        binding.btnAceptar.visibility = View.VISIBLE
                        binding.btnAceptar.text = "FINALIZAR CONTRATO"
                        binding.btnAceptar.setOnClickListener { onFinalizarClick.invoke(solicitud) }
                    } else {
                        binding.llAcciones.visibility = View.GONE
                    }
                }
                "FINALIZADA", "COMPLETADA" -> {
                    binding.tvEstado.setBackgroundResource(R.drawable.bg_chip_ok)
                    binding.tvEstado.setTextColor(
                        ContextCompat.getColor(binding.root.context, R.color.chip_ok_tx)
                    )
                    binding.llAcciones.visibility = View.GONE
                }
                else -> {
                    binding.tvEstado.setBackgroundResource(R.drawable.bg_chip_pen)
                    binding.tvEstado.setTextColor(
                        ContextCompat.getColor(binding.root.context, R.color.chip_pen_tx)
                    )
                    binding.llAcciones.visibility = View.GONE
                }
            }

            binding.root.setOnClickListener {
                onItemClick?.invoke(solicitud)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSolicitudRecibidaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(solicitudes[position])
    }

    override fun getItemCount(): Int = solicitudes.size

    fun updateData(newSolicitudes: List<SolicitudResponse>) {
        solicitudes = newSolicitudes
        notifyDataSetChanged()
    }
}
