package com.sdercolin.harmoloid.core.model

enum class Solfege(val displayName: String) {
    Do("Do"),
    DoS("Do♯"),
    Re("Re"),
    ReS("Re♯"),
    Mi("Mi"),
    Fa("Fa"),
    FaS("Fa♯"),
    Sol("Sol"),
    SolS("Sol♯"),
    La("La"),
    LaS("La♯"),
    Si("Si");

    fun shift(delta: Int): Solfege {
        val size = values().size
        val targetOrdinal = (ordinal + delta + size) % size
        return values()[targetOrdinal]
    }
}
