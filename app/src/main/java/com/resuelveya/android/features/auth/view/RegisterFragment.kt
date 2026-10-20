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
import com.resuelveya.android.features.auth.model.RegisterRequest
import com.resuelveya.android.features.auth.model.RegistroRequest
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

        if (phone.isEmpty()) {
            binding.tilTelefono.error = "Ingresa tu número de teléfono"
            binding.etTelefono.requestFocus()
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

        val registroRequest = RegistroRequest(
            nombre = name,
            email = email,
            password = password,
            telefono = phone,
            rol = backendRole
        )

        executeRegistration(registroRequest, password)
    }

    private fun executeRegistration(request: RegistroRequest, rawPassword: String) {
        binding.btnRegistrar.isEnabled = false
        binding.btnRegistrar.text = "Registrando..."

        authRepository.registrarUsuario(request, object : Callback<RegistroResponse> {
            override fun onResponse(call: Call<RegistroResponse>, response: Response<RegistroResponse>) {
                if (response.isSuccessful) {
                    val registroResponse = response.body()
                    Log.i(TAG, "Registro exitoso en backend BD: id=${registroResponse?.id}, rol=${registroResponse?.rol}")

                    // Sincronizar creación de cuenta en Firebase Auth para soportar login móvil
                    syncFirebaseUser(request.email, rawPassword, request.rol, request.nombre, request.telefono)
                    return
                }

                binding.btnRegistrar.isEnabled = true
                val isProvider = request.rol == "TECNICO"
                updateRoleView(isProvider)

                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Fallo en backend registro [${response.code()}] URL: ${call.request().url} - Body: $errorBody")
                val errorMessage = parseErrorMessage(errorBody) ?: "Error del servidor (${response.code()}): $errorBody"
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
            }

            override fun onFailure(call: Call<RegistroResponse>, t: Throwable) {
                binding.btnRegistrar.isEnabled = true
                val isProvider = request.rol == "TECNICO"
                updateRoleView(isProvider)

                Log.e(TAG, "Fallo de conexión en backend registro", t)
                val errorMsg = "Error de conexión: ${t.localizedMessage ?: "No se pudo contactar al servidor"}"
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun syncFirebaseUser(
        email: String,
        password: String,
        role: String,
        name: String,
        phone: String
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                binding.btnRegistrar.isEnabled = true
                val isProvider = role == "TECNICO"
                updateRoleView(isProvider)

                if (task.isSuccessful) {
                    val firebaseUser = task.result?.user
                    firebaseUser?.getIdToken(true)?.addOnSuccessListener { tokenResult ->
                        val token = tokenResult.token.orEmpty()
                        val firebaseReq = RegisterRequest(
                            firebaseToken = token,
                            rol = role,
                            nombre = name,
                            telefono = phone
                        )
                        // Vinculación opcional con backend firebase-login para almacenar token de sesión de inmediato
                        authRepository.registerFirebaseUser(firebaseReq, object : Callback<AuthResponse> {
                            override fun onResponse(call: Call<AuthResponse>, res: Response<AuthResponse>) {
                                if (res.isSuccessful) {
                                    res.body()?.token?.let { SessionManager.getInstance().saveToken(it) }
                                    res.body()?.rol?.let { SessionManager.getInstance().saveRol(it) }
                                }
                            }

                            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                                Log.w(TAG, "Fallo de enlace secundario firebase-login tras registro exitoso", t)
                            }
                        })
                    }
                } else {
                    val ex = task.exception
                    if (ex is FirebaseAuthUserCollisionException) {
                        Log.i(TAG, "Usuario ya existía en Firebase Auth. Registro backend completado.")
                    } else {
                        Log.w(TAG, "Aviso al sincronizar con Firebase: ${ex?.localizedMessage}")
                    }
                }

                Toast.makeText(requireContext(), "¡Registro completado con éxito! Ya puedes iniciar sesión.", Toast.LENGTH_LONG).show()
                findNavController().popBackStack()
            }
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