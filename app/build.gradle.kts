plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.homeschooling"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.homeschooling"
        minSdk = 24
        targetSdk = 36
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
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.material.v1110)
    //noinspection GradleDependency
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    //noinspection GradleDependency
    implementation(libs.firebase.database)
    implementation(libs.cloudinary.android)

    // Google Maps & Places
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.places)
}
