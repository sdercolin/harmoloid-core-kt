plugins {
    kotlin("multiplatform") version "1.5.10"
    kotlin("plugin.serialization") version "1.5.0"
    id("org.jlleitschuh.gradle.ktlint") version "9.4.1"
    id("org.jetbrains.dokka") version "1.4.32"
    id("maven-publish")
    id("signing")
}

group = "com.sdercolin.harmoloid"
version = "1.3"

repositories {
    mavenCentral()
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    version.set("0.36.0")
}

val dokkaHtml by tasks.getting(org.jetbrains.dokka.gradle.DokkaTask::class)

val javadocJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
    dependsOn(dokkaHtml)
    archiveClassifier.set("javadoc")
    from(dokkaHtml.outputDirectory)
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        testRuns["test"].executionTask.configure {
            useJUnit()
        }
    }
    js(LEGACY) {
        browser {
            useCommonJs()
        }
    }
    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.1")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting
        val jvmTest by getting
        val jsMain by getting
        val jsTest by getting
        val nativeMain by getting
        val nativeTest by getting
    }
}

publishing {
    publications.withType<MavenPublication> {
        artifact(javadocJar)
        pom {
            name.set("harmoloid-core")
            description.set("Core library for HARMOLOID built in Kotlin/Multiplatform.")
            url.set("https://github.com/sdercolin/harmoloid-core-kt")
            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            developers {
                developer {
                    id.set("sdercolin")
                    name.set("sdercolin")
                    email.set("sder.colin@gmail.com")
                }
            }
            scm {
                connection.set("git@github.com:sdercolin/harmoloid-core-kt.git")
                developerConnection.set("git@github.com:sdercolin/harmoloid-core-kt.git")
                url.set("https://github.com/sdercolin/harmoloid-core-kt")
            }
        }
    }
    repositories {
        maven {
            val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
            credentials {
                username = properties["ossrhUsername"] as? String
                password = properties["ossrhPassword"] as? String
            }
        }
    }
}

signing {
    publishing.publications.withType<MavenPublication> {
        sign(this)
    }
}
