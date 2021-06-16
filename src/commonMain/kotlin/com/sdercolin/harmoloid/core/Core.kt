package com.sdercolin.harmoloid.core

import com.sdercolin.harmoloid.core.exception.PassageTonalityNotMarkedException
import com.sdercolin.harmoloid.core.model.Content
import com.sdercolin.harmoloid.core.model.HarmonicType
import com.sdercolin.harmoloid.core.model.NoteShift
import com.sdercolin.harmoloid.core.model.Passage
import com.sdercolin.harmoloid.core.model.Track
import com.sdercolin.harmoloid.core.model.TrackTonalityAnalysisResult
import com.sdercolin.harmoloid.core.process.analyzeTonalityAuto
import com.sdercolin.harmoloid.core.process.analyzeTonalitySemiAuto

/**
 * Main class for com.sdercolin.harmoloid.core process of HARMOLOID
 */
class Core(content: Content, config: Config? = null) {
    constructor(tracks: List<Track>, config: Config? = null) : this(Content(tracks), config)

    /**
     * Current state of project
     */
    var content: Content = content.ensureValid().initializePassagesIfNeeded()
        private set

    /**
     * Current config being used
     */
    var config: Config = config?.ensureValid() ?: Config()
        private set

    /**
     * Load a new project
     */
    fun load(content: Content) {
        this.content = content.ensureValid().initializePassagesIfNeeded()
    }

    /**
     * Reload a config
     */
    fun reloadConfig(config: Config = Config()) {
        this.config = config.ensureValid()
    }

    /**
     * Copy passages settings from the given track to all the other tracks
     * @param trackIndex index of the track to be copied
     */
    fun copyPassageSettingsToAllTracks(trackIndex: Int) {
        val passages = getTrack(trackIndex).requirePassages()
        content.tracks.indices.minus(trackIndex).forEach { index ->
            updateTrack(index) { track ->
                track.applyPassageSettings(passages)
            }
        }
    }

    /**
     * Automatically calculate passages division and tonalities
     * @param trackIndex index of the track to be processed
     */
    fun setPassagesAuto(trackIndex: Int): TrackTonalityAnalysisResult =
        setPassagesAutoOrSemiAuto(isFullAuto = true, trackIndex)

    /**
     * Calculate every passage that has been divided
     * @param trackIndex index of the track to be processed
     * @param passages divided passages, but the tonality settings will be ignored
     */
    fun setPassagesSemiAuto(trackIndex: Int, passages: List<Passage>): TrackTonalityAnalysisResult {
        updateTrack(trackIndex) { track ->
            track.copy(passages = passages.map { it.clearedForAnalysis })
        }
        return setPassagesAutoOrSemiAuto(isFullAuto = false, trackIndex)
    }

    /**
     * Save passage settings.
     * Call this function before exiting passage edition
     * @param trackIndex index of the track to be processed
     * @return true if all passages are marked with tonality
     */
    fun savePassages(trackIndex: Int, passages: List<Passage>): Boolean {
        updateTrack(trackIndex) { it.copy(passages = passages) }
        return getTrack(trackIndex).isTonalityMarked
    }

    /**
     * Save harmonic settings
     * @param trackIndex index of the track to be processed
     * @param harmonicTypes types of chorus that you need to generate on this track
     */
    fun saveHarmonicTypes(trackIndex: Int, harmonicTypes: Set<HarmonicType>) {
        updateTrack(trackIndex) { track -> track.copy(harmonies = harmonicTypes.sortedBy { it.ordinal }.toSet()) }
    }

    /**
     * Get a map of generated chorus from a track with harmonic type as the key.
     * Make sure the track has been setup with tonality,
     * otherwise a [PassageTonalityNotMarkedException] will be thrown
     * @param trackIndex index of the track to be processed
     */
    fun getAllChorusTracks(trackIndex: Int): Map<HarmonicType, List<NoteShift>> {
        val track = getTrack(trackIndex)
        return track.harmonies.orEmpty().map {
            it to track.getNoteShifts(it, config)
        }.toMap()
    }

    private fun setPassagesAutoOrSemiAuto(
        isFullAuto: Boolean,
        trackIndex: Int
    ): TrackTonalityAnalysisResult {
        val track = getTrack(trackIndex)
        val result = if (isFullAuto) analyzeTonalityAuto(track, config) else analyzeTonalitySemiAuto(track, config)
        val newTrack = when (result) {
            is TrackTonalityAnalysisResult.Success -> track.copy(passages = result.passages)
            is TrackTonalityAnalysisResult.Failure -> track
        }
        updateTrack(trackIndex) { newTrack }
        return result
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
