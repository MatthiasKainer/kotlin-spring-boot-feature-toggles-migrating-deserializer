package de.matthiaskainer.acme.dbModels

data class User(val id: String, val lastName: String, val firstName: String, val email: String? = null)