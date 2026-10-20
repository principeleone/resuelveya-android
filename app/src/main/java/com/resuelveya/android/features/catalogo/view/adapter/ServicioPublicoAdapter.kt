package com.resuelveya.android.features.catalogo.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.resuelveya.android.databinding.ItemServicioTarifaBinding
import com.resuelveya.android.features.catalogo.model.ServicioPublico

class ServicioPublicoAdapter(
    private val onServicioClick: (ServicioPublico) -> Unit
) : RecyclerView.Adapter<ServicioPublicoAdapter.ServicioViewHolder>() {

    private var items: List<ServicioPublico> = emptyList()

    fun updateData(newItems: List<ServicioPublico>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ServicioViewHolder(
        private val binding: ItemServicioTarifaBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(servicio: ServicioPublico) {
            binding.tvNombreServicio.text = servicio.titulo
            val tecnicoInfo = servicio.tecnicoNombre ?: "Técnico verificado"
            val categoriaInfo = servicio.categoriaNombre?.let { " · $it" } ?: ""
            binding.tvDuracion.text = "$tecnicoInfo$categoriaInfo"
            binding.tvTarifa.text = "S/ ${String.format("%.2f", servicio.precioEstimado)}"

            binding.root.setOnClickListener {
                onServicioClick(servicio)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServicioViewHolder {
        val binding = ItemServicioTarifaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ServicioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ServicioViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
