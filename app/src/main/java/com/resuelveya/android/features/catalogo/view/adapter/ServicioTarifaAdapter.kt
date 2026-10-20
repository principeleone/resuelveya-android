package com.resuelveya.android.features.catalogo.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.resuelveya.android.databinding.ItemServicioTarifaBinding
import com.resuelveya.android.features.catalogo.model.ServicioTarifaItem

class ServicioTarifaAdapter(
    private val onServicioClick: ((ServicioTarifaItem) -> Unit)? = null
) : RecyclerView.Adapter<ServicioTarifaAdapter.ServicioTarifaViewHolder>() {

    private var items: List<ServicioTarifaItem> = emptyList()

    fun updateData(newItems: List<ServicioTarifaItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ServicioTarifaViewHolder(
        private val binding: ItemServicioTarifaBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(servicio: ServicioTarifaItem) {
            binding.tvNombreServicio.text = servicio.titulo
            binding.tvDuracion.text = servicio.tiempoEstimado ?: servicio.descripcion ?: "Duración a coordinar"
            binding.tvTarifa.text = "S/ ${String.format("%.2f", servicio.precioEstimado)}"

            binding.root.setOnClickListener {
                onServicioClick?.invoke(servicio)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServicioTarifaViewHolder {
        val binding = ItemServicioTarifaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ServicioTarifaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ServicioTarifaViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
