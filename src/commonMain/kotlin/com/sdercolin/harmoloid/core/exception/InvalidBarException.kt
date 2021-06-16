package com.sdercolin.harmoloid.core.exception

import com.sdercolin.harmoloid.core.model.Bar

abstract class InvalidBarException(val trackIndex: Int, val bar: Bar, detailMessage: String) : Exception(
    "Invalid bar found in track $trackIndex: $bar. $detailMessage"
) {
    class InvalidLength(trackIndex: Int, bar: Bar) : InvalidBarException(
        trackIndex, bar, "Length of a bar should be positive."
    )
}
