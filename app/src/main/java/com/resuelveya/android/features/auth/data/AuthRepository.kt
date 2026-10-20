package com.resuelveya.android.features.auth.data

import com.resuelveya.android.core.network.RetrofitClient
import com.resuelveya.android.features.auth.model.AuthResponse
import com.resuelveya.android.features.auth.model.LoginRequest
import com.resuelveya.android.features.auth.model.RegisterRequest
import com.resuelveya.android.features.auth.model.RegistroRequest
import com.resuelveya.android.features.auth.model.RegistroResponse
import retrofit2.Call
import retrofit2.Callback

class AuthRepository {

    private val authApi: AuthApi = RetrofitClient.createService(AuthApi::class.java)

    fun firebaseRegister(
        request: com.resuelveya.android.features.auth.model.FirebaseRegisterRequest,
        callback: Callback<RegistroResponse>
    ) {
        authApi.firebaseRegister(request).enqueue(callback)
    }

    fun registrarUsuario(
        request: RegistroRequest,
        callback: Callback<RegistroResponse>
    ) {
        authApi.registrarUsuario(request).enqueue(callback)
    }

    fun login(
        request: LoginRequest,
        callback: Callback<AuthResponse>
    ) {
        authApi.login(request).enqueue(callback)
    }

    fun registerFirebaseUser(
        request: RegisterRequest,
        callback: Callback<AuthResponse>
    ) {
        authApi.firebaseLogin(request).enqueue(callback)
    }
}