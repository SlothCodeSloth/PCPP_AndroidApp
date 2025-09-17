import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.kotlinKapt) // ✅ use the alias here instead
    id("org.jetbrains.dokka") version "1.9.0"
}

android {
    namespace = "com.example.pcpartpicker"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.pcpartpicker"
        minSdk = 33
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Read URL from local.properties
        val localProps = Properties()
        val localPropsFile = rootProject.file("local.properties")
        if (localPropsFile.exists()) {
            localProps.load(FileInputStream(localPropsFile))
        }

        val apiUrl = localProps.getProperty("API_URL") ?: "https://default.url/" // fallback
        buildConfigField("String", "API_URL", "\"$apiUrl\"")
    }

    buildFeatures {
        buildConfig = true
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

tasks.dokkaHtml { // This is the main task for generating HTML documentation
    outputDirectory.set(buildDir.resolve("dokka")) // Output directory for the documentation

    // Configure source sets if necessary (often defaults are fine for simple Android projects)
    // Dokka will typically find your Kotlin source files in src/main/java or src/main/kotlin
    dokkaSourceSets {
        configureEach { // Configures each source set (e.g., "main")
            // Example: Exclude specific files or directories if needed
            // suppressObviousFunctions.set(true) // Don't show "obvious" functions like getters/setters for data classes
            includeNonPublic.set(true) // Set to true if you want to include internal/private declarations

            // If you have external documentation links
            // externalDocumentationLink {
            //     url.set(java.net.URL("https://developer.android.com/reference/"))
            // }
        }
    }
}

dependencies {
    implementation("com.github.bumptech.glide:glide:4.14.2")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
    implementation("androidx.activity:activity-ktx:1.10.1")

    implementation("androidx.room:room-runtime:2.7.2")
    implementation("androidx.room:room-ktx:2.7.2")
    kapt("androidx.room:room-compiler:2.7.2")

    val fragment_version = "1.8.8"

    // Java language implementation
    implementation("androidx.fragment:fragment:$fragment_version")
    // Kotlin
    implementation("androidx.fragment:fragment-ktx:$fragment_version")
    // Compose
    implementation("androidx.fragment:fragment-compose:$fragment_version")
    // Testing Fragments in Isolation
    debugImplementation("androidx.fragment:fragment-testing:$fragment_version")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}