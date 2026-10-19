package com.resuelveya.android.features.tecnicos.network

import com.resuelveya.android.core.network.RetrofitClient as CoreRetrofitClient

/**
 * Delega al RetrofitClient centralizado de core/network,
 * que ya incorpora el AuthInterceptor con SessionManager.
 */
object RetrofitClient {

    val api: ApiService by lazy {
        CoreRetrofitClient.createService(ApiService::class.java)
    }
}