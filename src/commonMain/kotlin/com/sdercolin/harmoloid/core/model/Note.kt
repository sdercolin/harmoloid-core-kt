package com.sdercolin.harmoloid.core.model

data class Note(
    val index: Int,
    val key: Int,
    val tickOn: Long,
    val tickOff: Long,
    val lyric: String
) {
    val length get() = tickOff - tickOn

    internal fun getKeyRelativeToTonality(tonality: Tonality) = (key % KEY_IN_OCTAVE)
        .minus(tonality.ordinal)
        .let { if (it < 0) it + KEY_IN_OCTAVE else it }
}
