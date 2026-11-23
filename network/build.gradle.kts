plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.lfcapp.network"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 29

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

}

dependencies {
    // Hilt so this module's @Inject constructors (ApiService providers, etc.)
    // generate their Dagger Factory classes locally and are visible to :app's component.
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Exposed as `api` because retrofit2.Response<T> appears in the public
    // signatures of our ApiService interfaces (e.g. ImageProcessingApiService),
    // so consumers like :data need it on their compile classpath too.
    api(libs.retrofit.core)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging.interceptor)

    implementation(libs.moshi.kotlin)
    ksp(libs.moshi.kotlin.codegen)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}