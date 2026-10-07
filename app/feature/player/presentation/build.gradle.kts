plugins {
    alias(libs.plugins.buffered.client.android.feature)
}

android {
    namespace = "com.angelma.feature.player.presentation"
}

dependencies {
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.exoplayer.hls)
    implementation(libs.androidx.media3.ui.compose)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    implementation(projects.core.presentation)
}