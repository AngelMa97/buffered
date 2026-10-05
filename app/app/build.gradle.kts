plugins {
    alias(libs.plugins.buffered.client.android.application)
}

android {
    namespace = "com.angelma.bufferedclient"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)

    implementation(libs.bundles.compose)
    implementation(libs.androidx.activity.compose)

    implementation(libs.bundles.ktor)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    implementation(projects.core.domain)
    implementation(projects.core.presentation)
    implementation(projects.core.designSystem)
    implementation(projects.core.data)
}