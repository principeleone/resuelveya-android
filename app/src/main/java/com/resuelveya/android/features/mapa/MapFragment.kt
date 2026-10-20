package com.resuelveya.android.features.mapa

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.resuelveya.android.R
import com.resuelveya.android.databinding.FragmentMapBinding
import com.resuelveya.android.features.catalogo.data.CatalogoRepository
import com.resuelveya.android.features.catalogo.model.PageResponse
import com.resuelveya.android.features.catalogo.model.TecnicoPublico
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private var googleMap: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val repository = CatalogoRepository()

    private lateinit var adapter: ProveedorCercanoAdapter
    private var userLocation: LatLng = DEFAULT_LOCATION
    private var allTecnicos: List<TecnicoPublico> = emptyList()
    private val activeMarkers = mutableListOf<Marker>()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineGranted || coarseGranted) {
            enableMyLocation()
        } else {
            Toast.makeText(
                requireContext(),
                "Permiso de ubicación denegado. Mostrando zona por defecto.",
                Toast.LENGTH_SHORT
            ).show()
            centerCameraOn(userLocation, 13.5f)
            loadTecnicos()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        setupRecyclerView()
        setupListeners()
        setupChips()
        setupSearch()
        initMap()
    }

    private fun initMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isMapToolbarEnabled = false
        googleMap?.uiSettings?.isMyLocationButtonEnabled = false

        googleMap?.setOnMarkerClickListener { marker ->
            val item = marker.tag as? ProveedorCercanoItem
            if (item != null) {
                val index = adapter.selectItem(item)
                if (index != -1) {
                    binding.rvCercanos.smoothScrollToPosition(index)
                }
                marker.showInfoWindow()
                centerCameraOn(item.latLng, 15.5f)
                true
            } else {
                false
            }
        }

        googleMap?.setOnInfoWindowClickListener { marker ->
            val item = marker.tag as? ProveedorCercanoItem
            if (item != null) {
                navigateToDetalle(item.tecnico)
            }
        }

        checkLocationPermissions()
    }

    private fun setupRecyclerView() {
        adapter = ProveedorCercanoAdapter { item ->
            centerCameraOn(item.latLng, 16f)
            findMarkerForItem(item)?.showInfoWindow()
        }
        binding.rvCercanos.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.rvCercanos.adapter = adapter
    }

    private fun setupListeners() {
        binding.fabMiUbicacion.setOnClickListener {
            centerCameraOn(userLocation, 15.5f)
        }

        binding.btnVerPerfil.setOnClickListener {
            val selected = adapter.getSelectedItem()
            if (selected != null) {
                navigateToDetalle(selected.tecnico)
            } else {
                Toast.makeText(requireContext(), "Selecciona un técnico del mapa o lista", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupChips() {
        binding.chipGroupCategorias.setOnCheckedStateChangeListener { _, checkedIds ->
            val checkedId = checkedIds.firstOrNull() ?: R.id.chipTodos
            val categoriaFiltro = when (checkedId) {
                R.id.chipElectricidad -> "Electricidad"
                R.id.chipGasfiteria -> "Gasfiter"
                R.id.chipPintura -> "Pintura"
                R.id.chipCarpinteria -> "Carpinter"
                R.id.chipRefrigeracion -> "Refrigeraci"
                R.id.chipComputacion -> "Computaci"
                else -> null
            }
            aplicarFiltros(categoriaFiltro, binding.etBuscarZona.text?.toString()?.trim())
        }
    }

    private fun setupSearch() {
        binding.etBuscarZona.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.trim()
                val currentChip = when (binding.chipGroupCategorias.checkedChipId) {
                    R.id.chipElectricidad -> "Electricidad"
                    R.id.chipGasfiteria -> "Gasfiter"
                    R.id.chipPintura -> "Pintura"
                    R.id.chipCarpinteria -> "Carpinter"
                    R.id.chipRefrigeracion -> "Refrigeraci"
                    R.id.chipComputacion -> "Computaci"
                    else -> null
                }
                aplicarFiltros(currentChip, query)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun checkLocationPermissions() {
        val fineGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            enableMyLocation()
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        val map = googleMap ?: return
        map.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                userLocation = LatLng(location.latitude, location.longitude)
                centerCameraOn(userLocation, 14.5f)
            } else {
                centerCameraOn(userLocation, 13.5f)
            }
            loadTecnicos()
        }.addOnFailureListener {
            centerCameraOn(userLocation, 13.5f)
            loadTecnicos()
        }
    }

    private fun centerCameraOn(latLng: LatLng, zoom: Float) {
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }

    private fun loadTecnicos() {
        binding.progressMapa.visibility = View.VISIBLE

        repository.getTecnicos(size = 50, callback = object : Callback<PageResponse<TecnicoPublico>> {
            override fun onResponse(
                call: Call<PageResponse<TecnicoPublico>>,
                response: Response<PageResponse<TecnicoPublico>>
            ) {
                if (_binding == null) return
                binding.progressMapa.visibility = View.GONE

                if (response.isSuccessful) {
                    allTecnicos = response.body()?.content.orEmpty()
                    aplicarFiltros(null, binding.etBuscarZona.text?.toString()?.trim())
                    return
                }

                Log.e(TAG, "Error cargando técnicos: ${response.code()}")
                aplicarFiltros(null, null)
            }

            override fun onFailure(call: Call<PageResponse<TecnicoPublico>>, t: Throwable) {
                if (_binding == null) return
                binding.progressMapa.visibility = View.GONE
                Log.e(TAG, "Fallo al conectar con el servidor de técnicos", t)
                aplicarFiltros(null, null)
            }
        })
    }

    private fun aplicarFiltros(categoriaFiltro: String?, textoBusqueda: String?) {
        val map = googleMap ?: return

        // Filtrar técnicos
        val filtrados = allTecnicos.filter { tecnico ->
            val matchCategoria = if (categoriaFiltro.isNullOrBlank()) {
                true
            } else {
                val esp = tecnico.especialidadNombre.orEmpty()
                esp.contains(categoriaFiltro, ignoreCase = true)
            }

            val matchTexto = if (textoBusqueda.isNullOrBlank()) {
                true
            } else {
                val nombre = tecnico.nombre.lowercase(Locale.ROOT)
                val distrito = (tecnico.distrito ?: tecnico.ciudad.orEmpty()).lowercase(Locale.ROOT)
                val esp = tecnico.especialidadNombre.orEmpty().lowercase(Locale.ROOT)
                val q = textoBusqueda.lowercase(Locale.ROOT)
                nombre.contains(q) || distrito.contains(q) || esp.contains(q)
            }

            matchCategoria && matchTexto
        }

        // Limpiar marcadores anteriores
        activeMarkers.forEach { it.remove() }
        activeMarkers.clear()

        // Georreferenciar cada técnico
        val itemsConUbicacion = filtrados.map { tecnico ->
            val latLng = resolveCoordinatesForTecnico(tecnico)
            val distance = calculateDistance(userLocation, latLng)
            val distanceText = if (distance < 1000) {
                "${distance.toInt()} m"
            } else {
                String.format(Locale.US, "%.1f km", distance / 1000f)
            }
            ProveedorCercanoItem(
                tecnico = tecnico,
                latLng = latLng,
                distanciaMetros = distance,
                distanciaTexto = distanceText
            )
        }.sortedBy { it.distanciaMetros }

        // Agregar nuevos marcadores al mapa
        itemsConUbicacion.forEach { item ->
            val markerOptions = MarkerOptions()
                .position(item.latLng)
                .title(item.tecnico.nombre)
                .snippet("${item.tecnico.especialidadNombre ?: "Técnico"} · ${item.distanciaTexto}")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))

            val marker = map.addMarker(markerOptions)
            if (marker != null) {
                marker.tag = item
                activeMarkers.add(marker)
            }
        }

        // Actualizar RecyclerView y resumen
        adapter.updateData(itemsConUbicacion)
        binding.tvResumen.text = "${itemsConUbicacion.size} TÉCNICOS DISPONIBLES"

        // Si hay resultados y el usuario no está viendo un punto específico, enfocar primero
        if (itemsConUbicacion.isNotEmpty()) {
            val first = itemsConUbicacion.first()
            adapter.selectItem(first)
        }
    }

    private fun findMarkerForItem(item: ProveedorCercanoItem): Marker? {
        return activeMarkers.firstOrNull { (it.tag as? ProveedorCercanoItem)?.tecnico?.id == item.tecnico.id }
    }

    private fun resolveCoordinatesForTecnico(tecnico: TecnicoPublico): LatLng {
        val distrito = (tecnico.distrito ?: tecnico.ciudad.orEmpty()).lowercase(Locale.ROOT).trim()

        val baseCoord = DISTRICT_COORDS.entries.firstOrNull { distrito.contains(it.key) }?.value
            ?: userLocation

        // Dispersión pseudoaleatoria para evitar solapamiento entre técnicos
        val idSeed = tecnico.id.toInt()
        val latOffset = (((idSeed * 17) % 21) - 10) * 0.0018
        val lngOffset = (((idSeed * 31) % 21) - 10) * 0.0018

        return LatLng(baseCoord.latitude + latOffset, baseCoord.longitude + lngOffset)
    }

    private fun calculateDistance(from: LatLng, to: LatLng): Float {
        val results = FloatArray(1)
        Location.distanceBetween(
            from.latitude, from.longitude,
            to.latitude, to.longitude,
            results
        )
        return results[0]
    }

    private fun navigateToDetalle(tecnico: TecnicoPublico) {
        val bundle = Bundle().apply {
            putLong("proveedorId", tecnico.id)
            putString("nombre", tecnico.nombre)
            putString("especialidad", tecnico.especialidadNombre)
            putString("distrito", tecnico.distrito ?: tecnico.ciudad)
            putDouble("calificacion", tecnico.calificacionPromedio ?: 5.0)
        }
        try {
            findNavController().navigate(R.id.action_mapFragment_to_proveedorDetalleFragment, bundle)
        } catch (e: Exception) {
            Log.e(TAG, "Error navegando a proveedorDetalleFragment desde map", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activeMarkers.clear()
        googleMap = null
        _binding = null
    }

    companion object {
        private const val TAG = "MapFragment"

        // Ubicación por defecto en Lima Metropolitana (Miraflores / Surco)
        private val DEFAULT_LOCATION = LatLng(-12.1224, -77.0282)

        private val DISTRICT_COORDS = mapOf(
            "surco" to LatLng(-12.1350, -76.9950),
            "santiago de surco" to LatLng(-12.1350, -76.9950),
            "miraflores" to LatLng(-12.1215, -77.0295),
            "san isidro" to LatLng(-12.0970, -77.0360),
            "san borja" to LatLng(-12.0910, -77.0010),
            "la molina" to LatLng(-12.0830, -76.9400),
            "los olivos" to LatLng(-11.9700, -77.0700),
            "san miguel" to LatLng(-12.0780, -77.0890),
            "jesus maria" to LatLng(-12.0740, -77.0480),
            "magdalena" to LatLng(-12.0900, -77.0700),
            "barranco" to LatLng(-12.1480, -77.0210),
            "lima" to LatLng(-12.0464, -77.0428)
        )
    }
}