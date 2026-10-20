package com.resuelveya.android.features.auth.view

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.gson.Gson
import com.resuelveya.android.R
import com.resuelveya.android.core.network.ErrorResponse
import com.resuelveya.android.core.session.SessionManager
import com.resuelveya.android.databinding.FragmentRegisterBinding
import com.resuelveya.android.features.auth.data.AuthRepository
import com.resuelveya.android.features.auth.model.AuthResponse
import com.resuelveya.android.features.auth.model.FirebaseRegisterRequest
import com.resuelveya.android.features.auth.model.RegisterRequest
import com.resuelveya.android.features.auth.model.RegistroResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private val authRepository = AuthRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupListeners()
    }

    private fun setupViews() {
        updateRoleView(isProvider = false)
    }

    private fun setupListeners() {
        binding.toggleRol.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val isProvider = checkedId == R.id.btnRolProveedor
            updateRoleView(isProvider)
        }

        binding.btnRegistrar.setOnClickListener(this)
        binding.btnIrLogin.setOnClickListener(this)
    }

    private fun updateRoleView(isProvider: Boolean) {
        if (isProvider) {
            binding.tvRolInfo.setText(R.string.auth_subtitulo_proveedor)
            binding.btnRegistrar.text = getString(R.string.auth_btn_registrar) + " (PROVEEDOR)"
            return
        }

        binding.tvRolInfo.setText(R.string.auth_subtitulo_cliente)
        binding.btnRegistrar.text = getString(R.string.auth_btn_registrar) + " (CLIENTE)"
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.btnRegistrar -> handleRegister()
            binding.btnIrLogin -> findNavController().popBackStack()
        }
    }

    private fun handleRegister() {
        val name = binding.etNombre.text?.toString()?.trim().orEmpty()
        val email = binding.etCorreoReg.text?.toString()?.trim().orEmpty()
        val phone = binding.etTelefono.text?.toString()?.trim().orEmpty()
        val city = binding.etCiudad.text?.toString()?.trim().orEmpty()
        val address = binding.etDireccion.text?.toString()?.trim().orEmpty()
        val password = binding.etPasswordReg.text?.toString()?.trim().orEmpty()
        val confirmPassword = binding.etConfirmar.text?.toString()?.trim().orEmpty()

        // Limpiar errores previos
        binding.tilNombre.error = null
        binding.tilCorreoReg.error = null
        binding.tilTelefono.error = null
        binding.tilPasswordReg.error = null
        binding.tilConfirmar.error = null

        // Validaciones según requerimientos del endpoint de REST.md
        if (name.isEmpty()) {
            binding.tilNombre.error = "Ingresa tu nombre completo"
            binding.etNombre.requestFocus()
            return
        }

        if (email.isEmpty()) {
            binding.tilCorreoReg.error = "Ingresa tu correo electrónico"
            binding.etCorreoReg.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilCorreoReg.error = "Ingresa un correo electrónico válido"
            binding.etCorreoReg.requestFocus()
            return
        }

        if (password.isEmpty()) {
            binding.tilPasswordReg.error = "Ingresa una contraseña"
            binding.etPasswordReg.requestFocus()
            return
        }

        if (password.length < 6) {
            binding.tilPasswordReg.error = "La contraseña debe tener al menos 6 caracteres"
            binding.etPasswordReg.requestFocus()
            return
        }

        if (password != confirmPassword) {
            binding.tilConfirmar.error = "Las contraseñas no coinciden"
            binding.etConfirmar.requestFocus()
            return
        }

        val isProvider = binding.toggleRol.checkedButtonId == R.id.btnRolProveedor
        val backendRole = if (isProvider) "TECNICO" else "CLIENTE"

        val firebaseRegisterRequest = FirebaseRegisterRequest(
            nombre = name,
            correo = email,
            telefono = phone.ifBlank { null },
            direccion = address.ifBlank { null },
            ciudad = city.ifBlank { null },
            rol = backendRole
        )

        executeRegistration(firebaseRegisterRequest, password)
    }

    private fun executeRegistration(request: FirebaseRegisterRequest, rawPassword: String) {
        binding.btnRegistrar.isEnabled = false
        binding.btnRegistrar.text = "Registrando..."

        // 1. Crear el usuario en Firebase Authentication
        auth.createUserWithEmailAndPassword(request.correo, rawPassword)
            .addOnCompleteListener(requireActivity()) { task ->
                if (!task.isSuccessful) {
                    val ex = task.exception
                    binding.btnRegistrar.isEnabled = true
                    val isProvider = request.rol == "TECNICO"
                    updateRoleView(isProvider)

                    if (ex is FirebaseAuthUserCollisionException) {
                        // Si ya existe en Firebase, intentamos registrarlo directamente en MySQL
                        sendBackendFirebaseRegister(request, wasAlreadyInFirebase = true)
                        return@addOnCompleteListener
                    }

                    val errorMsg = ex?.localizedMessage ?: "Error al registrar usuario en Firebase"
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
                    return@addOnCompleteListener
                }

                // 2. Firebase exitoso -> Registrar en MySQL vía api/v1/auth/firebase-register
                sendBackendFirebaseRegister(request, wasAlreadyInFirebase = false)
            }
    }

    private fun sendBackendFirebaseRegister(request: FirebaseRegisterRequest, wasAlreadyInFirebase: Boolean) {
        authRepository.firebaseRegister(request, object : Callback<RegistroResponse> {
            override fun onResponse(call: Call<RegistroResponse>, response: Response<RegistroResponse>) {
                binding.btnRegistrar.isEnabled = true
                val isProvider = request.rol == "TECNICO"
                updateRoleView(isProvider)

                if (response.isSuccessful) {
                    val registroResponse = response.body()
                    Log.i(TAG, "Registro exitoso en backend MySQL: id=${registroResponse?.id}, rol=${registroResponse?.rol}")
                    Toast.makeText(requireContext(), "¡Registro completado con éxito! Ya puedes iniciar sesión.", Toast.LENGTH_LONG).show()
                    findNavController().popBackStack()
                    return
                }

                // Si el backend falla y acabamos de crear la cuenta en Firebase, la removemos para no dejar estado inconsistente
                if (!wasAlreadyInFirebase) {
                    auth.currentUser?.delete()
                }

                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Fallo en backend firebase-register [${response.code()}] URL: ${call.request().url} - Body: $errorBody")
                val errorMessage = parseErrorMessage(errorBody) ?: "Error del servidor (${response.code()}): $errorBody"
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
            }

            override fun onFailure(call: Call<RegistroResponse>, t: Throwable) {
                binding.btnRegistrar.isEnabled = true
                val isProvider = request.rol == "TECNICO"
                updateRoleView(isProvider)

                if (!wasAlreadyInFirebase) {
                    auth.currentUser?.delete()
                }

                Log.e(TAG, "Fallo de conexión en backend firebase-register", t)
                val errorMsg = "Error de conexión: ${t.localizedMessage ?: "No se pudo contactar al servidor"}"
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun parseErrorMessage(errorJson: String?): String? {
        if (errorJson.isNullOrBlank()) return null
        return try {
            val errorResponse = Gson().fromJson(errorJson, ErrorResponse::class.java)
            errorResponse.message ?: errorResponse.error
        } catch (_: Exception) {
            errorJson
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "RegisterFragment"
    }
}
