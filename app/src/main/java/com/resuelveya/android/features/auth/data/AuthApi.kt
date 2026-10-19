package com.resuelveya.android.features.auth.data

import com.resuelveya.android.features.auth.model.AuthResponse
import com.resuelveya.android.features.auth.model.RegisterRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("api/v1/auth/firebase-login")
    fun firebaseLogin(
        @Body request: RegisterRequest
    ): Call<AuthResponse>
}