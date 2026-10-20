package com.resuelveya.android.features.auth.view

import android.content.Intent
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
import com.resuelveya.android.databinding.FragmentLoginBinding
import com.resuelveya.android.features.auth.data.AuthRepository
import com.resuelveya.android.features.auth.model.AuthResponse
import com.resuelveya.android.features.auth.model.RegisterRequest
import com.resuelveya.android.features.home.view.MainClienteActivity
import com.resuelveya.android.features.home.view.MainProveedorActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private val authRepository = AuthRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnLogin.setOnClickListener(this)
        binding.btnIrRegistro.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.btnLogin -> {
                val email = binding.etCorreo.text?.toString()?.trim().orEmpty()
                val password = binding.etPassword.text?.toString()?.trim().orEmpty()

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(requireContext(), "Por favor ingresa correo y contraseña", Toast.LENGTH_SHORT).show()
                    return
                }

                loginUser(email, password)
            }
            binding.btnIrRegistro -> {
                findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (!task.isSuccessful) {
                    val errorMsg = task.exception?.localizedMessage ?: "Error al iniciar sesión"
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
                    return@addOnCompleteListener
                }

                val firebaseUser = task.result?.user
                firebaseUser?.getIdToken(true)?.addOnSuccessListener { tokenResult ->
                    val token = tokenResult.token.orEmpty()
                    val request = RegisterRequest(
                        firebaseToken = token
                    )
                    sendFirebaseLoginToBackend(request)
                }?.addOnFailureListener { e ->
                    auth.signOut()
                    SessionManager.logout()
                    Toast.makeText(
                        requireContext(),
                        "Error al obtener token de Firebase: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun sendFirebaseLoginToBackend(request: RegisterRequest) {
        authRepository.registerFirebaseUser(request, object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful) {
                    val authResponse = response.body()
                    val jwtToken = authResponse?.token.orEmpty()
                    val rol = authResponse?.rol.orEmpty()

                    if (jwtToken.isNotEmpty()) {
                        SessionManager.getInstance().saveToken(jwtToken)
                    }
                    if (rol.isNotEmpty()) {
                        SessionManager.getInstance().saveRol(rol)
                    }

                    Toast.makeText(requireContext(), "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                    dispatchByRole(rol)
                    return
                }

                // Si el backend falla, cerrar sesión en Firebase para no dejar estado inconsistente
                auth.signOut()
                SessionManager.logout()

                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Fallo en backend login [${response.code()}] URL: ${call.request().url} - Body: $errorBody")
                val errorMessage = parseErrorMessage(errorBody) ?: "Error del servidor (${response.code()}): $errorBody"
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                auth.signOut()
                SessionManager.logout()
                Log.e(TAG, "Fallo de conexión en backend login", t)
                val errorMsg = "Error de conexión: ${t.localizedMessage ?: "No se pudo contactar al servidor"}"
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun dispatchByRole(rol: String) {
        val destinationActivity = if (rol.equals("TECNICO", ignoreCase = true)) {
            MainProveedorActivity::class.java
        } else {
            MainClienteActivity::class.java
        }

        val intent = Intent(requireContext(), destinationActivity).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        requireActivity().finish()
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
        private const val TAG = "LoginFragment"
    }
}