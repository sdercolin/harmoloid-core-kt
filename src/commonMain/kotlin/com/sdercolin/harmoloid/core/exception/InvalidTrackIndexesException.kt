package com.sdercolin.harmoloid.core.exception

class InvalidTrackIndexesException(val trackIndexes: List<Int>) : Exception(
    "Invalid indexes of track found: $trackIndexes."
)
