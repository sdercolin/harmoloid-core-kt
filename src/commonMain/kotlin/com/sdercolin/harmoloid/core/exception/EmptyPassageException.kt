package com.sdercolin.harmoloid.core.exception

class EmptyPassageException(val trackIndex: Int, val passageIndex: Int) : Exception(
    "Passage $passageIndex in track $trackIndex is empty."
)
