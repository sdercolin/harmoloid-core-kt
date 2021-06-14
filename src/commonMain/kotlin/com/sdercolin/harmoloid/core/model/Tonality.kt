package com.sdercolin.harmoloid.core.model

enum class Tonality(val displayName: String) {
    C("C"),
    CS("C♯"),
    D("D"),
    DS("D♯"),
    E("E"),
    F("F"),
    FS("F♯"),
    G("G"),
    GS("G♯"),
    A("A"),
    AS("A♯"),
    B("B"),
    Atonal("-");

    val isMelodic get() = this != Atonal
}
