package com.dev.myapplication

data class User(
    val id: Int,
    val name: String,
    val email: String
)

data class LoginResponse(
    val user: User,
    val token: String
)

data class LoginRequest(
    val email: String,
    val password: String
)