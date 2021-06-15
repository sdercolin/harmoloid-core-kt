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

## License

[Apache License, Version 2.0](https://github.com/sdercolin/harmoloid-core-kt/blob/main/LICENSE.md)
