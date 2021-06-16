package com.sdercolin.harmoloid.core.exception

class PassageTonalityNotMarkedException(val trackIndex: Int, val passageIndex: Int) : Exception(
    "Passage $passageIndex in track $trackIndex is not marked with tonality."
)
