package com.resuelveya.android.features.proveedor.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.resuelveya.android.R
import com.resuelveya.android.databinding.FragmentInicioProveedorBinding
import com.resuelveya.android.features.catalogo.model.SolicitudResponse
import com.resuelveya.android.features.perfil.data.PerfilRepository
import com.resuelveya.android.features.perfil.model.PerfilResponse
import com.resuelveya.android.features.proveedor.adapter.SolicitudRecibidaAdapter
import com.resuelveya.android.features.proveedor.data.ProveedorRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InicioProveedorFragment : Fragment() {

    private var _binding: FragmentInicioProveedorBinding? = null
    private val binding get() = _binding!!

    private val perfilRepository = PerfilRepository()
    private val proveedorRepository = ProveedorRepository()
    private lateinit var adapter: SolicitudRecibidaAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInicioProveedorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadDashboardData()
    }

    private fun setupRecyclerView() {
        adapter = SolicitudRecibidaAdapter(
            onItemClick = { _ ->
                navigateToSolicitudes()
            }
        )
        binding.rvUltimas.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUltimas.adapter = adapter

        binding.tvUltimasLabel.setOnClickListener {
            navigateToSolicitudes()
        }
    }

    private fun navigateToSolicitudes() {
        requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
            R.id.bottomNavigationProveedor
        )?.selectedItemId = R.id.solicitudesRecibidasFragment
    }

    private fun loadDashboardData() {
        binding.progress.visibility = View.VISIBLE

        loadPerfilInfo()
        loadSolicitudesData()
    }

    private fun loadPerfilInfo() {
        perfilRepository.getMiPerfil(object : Callback<PerfilResponse> {
            override fun onResponse(call: Call<PerfilResponse>, response: Response<PerfilResponse>) {
                if (_binding == null) return

                if (response.isSuccessful && response.body() != null) {
                    val perfil = response.body()!!
                    val nombre = perfil.nombre?.ifBlank { null }
                        ?: FirebaseAuth.getInstance().currentUser?.displayName?.ifBlank { null }
                        ?: "Técnico"
                    binding.tvSaludo.text = "Hola, $nombre"

                    val calificacion = perfil.calificacionPromedio ?: 5.0
                    binding.tvCalificacionNumero.text = String.format("%.1f", calificacion)
                } else {
                    fallbackPerfilInfo()
                }
            }

            override fun onFailure(call: Call<PerfilResponse>, t: Throwable) {
                if (_binding == null) return
                Log.e(TAG, "Error al cargar datos del perfil técnico", t)
                fallbackPerfilInfo()
            }
        })
    }

    private fun fallbackPerfilInfo() {
        val fbUser = FirebaseAuth.getInstance().currentUser
        val nombre = fbUser?.displayName?.ifBlank { null }
            ?: fbUser?.email?.substringBefore("@")
            ?: "Técnico"
        binding.tvSaludo.text = "Hola, $nombre"
        binding.tvCalificacionNumero.text = "5.0"
    }

    private fun loadSolicitudesData() {
        proveedorRepository.getSolicitudesTecnico(null, object : Callback<List<SolicitudResponse>> {
            override fun onResponse(
                call: Call<List<SolicitudResponse>>,
                response: Response<List<SolicitudResponse>>
            ) {
                if (_binding == null) return
                binding.progress.visibility = View.GONE

                if (response.isSuccessful) {
                    val lista = response.body().orEmpty()
                    computeStatsAndPopulate(lista)
                } else {
                    computeStatsAndPopulate(emptyList())
                }
            }

            override fun onFailure(call: Call<List<SolicitudResponse>>, t: Throwable) {
                if (_binding == null) return
                binding.progress.visibility = View.GONE
                Log.e(TAG, "Error cargando solicitudes del técnico", t)
                computeStatsAndPopulate(emptyList())
            }
        })
    }

    private fun computeStatsAndPopulate(solicitudes: List<SolicitudResponse>) {
        val nuevas = solicitudes.count { it.estado.equals("PENDIENTE", ignoreCase = true) }
        val enProceso = solicitudes.count {
            it.estado.equals("EN_PROCESO", ignoreCase = true) ||
            it.estado.equals("ACEPTADA", ignoreCase = true)
        }
        val finalizadas = solicitudes.count {
            it.estado.equals("FINALIZADA", ignoreCase = true) ||
            it.estado.equals("COMPLETADA", ignoreCase = true)
        }

        binding.tvNuevasNumero.text = nuevas.toString()
        binding.tvProcesoNumero.text = enProceso.toString()
        binding.tvFinalizadosNumero.text = finalizadas.toString()

        val ultimas = solicitudes.take(5)
        adapter.updateData(ultimas)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "InicioProveedorFrag"
    }
}