package com.resuelveya.android.features.contrataciones.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.resuelveya.android.R
import com.resuelveya.android.databinding.FragmentMisContratacionesBinding
import com.resuelveya.android.features.catalogo.data.CatalogoRepository
import com.resuelveya.android.features.catalogo.model.SolicitudResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MisContratacionesFragment : Fragment() {

    private var _binding: FragmentMisContratacionesBinding? = null
    private val binding get() = _binding!!

    private val repository = CatalogoRepository()
    private lateinit var adapter: ContratoAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMisContratacionesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        loadSolicitudes()
    }

    private fun setupListeners() {
        binding.btnIrAInicio.setOnClickListener {
            findNavController().navigate(R.id.inicioClienteFragment)
        }
    }

    private fun setupRecyclerView() {
        adapter = ContratoAdapter { solicitud ->
            val bundle = Bundle().apply {
                putLong("solicitudId", solicitud.id ?: 0L)
                putString("servicioTitulo", solicitud.servicioTitulo)
                putString("tecnicoNombre", solicitud.tecnicoNombre)
                putString("estado", solicitud.estado)
                putString("fechaProgramada", solicitud.fechaProgramada)
            }
            try {
                findNavController().navigate(R.id.action_misContratacionesFragment_to_seguimientoFragment, bundle)
            } catch (e: Exception) {
                Log.e(TAG, "Error navegando a seguimiento", e)
            }
        }
        binding.rvContrataciones.layoutManager = LinearLayoutManager(requireContext())
        binding.rvContrataciones.adapter = adapter
    }

    private fun loadSolicitudes() {
        binding.progress.visibility = View.VISIBLE
        binding.rvContrataciones.visibility = View.GONE
        binding.ivIconoVacio.visibility = View.GONE
        binding.tvVacioContratos.visibility = View.GONE
        binding.btnIrAInicio.visibility = View.GONE

        repository.getMisSolicitudes(object : Callback<List<SolicitudResponse>> {
            override fun onResponse(
                call: Call<List<SolicitudResponse>>,
                response: Response<List<SolicitudResponse>>
            ) {
                if (_binding == null) return
                binding.progress.visibility = View.GONE

                if (response.isSuccessful) {
                    val lista = response.body().orEmpty()
                    if (lista.isNotEmpty()) {
                        binding.rvContrataciones.visibility = View.VISIBLE
                        binding.ivIconoVacio.visibility = View.GONE
                        binding.tvVacioContratos.visibility = View.GONE
                        binding.btnIrAInicio.visibility = View.GONE
                        adapter.updateData(lista)
                    } else {
                        showEmptyState()
                    }
                    return
                }

                showEmptyState()
            }

            override fun onFailure(call: Call<List<SolicitudResponse>>, t: Throwable) {
                if (_binding == null) return
                binding.progress.visibility = View.GONE
                showEmptyState()
                Log.e(TAG, "Error cargando solicitudes del cliente", t)
            }
        })
    }

    private fun showEmptyState() {
        binding.progress.visibility = View.GONE
        binding.rvContrataciones.visibility = View.GONE
        binding.ivIconoVacio.visibility = View.VISIBLE
        binding.tvVacioContratos.visibility = View.VISIBLE
        binding.btnIrAInicio.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "MisContratacionesFrag"
    }
}