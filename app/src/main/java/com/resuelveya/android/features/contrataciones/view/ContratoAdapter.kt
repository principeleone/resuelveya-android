package com.resuelveya.android.features.contrataciones.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.resuelveya.android.R
import com.resuelveya.android.databinding.ItemContratacionClienteBinding
import com.resuelveya.android.features.catalogo.model.SolicitudResponse

class ContratoAdapter(
    private val onContratoClick: (SolicitudResponse) -> Unit
) : RecyclerView.Adapter<ContratoAdapter.ContratoViewHolder>() {

    private var items: List<SolicitudResponse> = emptyList()

    fun updateData(newItems: List<SolicitudResponse>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ContratoViewHolder(
        private val binding: ItemContratacionClienteBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(solicitud: SolicitudResponse) {
            val contexto = binding.root.context
            binding.tvServicio.text = solicitud.servicioTitulo ?: "Servicio contratado"
            val tecnico = solicitud.tecnicoNombre ?: "Técnico asignado"
            val fecha = solicitud.fechaProgramada ?: "Fecha programada"
            binding.tvDetalle.text = "$tecnico · $fecha"

            val estado = solicitud.estado ?: "PENDIENTE"
            binding.tvEstado.text = estado.replace("_", " ")

            when (estado.uppercase()) {
                "PENDIENTE" -> {
                    binding.tvEstado.setBackgroundResource(R.drawable.bg_chip_pen)
                    binding.tvEstado.setTextColor(ContextCompat.getColor(contexto, R.color.chip_pen_tx))
                }
                "EN_PROCESO", "ACEPTADA" -> {
                    binding.tvEstado.setBackgroundResource(R.drawable.bg_chip_pro)
                    binding.tvEstado.setTextColor(ContextCompat.getColor(contexto, R.color.chip_pro_tx))
                }
                "FINALIZADA", "COMPLETADA" -> {
                    binding.tvEstado.setBackgroundResource(R.drawable.bg_chip_ok)
                    binding.tvEstado.setTextColor(ContextCompat.getColor(contexto, R.color.chip_ok_tx))
                }
                "RECHAZADA", "CANCELADA" -> {
                    binding.tvEstado.setBackgroundResource(R.drawable.bg_chip_er)
                    binding.tvEstado.setTextColor(ContextCompat.getColor(contexto, R.color.chip_er_tx))
                }
                else -> {
                    binding.tvEstado.setBackgroundResource(R.drawable.bg_chip_pen)
                    binding.tvEstado.setTextColor(ContextCompat.getColor(contexto, R.color.chip_pen_tx))
                }
            }

            binding.root.setOnClickListener {
                onContratoClick(solicitud)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContratoViewHolder {
        val binding = ItemContratacionClienteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ContratoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContratoViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
