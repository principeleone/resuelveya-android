package com.resuelveya.android.features.tecnicos.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.resuelveya.databinding.ItemServicioBinding
import com.resuelveya.android.features.tecnicos.model.Servicio

class ServicioAdapter : ListAdapter<Servicio, ServicioAdapter.ServicioViewHolder>(Diff) {

    object Diff : DiffUtil.ItemCallback<Servicio>() {
        override fun areItemsTheSame(a: Servicio, b: Servicio) = a.id == b.id
        override fun areContentsTheSame(a: Servicio, b: Servicio) = a == b
    }

    inner class ServicioViewHolder(
        private val binding: ItemServicioBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(s: Servicio) {
            binding.tvTituloServicio.text = s.titulo
            binding.tvTiempo.text = s.tiempoEstimado?.let { "$it aprox." }.orEmpty()
            binding.tvPrecio.text = formatearPrecio(s.precioEstimado)
        }

        private fun formatearPrecio(precio: Double?): String {
            if (precio == null) return "Consultar"
            return if (precio % 1.0 == 0.0) "S/ ${precio.toInt()}"
            else "S/ ${"%.2f".format(precio)}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServicioViewHolder {
        val binding = ItemServicioBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ServicioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ServicioViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}