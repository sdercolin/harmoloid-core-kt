package com.sdercolin.harmoloid.core.model

/**
 * Class that represents a note generated in a chorus track
 */
data class NoteShift(
    val noteIndex: Int,
    val keyDelta: Int
)
