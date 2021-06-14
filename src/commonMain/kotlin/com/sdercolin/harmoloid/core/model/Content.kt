package com.sdercolin.harmoloid.core.model

import com.sdercolin.harmoloid.core.util.update

/**
 * This class contains everything that the app needs to handle
 */
data class Content(
    val tracks: List<Track>
) {
    fun getTrack(index: Int): Track {
        return tracks.getOrNull(index) ?: throw Exception("Illegal track index $index")
    }

    internal fun updateTrack(index: Int, updater: (Track) -> Track): Content {
        val track = getTrack(index)
        return copy(tracks = tracks.update(track, updater))
    }

    internal fun initializePassagesIfNeeded() = copy(
        tracks = tracks.map { it.passagesInitializedIfNeeded() }
    )
}
