package net.vanolex

data class Config(
    val ktorHost: String,
    val ktorPort: Int,
    val dbHost: String,
    val dbDatabase: String,
    val dbUsername: String,
    val dbPassword: String,
    val prime: Int,
    val inverse: Int,
    val salt: Int,
)
