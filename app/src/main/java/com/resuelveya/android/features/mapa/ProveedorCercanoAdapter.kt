package com.resuelveya.android.features.mapa

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.maps.model.LatLng
import com.resuelveya.android.R
import com.resuelveya.android.databinding.ItemProveedorCercanoBinding
import com.resuelveya.android.features.catalogo.model.TecnicoPublico

data class ProveedorCercanoItem(
    val tecnico: TecnicoPublico,
    val latLng: LatLng,
    var distanciaMetros: Float = 0f,
    var distanciaTexto: String = "Cerca"
)

class ProveedorCercanoAdapter(
    private val onItemClick: (ProveedorCercanoItem) -> Unit
) : RecyclerView.Adapter<ProveedorCercanoAdapter.ViewHolder>() {

    private val items = mutableListOf<ProveedorCercanoItem>()
    private var selectedIndex: Int = -1

    fun updateData(newItems: List<ProveedorCercanoItem>) {
        items.clear()
        items.addAll(newItems)
        selectedIndex = if (items.isNotEmpty()) 0 else -1
        notifyDataSetChanged()
    }

    fun selectItem(item: ProveedorCercanoItem): Int {
        val index = items.indexOfFirst { it.tecnico.id == item.tecnico.id }
        if (index != -1 && index != selectedIndex) {
            val prev = selectedIndex
            selectedIndex = index
            notifyItemChanged(prev)
            notifyItemChanged(index)
        }
        return index
    }

    fun getSelectedItem(): ProveedorCercanoItem? {
        return if (selectedIndex in items.indices) items[selectedIndex] else items.firstOrNull()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProveedorCercanoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position == selectedIndex)
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(
        private val binding: ItemProveedorCercanoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ProveedorCercanoItem, isSelected: Boolean) {
            val context = itemView.context
            val tecnico = item.tecnico

            binding.tvNombre.text = tecnico.nombre
            val especialidad = tecnico.especialidadNombre ?: "Técnico Especialista"
            val zona = tecnico.distrito ?: tecnico.ciudad ?: "Lima"
            binding.tvEspecialidad.text = "$especialidad · $zona"

            val calif = tecnico.calificacionPromedio ?: 5.0
            binding.tvCalificacion.text = String.format("%.1f", calif)
            binding.tvDistancia.text = item.distanciaTexto

            if (!tecnico.fotoUrl.isNullOrBlank()) {
                Glide.with(context)
                    .load(tecnico.fotoUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(binding.ivAvatar)
            } else {
                binding.ivAvatar.setImageResource(R.drawable.ic_person)
            }

            // Resaltar visualmente si es el seleccionado
            if (isSelected) {
                binding.root.strokeColor = ContextCompat.getColor(context, R.color.primary)
                binding.root.strokeWidth = (2 * context.resources.displayMetrics.density).toInt()
            } else {
                binding.root.strokeColor = ContextCompat.getColor(context, R.color.border)
                binding.root.strokeWidth = (1 * context.resources.displayMetrics.density).toInt()
            }

            binding.root.setOnClickListener {
                val currentPos = bindingAdapterPosition
                if (currentPos != RecyclerView.NO_POSITION) {
                    val prev = selectedIndex
                    selectedIndex = currentPos
                    notifyItemChanged(prev)
                    notifyItemChanged(selectedIndex)
                    onItemClick(items[currentPos])
                }
            }
        }
    }
}
