package com.sdercolin.harmoloid.core.exception

import com.sdercolin.harmoloid.core.model.Passage

class InvalidPassageDivisionException(val trackIndex: Int, val passages: List<Passage>) : Exception(
    "Passage division is invalid in track $trackIndex: $passages. " +
            "Please make sure passages own all the bars in a correct order."
)
