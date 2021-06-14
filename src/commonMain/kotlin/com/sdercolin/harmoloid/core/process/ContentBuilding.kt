package com.sdercolin.harmoloid.core.process

import com.sdercolin.harmoloid.core.model.Bar
import com.sdercolin.harmoloid.core.model.Note
import com.sdercolin.harmoloid.core.model.TimeSignature

internal fun buildBars(notes: List<Note>, timeSignatures: List<TimeSignature>): List<Bar> {
    val lastTick = notes.lastOrNull()?.tickOff ?: return emptyList()
    val timeSignatureAtInfinity = timeSignatures.last().copy(measurePosition = Int.MAX_VALUE)

    val barRanges = mutableListOf<IntRange>()
    for (timeSignaturePair in timeSignatures.plus(timeSignatureAtInfinity).zipWithNext()) {
        val (thisTimeSignature, nextTimeSignature) = timeSignaturePair

        while (true) {
            if (barRanges.size == nextTimeSignature.measurePosition) break
            val start = barRanges.lastOrNull()?.last?.plus(1) ?: 0
            if (start >= lastTick) break
            val end = start + thisTimeSignature.ticksInMeasure
            barRanges.add(start until end)
        }
    }

    return barRanges.mapIndexed { index, barRange ->
        Bar(
            index = index,
            tickOn = barRange.first,
            tickOff = barRange.last + 1,
            notes = notes.filter { it.tickOn in barRange }
        )
    }
}
