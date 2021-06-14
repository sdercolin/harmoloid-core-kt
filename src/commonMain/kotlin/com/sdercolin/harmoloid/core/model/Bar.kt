package com.sdercolin.harmoloid.core.model

import com.sdercolin.harmoloid.core.util.sumByLong

/**
 * Bar, or measure.
 * It should contain all the notes where [Note.tickOn] belongs to [[tickOn], [tickOff])
 */
data class Bar(
    val index: Int,
    val tickOn: Int,
    val tickOff: Int,
    val notes: List<Note>
) {
    val number get() = index + 1
    val length get() = tickOff - tickOn
    val isEmpty get() = notes.isEmpty()
    internal val validLength get() = notes.sumByLong { it.length }
}
