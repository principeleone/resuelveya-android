package com.resuelveya.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.resuelveya.R
import com.resuelveya.databinding.ItemEspecialidadBinding
import com.resuelveya.model.Especialidad

class EspecialidadAdapter(
    private val onClick: (Especialidad) -> Unit
) : RecyclerView.Adapter<EspecialidadAdapter.ViewHolder>() {

    private var items: List<Especialidad> = emptyList()

    fun submitList(nuevaLista: List<Especialidad>) {
        items = nuevaLista
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemEspecialidadBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEspecialidadBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val especialidad = items[position]
        holder.binding.tvNombre.text = especialidad.nombre
        holder.binding.ivIcon.setImageResource(iconoPara(especialidad.nombre))
        holder.binding.root.setOnClickListener { onClick(especialidad) }
    }

    override fun getItemCount() = items.size

    // Asigna el ícono local según el nombre que llega de la API
    private fun iconoPara(nombre: String): Int = when (nombre.lowercase()) {
        "electricidad" -> R.drawable.ic_gasfiteria
        "gasfiteria", "gasfiteria" -> R.drawable.ic_gasfiteria
        "cerrajeria", "cerrajeria" -> R.drawable.ic_cerrajeria
        "jardineria" -> R.drawable.ic_jardineria
        "pintura" -> R.drawable.ic_pintura
        "carpintería", "carpinteria" -> R.drawable.ic_carpinteria
        "refrigeracion", "refrigeracion" -> R.drawable.ic_carpinteria
        "computacion", "computacion" -> R.drawable.ic_computacion
        else -> R.drawable.ic_otros // ícono por defecto
    }
}