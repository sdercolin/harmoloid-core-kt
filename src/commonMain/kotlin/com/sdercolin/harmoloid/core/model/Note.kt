package com.sdercolin.harmoloid.core.model

import com.sdercolin.harmoloid.core.exception.InvalidNoteException

/**
 * Basic data of a note required for process
 */
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

    internal fun ensureValid(trackIndex: Int) {
        if (key !in NOTE_KEY_MIN..NOTE_KEY_MAX) {
            throw InvalidNoteException.KeyOutOfRange(trackIndex, this, (NOTE_KEY_MIN..NOTE_KEY_MAX).toString())
        }
        if (length <= 0) {
            throw InvalidNoteException.InvalidLength(trackIndex, this)
        }
    }

    companion object {
        const val NOTE_KEY_MAX = 120
        const val NOTE_KEY_MIN = 0
    }
}
