plugins {
    alias(libs.plugins.buffered.client.android.library)
}

android {
    namespace = "com.angelma.core.data"
}

dependencies {
    // api: the public extensions (HttpClient.get, safeCall) expose HttpClient, Result and DataError
    // in their signatures, so anyone depending on :core:data needs to see those types.
    api(libs.ktor.client.core)
    api(projects.core.domain)

    implementation(libs.bundles.ktor)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
}
