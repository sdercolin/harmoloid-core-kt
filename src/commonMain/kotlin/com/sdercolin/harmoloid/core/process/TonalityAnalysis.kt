package com.sdercolin.harmoloid.core.process

import com.sdercolin.harmoloid.core.Config
import com.sdercolin.harmoloid.core.model.Bar
import com.sdercolin.harmoloid.core.model.KEY_IN_OCTAVE
import com.sdercolin.harmoloid.core.model.Passage
import com.sdercolin.harmoloid.core.model.Tonality
import com.sdercolin.harmoloid.core.model.TonalityCertainty
import com.sdercolin.harmoloid.core.model.Track
import com.sdercolin.harmoloid.core.process.TrackTonalityAnalysisResult.Failure
import com.sdercolin.harmoloid.core.process.TrackTonalityAnalysisResult.Success
import com.sdercolin.harmoloid.core.util.sumByLong
import com.sdercolin.harmoloid.core.util.update

sealed class TrackTonalityAnalysisResult {
    data class Success(
        val passages: List<Passage>,
        val passageResults: List<PassageTonalityAnalysisResult>
    ) : TrackTonalityAnalysisResult()

    sealed class Failure : TrackTonalityAnalysisResult() {
        object TrackIsTooShort : Failure()
    }
}

sealed class PassageTonalityAnalysisResult {
    data class Certain(val tonality: Tonality) : PassageTonalityAnalysisResult()
    data class SimilarlyCertain(val tonalities: List<Tonality>) : PassageTonalityAnalysisResult()
    object Unknown : PassageTonalityAnalysisResult()
}

internal fun analyzeTonalityAuto(track: Track, config: Config): TrackTonalityAnalysisResult {
    val bars = track.bars
    val barTotal = bars.count()
    var passages = listOf<Passage>()
    var barPos = -1
    val minBarCount = config.minBarCountForPassageAutoDivision

    // divide passages
    for (passageIndex in bars.indices) {
        barPos++
        var firstBarIndex = barPos
        var lastBarIndex = bars.lastIndex
        if (barPos > 0 && !bars[barPos - 1].isValid(config)) {
            firstBarIndex--
            passages = passages.update<Passage>(passageIndex - 1) { it.copy(bars = it.bars.dropLast(1)) }
            barPos--
        }
        if (barPos + minBarCount >= barTotal) {
            val newPassage = Passage(
                index = passageIndex,
                bars = bars.subList(firstBarIndex, lastBarIndex + 1)
            ).estimateTonality(config)

            passages = passages + newPassage
            break
        }
        while (bars[barPos].isEmpty || !bars[barPos].isValid(config)) {
            barPos++
        }
        if (barPos + minBarCount - 1 >= barTotal) {
            return Failure.TrackIsTooShort
        }
        barPos += minBarCount - 1
        lastBarIndex = barPos
        var newPassage = Passage(
            index = passageIndex,
            bars = bars.subList(firstBarIndex, lastBarIndex + 1)
        )
        while (barPos < barTotal - 1) {
            barPos++
            newPassage = newPassage.estimateTonality(config)
            val barBelongsToPassage = bars[barPos].estimateIfBelongToPassage(newPassage, config)
            if (barBelongsToPassage) {
                lastBarIndex++
                newPassage = newPassage.copy(
                    bars = bars.subList(firstBarIndex, lastBarIndex + 1)
                )
            } else {
                barPos--
                passages = passages + newPassage
                break
            }
        }
        if (barPos == barTotal - 1) {
            passages = passages + newPassage
            break
        }
    }

    // combine passages with same results
    passages = passages.fold(listOf()) { acc, passage ->
        val previous = acc.lastOrNull()
        when {
            previous == null -> acc + passage
            previous.tonalityCertainties == passage.tonalityCertainties -> {
                acc.dropLast(1) + previous.copy(bars = previous.bars + passage.bars)
            }
            else -> acc + passage
        }
    }

    // Assign correct indexes
    passages = passages.mapIndexed { index, passage -> passage.copy(index = index) }

    // Save certain results
    passages = passages.map { it.takeCertainTonality() }
    return Success(passages, passages.map { it.getAnalysisResult() })
}

internal fun analyzeTonalitySemiAuto(track: Track, config: Config): TrackTonalityAnalysisResult {
    var passages = track.passages ?: track.passagesInitialized().passages!!
    passages = passages
        .map { it.estimateTonality(config) }
        .map { it.takeCertainTonality() }
    return Success(passages, passages.map { it.getAnalysisResult() })
}

