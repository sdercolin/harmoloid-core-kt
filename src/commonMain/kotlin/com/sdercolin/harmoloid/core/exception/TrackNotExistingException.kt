package com.sdercolin.harmoloid.core.exception

class TrackNotExistingException(val trackIndex: Int) : Exception("Track $trackIndex is not existing.")
