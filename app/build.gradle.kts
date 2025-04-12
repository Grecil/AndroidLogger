plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.androidlogger"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.androidlogger"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    
    // WorkManager
    implementation("androidx.work:work-runtime:2.9.0")
    
    // SQLCipher
    implementation("net.zetetic:android-database-sqlcipher:4.5.4@aar")
    implementation("androidx.sqlite:sqlite:2.4.0") // Required by SQLCipher
    
    // Biometric
    implementation("androidx.biometric:biometric:1.2.0-alpha05")

    // MPAndroidChart
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}