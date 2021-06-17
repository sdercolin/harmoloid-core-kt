# HARMOLOID Core

[![Maven Central](https://img.shields.io/maven-central/v/com.sdercolin.harmoloid/harmoloid-core/1.1)](https://search.maven.org/artifact/com.sdercolin.harmoloid/harmoloid-core/1.1/pom)

Core library for HARMOLOID built in [Kotlin Multiplatform](https://kotlinlang.org/docs/mpp-intro.html).

## What is HARMOLOID?

HARMOLOID is an application for generating simple chorus based on projects of singing voice synthesizers.

The [current version](https://github.com/sdercolin/harmoloid2) `2.x` of the application is built as a web application in Kotlin/JS.  

## The core library

This project is a Kotlin implementation of the core algorithm of HARMOLOID. It can be applied on any MIDI-like formats
with external IO modules.

### Basic features

- Tonality analysis (auto: with detection of modulation)
- Tonality analysis (semi-auto: for single tonality)
- Note shift based on tonality and harmonic type
- Parameter configuration

## Getting Started

This library targets `jvm`, `js`, and `native`. Basically you can use it in any Kotlin Gradle project or Java Gradle
project.

Kotlin DSL:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("com.sdercolin.harmoloid:harmoloid-core:1.1")
}
```

Groovy DSL:

```gradle
repositories {
    mavenCentral()
}

dependencies {
    implementation "com.sdercolin.harmoloid:harmoloid-core:1.1"
}
```

### Basic usages

#### Initialization

You have to implement IO modules to handle data tranformation between the original structure and HARMOLOID structure.

```kotlin
// Extract content from the MIDI-like data
val tracks = Track.build(index, name, notes, timeSignatures)

// Initialize core object
val core = Core(tracks)

```

#### Configuration (optional)

You can configure all the parameters used.
See [Config.kt](https://github.com/sdercolin/harmoloid-core-kt/blob/main/src/commonMain/kotlin/com/sdercolin/harmoloid/core/Config.kt)
for details.

```kotlin
val config = Config(
    keyShiftForUpperThirdHarmony = Config.keyShiftForUpperThirdHarmonyStandard,
    keyShiftForLowerThirdHarmony = Config.keyShiftForLowerThirdHarmonyStandard
)

// Pass config to constructor
val core = Core(tracks, config)

// Or reload config later
core.reloadConfig(config)
```

#### Setup tonality

Tracks have to be marked with tonality before harmony generation.

```kotlin
val track = core.getTrack(trackIndex)
val bar = track.bars

// Method 1: auto
when (val result = core.setPassagesAuto(trackIndex)) {
    is TrackTonalityAnalysisResult.Success -> {
        val passageResults = result.passageResults
        // Notify results
    }
    is TrackTonalityAnalysisResult.Failure -> {
        // Notify error
    }
}

// Method 2: semi-auto, have to construct passages by yourself
val passages = listOf(
    Passage(index = 0, bars = bars.subList(0, 20)),
    Passage(index = 1, bars = bars.subList(21, bars.size))
)
when (val result = core.setPassagesSemiAuto(trackIndex, passages)) {
    is TrackTonalityAnalysisResult.Success -> {
        val passageResults = result.passageResults
        // Notify results
    }
    is TrackTonalityAnalysisResult.Failure -> {
        // Notify error
    }
}

// Method 3: manual, have to construct passages with tonalities by yourself 
val passages = listOf(
    Passage(index = 0, bars = bars.subList(0, 20), tonality = Tonality.C),
    Passage(index = 1, bars = bars.subList(21, bars.size), tonality = Tonality.D)
)
core.savePassages(trackIndex, passages)

// Check if track is setup
if (!core.getTrack(trackIndex).isTonalityMarked) {
    // Tonality is not fully analysed or set, check result given by Method 1 or Method 2
    // and do Method 3 again
}
```

#### Generate chorus tracks

```kotlin
// Save harmonic settings
core.saveHarmonicTypes(trackIndex, setOf(HarmonicType.UpperThird, HarmonicType.LowerThird))

// Get note shifts
core.getAllChorusTracks(trackIndex).forEach { (harmony, noteShifts) ->
    // Do your output work with the note shifts
    // The following code is an example with a pseudo NoteElement model
    val shiftedNoteElements = noteElements.mapNotNull { noteElement ->
        val keyDelta = noteShifts.find { it.noteIndex == noteElement }?.keyDelta
        if (keyDelta == null) {
            // delete the note
            null
        } else {
            noteElement.copy(key = noteElement.key + keyDelta)
        }
    }
}
```

## License

[Apache License, Version 2.0](https://github.com/sdercolin/harmoloid-core-kt/blob/main/LICENSE.md)
