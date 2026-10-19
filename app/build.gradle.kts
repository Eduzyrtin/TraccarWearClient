import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val testDefaults = Properties().apply {
    val file = rootProject.file("test-defaults.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

fun quoted(key: String) = "\"" + testDefaults.getProperty(key, "").replace("\\", "\\\\").replace("\"", "\\\"") + "\""

android {
    namespace = "io.github.eduzyrtin.traccarwear.client"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.sanabria.traccarwearosclient"
        // Wear OS 3 o superior
        minSdk = 30
        targetSdk = 36
        versionCode = 2
        versionName = "0.1.1"
    }

    buildTypes {
        debug {
            buildConfigField("String", "TEST_SERVER", quoted("server"))
            buildConfigField("String", "TEST_DEVICE_ID", quoted("deviceId"))
        }
        release {
            buildConfigField("String", "TEST_SERVER", "\"\"")
            buildConfigField("String", "TEST_DEVICE_ID", "\"\"")
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.traccar.client.sdk)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.wear.compose.material3)
    implementation(libs.androidx.wear.compose.foundation)
    implementation(libs.androidx.wear.compose.navigation)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.work.runtime)

    implementation(libs.play.services.wearable)
    implementation(libs.kotlinx.coroutines.play.services)
}
