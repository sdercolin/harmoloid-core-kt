package com.sdercolin.harmoloid.core.exception

class InvalidBarIndexesException(val trackIndex: Int, val barIndexes: List<Int>) : Exception(
    "Invalid indexes of bar found in track $trackIndex: $barIndexes."
)
