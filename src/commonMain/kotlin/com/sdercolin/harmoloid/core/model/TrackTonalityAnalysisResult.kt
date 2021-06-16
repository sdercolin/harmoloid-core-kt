package com.sdercolin.harmoloid.core.model

/**
 * Returned object from a auto/semi-auto tonality analysis
 */
sealed class TrackTonalityAnalysisResult {

    /**
     * The process is basically successful, but details inside have to be confirmed
     * before moving on for chorus generation. Because some passages may remain unmarked
     * with a certain tonality
     */
    data class Success(
        val passages: List<Passage>,
        val passageResults: List<PassageTonalityAnalysisResult>
    ) : TrackTonalityAnalysisResult()

    /**
     * The process is not finished successfully.
     * Use manual tonality marking instead
     */
    sealed class Failure : TrackTonalityAnalysisResult() {
        object TrackIsTooShort : Failure()
    }
}
