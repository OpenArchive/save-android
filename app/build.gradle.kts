import java.io.FileOutputStream
import java.util.Properties

plugins {
    id("kotlin-android")
    id("com.android.application")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.android")
    id("androidx.navigation.safeargs.kotlin")
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.10"
    kotlin("plugin.serialization") version "1.8.10"
    id("kotlin-parcelize")
}

android {
    val patch: String
    val major: String
    val minor: String
    val newVersionCode: String

    val localPropsFile = file("../local.properties")
    val localProps = Properties()
    if (!localPropsFile.canRead()) {
        throw GradleException("Could not read local.properties!")
    }
    localProps.load(localPropsFile.inputStream())

    val versionPropsFile = file("version.properties")
    val versionProps = Properties()
    if (!versionPropsFile.canRead()) {
        throw GradleException("Could not read version.properties!")
    }
    versionProps.load(versionPropsFile.inputStream())

    patch = versionProps["PATCH"] as String? ?: ""
    major = versionProps["MAJOR"] as String? ?: ""
    minor = versionProps["MINOR"] as String? ?: ""
    newVersionCode = versionProps["VERSION_CODE"] as String? ?: "0"

    versionProps["VERSION_CODE"] = (newVersionCode.toInt() + 1).toString()

    versionProps.store(FileOutputStream(versionPropsFile), null)

    val newVersionName = "${major}.${minor}.${patch}.${newVersionCode}"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    signingConfigs {
    }

    compileSdk = 34

    defaultConfig {
        applicationId = "net.opendasharchive.openarchive"
        minSdk = 28
        targetSdk = 34
        versionCode = newVersionCode.toInt()
        versionName = newVersionName
        project.setProperty("archivesBaseName", "save-$versionName")
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        resValue("string", "mixpanel_key", localProps.getProperty("mixpanel.key") ?: "")
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
            isUniversalApk = true
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            isMinifyEnabled = false
        }
        getByName("release") {
            applicationIdSuffix = ".release"
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    packaging {
        resources {
            excludes += listOf("META-INF/LICENSE.txt", "META-INF/NOTICE.txt", "META-INF/LICENSE", "META-INF/NOTICE", "META-INF/DEPENDENCIES", "LICENSE.txt")
        }
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
        compose = true
    }

    lint {
        abortOnError = false
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.13"
    }

    namespace = "net.opendasharchive.openarchive"
}

dependencies {
    implementation("androidx.navigation:navigation-compose:2.8.0")

    val cameraxVersion = "1.3.4"
    implementation("androidx.camera:camera-core:${cameraxVersion}")
    implementation("androidx.camera:camera-camera2:${cameraxVersion}")
    implementation("androidx.camera:camera-lifecycle:${cameraxVersion}")
    implementation("androidx.camera:camera-view:${cameraxVersion}")

    val navigationVersion = "2.8.0"
    implementation("androidx.navigation:navigation-fragment-ktx:$navigationVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navigationVersion")

    implementation("org.aviran.cookiebar2:cookiebar2:1.1.5")

    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.0.10")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.biometric:biometric:1.1.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")

    val lifecycleVersion = "2.8.5"
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion")

    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("androidx.work:work-runtime:2.9.1")
    implementation("androidx.work:work-runtime-ktx:2.9.1")
    implementation("androidx.work:work-testing:2.9.1")
    implementation("androidx.compose.material3:material3:1.3.0")

    val composeVersion = "1.7.1"
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.foundation:foundation:$composeVersion")
    implementation("androidx.compose.ui:ui-tooling-preview:$composeVersion")
    implementation("androidx.compose.material:material-icons-extended:$composeVersion")
    implementation("androidx.activity:activity-compose:1.9.2")

    val koinVersion = "3.5.3"
    implementation("io.insert-koin:koin-core:$koinVersion")
    implementation("io.insert-koin:koin-android:$koinVersion")
    implementation("io.insert-koin:koin-androidx-compose:$koinVersion")

    implementation("com.github.satyan:sugar:1.5")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // adding web dav support: https://github.com/thegrizzlylabs/sardine-android"
    implementation("com.github.guardianproject:sardine-android:89f7eae512")

    implementation("com.google.android.material:material:1.12.0")
//    implementation("com.github.bumptech.glide:glide:4.16.0")
//    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation("com.github.derlio:audio-waveform:v1.0.1")
    implementation("com.github.esafirm:android-image-picker:3.0.0")
//    implementation("com.squareup.picasso:picasso:2.5.2")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("io.coil-kt:coil-video:2.7.0")

    implementation("com.amulyakhare:com.amulyakhare.textdrawable:1.0.1")
    implementation("com.github.abdularis:circularimageview:1.4")

    implementation("info.guardianproject.netcipher:netcipher:2.2.0-alpha")

    //from here: https://github.com/guardianproject/proofmode
    implementation("org.proofmode:android-libproofmode:1.0.29") {

        isTransitive = false

        exclude(group = "org.bitcoinj")
        exclude(group = "com.google.protobuf")
        exclude(group = "org.slf4j")
        exclude(group = "net.jcip")
        exclude(group = "commons-cli")
        exclude(group = "org.json")
        exclude(group = "com.google.guava")
        exclude(group = "com.google.guava", module = "guava-jdk5")
        exclude(group = "com.google.code.findbugs", module = "annotations")
        exclude(group = "com.squareup.okio", module = "okio")
    }

    implementation("com.google.guava:guava:32.0.1-jre")
    implementation("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")

    implementation("org.bouncycastle:bcpkix-jdk15to18:1.72")
    implementation("org.bouncycastle:bcprov-jdk15to18:1.72")
    api("org.bouncycastle:bcpg-jdk15to18:1.71")

    implementation("com.tbuonomo:dotsindicator:5.0")
    implementation("com.guolindev.permissionx:permissionx:1.6.4")

    implementation("com.jakewharton.timber:timber:5.0.1")

    // Google Drive API
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.google.http-client:google-http-client-gson:1.42.3")
    implementation("com.google.api-client:google-api-client-android:1.26.0") // Don"t upgrade this yet. Will break code.
    implementation("com.google.apis:google-api-services-drive:v3-rev136-1.25.0")

    // Tor
    implementation("info.guardianproject:tor-android:0.4.7.14")
    implementation("info.guardianproject:jtorctl:0.4.5.7")

    // New Play libraries
    implementation("com.google.android.play:asset-delivery:2.2.2")
    implementation("com.google.android.play:asset-delivery-ktx:2.2.2")

    implementation("com.google.android.play:feature-delivery:2.1.0")
    implementation("com.google.android.play:feature-delivery-ktx:2.1.0")

    implementation("com.google.android.play:review:2.0.1")
    implementation("com.google.android.play:review-ktx:2.0.1")

    implementation("com.google.android.play:app-update:2.1.0")
    implementation("com.google.android.play:app-update-ktx:2.1.0")

//    implementation("androidx.credentials:credentials:1.2.2"
//    implementation("androidx.credentials:credentials-play-services-auth:1.2.2"
//    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1"

    implementation("com.google.zxing:core:3.4.1")
    implementation("com.journeyapps:zxing-android-embedded:4.2.0")

    implementation("com.eclipsesource.j2v8:j2v8:6.2.1@aar")


    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Choose one of the following:
    // Material Design 3
    implementation("androidx.compose.material3:material3")
    // or Material Design 2
    implementation("androidx.compose.material:material")
    // or skip Material Design and build directly on top of foundational components
    implementation("androidx.compose.foundation:foundation")
    // or only import the main APIs for the underlying toolkit systems,
    // such as input and measurement/layout
    implementation("androidx.compose.ui:ui")

    // Android Studio Preview support
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // UI Tests
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Optional - Included automatically by material, only add when you need
    // the icons but not the material library (e.g. when using Material3 or a
    // custom design system based on Foundation)
    implementation("androidx.compose.material:material-icons-core")
    // Optional - Add full set of material icons
    implementation("androidx.compose.material:material-icons-extended")
    // Optional - Add window size utils
    implementation("androidx.compose.material3:material3-window-size-class")

    // Optional - Integration with activities
    implementation("androidx.activity:activity-compose:1.9.2")
    // Optional - Integration with ViewModels
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.5")
    // Optional - Integration with LiveData
    implementation("androidx.compose.runtime:runtime-livedata")
    // Optional - Integration with RxJava
    implementation("androidx.compose.runtime:runtime-rxjava2")

    // A more customization popup menu
    implementation("com.github.skydoves:powermenu:2.2.4")

    // Mixpanel analytics
    implementation("com.mixpanel.android:mixpanel-android:7.5.2")

    implementation("androidx.webkit:webkit:1.11.0")

    // Tests
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.7.3")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
}

configurations {
    all {
        exclude(group = "com.google.guava", module = "listenablefuture")
    }
}
