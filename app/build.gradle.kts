import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
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
        minSdk = 29
        targetSdk = 36
        versionCode = 4
        versionName = "1.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // OAuth callback host/scheme used by AppAuth's RedirectUriReceiverActivity manifest placeholder.
        manifestPlaceholders["appAuthRedirectScheme"] = "Saat"
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

    buildTypes {
        debug {
            // Point debug at production APIs so behaviour matches release.
            buildConfigField("String", "QF_API_BASE_URL", "\"https://apis.quran.foundation\"")
            buildConfigField("String", "QF_OAUTH_TOKEN_URL", "\"https://oauth2.quran.foundation/oauth2/token\"")
            buildConfigField("String", "QF_OAUTH_AUTHORIZE_URL", "\"https://oauth2.quran.foundation/oauth2/auth\"")
            buildConfigField("String", "QF_OAUTH_CALLBACK_URL", "\"https://elmee.my/oauth/callback\"")
            buildConfigField("String", "QF_OAUTH_APP_CALLBACK_URL", "\"Saat://oauth/callback\"")
            buildConfigField("String", "QF_OAUTH_CLIENT_ID", "\"9fd71c6c-efb4-406e-84d0-ff39f186ca9b\"")
            buildConfigField(
                "String",
                "QF_OAUTH_CLIENT_SECRET",
                "\"${secret("QF_OAUTH_CLIENT_SECRET_RELEASE")}\""
            )
            buildConfigField(
                "String",
                "QF_OAUTH_SCOPES",
                "\"openid offline_access user post streak activity_day reading_session\""
            )
            buildConfigField("String", "QF_VERSES_WEB_BASE", "\"https://verses.quran.com\"")
            buildConfigField("String", "QF_ALADHAN_ROOT", "\"https://api.aladhan.com\"")
            buildConfigField("int", "QF_DEFAULT_TRANSLATION_ID", "1")
            buildConfigField("String", "API_KEY_GROQ", "\"${secret("API_KEY_GROQ")}\"")
            buildConfigField("String", "AI_MODEL", "\"${secret("AI_MODEL", "openai/gpt-oss-20b")}\"")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            } else {
                signingConfig = signingConfigs.getByName("debug")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            buildConfigField("String", "QF_API_BASE_URL", "\"https://apis.quran.foundation\"")
            buildConfigField("String", "QF_OAUTH_TOKEN_URL", "\"https://oauth2.quran.foundation/oauth2/token\"")
            buildConfigField("String", "QF_OAUTH_AUTHORIZE_URL", "\"https://oauth2.quran.foundation/oauth2/auth\"")
            buildConfigField("String", "QF_OAUTH_CALLBACK_URL", "\"https://elmee.my/oauth/callback\"")
            buildConfigField("String", "QF_OAUTH_APP_CALLBACK_URL", "\"Saat://oauth/callback\"")
            buildConfigField("String", "QF_OAUTH_CLIENT_ID", "\"9fd71c6c-efb4-406e-84d0-ff39f186ca9b\"")
            buildConfigField(
                "String",
                "QF_OAUTH_CLIENT_SECRET",
                "\"${secret("QF_OAUTH_CLIENT_SECRET_RELEASE")}\""
            )
            buildConfigField(
                "String",
                "QF_OAUTH_SCOPES",
                "\"openid offline_access user post streak activity_day reading_session\""
            )
            buildConfigField("String", "QF_VERSES_WEB_BASE", "\"https://verses.quran.com\"")
            buildConfigField("String", "QF_ALADHAN_ROOT", "\"https://api.aladhan.com\"")
            buildConfigField("int", "QF_DEFAULT_TRANSLATION_ID", "1")
            buildConfigField("String", "API_KEY_GROQ", "\"${secret("API_KEY_GROQ")}\"")
            buildConfigField("String", "AI_MODEL", "\"${secret("AI_MODEL", "openai/gpt-oss-20b")}\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf("-opt-in=kotlin.RequiresOptIn")
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    applicationVariants.all {
        if (name == "release") {
            outputs.all {
                val output = this as? com.android.build.gradle.api.ApkVariantOutput
                output?.outputFileName = "Sāat-Production.apk"
            }
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
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
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

    // Media3 ExoPlayer for recitation playback
    implementation(libs.androidx.media3.exoplayer)
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
