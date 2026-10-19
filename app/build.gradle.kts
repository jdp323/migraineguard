import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

fun localOrDefault(key: String, default: String = ""): String =
    (localProps.getProperty(key) ?: project.findProperty(key)?.toString() ?: default)
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")

// Baked Web OAuth client (client_type 3) for Google Sign-In via Credential Manager.
// The installed APK certificate must also be registered with an Android OAuth client.
val bakedGoogleWebClientId =
    "539944675853-i6otink5q9a4tr94mo7hm85jli1edq61.apps.googleusercontent.com"

android {
    namespace = "com.migraineguard.app"
    compileSdk = 35

    defaultConfig {
        val preview = project.findProperty("MG_PREVIEW")?.toString() == "true"
        applicationId = if (preview) "com.migraineguard.app.preview" else "com.migraineguard.app"
        manifestPlaceholders["appLabel"] = if (preview) "@string/preview_app_name" else "@string/app_name"
        minSdk = 26
        targetSdk = 35
        versionCode = 11
        versionName = "1.6.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "GOOGLE_WEB_CLIENT_ID",
            "\"${localOrDefault("GOOGLE_WEB_CLIENT_ID", bakedGoogleWebClientId)}\""
        )
        // NEVER ship developer AI tokens — empty defaults only. Users paste their own keys.
        buildConfigField("String", "GEMINI_API_KEY", "\"\"")
        buildConfigField("String", "GROQ_API_KEY", "\"\"")
        buildConfigField("String", "OPENROUTER_API_KEY", "\"\"")
    }

    // CI passes an explicit path: SDK defaults can vary between runner images.
    signingConfigs.getByName("debug") {
        System.getenv("MG_DEBUG_KEYSTORE_PATH")?.takeIf { it.isNotBlank() }?.let {
            storeFile = file(it)
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

ksp { arg("room.schemaLocation", "$projectDir/schemas") }

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services)
    implementation(libs.googleid)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.okhttp)
    implementation(libs.androidx.health.connect)
    implementation(libs.coil.compose)
    implementation(libs.androidx.work.runtime.ktx)

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("org.json:json:20240303")

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:core:1.6.1")
    androidTestImplementation("androidx.test:runner:1.6.2")

    debugImplementation(libs.androidx.compose.ui.tooling)
}
