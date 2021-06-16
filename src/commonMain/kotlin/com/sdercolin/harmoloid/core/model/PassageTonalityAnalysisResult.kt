package com.sdercolin.harmoloid.core.model

/**
 * Included in [TrackTonalityAnalysisResult.Success], representing detail result of each passge
 */
sealed class PassageTonalityAnalysisResult {
    /**
     * Single option of tonality is detected.
     * The passage is already marked with this tonality.
     * Basically it's fine to use this result without confirmation from the user
     */
    data class Certain(val tonality: Tonality) : PassageTonalityAnalysisResult()

    /**
     * Several options of tonality are detected.
     * The user has to be notified with this result and choose one of the options
     */
    data class SimilarlyCertain(val tonalities: List<Tonality>) : PassageTonalityAnalysisResult()

    /**
     * No possible tonality option is detected.
     * The passage is marked atonal and expelled from chorus generation.
     */
    object Unknown : PassageTonalityAnalysisResult()
}
