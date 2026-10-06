plugins {
    alias(libs.plugins.buffered.client.android.feature)
}

android {
    namespace = "com.angelma.feature.player.presentation"
}

dependencies {
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}