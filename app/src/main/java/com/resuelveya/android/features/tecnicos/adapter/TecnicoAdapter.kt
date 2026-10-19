package com.resuelveya.android.features.tecnicos.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.resuelveya.android.R
import com.resuelveya.android.databinding.ItemTecnicoBinding
import com.resuelveya.android.features.tecnicos.model.Tecnico

class TecnicoAdapter(
    private val onClick: (Tecnico) -> Unit = {}
) : ListAdapter<Tecnico, TecnicoAdapter.TecnicoViewHolder>(Diff) {

    object Diff : DiffUtil.ItemCallback<Tecnico>() {
        override fun areItemsTheSame(a: Tecnico, b: Tecnico) = a.id == b.id
        override fun areContentsTheSame(a: Tecnico, b: Tecnico) = a == b
    }

    inner class TecnicoViewHolder(
        private val binding: ItemTecnicoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(t: Tecnico) {
            binding.tvNombre.text = t.nombre
            binding.tvDetalle.text = listOfNotNull(t.especialidadNombre, t.distrito)
                .joinToString(" · ")
            binding.tvRating.text =
                "★ ${"%.1f".format(t.calificacionPromedio ?: 0.0)} · ${t.totalResenias ?: 0} reseñas"

            Glide.with(binding.root)
                .load(t.fotoUrl)
                .circleCrop()
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(binding.ivFoto)

            binding.root.setOnClickListener { onClick(t) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TecnicoViewHolder {
        val binding = ItemTecnicoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TecnicoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TecnicoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}