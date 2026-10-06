plugins {
    alias(libs.plugins.buffered.client.android.feature)
}

android {
    namespace = "com.angelma.feature.catalog.presentation"
}

dependencies {
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    implementation(projects.feature.catalog.domain)
    implementation(projects.core.domain)
}