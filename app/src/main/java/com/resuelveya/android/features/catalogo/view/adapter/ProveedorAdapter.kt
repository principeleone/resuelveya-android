package com.resuelveya.android.features.catalogo.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.resuelveya.android.databinding.ItemProveedorBinding
import com.resuelveya.android.features.catalogo.model.TecnicoPublico

class ProveedorAdapter(
    private val onProveedorClick: (TecnicoPublico) -> Unit
) : RecyclerView.Adapter<ProveedorAdapter.ProveedorViewHolder>() {

    private var items: List<TecnicoPublico> = emptyList()

    fun updateData(newItems: List<TecnicoPublico>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ProveedorViewHolder(
        private val binding: ItemProveedorBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(tecnico: TecnicoPublico) {
            binding.tvNombre.text = tecnico.nombre
            val especialidad = tecnico.especialidadNombre ?: "Técnico Profesional"
            val ubicacion = tecnico.distrito ?: tecnico.ciudad ?: "Lima"
            binding.tvEspecialidad.text = "$especialidad · $ubicacion"

            val rating = tecnico.calificacionPromedio ?: 5.0
            val resenias = tecnico.totalResenias ?: 0
            binding.tvCalificacion.text = "${String.format("%.1f", rating)} · $resenias reseñas"

            binding.root.setOnClickListener {
                onProveedorClick(tecnico)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProveedorViewHolder {
        val binding = ItemProveedorBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProveedorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProveedorViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
