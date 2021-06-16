package com.sdercolin.harmoloid.core.exception

import com.sdercolin.harmoloid.core.model.Bar

class BarOverlappingException(val trackIndex: Int, val bars: List<Bar>) : Exception(
    "Overlapped bars found in track $trackIndex: $bars."
)
