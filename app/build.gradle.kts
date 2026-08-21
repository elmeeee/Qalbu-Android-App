import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

// Load local secrets from local.properties (never committed).
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
fun secret(key: String, default: String = ""): String =
    (localProps.getProperty(key) ?: System.getenv(key) ?: default)

val releaseStoreFile = secret("RELEASE_STORE_FILE")
val hasReleaseSigning = releaseStoreFile.isNotBlank() &&
    rootProject.file(releaseStoreFile).exists() &&
    secret("RELEASE_STORE_PASSWORD").isNotBlank() &&
    secret("RELEASE_KEY_ALIAS").isNotBlank() &&
    secret("RELEASE_KEY_PASSWORD").isNotBlank()

android {
    namespace = "app.kamy.saatApp"
    compileSdk = 36

    defaultConfig {
        applicationId = "app.kamy.saatApp"
        minSdk = 30
        targetSdk = 36
        versionCode = 15
        versionName = "1.4.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = rootProject.file(releaseStoreFile)
                storePassword = secret("RELEASE_STORE_PASSWORD")
                keyAlias = secret("RELEASE_KEY_ALIAS")
                keyPassword = secret("RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    // Keep bundled SQLite/gzip assets readable without APK re-compression.
    androidResources {
        noCompress += listOf("gz", "db")
        ignoreAssetsPattern = "!*.bak"
    }

    bundle {
        language { enableSplit = false }
        density { enableSplit = true }
        abi { enableSplit = true }
    }

    buildTypes {
        debug {
            buildConfigField("String", "API_KEY_GROQ", "\"${secret("API_KEY_GROQ")}\"")
            buildConfigField("String", "AI_MODEL", "\"${secret("AI_MODEL", "openai/gpt-oss-20b")}\"")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            requireNotNull(signingConfigs.findByName("release")) {
                "Release signing config is required for release builds. " +
                "Configure RELEASE_STORE_FILE, RELEASE_STORE_PASSWORD, " +
                "RELEASE_KEY_ALIAS, and RELEASE_KEY_PASSWORD in local.properties."
            }.also { signingConfig = it }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            buildConfigField("String", "API_KEY_GROQ", "\"${secret("API_KEY_GROQ")}\"")
            buildConfigField("String", "AI_MODEL", "\"${secret("AI_MODEL", "openai/gpt-oss-20b")}\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        freeCompilerArgs.addAll(
            "-opt-in=kotlin.RequiresOptIn",
            "-Xannotation-default-target=param-property"
        )
    }
}

androidComponents {
    onVariants(selector().withBuildType("release")) { variant ->
        variant.outputs.forEach { output ->
            output.outputFileName.set("Sāat-Production.apk")
        }
    }
}


dependencies {
    // AndroidX core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.play.services.location)
    implementation(libs.play.review.ktx)
    implementation(libs.play.update.ktx)
    implementation(libs.adhan)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.accompanist.permissions)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.retrofit.kotlinx.serialization.converter)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)

    // Media3 ExoPlayer for recitation playback & HLS radio streaming
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.exoplayer.hls)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.session)

    // Image loading
    implementation(libs.coil.compose)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // Debug network inspector — ProwlKit (debug builds only)
    // debugImplementation(libs.prowl)
    // debugImplementation(libs.prowl.grpc)

    // Test
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
