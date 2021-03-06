package com.sdercolin.harmoloid.core.model

import com.sdercolin.harmoloid.core.Config
import com.sdercolin.harmoloid.core.util.sumByLong

/**
 * A range of bars which has a same tonality.
 * A passage is the smallest unit for tonality analysis or harmony calculation
 * @param tonalityCertainties Show result of a tonality analysis. Do not set this parameter manually.
 * @param tonality Can be set by tonality analysis or manually.
 */
data class Passage(
    val index: Int,
    val bars: List<Bar>,
    val tonalityCertainties: Map<Tonality, TonalityCertainty>? = null,
    val tonality: Tonality? = null
) {
    val number get() = index + 1
    val notes get() = bars.flatMap { it.notes }

    internal val validLength get() = bars.sumByLong { it.validLength }
    internal val isAtonal: Boolean get() = tonalityCertainties?.isEmpty() != false
    internal val isCertain: Boolean
        get() = tonalityCertainties?.count { it.value == TonalityCertainty.Certain } == 1

    internal fun getAnalysisResult(): PassageTonalityAnalysisResult {
        return when {
            isCertain -> PassageTonalityAnalysisResult.Certain(tonality!!)
            isAtonal -> PassageTonalityAnalysisResult.Unknown
            else -> PassageTonalityAnalysisResult.SimilarlyCertain(
                tonalityCertainties!!.entries
                    .sortedByDescending { it.value }
                    .map { it.key }
            )
        }
    }

    internal fun getNoteShifts(harmonicType: HarmonicType, config: Config): List<NoteShift> {
        val tonality = requireNotNull(tonality)
        if (tonality.isMelodic.not()) return listOf()
        return when (harmonicType) {
            HarmonicType.Copy -> {
                notes.map { NoteShift(it.index, 0) }
            }
            HarmonicType.UpperThird -> {
                notes.map {
                    NoteShift(
                        it.index,
                        config.keyShiftForUpperThirdHarmony[it.getKeyRelativeToTonality(tonality)]
                    )
                }
            }
            HarmonicType.LowerThird -> {
                notes.map {
                    NoteShift(
                        it.index,
                        config.keyShiftForLowerThirdHarmony[it.getKeyRelativeToTonality(tonality)]
                    )
                }
            }
            HarmonicType.UpperSixth -> {
                notes.map {
                    NoteShift(
                        it.index,
                        config.keyShiftForLowerThirdHarmony[it.getKeyRelativeToTonality(tonality)] + KEY_IN_OCTAVE
                    )
                }
            }
            HarmonicType.LowerSixth -> {
                notes.map {
                    NoteShift(
                        it.index,
                        config.keyShiftForUpperThirdHarmony[it.getKeyRelativeToTonality(tonality)] - KEY_IN_OCTAVE
                    )
                }
            }
            HarmonicType.UpperOctave -> {
                notes.map { NoteShift(it.index, KEY_IN_OCTAVE) }
            }
            HarmonicType.LowerOctave -> {
                notes.map { NoteShift(it.index, -KEY_IN_OCTAVE) }
            }
        }
    }

    internal val clearedForAnalysis: Passage
        get() = copy(tonality = null, tonalityCertainties = null)

    internal fun takeCertainTonality(): Passage =
        when {
            isCertain -> copy(
                tonality = tonalityCertainties!!.entries.find { it.value == TonalityCertainty.Certain }!!.key
            )
            isAtonal -> copy(tonality = Tonality.Atonal)
            else -> this
        }
}
