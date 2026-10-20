package com.resuelveya.android.features.catalogo.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.resuelveya.android.R
import com.resuelveya.android.databinding.ItemCategoriaBinding
import com.resuelveya.android.features.catalogo.model.Categoria

class CategoriaAdapter(
    private val onCategoriaClick: (Categoria) -> Unit
) : RecyclerView.Adapter<CategoriaAdapter.CategoriaViewHolder>() {

    private var originalList: List<Categoria> = emptyList()
    private var displayedList: List<Categoria> = emptyList()

    fun updateData(newList: List<Categoria>) {
        originalList = newList
        displayedList = newList
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        val cleanQuery = query.trim().lowercase()
        displayedList = if (cleanQuery.isEmpty()) {
            originalList
        } else {
            originalList.filter { categoria ->
                categoria.nombre.lowercase().contains(cleanQuery) ||
                        (categoria.descripcion?.lowercase()?.contains(cleanQuery) == true)
            }
        }
        notifyDataSetChanged()
    }

    inner class CategoriaViewHolder(
        private val binding: ItemCategoriaBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(categoria: Categoria) {
            binding.tvNombre.text = categoria.nombre
            binding.ivIcono.setImageResource(getIconForCategory(categoria.nombre))
            binding.root.setOnClickListener {
                onCategoriaClick(categoria)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriaViewHolder {
        val binding = ItemCategoriaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoriaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoriaViewHolder, position: Int) {
        holder.bind(displayedList[position])
    }

    override fun getItemCount(): Int = displayedList.size

    private fun getIconForCategory(name: String): Int {
        return when (name.lowercase()) {
            "electricidad" -> R.drawable.ic_cat_electricidad
            "gasfitería", "gasfiteria" -> R.drawable.ic_cat_gasfiteria
            "pintura" -> R.drawable.ic_cat_pintura
            "carpintería", "carpinteria" -> R.drawable.ic_cat_carpinteria
            "refrigeración", "refrigeracion" -> R.drawable.ic_cat_refrigeracion
            "computación", "computacion" -> R.drawable.ic_cat_computacion
            else -> R.drawable.ic_build
        }
    }
}
