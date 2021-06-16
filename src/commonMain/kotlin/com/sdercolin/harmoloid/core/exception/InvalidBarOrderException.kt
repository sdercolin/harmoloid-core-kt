package com.sdercolin.harmoloid.core.exception

import com.sdercolin.harmoloid.core.model.Bar

class InvalidBarOrderException(val trackIndex: Int, val bars: List<Bar>) : Exception(
    "Invalid bar order (by tickOn) found in track $trackIndex: $bars"
)
