package com.resuelveya.android.features.proveedor.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.resuelveya.android.databinding.ItemServicioProveedorBinding
import com.resuelveya.android.features.proveedor.controller.ServicioProveedor

class ServiciosAdapter(
    private var items: List<ServicioProveedor> = emptyList(),
    private val onEliminarClick: (ServicioProveedor) -> Unit,
    private val onEditarClick: (ServicioProveedor) -> Unit
) : RecyclerView.Adapter<ServiciosAdapter.ServicioViewHolder>() {

    fun updateList(newItems: List<ServicioProveedor>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServicioViewHolder {
        val binding = ItemServicioProveedorBinding.inflate(
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

    inner class ServicioViewHolder(
        private val binding: ItemServicioProveedorBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(servicio: ServicioProveedor) {
            binding.tvNombreServicio.text = servicio.titulo
            val categoria = servicio.categoriaNombre ?: "General"
            val precio = servicio.precioEstimado
            val duracion = servicio.tiempoEstimado
            binding.tvDetalle.text = "$categoria · S/ $precio · $duracion"

            binding.btnEliminar.setOnClickListener {
                onEliminarClick(servicio)
            }
            binding.btnEditar.setOnClickListener {
                onEditarClick(servicio)
            }
        }
    }
}
