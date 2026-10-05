plugins {
    alias(libs.plugins.buffered.client.android.library.compose)
}

android {
    namespace = "com.angelma.core.designsystem"
}

dependencies {
    // api: quien use BufferedTheme también usa MaterialTheme, Text, Button…
    api(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}