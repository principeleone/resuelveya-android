package com.resuelveya.android.features.auth.data

import com.resuelveya.android.features.auth.model.AuthResponse
import com.resuelveya.android.features.auth.model.LoginRequest
import com.resuelveya.android.features.auth.model.RegisterRequest
import com.resuelveya.android.features.auth.model.RegistroRequest
import com.resuelveya.android.features.auth.model.RegistroResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("api/v1/auth/registro")
    fun registrarUsuario(
        @Body request: RegistroRequest
    ): Call<RegistroResponse>

    @POST("api/v1/auth/login")
    fun login(
        @Body request: LoginRequest
    ): Call<AuthResponse>

    @POST("api/v1/auth/firebase-login")
    fun firebaseLogin(
        @Body request: RegisterRequest
    ): Call<AuthResponse>
}