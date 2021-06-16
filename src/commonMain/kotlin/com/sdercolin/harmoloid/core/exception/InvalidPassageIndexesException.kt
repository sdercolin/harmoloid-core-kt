package com.sdercolin.harmoloid.core.exception

class InvalidPassageIndexesException(val trackIndex: Int, val passageIndexes: List<Int>) : Exception(
    "Invalid indexes of passage found in track $trackIndex: $passageIndexes."
)
