package com.resuelveya.android.features.tecnicos

import android.R
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import com.resuelveya.android.features.tecnicos.adapter.ServicioAdapter
import com.resuelveya.databinding.FragmentTecnicoDetalleBinding
import com.resuelveya.android.features.tecnicos.model.LoginRequest
import com.resuelveya.android.features.tecnicos.model.TecnicoCompleto
import com.resuelveya.android.features.tecnicos.network.RetrofitClient

class TecnicoDetalleFragment : Fragment() {

    private var _binding: FragmentTecnicoDetalleBinding? = null
    private val binding get() = _binding!!

    private var tecnicoId: Long = 0L
    private var tecnicoNombre: String = ""
    private val adapter = ServicioAdapter()

    companion object {
        fun newInstance(id: Long, nombre: String) = TecnicoDetalleFragment().apply {
            arguments = bundleOf(
                "tecnicoId" to id,
                "tecnicoNombre" to nombre
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tecnicoId = arguments?.getLong("tecnicoId") ?: 0L
        tecnicoNombre = arguments?.getString("tecnicoNombre").orEmpty()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTecnicoDetalleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvNombre.text = tecnicoNombre

        binding.tvTitulo.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.rvServicios.layoutManager = LinearLayoutManager(requireContext())
        binding.rvServicios.adapter = adapter

        binding.btnSolicitar.setOnClickListener {
            Toast.makeText(requireContext(), "Próximamente", Toast.LENGTH_SHORT).show()
        }

        viewLifecycleOwner.lifecycleScope.launch { cargarDetalle() }
    }

    // TEMPORAL: login de prueba. Se reemplaza por la pantalla de login real.
    private suspend fun asegurarSesion() {
        if (RetrofitClient.token != null) return
        val res = RetrofitClient.api.login(
            LoginRequest("carlos@ejemplo.com", "Password123")
        )
        RetrofitClient.token = res.token
    }

    private suspend fun cargarDetalle() {
        try {
            asegurarSesion()
            val tecnico = RetrofitClient.api.getTecnicoCompleto(tecnicoId)
            if (_binding == null) return
            mostrar(tecnico)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("TecnicoDetalle", "Error al cargar el detalle", e)
            if (_binding != null) {
                Toast.makeText(
                    requireContext(),
                    "No se pudo cargar el detalle",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun mostrar(t: TecnicoCompleto) {
        binding.tvNombre.text = t.nombre
        binding.tvEspecialidad.text = t.especialidadNombre.orEmpty()

        val totalResenias = t.resenias.orEmpty().size
        binding.tvRating.text =
            "${"%.1f".format(t.calificacionPromedio ?: 0.0)} · $totalResenias reseñas"

        val acerca = listOfNotNull(
            t.aniosExperiencia?.let { "$it años de experiencia." },
            t.presentacion
        ).joinToString(" ")
        binding.tvAcerca.text = acerca.ifBlank { "Sin descripción" }

        Glide.with(binding.root)
            .load(t.fotoUrl)
            .circleCrop()
            .placeholder(R.drawable.ic_menu_myplaces)
            .error(R.drawable.ic_menu_myplaces)
            .into(binding.ivFoto)

        val servicios = t.servicios.orEmpty().filter { it.activo != false }
        adapter.submitList(servicios)
        binding.tvSinServicios.visibility =
            if (servicios.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}