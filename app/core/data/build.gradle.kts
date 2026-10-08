plugins {
    alias(libs.plugins.buffered.client.android.library)
}

android {
    namespace = "com.angelma.core.data"
}

dependencies {
    api(libs.ktor.client.core)
    api(projects.core.domain)

    implementation(libs.bundles.ktor)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
}
