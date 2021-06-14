package com.sdercolin.harmoloid.core

import com.sdercolin.harmoloid.core.Config.Companion.keyShiftForUpperThirdHarmonyDefault
import com.sdercolin.harmoloid.core.Config.Companion.keyShiftForUpperThirdHarmonyStandard
import com.sdercolin.harmoloid.core.model.Tonality
import com.sdercolin.harmoloid.core.model.TonalityCertainty
import kotlinx.serialization.Serializable

/**
 * Config object for HARMOLOID's all process
 *
 * @param minLengthRatioOfNoteForValidBar
 * For a bar, if all of its notes occupy less percentage in length than this value, it is
 * counted as an invalid bar. An invalid bar is taken as an "empty bar" which has no melodic
 * contents when build passages automatically.
 * Valid range: [0, 1] (Double)
 *
 * @param minProbabilityForCertainTonality
 * During tonality estimation for a passage, if the probabilities for all tonalities are
 * smaller than this value, the estimation result is considered unreliable.
 * Valid range: [0, 1] (Double)
 *
 * @param maxProbabilityDifferenceForSimilarlyCertainTonalities
 * During tonality estimation for a passage, if the difference between probabilities of two
 * tonalities is greater than this value, they are considered differently certain.
 * Otherwise, the two tonalities are considered to be similarly probable as the result.
 * Valid range: [0, 1] (Double)
 *
 * @param minUncertaintyForInvalidAnalysisResult
 * During tonality estimation for a passage, we calculated a value of "uncertainty" for the
 * result (A result is represented as Map<[Tonality], [TonalityCertainty]>). Only if the
 * uncertainty is smaller than this value, the result is considered reliable.
 * Valid range: [0, 11] (Int)
 *
 * @param minScoreForBarBelongingToPassage
 * During automatic passage building, we add bar one by one to the current passage.
 * Before that, a score is calculated to estimate if the bar should belong to the current
 * passage. If the score is lower than this value, the current passage is finished and a new
 * passage is created with the next several bars.
 * Valid range: [0, 1] (Double)
 *
 * @param minBarCountForPassageAutoDivision
 * During automatic passage building, we use this value to make sure a passage is long enough
 * to be melodic. If not, it will be merged to other passages.
 * Valid range: [1, 64] (Int)
 *
 * @param keyShiftForUpperThirdHarmony
 * Harmonic shifting values for upper third harmony.
 * Indexes represent keys relative to the current tonality (0=Do, 1=Do#, 2=Re, ...).
 * Values represent how many keys should be shifted for the index key.
 * The List length should be 12.
 * Every value should be in [0, 11] (Int)
 * Two default values are given here:
 * [keyShiftForUpperThirdHarmonyStandard] gives a definite upper third harmony,
 * while [keyShiftForUpperThirdHarmonyDefault] includes some arrangement to be more universal.
 *
 * @param keyShiftForLowerThirdHarmony
 * Similar as [keyShiftForUpperThirdHarmony], for lower third harmony.
 * The List length should be 12.
 * Every value should be in [-11, 0] (Int)
 *
 * @param validSolfegeSyllablesInOctave
 * When evaluating probabilities of tonalities, we count the occurrence in length of valid
 * notes defined in this list.
 * The default value gives a list of all the solfege syllables in a major scale.
 * Every item in the list should be in [0, 11] (Int)
 */
@Serializable
data class Config(
    val minLengthRatioOfNoteForValidBar: Double = 0.25,
    val minProbabilityForCertainTonality: Double = 0.1,
    val maxProbabilityDifferenceForSimilarlyCertainTonalities: Double = 0.03,
    val minUncertaintyForInvalidAnalysisResult: Int = 3,
    val minScoreForBarBelongingToPassage: Double = 0.5,
    val minBarCountForPassageAutoDivision: Int = 4,
    val keyShiftForUpperThirdHarmony: List<Int> = keyShiftForUpperThirdHarmonyDefault,
    val keyShiftForLowerThirdHarmony: List<Int> = keyShiftForLowerThirdHarmonyDefault,
    val validSolfegeSyllablesInOctave: List<Int> = validSolfegeSyllablesInOctaveDefault
) {
    companion object {
        val keyShiftForUpperThirdHarmonyDefault = listOf(4, 3, 5, 4, 3, 4, 3, 5, 4, 3, 4, 3)
        val keyShiftForUpperThirdHarmonyStandard = listOf(4, 3, 3, 4, 3, 4, 3, 4, 3, 3, 4, 3)
        val keyShiftForLowerThirdHarmonyDefault = listOf(-5, -2, -3, -3, -4, -3, -4, -3, -4, -4, -3, -4)
        val keyShiftForLowerThirdHarmonyStandard = listOf(-3, -2, -3, -3, -4, -3, -4, -3, -4, -4, -3, -4)
        val validSolfegeSyllablesInOctaveDefault = listOf(0, 2, 4, 5, 7, 9, 11)
    }
}
