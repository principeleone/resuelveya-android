package com.resuelveya.android.features.auth.view

import android.os.Bundle
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
import com.resuelveya.android.features.auth.model.RegisterRequest
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
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (!task.isSuccessful) {
                    val errorMsg = task.exception?.localizedMessage ?: "Error al registrar usuario en Firebase"
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
                    return@addOnCompleteListener
                }

                val firebaseUser = task.result?.user
                firebaseUser?.getIdToken(true)?.addOnSuccessListener { tokenResult ->
                    val token = tokenResult.token.orEmpty()
                    val request = RegisterRequest(
                        firebaseToken = token,
                        rol = role,
                        nombre = name,
                        telefono = phone,
                        direccion = address,
                        ciudad = city
                    )

                    sendRegisterToBackend(request)
                }?.addOnFailureListener { e ->
                    Toast.makeText(
                        requireContext(),
                        "Error al obtener token de Firebase: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun sendRegisterToBackend(request: RegisterRequest) {
        authRepository.registerFirebaseUser(request, object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful) {
                    val authResponse = response.body()
                    authResponse?.token?.let { jwtToken ->
                        SessionManager.getInstance().saveToken(jwtToken)
                    }
                    authResponse?.rol?.let { rol ->
                        SessionManager.getInstance().saveRol(rol)
                    }

                    Toast.makeText(requireContext(), "Registro completado con éxito", Toast.LENGTH_LONG).show()
                    findNavController().popBackStack()
                    return
                }

                val errorBody = response.errorBody()?.string()
                val errorMessage = parseErrorMessage(errorBody) ?: "Error del servidor (${response.code()})"
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
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
}