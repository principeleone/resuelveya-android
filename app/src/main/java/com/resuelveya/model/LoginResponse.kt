package com.resuelveya.model

data class LoginResponse(
    val token: String,
    val tipo: String,
    val expiresIn: Long,
    val email: String,
    val rol: String
)
