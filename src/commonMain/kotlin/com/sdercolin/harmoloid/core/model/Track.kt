package com.sdercolin.harmoloid.core.model

import com.sdercolin.harmoloid.core.Config
import com.sdercolin.harmoloid.core.exception.BarOverlappingException
import com.sdercolin.harmoloid.core.exception.EmptyPassageException
import com.sdercolin.harmoloid.core.exception.InvalidBarIndexesException
import com.sdercolin.harmoloid.core.exception.InvalidBarOrderException
import com.sdercolin.harmoloid.core.exception.InvalidNoteIndexesException
import com.sdercolin.harmoloid.core.exception.InvalidNoteOrderException
import com.sdercolin.harmoloid.core.exception.InvalidPassageDivisionException
import com.sdercolin.harmoloid.core.exception.InvalidPassageIndexesException
import com.sdercolin.harmoloid.core.exception.NoteOverlappingException
import com.sdercolin.harmoloid.core.exception.PassageTonalityNotMarkedException
import com.sdercolin.harmoloid.core.process.buildBars

/**
 * An original track in the project
 */
data class Track(
    val index: Int,
    val name: String,
    val bars: List<Bar>,
    val passages: List<Passage>? = null,
    val harmonies: Set<HarmonicType>? = null
) {
    val number get() = index + 1
    val isTonalityMarked get() = passages?.all { it.tonality != null } == true
    val notes get() = bars.flatMap { it.notes }

    /**
     * Get a track copy with passage settings initialized
     */
    fun passagesInitialized(): Track {
        val passages = listOf(Passage(0, bars.toList()))
        return this.copy(passages = passages)
    }

    internal fun passagesInitializedIfNeeded(): Track {
        return if (passages == null) passagesInitialized()
        else this
    }

    internal fun getNoteShifts(harmonicType: HarmonicType, config: Config): List<NoteShift> {
        ensureValidPassagesWithTonalityMarked()
        return requirePassages().flatMap { it.getNoteShifts(harmonicType, config) }
    }

    internal fun applyPassageSettings(passagesFromOther: List<Passage>): Track {
        val passages = passagesFromOther.mapNotNull { passageFromOther ->
            val barIndexesFromOther = passageFromOther.bars.map { it.index }
            val barIndexes = this.bars.indices.intersect(barIndexesFromOther)
                .takeUnless { it.isEmpty() } ?: return@mapNotNull null
            val bars = this.bars.subList(barIndexes.first(), barIndexes.last() + 1)
            passageFromOther.copy(bars = bars)
        }
        return copy(passages = passages.mapIndexed { index, passage ->
            passage.copy(index = index)
        })
    }

    internal fun ensureValid() {
        ensureValidBars()
        ensureValidNotes()
        if (passages != null) ensureValidPassages()
    }

    private fun ensureValidNotes() {
        notes.forEach { it.ensureValid(index) }
        val noteIndexes = notes.map { it.index }
        if (noteIndexes != notes.indices.toList()) {
            throw InvalidNoteIndexesException(index, noteIndexes)
        }
        notes.zipWithNext().forEach { pair ->
            if (pair.first.tickOn >= pair.second.tickOn) {
                throw InvalidNoteOrderException(index, pair.toList())
            }
            if (pair.first.tickOff > pair.second.tickOn) {
                throw NoteOverlappingException(index, pair.toList())
            }
        }
    }

    private fun ensureValidBars() {
        bars.forEach { it.ensureValid(index) }
        val barIndexes = bars.map { it.index }
        if (barIndexes != bars.indices.toList()) {
            throw InvalidBarIndexesException(index, barIndexes)
        }
        bars.zipWithNext().forEach { pair ->
            if (pair.first.tickOn >= pair.second.tickOn) {
                throw InvalidBarOrderException(index, pair.toList())
            }
            if (pair.first.tickOff > pair.second.tickOn) {
                throw BarOverlappingException(index, pair.toList())
            }
        }
    }

    private fun ensureValidPassages() {
        val passages = requirePassages()
        val passageIndexes = passages.map { it.index }
        if (passageIndexes != passages.indices.toList()) {
            throw InvalidPassageIndexesException(index, passageIndexes)
        }
        passages.forEachIndexed { index, passage ->
            if (passage.bars.isEmpty()) throw EmptyPassageException(this.index, index)
        }
        if (passages.flatMap { it.bars } != bars) throw InvalidPassageDivisionException(index, passages)
    }

    private fun ensureValidPassagesWithTonalityMarked() {
        ensureValidPassages()
        val passages = requirePassages()
        passages.forEachIndexed { index, passage ->
            if (passage.tonality == null) throw PassageTonalityNotMarkedException(this.index, index)
        }
    }

    internal fun requirePassages() = requireNotNull(passages)

    companion object {

        /**
         * Build a track using notes and time signatures
         */
        fun build(index: Int, name: String, notes: List<Note>, timeSignatures: List<TimeSignature>) = Track(
            index = index,
            name = name,
            bars = buildBars(notes, timeSignatures)
        )
    }
}
