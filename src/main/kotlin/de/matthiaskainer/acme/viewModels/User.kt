package de.matthiaskainer.acme.viewModels

data class UserV1(val name: String, val email: String)

data class UserV2(
    val id: String,
    val firstName: String,
    val middleName: String,
    val lastName: String,
    val email: String,
)
