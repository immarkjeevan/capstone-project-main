plugins {
    alias(libs.plugins.androidApplication)
    //Added the google services plugin
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.teachassist"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.teachassist"
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
}

dependencies {



    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.play.services.base)
    implementation(libs.firebase.auth)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    //Github Dependencies
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")
    //Firebase BOM
    implementation(platform("com.google.firebase:firebase-bom:33.2.0"))
    implementation ("com.google.firebase:firebase-firestore:24.0.0")
    implementation ("com.google.firebase:firebase-storage:20.0.0")
    implementation("com.google.firebase:firebase-analytics")
    //Firebase Implementations
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.google.firebase:firebase-database")
    //Fragment Implementations
    implementation("androidx.fragment:fragment:1.8.4")
    //okhttp dependency
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

}