private fun Passage.estimateTonality(config: Config): Passage {
    val totalLength = validLength
    val solfegePercentages = MutableList(KEY_IN_OCTAVE) { solfege ->
        val lengthForThisSolfege = notes
            .filter { it.key % KEY_IN_OCTAVE == solfege }
            .sumByLong { it.length }
        lengthForThisSolfege.toDouble() / totalLength
    }

    var validSolfegeSyllables = config.validSolfegeSyllablesInOctave
    val tonalityProbabilities = MutableList(KEY_IN_OCTAVE) {
        var probability = 0.0
        if (it != 0) validSolfegeSyllables = validSolfegeSyllables.bumpUpSolfege()
        for (solfege in validSolfegeSyllables) {
            probability += solfegePercentages[solfege]
        }
        probability
    }

    val maxProbability = tonalityProbabilities.maxOrNull()!!
    if (maxProbability < config.minProbabilityForCertainTonality) {
        return this
    }

    val tonalityCertainties = mutableMapOf<Int, TonalityCertainty>()

    var uncertainty = -1
    for (tonality in tonalityProbabilities.indices) {
        val probability = tonalityProbabilities[tonality]
        if (probability == maxProbability) {
            tonalityCertainties[tonality] = TonalityCertainty.SamelyPossible
            uncertainty++
        } else if (maxProbability - probability <=
            config.maxProbabilityDifferenceForSimilarlyCertainTonalities
        ) {
            tonalityCertainties[tonality] = TonalityCertainty.Possible
            uncertainty++
        }
    }
    val mostPossibleTonality = tonalityProbabilities.indexOfFirst { it == maxProbability }
    when {
        uncertainty == 0 -> {
            tonalityCertainties[mostPossibleTonality] = TonalityCertainty.Certain
        }
        uncertainty >= config.minUncertaintyForInvalidAnalysisResult -> {
            tonalityCertainties.clear()
        }
        else -> {
            // Take the top 3 tonality results,
            // if they line up with the same distances of `Do` - `Fa` - `Sol`,
            // take the result with `Do`.

            val topThreePossibleTonalities = tonalityProbabilities.withIndex()
                .sortedByDescending { it.value }
                .take(3)
                .map { it.index }
                .sorted()

            val tonality1 = topThreePossibleTonalities[0]
            val tonality2 = topThreePossibleTonalities[1]
            val tonality3 = topThreePossibleTonalities[2]

            when {
                tonality1 + 5 == tonality2 || tonality2 + 2 == tonality3 -> {
                    tonalityCertainties.clear()
                    tonalityCertainties[tonality1] = TonalityCertainty.Certain
                }
                tonality2 + 5 == tonality3 || tonality3 + 2 == tonality1 + 12 -> {
                    tonalityCertainties.clear()
                    tonalityCertainties[tonality2] = TonalityCertainty.Certain
                }
                tonality3 + 5 == tonality1 + 12 || tonality1 + 2 == tonality2 -> {
                    tonalityCertainties.clear()
                    tonalityCertainties[tonality3] = TonalityCertainty.Certain
                }
            }
        }
    }
    return copy(tonalityCertainties = tonalityCertainties.mapKeys { Tonality.values()[it.key] })
}

private fun Bar.estimateIfBelongToPassage(passage: Passage, config: Config): Boolean {
    if (isEmpty) return true
    if (!isValid(config)) return true
    if (passage.isAtonal!!) return true

    val totalLength = validLength
    val solfegePercentages = MutableList(KEY_IN_OCTAVE) { solfege ->
        val lengthForThisSolfege = notes
            .filter { it.key % KEY_IN_OCTAVE == solfege }
            .sumByLong { it.length }
        lengthForThisSolfege.toDouble() / totalLength
    }

    if (passage.isCertain!!) {
        val passageTonality = passage.takeCertainTonality().tonality!!
        var probability = 0.0
        var validSolfegeSyllables = config.validSolfegeSyllablesInOctave
        repeat(passageTonality.ordinal) {
            validSolfegeSyllables = validSolfegeSyllables.bumpUpSolfege()
        }
        for (solfege in validSolfegeSyllables) {
            probability += solfegePercentages[solfege]
        }
        return probability >= config.minScoreForBarBelongingToPassage
    } else {
        var validSolfegeSyllables = config.validSolfegeSyllablesInOctave
        for (tonality in Tonality.values()) {
            if (tonality.ordinal != 0) validSolfegeSyllables = validSolfegeSyllables.bumpUpSolfege()
            passage.tonalityCertainties!![tonality] ?: continue
            var probability = 0.0
            for (solfege in validSolfegeSyllables) {
                probability += solfegePercentages[solfege]
            }
            if (probability >= config.minScoreForBarBelongingToPassage) return true
        }
        return false
    }
}

private fun List<Int>.bumpUpSolfege() = map { (it + 1) % 12 }.sorted()

private fun Bar.isValid(config: Config) =
    validLength.toDouble() / length >= config.minLengthRatioOfNoteForValidBar
