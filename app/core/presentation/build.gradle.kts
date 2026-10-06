plugins {
    alias(libs.plugins.buffered.client.android.library.compose)
}

android {
    namespace = "com.angelma.core.presentation"
}

dependencies {
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.lifecycle.runtime.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    implementation(projects.core.domain)
    implementation(projects.core.designSystem)
}