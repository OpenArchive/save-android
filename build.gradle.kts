import io.gitlab.arturbosch.detekt.Detekt

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.testdroid:gradle:2.63.1")
        classpath("com.android.tools.build:gradle:8.7.1")
        classpath("com.neenbedankt.gradle.plugins:android-apt:1.8")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.10")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.8.3")
        classpath("gradle.plugin.com.browserstack.gradle:browserstack-gradle-plugin:2.3.1")
    }
}

plugins {
    id("io.gitlab.arturbosch.detekt") version "1.23.4" apply false
    id("org.jlleitschuh.gradle.ktlint") version "11.6.1" apply false
    id("com.google.devtools.ksp") version "2.0.10-1.0.24" apply false
    id("org.jetbrains.kotlin.android") version "2.0.10" apply false
    kotlin("plugin.serialization") version "1.8.10" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()

        maven {
            url = uri("https://raw.githubusercontent.com/guardianproject/gpmaven/master")
        }

        maven {
            url = uri("https://jitpack.io")
            content {
                includeModule("com.github.esafirm", "android-image-picker")
                includeModule("com.github.derlio", "audio-waveform")
                includeModule("com.github.abdularis", "circularimageview")
                includeModule("com.github.guardianproject", "sardine-android")
            }
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

tasks.withType<Detekt>().configureEach {
    reports {
        html.required.set(true) // observe findings in your browser with structure and code snippets
        xml.required.set(true) // checkstyle like format mainly for integrations like Jenkins
        sarif.required.set(true) // standardized SARIF format (https://sarifweb.azurewebsites.net/) to support integrations with GitHub Code Scanning
        md.required.set(true) // simple Markdown format
    }
}