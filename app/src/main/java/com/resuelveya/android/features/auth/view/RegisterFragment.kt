package com.resuelveya.android.features.auth.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
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
        val email = binding.etCorreoReg.text?.toString()?.trim().orEmpty()
        val password = binding.etPasswordReg.text?.toString()?.trim().orEmpty()
        val confirmPassword = binding.etConfirmar.text?.toString()?.trim().orEmpty()
        val name = binding.etNombre.text?.toString()?.trim().orEmpty()
        val phone = binding.etTelefono.text?.toString()?.trim().orEmpty()
        val city = binding.etCiudad.text?.toString()?.trim().orEmpty()
        val address = binding.etDireccion.text?.toString()?.trim().orEmpty()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor ingresa correo y contraseña", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(requireContext(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            return
        }

        val isProvider = binding.toggleRol.checkedButtonId == R.id.btnRolProveedor
        val backendRole = if (isProvider) "TECNICO" else "CLIENTE"

        Log.d(TAG, "==================================================")
        Log.d(TAG, "[REGISTRO] 1. Inicio de registro")
        Log.d(TAG, "[REGISTRO] Botón marcado: ${if (isProvider) "PROVEEDOR" else "CLIENTE"}")
        Log.d(TAG, "[REGISTRO] Rol asignado: '$backendRole'")
        Log.d(TAG, "[REGISTRO] Datos ingresados -> Nombre: '$name', Correo: '$email', Tel: '$phone', Ciudad: '$city', Dir: '$address'")
        Log.d(TAG, "==================================================")

        registerUser(
            email = email,
            password = password,
            role = backendRole,
            name = name,
            phone = phone,
            city = city,
            address = address
        )
    }

    private fun registerUser(
        email: String,
        password: String,
        role: String,
        name: String,
        phone: String,
        city: String,
        address: String
    ) {
        binding.btnRegistrar.isEnabled = false
        binding.btnRegistrar.text = "Registrando..."

        Log.d(TAG, "[REGISTRO] 2. Creando usuario en Firebase Auth: $email ...")
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (!task.isSuccessful) {
                    binding.btnRegistrar.isEnabled = true
                    updateRoleView(role == "TECNICO")

                    val ex = task.exception
                    val errorMsg = ex?.localizedMessage ?: "Error al registrar usuario en Firebase"
                    Log.e(TAG, "[REGISTRO] Falló Firebase Auth: $errorMsg", ex)
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
                    return@addOnCompleteListener
                }

                val firebaseUser = task.result?.user
                val uid = firebaseUser?.uid
                Log.i(TAG, "[REGISTRO] 3. Firebase Auth OK. UID: $uid")

                val request = FirebaseRegisterRequest(
                    nombre = name,
                    correo = email,
                    telefono = phone.ifBlank { null },
                    direccion = address.ifBlank { null },
                    ciudad = city.ifBlank { null },
                    rol = role
                )

                val jsonPayload = Gson().toJson(request)
                Log.d(TAG, "[REGISTRO] 4. Payload JSON para el backend (/api/v1/auth/firebase-register):\n$jsonPayload")

                sendRegisterToBackend(request)
            }
    }

    private fun sendRegisterToBackend(request: FirebaseRegisterRequest) {
        Log.d(TAG, "[REGISTRO] 5. Enviando petición POST a /api/v1/auth/firebase-register con rol: '${request.rol}'...")

        authRepository.firebaseRegister(request, object : Callback<RegistroResponse> {
            override fun onResponse(call: Call<RegistroResponse>, response: Response<RegistroResponse>) {
                binding.btnRegistrar.isEnabled = true
                updateRoleView(request.rol == "TECNICO")

                val code = response.code()
                val url = call.request().url

                if (response.isSuccessful) {
                    val registroResponse = response.body()
                    val responseJson = Gson().toJson(registroResponse)
                    Log.i(TAG, "[REGISTRO] 6. ÉXITO en Backend [HTTP $code] URL: $url")
                    Log.i(TAG, "[REGISTRO] Respuesta del servidor:\n$responseJson")

                    Toast.makeText(requireContext(), "¡Registro completado con éxito! Ya puedes iniciar sesión.", Toast.LENGTH_LONG).show()
                    findNavController().popBackStack()
                    return
                }

                // Si falla el backend, eliminamos el usuario recién creado en Firebase para no dejar estado inconsistente
                auth.currentUser?.delete()
                auth.signOut()

                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "==================================================")
                Log.e(TAG, "[REGISTRO] 6. FALLO EN BACKEND [HTTP $code] URL: $url")
                Log.e(TAG, "[REGISTRO] Rol enviado: '${request.rol}'")
                Log.e(TAG, "[REGISTRO] Body de error completo:\n$errorBody")
                Log.e(TAG, "==================================================")

                val errorMessage = parseErrorMessage(errorBody) ?: "Error del servidor ($code)"
                Toast.makeText(
                    requireContext(),
                    "Fallo backend [$code]: $errorMessage\n(Revisa Logcat tag: RegisterFragment)",
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onFailure(call: Call<RegistroResponse>, t: Throwable) {
                binding.btnRegistrar.isEnabled = true
                updateRoleView(request.rol == "TECNICO")

                auth.currentUser?.delete()
                auth.signOut()

                Log.e(TAG, "[REGISTRO] Fallo de conexión con el backend", t)
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