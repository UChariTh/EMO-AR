plugins {
    alias(libs.plugins.android.application)
}
subprojects {
    apply("com.google.ar.sceneform.plugin")
}
android {
    namespace = "com.example.emoar"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.emoar"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        mlModelBinding = true
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.tensorflow.lite.support)
    implementation(libs.tensorflow.lite.metadata)
    implementation(libs.tensorflow.lite.gpu)

    // CameraX core library
    implementation(libs.camera.core.v110)
    // CameraX Camera2 implementation
    implementation(libs.camera.camera2)
    // CameraX Lifecycle library
    implementation(libs.camera.lifecycle.v110)
    // CameraX View class
    implementation(libs.camera.view)
//    implementation(libs.core)
//    implementation(libs.sceneform.ux)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)



}