package com.sdercolin.harmoloid.core.model

import com.sdercolin.harmoloid.core.exception.InvalidTrackIndexesException
import com.sdercolin.harmoloid.core.exception.TrackNotExistingException
import com.sdercolin.harmoloid.core.util.update

/**
 * This class contains everything that the app needs to handle
 */
data class Content(
    val tracks: List<Track>
) {
    fun getTrack(index: Int): Track {
        return tracks.getOrNull(index) ?: throw TrackNotExistingException(index)
    }

    internal fun updateTrack(index: Int, updater: (Track) -> Track): Content {
        val track = getTrack(index)
        return copy(tracks = tracks.update(track, updater))
    }

    internal fun initializePassagesIfNeeded() = copy(
        tracks = tracks.map { it.passagesInitializedIfNeeded() }
    )

    internal fun ensureValid(): Content {
        val trackIndexes = tracks.map { it.index }
        if (trackIndexes != tracks.indices.toList()) {
            throw InvalidTrackIndexesException(trackIndexes)
        }
        tracks.forEach { it.ensureValid() }
        return this
    }
}
