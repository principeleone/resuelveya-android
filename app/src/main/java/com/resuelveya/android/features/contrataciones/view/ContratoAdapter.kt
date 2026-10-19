package com.resuelveya.android.features.contrataciones.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.resuelveya.android.databinding.ItemServicioTarifaBinding
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
        private val binding: ItemServicioTarifaBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(solicitud: SolicitudResponse) {
            binding.tvNombreServicio.text = solicitud.servicioTitulo ?: "Servicio solicitado"
            val tecnico = solicitud.tecnicoNombre ?: "Técnico asignado"
            val estado = solicitud.estado ?: "PENDIENTE"
            binding.tvDuracion.text = "$tecnico · Estado: $estado"
            binding.tvTarifa.text = "S/ ${String.format("%.2f", solicitud.precioAcordado ?: 0.0)}"

            binding.root.setOnClickListener {
                onContratoClick(solicitud)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContratoViewHolder {
        val binding = ItemServicioTarifaBinding.inflate(
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
