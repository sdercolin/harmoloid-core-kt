package com.sdercolin.harmoloid.core.model

enum class HarmonicType(val simpleName: String) {
    Copy("copy"),
    UpperThird("+3rd"),
    LowerThird("-3rd"),
    UpperSixth("+6th"),
    LowerSixth("-6th"),
    UpperOctave("+8th"),
    LowerOctave("-8th");

    fun getHarmonicTrackName(trackName: String): String {
        return "$trackName $simpleName"
    }
}
