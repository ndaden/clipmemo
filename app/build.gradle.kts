plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

import java.util.Properties

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(localPropertiesFile.inputStream())
    }
}
/**
 * Resout une valeur de configuration depuis l'environnement puis local.properties.
 * Echoue explicitement si elle est absente : un repli silencieux sur une valeur
 * en dur publierait un binaire porteur d'un secret public.
 */
fun requiredConfig(envName: String, propertyName: String): String {
    val value = System.getenv(envName) ?: localProperties.getProperty(propertyName)
    if (value.isNullOrBlank()) {
        throw GradleException(
            "Configuration manquante : definissez la variable d'environnement " +
                "$envName ou la propriete $propertyName dans local.properties."
        )
    }
    return value
}

val backendBaseUrl = requiredConfig("BACKEND_BASE_URL", "backend.base.url")
val backendAppKey = requiredConfig("BACKEND_APP_KEY", "backend.app.key")

android {
    namespace = "com.danstudios.reelnotes"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.danstudios.clipmemo"
        minSdk = 26
        targetSdk = 36
        versionCode = 8
        versionName = "1.2.1"

        buildConfigField("String", "BACKEND_BASE_URL", "\"$backendBaseUrl\"")
        buildConfigField("String", "BACKEND_APP_KEY", "\"$backendAppKey\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    val releaseKeystoreFile = file("release.keystore")
    signingConfigs {
        create("release") {
            if (releaseKeystoreFile.exists()) {
                storeFile = releaseKeystoreFile
                storePassword = requiredConfig("KEYSTORE_PASSWORD", "keystore.password")
                keyAlias = requiredConfig("KEY_ALIAS", "key.alias")
                keyPassword = requiredConfig("KEY_PASSWORD", "key.password")
            }
        }
    }

    buildTypes {
        release {
            if (releaseKeystoreFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            } else {
                signingConfig = signingConfigs.getByName("debug")
            }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
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

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // OkHttp & Serialization
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Coil Image Loading
    implementation(libs.coil.compose)

    // Google Mobile Ads
    implementation(libs.play.services.ads)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.room.runtime) // for in-memory room testing if needed
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
