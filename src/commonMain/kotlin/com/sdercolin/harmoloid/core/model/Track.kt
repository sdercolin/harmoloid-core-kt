package com.sdercolin.harmoloid.core.model

import com.sdercolin.harmoloid.core.Config
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
        ensureValidPassageWithTonalityMarked()
        return requirePassages().flatMap { it.getNoteShifts(harmonicType, config) }
    }

    internal fun applyPassageSettings(originPassages: List<Passage>): Track {
        val passages = originPassages.mapNotNull { originPassage ->
            val originBarIndexes = originPassage.bars.map { it.index }
            val barIndexes = this.bars.indices.intersect(originBarIndexes)
                .takeUnless { it.isEmpty() } ?: return@mapNotNull null
            val bars = this.bars.subList(barIndexes.first(), barIndexes.last() + 1)
            originPassage.copy(bars = bars)
        }
        return copy(passages = passages)
    }

    private fun ensureValidPassageDivision() {
        val passages = requirePassages()
        if (passages.any { it.bars.isEmpty() }) throw Exception("Empty passage existing: $passages")
        if (passages.flatMap { it.bars } != bars) throw Exception("Invalid passage division: $passages")
    }

    private fun ensureValidPassageWithTonalityMarked() {
        val passages = requirePassages()
        ensureValidPassageDivision()
        if (passages.any { it.tonality == null }) throw Exception("Passage without tonality existing: $passages")
    }

    private fun requirePassages() = passages ?: throw Exception("Passages have not been set")

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
