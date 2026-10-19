package com.resuelveya.android.features.perfil.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.resuelveya.android.R
import com.resuelveya.android.core.session.SessionManager
import com.resuelveya.android.databinding.FragmentPerfilBinding
import com.resuelveya.android.features.auth.view.AuthActivity
import com.resuelveya.android.features.perfil.data.PerfilRepository
import com.resuelveya.android.features.perfil.model.PerfilResponse
import com.resuelveya.android.features.perfil.model.PerfilUpdateRequest
import com.resuelveya.android.features.perfil.model.TecnicoPerfilUpdateRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PerfilFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!

    private val repository = PerfilRepository()
    private var perfilActual: PerfilResponse? = null
    private var esTecnico: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rol = SessionManager.getRol() ?: "CLIENTE"
        esTecnico = rol.equals("TECNICO", ignoreCase = true)

        binding.tvRol.text = rol.uppercase()
        setupListeners()
        loadPerfilData()
    }

    private fun setupListeners() {
        binding.btnEditarPerfil.setOnClickListener {
            switchToEditMode()
        }

        binding.btnCancelarEdicion.setOnClickListener {
            switchToViewMode()
        }

        binding.btnGuardarPerfil.setOnClickListener {
            savePerfilData()
        }

        binding.btnCerrarSesion.setOnClickListener {
            performLogout()
        }
    }

    private fun loadPerfilData() {
        binding.progressPerfil.visibility = View.VISIBLE

        repository.getMiPerfil(object : Callback<PerfilResponse> {
            override fun onResponse(call: Call<PerfilResponse>, response: Response<PerfilResponse>) {
                if (_binding == null) return
                binding.progressPerfil.visibility = View.GONE

                if (response.isSuccessful && response.body() != null) {
                    val perfil = response.body()!!
                    perfilActual = perfil
                    populateViewData(perfil)
                } else {
                    fallbackToFirebaseData()
                }
            }

            override fun onFailure(call: Call<PerfilResponse>, t: Throwable) {
                if (_binding == null) return
                binding.progressPerfil.visibility = View.GONE
                Log.e(TAG, "Error al cargar perfil desde el backend", t)
                fallbackToFirebaseData()
            }
        })
    }

    private fun populateViewData(perfil: PerfilResponse) {
        val displayName = perfil.nombre?.ifBlank { null }
            ?: FirebaseAuth.getInstance().currentUser?.displayName
            ?: "Usuario ResuelveYa"

        binding.tvNombre.text = displayName
        binding.tvCorreoValor.text = perfil.email ?: FirebaseAuth.getInstance().currentUser?.email ?: "-"
        binding.tvTelefonoValor.text = perfil.telefono?.ifBlank { null } ?: "Sin registrar"

        val ubicacion = listOfNotNull(
            perfil.direccion?.ifBlank { null },
            perfil.ciudad?.ifBlank { null }
        ).joinToString(", ")
        binding.tvDireccionValor.text = if (ubicacion.isNotBlank()) ubicacion else "Sin registrar"

        if (esTecnico) {
            binding.cardTecnicoLectura.visibility = View.VISIBLE
            binding.tvPresentacionValor.text = perfil.presentacion?.ifBlank { null } ?: "Sin presentación"
            val exp = perfil.aniosExperiencia ?: 0
            binding.tvExperienciaValor.text = if (exp > 0) "$exp años de experiencia" else "No especificado"

            val yape = perfil.yapeNumero?.ifBlank { null } ?: "-"
            val plin = perfil.plinNumero?.ifBlank { null } ?: "-"
            val titular = perfil.titularPago?.ifBlank { null } ?: "-"
            binding.tvPagosValor.text = "Yape: $yape | Plin: $plin\nTitular: $titular"
        } else {
            binding.cardTecnicoLectura.visibility = View.GONE
        }
    }

    private fun fallbackToFirebaseData() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val displayName = currentUser?.displayName?.ifBlank { null }
            ?: currentUser?.email?.substringBefore("@")
            ?: "Usuario ResuelveYa"

        binding.tvNombre.text = displayName
        binding.tvCorreoValor.text = currentUser?.email ?: "-"
        binding.tvTelefonoValor.text = currentUser?.phoneNumber?.ifBlank { null } ?: "Sin registrar"
        binding.tvDireccionValor.text = "Sin registrar"
        binding.cardTecnicoLectura.visibility = View.GONE
    }

    private fun switchToEditMode() {
        val perfil = perfilActual
        binding.etNombreEd.setText(perfil?.nombre ?: binding.tvNombre.text)
        binding.etTelefonoEd.setText(perfil?.telefono.orEmpty())
        binding.etDireccionEd.setText(perfil?.direccion.orEmpty())
        binding.etCiudadEd.setText(perfil?.ciudad.orEmpty())

        if (esTecnico) {
            binding.llCamposTecnicoEd.visibility = View.VISIBLE
            binding.etPresentacionEd.setText(perfil?.presentacion.orEmpty())
            binding.etExperienciaEd.setText(perfil?.aniosExperiencia?.toString().orEmpty())
            binding.etYapeEd.setText(perfil?.yapeNumero.orEmpty())
            binding.etPlinEd.setText(perfil?.plinNumero.orEmpty())
            binding.etTitularEd.setText(perfil?.titularPago.orEmpty())
        } else {
            binding.llCamposTecnicoEd.visibility = View.GONE
        }

        binding.groupLectura.visibility = View.GONE
        binding.cardTecnicoLectura.visibility = View.GONE
        binding.groupEdicion.visibility = View.VISIBLE
    }

    private fun switchToViewMode() {
        binding.groupEdicion.visibility = View.GONE
        binding.llCamposTecnicoEd.visibility = View.GONE
        binding.groupLectura.visibility = View.VISIBLE
        if (esTecnico) {
            binding.cardTecnicoLectura.visibility = View.VISIBLE
        }
    }

    private fun savePerfilData() {
        val nuevoNombre = binding.etNombreEd.text?.toString()?.trim().orEmpty()
        val nuevoTelefono = binding.etTelefonoEd.text?.toString()?.trim()
        val nuevaDireccion = binding.etDireccionEd.text?.toString()?.trim()
        val nuevaCiudad = binding.etCiudadEd.text?.toString()?.trim()

        if (nuevoNombre.isBlank()) {
            binding.tilNombreEd.error = "Ingresa tu nombre"
            return
        }
        binding.tilNombreEd.error = null

        binding.progressPerfil.visibility = View.VISIBLE
        binding.btnGuardarPerfil.isEnabled = false

        val updateRequest = PerfilUpdateRequest(
            nombre = nuevoNombre,
            telefono = nuevoTelefono,
            fotoUrl = perfilActual?.fotoUrl,
            direccion = nuevaDireccion,
            distrito = perfilActual?.distrito,
            ciudad = nuevaCiudad
        )

        repository.updatePerfil(updateRequest, object : Callback<PerfilResponse> {
            override fun onResponse(call: Call<PerfilResponse>, response: Response<PerfilResponse>) {
                if (_binding == null) return

                if (!response.isSuccessful) {
                    binding.progressPerfil.visibility = View.GONE
                    binding.btnGuardarPerfil.isEnabled = true
                    Toast.makeText(requireContext(), "Error al guardar perfil", Toast.LENGTH_SHORT).show()
                    return
                }

                val perfilBackend = response.body()
                val perfilActualizado = (perfilBackend ?: perfilActual)?.copy(
                    nombre = nuevoNombre,
                    telefono = nuevoTelefono,
                    direccion = nuevaDireccion,
                    ciudad = if (!perfilBackend?.ciudad.isNullOrBlank()) perfilBackend?.ciudad else nuevaCiudad
                )

                if (esTecnico) {
                    saveTecnicoExtraData(perfilActualizado)
                } else {
                    finishSaving(perfilActualizado)
                }
            }

            override fun onFailure(call: Call<PerfilResponse>, t: Throwable) {
                if (_binding == null) return
                binding.progressPerfil.visibility = View.GONE
                binding.btnGuardarPerfil.isEnabled = true
                Toast.makeText(requireContext(), "Error de red al actualizar", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Error updating perfil", t)
            }
        })
    }

    private fun saveTecnicoExtraData(perfilBase: PerfilResponse?) {
        val presentacion = binding.etPresentacionEd.text?.toString()?.trim()
        val experienciaStr = binding.etExperienciaEd.text?.toString()?.trim()
        val experiencia = experienciaStr?.toIntOrNull()
        val yape = binding.etYapeEd.text?.toString()?.trim()
        val plin = binding.etPlinEd.text?.toString()?.trim()
        val titular = binding.etTitularEd.text?.toString()?.trim()

        val tecnicoRequest = TecnicoPerfilUpdateRequest(
            presentacion = presentacion,
            aniosExperiencia = experiencia,
            especialidadId = perfilActual?.especialidadId,
            yapeNumero = yape,
            plinNumero = plin,
            titularPago = titular,
            fotoUrl = perfilActual?.fotoUrl
        )

        repository.updateTecnicoPerfil(tecnicoRequest, object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (_binding == null) return
                val perfilFinal = perfilBase?.copy(
                    presentacion = presentacion,
                    aniosExperiencia = experiencia,
                    yapeNumero = yape,
                    plinNumero = plin,
                    titularPago = titular
                )
                finishSaving(perfilFinal)
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                if (_binding == null) return
                Log.e(TAG, "Error actualizando perfil técnico", t)
                finishSaving(perfilBase)
            }
        })
    }

    private fun finishSaving(perfilFinal: PerfilResponse?) {
        binding.progressPerfil.visibility = View.GONE
        binding.btnGuardarPerfil.isEnabled = true
        perfilActual = perfilFinal
        perfilFinal?.let { populateViewData(it) }
        switchToViewMode()
        Toast.makeText(requireContext(), R.string.perfil_actualizado_exito, Toast.LENGTH_SHORT).show()
    }

    private fun performLogout() {
        FirebaseAuth.getInstance().signOut()
        SessionManager.logout()

        val intent = Intent(requireContext(), AuthActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "PerfilFragment"
    }
}