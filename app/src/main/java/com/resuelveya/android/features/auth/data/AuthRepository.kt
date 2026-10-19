package com.resuelveya.android.features.auth.data

import com.resuelveya.android.core.network.RetrofitClient
import com.resuelveya.android.features.auth.model.AuthResponse
import com.resuelveya.android.features.auth.model.RegisterRequest
import retrofit2.Call
import retrofit2.Callback

class AuthRepository {

    private val authApi: AuthApi = RetrofitClient.createService(AuthApi::class.java)

    fun registerFirebaseUser(
        request: RegisterRequest,
        callback: Callback<AuthResponse>
    ) {
        authApi.firebaseLogin(request).enqueue(callback)
    }
}