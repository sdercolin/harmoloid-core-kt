# HARMOLOID Core
[![Maven Central](https://img.shields.io/maven-central/v/com.sdercolin.harmoloid/harmoloid-core/1.0)](https://search.maven.org/artifact/com.sdercolin.harmoloid/harmoloid-core/1.0/pom)

Core library for HARMOLOID built in Kotlin/Multiplatform.

## What is HARMOLOID?

HARMOLOID is an application for generating simple chorus based on projects of singing voice synthesizer softwares.

The [first version](https://github.com/sdercolin/HARMOLOID) `1.x` is built as a WinForm application and a new web application is being developed now.

## The core library

This project is a Kotlin implementation of the core algorithm of HARMOLOID. It can be applied on any MIDI-like formats with external IO modules.

### Basic features
- Tonality analysis (auto: with detection of modulation)
- Tonality analysis (semi-auto: for single tonality)
- Note shift based on tonality and harmonic type
- Parameter configuration

## Getting Started

This library targets `jvm`, `js`, and `native`. Basically you can use it in any Kotlin Gradle project or Java Gradle project.

Kotlin DSL:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("com.sdercolin.harmoloid:harmoloid-core:x.y")
}
```

Groovy DSL:

```gradle
repositories {
    mavenCentral()
}

dependencies {
    implementation "com.sdercolin.harmoloid:harmoloid-core:x.y"
}
```

### Basic usages

#### Initialization

You have to implement IO modules to handle data tranformation between the original structure and HARMOLOID structure.

```kotlin

// Extract content from the MIDI-like data
val tracks = Track.build(index, name, notes, timeSignatures)
val content = Content(tracks)

// Initialize core object
val core = Core(content)

```

#### Configuration (optional) 

You can configure all the parameters used. See [Config.kt](https://github.com/sdercolin/harmoloid-core-kt/blob/main/src/commonMain/kotlin/com/sdercolin/harmoloid/core/Config.kt) for details.

```kotlin
val config = Config(
    keyShiftForUpperThirdHarmony = Config.keyShiftForUpperThirdHarmonyStandard,
    keyShiftForLowerThirdHarmony = Config.keyShiftForLowerThirdHarmonyStandard
)

// Pass config to constructor
val core = Core(content, config)

// Or reload config later
core.reloadConfig(config)

```

#### Setup tonality

Tracks have to be marked with tonality before harmony generation.

```kotlin

val track = core.getTrack(trackIndex)
val bar = track.bars

// Method 1: auto
val maybeFailure = core.setPassagesAuto(trackIndex)
if (maybeFailure != null) {
    // Handle error
}

// Method 2: semi-auto, have to construct passages by yourself
val passages = listOf(
    Passage(index = 0, bars = bars.subList(0, 20)),
    Passage(index = 1, bars = bars.subList(21, bars.size))
)
val maybeFailure = core.setPassagesSemiAuto(trackIndex, passages)
if (maybeFailure != null) {
    // Handle error
}

// Method 3: manual, have to construct passages with tonalities by yourself 
val passages = listOf(
    Passage(index = 0, bars = bars.subList(0, 20), tonality = Tonality.C),
    Passage(index = 1, bars = bars.subList(21, bars.size), tonality = Tonality.D)
)
core.savePassages(trackIndex, passages)

// Check if track is setup
if (!track.isTonalityMarked) {
    track.passages.forEach {
        val tonalityCertainties = it.tonalityCertainties
        // Notify user with the analysis result and do Method 3 again
    }
}

```

#### Get harmonic tracks

```kotlin

// (Optional) Save harmonic settings
core.saveHarmonics(trackIndex, setOf(HarmonicType.UpperThird, HarmonicType.LowerThird))

// Get note shifts
for (harmony in track.harmonies) {
    val noteShifts = track.getNoteShifts(harmony, core.config).map { it.id to it.keyDelta }.toMap()
    
    // Do your output work with the note shifts
    // The following code is an example with a pseudo NoteElement model
    val shiftedNoteElements = noteElements.mapNotNull {
        val keyDelta = noteShifts[it.index]
        if (keyDelta == null) {
            // delete the note
            null
        } else {
            it.copy(key = it.key + keyDelta)
        }
    }
}

```


## License

[Apache License, Version 2.0](https://github.com/sdercolin/harmoloid-core-kt/blob/main/LICENSE.md)
