package com.sdercolin.harmoloid.core

import com.sdercolin.harmoloid.core.model.Content
import com.sdercolin.harmoloid.core.model.HarmonicType
import com.sdercolin.harmoloid.core.model.Passage
import com.sdercolin.harmoloid.core.model.Track
import com.sdercolin.harmoloid.core.process.TonalityAnalysisResult
import com.sdercolin.harmoloid.core.process.analyzeTonalityAuto
import com.sdercolin.harmoloid.core.process.analyzeTonalitySemiAuto

/**
 * Main class for com.sdercolin.harmoloid.core process of HARMOLOID
 */
class Core(content: Content, config: Config? = null) {

    /**
     * Current state of project
     */
    var content: Content = content.initializePassagesIfNeeded()
        private set

    /**
     * Current config being used
     */
    var config: Config = config ?: Config()
        private set

    /**
     * Load a new project
     */
    fun load(content: Content) {
        this.content = content.initializePassagesIfNeeded()
    }

    /**
     * Reload a config
     */
    fun reloadConfig(config: Config = Config()) {
        this.config = config
    }

    /**
     * Copy passages settings from the given track to all the other tracks
     * @param trackIndex index of the track to be copied
     */
    fun copyPassageSettingsToAllTracks(trackIndex: Int) {
        val passages = getTrack(trackIndex).passages!!
        content.tracks.indices.minus(trackIndex).forEach { index ->
            updateTrack(index) { track ->
                track.applyPassageSettings(passages)
            }
        }
    }

    /**
     * Automatically calculate passages division and tonalities
     * @return certain failure happened or `null` if everything is ok
     */
    fun setPassagesAuto(trackIndex: Int): TonalityAnalysisResult.Failure? =
        setPassagesAutoOrSemiAuto(isFullAuto = true, trackIndex)

    /**
     * Calculate every passage that has been divided
     * @param passages divided passages, but the tonality settings will be ignored
     * @return certain failure happened or `null` if everything is ok
     */
    fun setPassagesSemiAuto(trackIndex: Int, passages: List<Passage>): TonalityAnalysisResult.Failure? {
        updateTrack(trackIndex) { track ->
            track.copy(passages = passages.map { it.clearedForAnalysis })
        }
        return setPassagesAutoOrSemiAuto(isFullAuto = false, trackIndex)
    }

    /**
     * Save passage settings.
     * Call this function before exiting passage edition
     * @return true if all passages are marked with tonality
     */
    fun savePassages(trackIndex: Int, passages: List<Passage>): Boolean {
        updateTrack(trackIndex) { it.copy(passages = passages) }
        return getTrack(trackIndex).isTonalityMarked
    }

    /**
     * Save harmonic settings
     */
    fun saveHarmonics(trackIndex: Int, harmonicTypes: Set<HarmonicType>) {
        updateTrack(trackIndex) { track -> track.copy(harmonies = harmonicTypes.sortedBy { it.ordinal }.toSet()) }
    }

    private fun setPassagesAutoOrSemiAuto(
        isFullAuto: Boolean,
        trackIndex: Int
    ): TonalityAnalysisResult.Failure? {
        var maybeFailure: TonalityAnalysisResult.Failure? = null
        updateTrack(trackIndex) {
            val result =
                if (isFullAuto) analyzeTonalityAuto(it, config)
                else analyzeTonalitySemiAuto(it, config)
            when (result) {
                is TonalityAnalysisResult.Success -> result.track
                is TonalityAnalysisResult.Failure -> {
                    maybeFailure = result
                    it
                }
            }
        }
        return maybeFailure
    }

    private fun getTrack(trackIndex: Int) = content.getTrack(trackIndex)

    private fun updateTrack(trackIndex: Int, updater: (Track) -> Track): Boolean {
        val newContent = content.updateTrack(trackIndex) {
            updater.invoke(it)
        }
        val changed = newContent != content
        this.content = newContent
        return changed
    }
}
