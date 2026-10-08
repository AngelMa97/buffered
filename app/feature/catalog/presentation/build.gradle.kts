plugins {
    alias(libs.plugins.buffered.client.android.feature)
}

android {
    namespace = "com.angelma.feature.catalog.presentation"
}

dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.assertk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.navigation.testing)
    testImplementation(libs.robolectric)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    implementation(libs.coil.compose)
    implementation(libs.coil.network.ktor3)

    implementation(projects.feature.catalog.domain)
    implementation(projects.core.domain)
    implementation(projects.core.presentation)
}
