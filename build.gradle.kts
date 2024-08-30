// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()

        maven {
            url = uri("https://plugins.gradle.org/m2/")
            content {
                includeGroupByRegex("com\\.google.*")
                includeGroup("com.squareup")
                includeGroupByRegex("commons-.*")
                includeModule("org.jdom","jdom2")
                includeModule("org.ow2","ow2")
                includeGroup("org.ow2.asm")
                includeGroupByRegex("org\\.jetbrains.*")
                includeGroup("org.slf4j")
                includeModule("org.bitbucket.b_c","jose4j")
                includeModule("org.checkerframework","checker-qual")
                includeGroup("net.java.dev.jna")
                includeModule("net.java","jvnet-parent")
                includeModule("javax.annotation","javax.annotation-api")
                includeGroupByRegex("org\\.apache.*")
                includeGroupByRegex("com\\.sun.*")
                includeModule("xerces","xercesImpl")
                includeModule("xml-apis","xml-apis")
                includeGroup("org.bouncycastle")
                includeGroupByRegex("net\\.sf.*")
                includeModule("javax.inject","javax.inject")
                includeModule("org.tensorflow","tensorflow-lite-metadata")
                includeModule("org.json","json")
                includeGroup("io.grpc")
                includeGroup("io.netty")
                includeModule("io.perfmark","perfmark-api")
                includeModule("org.codehaus.mojo","animal-sniffer-annotations")
                includeGroup("org.glassfish.jaxb")
                includeGroupByRegex("jakarta.*")
                includeModule("org.jvnet.staxex","stax-ex")
                includeModule("gradle.plugin.com.browserstack.gradle","browserstack-gradle-plugin")
                includeGroup("com.testdroid")
                includeModule("log4j","log4j")
                includeModule("com.fasterxml","oss-parent")
                includeGroupByRegex("com\\.fasterxml\\.jackson.*")
                includeModule("com.neenbedankt.gradle.plugins","android-apt")
                includeModule("org.sonatype.oss","oss-parent")
                includeModule("org.eclipse.ee4j","project")
                includeGroup("org.codehaus.mojo")
            }
        }
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.5.2")
        classpath("com.neenbedankt.gradle.plugins:android-apt:1.8")
        classpath("com.testdroid:gradle:2.63.1")
        classpath("gradle.plugin.com.browserstack.gradle:browserstack-gradle-plugin:2.3.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.10")
    }
}

plugins {
    id("com.google.devtools.ksp") version "2.0.10-1.0.24" apply false
    id("org.jetbrains.kotlin.android") version "2.0.10" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()

        maven {
            url = uri("https://raw.githubusercontent.com/guardianproject/gpmaven/master")
            content {
                includeModule("org.proofmode", "android-libproofmode")
            }
        }

        maven {
            url = uri("https://jitpack.io")
            content {
                includeModule("com.github.esafirm","android-image-picker")
                includeModule("com.github.derlio","audio-waveform")
                includeModule("com.github.abdularis","circularimageview")
                includeModule("com.github.guardianproject","sardine-android")
            }
        }

        maven {
            url = uri("https://jcenter.bintray.com")
            content {
                includeModule("com.amulyakhare", "com.amulyakhare.textdrawable")
                includeModule("com.github.stfalcon", "frescoimageviewer")
                includeModule("me.relex", "photodraweeview")
            }
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}