plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidLibrary {
        namespace = "app.kamy.saatApp.shared"
        compileSdk = 36
        minSdk = 30
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}

tasks.register<Exec>("syncSharedStrings") {
    group = "localization"
    description = "Synchronizes strings.xml into KMP SharedStrings and iOS Localizable.strings"
    commandLine("python3", "${rootProject.rootDir}/scripts/generate_kmp_strings.py")
}