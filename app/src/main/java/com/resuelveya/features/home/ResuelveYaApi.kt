package com.resuelveya.features.home

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ResuelveYaApi {
    private const val BASE_URL =
        "https://resuelveya-backend-production.up.railway.app/"

    val servicios: ServicioApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ServicioApi::class.java)
    }
}