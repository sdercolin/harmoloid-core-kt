package com.sdercolin.harmoloid.core.exception

class InvalidNoteIndexesException(val trackIndex: Int, val noteIndexes: List<Int>) : Exception(
    "Invalid indexes of note found in track $trackIndex: $noteIndexes."
)
