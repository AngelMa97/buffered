plugins {
    alias(libs.plugins.buffered.client.android.library)
}

android {
    namespace = "com.angelma.core.data"
}

dependencies {
    // api: las extensiones públicas (HttpClient.get, safeCall) usan HttpClient, Result y DataError
    // en su firma, así que quien dependa de :core:data necesita ver esos tipos.
    api(libs.ktor.client.core)
    api(projects.core.domain)

    implementation(libs.bundles.ktor)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
}
