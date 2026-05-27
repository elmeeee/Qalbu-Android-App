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

android {
    namespace = "app.kamy.qalbuApp"
    compileSdk = 36

    defaultConfig {
        applicationId = "app.kamy.qalbuApp"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // OAuth callback host/scheme used by AppAuth's RedirectUriReceiverActivity manifest placeholder.
        manifestPlaceholders["appAuthRedirectScheme"] = "alkhatib"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    buildTypes {
        debug {
            // Point debug at production APIs so behaviour matches release.
            buildConfigField("String", "QF_API_BASE_URL", "\"https://apis.quran.foundation\"")
            buildConfigField("String", "QF_OAUTH_TOKEN_URL", "\"https://oauth2.quran.foundation/oauth2/token\"")
            buildConfigField("String", "QF_OAUTH_AUTHORIZE_URL", "\"https://oauth2.quran.foundation/oauth2/auth\"")
            buildConfigField("String", "QF_OAUTH_CALLBACK_URL", "\"https://elmee.my/oauth/callback\"")
            buildConfigField("String", "QF_OAUTH_APP_CALLBACK_URL", "\"alkhatib://oauth/callback\"")
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
            buildConfigField("int", "QF_DEFAULT_TRANSLATION_ID", "22")
            buildConfigField("String", "API_KEY_GROQ", "\"${secret("API_KEY_GROQ")}\"")
            buildConfigField("String", "AI_MODEL", "\"${secret("AI_MODEL", "qwen/qwen3-32b")}\"")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Production environment — mirrors iOS Config/Release.xcconfig
            buildConfigField("String", "QF_API_BASE_URL", "\"https://apis.quran.foundation\"")
            buildConfigField("String", "QF_OAUTH_TOKEN_URL", "\"https://oauth2.quran.foundation/oauth2/token\"")
            buildConfigField("String", "QF_OAUTH_AUTHORIZE_URL", "\"https://oauth2.quran.foundation/oauth2/auth\"")
            buildConfigField("String", "QF_OAUTH_CALLBACK_URL", "\"https://elmee.my/oauth/callback\"")
            buildConfigField("String", "QF_OAUTH_APP_CALLBACK_URL", "\"alkhatib://oauth/callback\"")
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
            buildConfigField("int", "QF_DEFAULT_TRANSLATION_ID", "22")
            buildConfigField("String", "API_KEY_GROQ", "\"${secret("API_KEY_GROQ")}\"")
            buildConfigField("String", "AI_MODEL", "\"${secret("AI_MODEL", "qwen/qwen3-32b")}\"")
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

    // OAuth (PKCE)
    implementation(libs.appauth)

    // Media3 ExoPlayer for recitation playback
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.session)

    // Image loading
    implementation(libs.coil.compose)

    // Debug HTTP inspector (Chucker) — uncomment to re-enable
    // debugImplementation(libs.chucker)
    // releaseImplementation(libs.chucker.no.op)

    // Test
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
